package com.dispo.app.ui

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.dispo.app.R
import com.dispo.app.core.ChatMessage
import com.dispo.app.ui.theme.CircusRed
import com.dispo.app.ui.theme.Cream
import com.dispo.app.ui.theme.InkBrown
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Écran carte plein écran, ouvert depuis le chat (pin cliqué ou lieu partagé).
 * Hors du pager : aucun conflit entre le drag de la carte et le swipe de pages.
 */
@Composable
fun MapScreen(
    pins: List<ChatMessage>,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        CircusMap(pins = pins, modifier = Modifier.fillMaxSize())

        // Compteur de lieux
        LedPanel(
            text = "${pins.size} LIEU${if (pins.size > 1) "X" else ""}",
            fontSize = 15.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp),
        )

        // Bouton fermer : retour au chat
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp)
                .size(52.dp)
                .shadow(6.dp, CircleShape)
                .background(CircusRed, CircleShape)
                .border(3.dp, InkBrown, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClose,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "✕",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                color = Cream,
            )
        }
    }
}

/**
 * Carte OpenStreetMap plein écran, teinte chaude façon vieille affiche,
 * pins de cirque custom. Zoom au pincement uniquement (pas de boutons +/-).
 */
@Composable
fun CircusMap(
    pins: List<ChatMessage>,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        modifier = modifier,
        factory = { context -> createMapView(context) },
        update = { mapView ->
            val pinDrawable = ContextCompat.getDrawable(mapView.context, R.drawable.pin_circus)
            mapView.overlays.removeAll { it is Marker }
            pins.forEach { msg ->
                val marker = Marker(mapView).apply {
                    position = GeoPoint(msg.lat!!, msg.lon!!)
                    title = "${msg.authorName} : ${msg.text}"
                    icon = pinDrawable
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                mapView.overlays.add(marker)
            }
            pins.lastOrNull()?.let { last ->
                mapView.controller.animateTo(GeoPoint(last.lat!!, last.lon!!))
            }
            mapView.invalidate()
        },
    )
}

private fun createMapView(context: Context): MapView {
    Configuration.getInstance().userAgentValue = context.packageName
    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        // Zoom au pincement, sans les boutons +/- superposés
        setMultiTouchControls(true)
        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        controller.setZoom(14.0)
        controller.setCenter(GeoPoint(45.5019, -73.5674)) // Montréal par défaut

        // Teinte chaude "vieille affiche" pour coller à la DA cirque :
        // désaturation légère + virage crème/sépia.
        val desaturate = ColorMatrix().apply { setSaturation(0.55f) }
        val warm = ColorMatrix(
            floatArrayOf(
                1.08f, 0f, 0f, 0f, 14f,
                0f, 1.0f, 0f, 0f, 6f,
                0f, 0f, 0.86f, 0f, -8f,
                0f, 0f, 0f, 1f, 0f,
            )
        )
        warm.preConcat(desaturate)
        overlayManager.tilesOverlay.setColorFilter(ColorMatrixColorFilter(warm))
    }
}
