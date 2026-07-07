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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

/**
 * Écran carte plein écran, ouvert depuis le chat. Hors du pager :
 * aucun conflit entre le drag de la carte et le swipe de pages.
 * Un tap sur la carte choisit le lieu de rendez-vous.
 */
@Composable
fun MapScreen(
    pins: List<ChatMessage>,
    onPickLocation: (lat: Double, lon: Double) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Ferme le clavier laissé ouvert par le chat
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) { keyboard?.hide() }

    Box(modifier = modifier.fillMaxSize()) {
        CircusMap(
            pins = pins,
            onMapTap = onPickLocation,
            modifier = Modifier.fillMaxSize(),
        )

        // Contrôles au-dessus de la carte, dans la zone sûre
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
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

            LedPanel(
                text = "TAPE LA CARTE POUR CHOISIR LE LIEU",
                fontSize = 15.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(14.dp),
            )
        }
    }
}

/**
 * Carte OpenStreetMap plein écran, teinte chaude façon vieille affiche,
 * pins de cirque custom. Zoom au pincement uniquement (pas de boutons +/-).
 * [onMapTap] est appelé avec les coordonnées du tap sur la carte.
 */
@Composable
fun CircusMap(
    pins: List<ChatMessage>,
    modifier: Modifier = Modifier,
    onMapTap: ((lat: Double, lon: Double) -> Unit)? = null,
) {
    // Garde la dernière lambda sans recréer la MapView
    val currentOnTap by rememberUpdatedState(onMapTap)

    AndroidView(
        modifier = modifier,
        factory = { context ->
            createMapView(context).also { mapView ->
                val receiver = object : MapEventsReceiver {
                    override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                        currentOnTap?.invoke(p.latitude, p.longitude)
                        return currentOnTap != null
                    }

                    override fun longPressHelper(p: GeoPoint): Boolean = false
                }
                mapView.overlays.add(0, MapEventsOverlay(receiver))
            }
        },
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
