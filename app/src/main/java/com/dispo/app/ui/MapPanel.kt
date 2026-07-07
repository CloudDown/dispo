package com.dispo.app.ui

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.dispo.app.R
import com.dispo.app.core.ChatMessage
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

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
