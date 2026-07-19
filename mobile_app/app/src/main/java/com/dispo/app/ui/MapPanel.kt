package com.dispo.app.ui

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
import com.dispo.app.ui.theme.DispoGreen
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
 * Écran carte plein écran. Un tap place un pin provisoire ;
 * « Appliquer » confirme et envoie le lieu dans le chat.
 */
@Composable
fun MapScreen(
    pins: List<ChatMessage>,
    onPickLocation: (lat: Double, lon: Double) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) { keyboard?.hide() }

    var draft by remember { mutableStateOf<GeoPoint?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        CircusMap(
            pins = pins,
            draft = draft,
            onMapTap = { lat, lon -> draft = GeoPoint(lat, lon) },
            modifier = Modifier.fillMaxSize(),
        )

        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
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

            if (draft != null) {
                Button(
                    onClick = {
                        val point = draft ?: return@Button
                        onPickLocation(point.latitude, point.longitude)
                        draft = null
                        onClose()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(0.7f),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DispoGreen),
                    border = BorderStroke(3.dp, InkBrown),
                ) {
                    Text(
                        "ENVOYER",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                        color = Cream,
                    )
                }
            }
        }
    }
}

@Composable
fun CircusMap(
    pins: List<ChatMessage>,
    modifier: Modifier = Modifier,
    draft: GeoPoint? = null,
    onMapTap: ((lat: Double, lon: Double) -> Unit)? = null,
) {
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

            draft?.let { point ->
                val draftMarker = Marker(mapView).apply {
                    position = point
                    title = "Nouveau lieu"
                    icon = pinDrawable
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                mapView.overlays.add(draftMarker)
                mapView.controller.animateTo(point)
            }

            mapView.invalidate()
        },
    )
}

private fun createMapView(context: Context): MapView {
    Configuration.getInstance().userAgentValue = context.packageName
    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        controller.setZoom(14.0)
        controller.setCenter(GeoPoint(45.5019, -73.5674))

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
