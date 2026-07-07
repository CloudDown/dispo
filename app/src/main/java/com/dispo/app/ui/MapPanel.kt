package com.dispo.app.ui

import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.dispo.app.R
import com.dispo.app.core.DispoUiState
import com.dispo.app.ui.theme.CircusRed
import com.dispo.app.ui.theme.Cream
import com.dispo.app.ui.theme.InkBrown
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Carte OpenStreetMap (osmdroid) avec teinte chaude façon vieille affiche,
 * pins de cirque custom, dans un cadre cartoon.
 */
@Composable
fun MapPanel(
    state: DispoUiState,
    modifier: Modifier = Modifier,
) {
    val pins = state.messages.filter { it.hasLocation }

    Column(modifier = modifier.fillMaxSize().padding(12.dp)) {
        // En-tête chapiteau
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CircusRed, RoundedCornerShape(14.dp))
                .border(3.dp, InkBrown, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "LA CARTE",
                style = MaterialTheme.typography.titleLarge,
                color = Cream,
            )
            LedPanel(
                text = "${pins.size} LIEU${if (pins.size > 1) "X" else ""}",
                fontSize = 15.sp,
            )
        }

        Spacer(Modifier.height(10.dp))

        if (pins.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Cream, RoundedCornerShape(20.dp))
                    .border(3.dp, InkBrown, RoundedCornerShape(20.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎯", fontSize = 56.sp)
                    Spacer(Modifier.height(14.dp))
                    LedPanel(
                        text = "AUCUN LIEU PARTAGÉ",
                        fontSize = 18.sp,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Partage un lieu depuis le chat et il apparaîtra ici.",
                        color = InkBrown,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            // Cadre cartoon autour de la carte
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .border(4.dp, InkBrown, RoundedCornerShape(20.dp)),
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context -> createMapView(context) },
                    update = { mapView ->
                        val pinDrawable =
                            ContextCompat.getDrawable(mapView.context, R.drawable.pin_circus)
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
        }
    }
}

private fun createMapView(context: Context): MapView {
    Configuration.getInstance().userAgentValue = context.packageName
    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
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
