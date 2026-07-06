package com.dispo.app.ui

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.dispo.app.core.DispoUiState
import com.dispo.app.ui.theme.InkBrown
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Carte OpenStreetMap (osmdroid) : affiche les pins partagés dans le chat.
 * Pas de clé API nécessaire, parfait pour le MVP.
 */
@Composable
fun MapPanel(
    state: DispoUiState,
    modifier: Modifier = Modifier,
) {
    val pins = state.messages.filter { it.hasLocation }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            "La carte 🗺️",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(12.dp),
        )

        if (pins.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎯", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Partage un lieu depuis le chat et il apparaîtra ici.",
                        color = InkBrown,
                    )
                }
            }
        } else {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context -> createMapView(context) },
                update = { mapView ->
                    mapView.overlays.removeAll { it is Marker }
                    pins.forEach { msg ->
                        val marker = Marker(mapView).apply {
                            position = GeoPoint(msg.lat!!, msg.lon!!)
                            title = "${msg.authorName} : ${msg.text}"
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
    }
}

private fun createMapView(context: Context): MapView {
    Configuration.getInstance().userAgentValue = context.packageName
    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        controller.setZoom(13.0)
        controller.setCenter(GeoPoint(45.5019, -73.5674)) // Montréal par défaut
    }
}
