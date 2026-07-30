package com.dispo.app.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.dispo.app.R
import com.dispo.app.core.ChatMessage
import com.dispo.app.ui.theme.Cream
import com.dispo.app.ui.theme.DispoGreen
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

/** Fond clair type Apple Maps (CartoDB Positron), sans clé API. */
private val CartoPositron: OnlineTileSourceBase = object : XYTileSource(
    "CartoPositron",
    0,
    20,
    256,
    ".png",
    arrayOf(
        "https://a.basemaps.cartocdn.com/",
        "https://b.basemaps.cartocdn.com/",
        "https://c.basemaps.cartocdn.com/",
        "https://d.basemaps.cartocdn.com/",
    ),
    "© OpenStreetMap © CARTO",
) {
    override fun getTileURLString(pMapTileIndex: Long): String {
        val zoom = MapTileIndex.getZoom(pMapTileIndex)
        val x = MapTileIndex.getX(pMapTileIndex)
        val y = MapTileIndex.getY(pMapTileIndex)
        return "${baseUrl}light_all/$zoom/$x/$y.png"
    }
}

/**
 * Écran carte plein écran. Un tap place un pin provisoire ;
 * « Envoyer » confirme et envoie le lieu (lien Google Maps) dans le chat.
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
        DispoMap(
            pins = pins,
            draft = draft,
            onMapTap = { lat, lon -> draft = GeoPoint(lat, lon) },
            modifier = Modifier.fillMaxSize(),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Black.copy(alpha = 0.18f), Color.Transparent),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.22f)),
                    ),
                ),
        )

        Box(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(40.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.92f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClose,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text("✕", fontSize = 16.sp, color = Color(0xFF333333), fontWeight = FontWeight.Medium)
            }

            Text(
                if (draft == null) "Touche la carte pour placer un lieu" else "Lieu sélectionné",
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 18.dp)
                    .shadow(2.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.92f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                color = if (draft == null) Color(0xFF666666) else DispoGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )

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
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DispoGreen),
                    border = BorderStroke(1.dp, Cream.copy(alpha = 0.15f)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                ) {
                    Text(
                        "Envoyer ce lieu",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Cream,
                    )
                }
            }
        }
    }
}

@Composable
fun DispoMap(
    pins: List<ChatMessage>,
    modifier: Modifier = Modifier,
    draft: GeoPoint? = null,
    onMapTap: ((lat: Double, lon: Double) -> Unit)? = null,
) {
    val currentOnTap by rememberUpdatedState(onMapTap)
    val lastAnimatedDraft = remember { mutableStateOf<GeoPoint?>(null) }

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
            // osmdroid ne dessine pas bien les VectorDrawable → bitmap rouge
            val pinIcon = redPinDrawable(mapView.context)
            mapView.overlays.removeAll { it is Marker }

            pins.filter { it.hasLocation }.forEach { msg ->
                val marker = Marker(mapView).apply {
                    position = GeoPoint(msg.lat!!, msg.lon!!)
                    title = msg.authorName
                    icon = pinIcon.constantState?.newDrawable()?.mutate() ?: pinIcon
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                mapView.overlays.add(marker)
            }

            draft?.let { point ->
                val draftMarker = Marker(mapView).apply {
                    position = point
                    title = "Nouveau lieu"
                    icon = pinIcon.constantState?.newDrawable()?.mutate() ?: pinIcon
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                mapView.overlays.add(draftMarker)
                val prev = lastAnimatedDraft.value
                val moved = prev == null ||
                    prev.latitude != point.latitude ||
                    prev.longitude != point.longitude
                if (moved) {
                    mapView.controller.animateTo(point)
                    lastAnimatedDraft.value = point
                }
            } ?: run {
                lastAnimatedDraft.value = null
            }

            mapView.invalidate()
        },
    )
}

/** Rasterise le pin vectoriel : osmdroid ignore sinon le VectorDrawable et garde son pin vert. */
private fun redPinDrawable(context: Context): Drawable {
    val vector = ContextCompat.getDrawable(context, R.drawable.pin_map)
        ?: return ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)!!
    val density = context.resources.displayMetrics.density
    val width = (36f * density).toInt().coerceAtLeast(1)
    val height = (48f * density).toInt().coerceAtLeast(1)
    val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    vector.setBounds(0, 0, width, height)
    vector.draw(canvas)
    return BitmapDrawable(context.resources, bitmap)
}

private fun createMapView(context: Context): MapView {
    Configuration.getInstance().userAgentValue = context.packageName
    return MapView(context).apply {
        setTileSource(CartoPositron)
        setMultiTouchControls(true)
        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        controller.setZoom(14.0)
        controller.setCenter(GeoPoint(45.5019, -73.5674))
    }
}
