package com.dispo.app.core

import java.util.Locale

/**
 * Lien partageable Google Maps (aucune clé) + vignette libre (OSM static).
 */
object GoogleMapsUrls {

    fun placeLink(lat: Double, lon: Double): String =
        String.format(Locale.US, "https://www.google.com/maps?q=%.6f,%.6f", lat, lon)

    /** Aperçu carto sans clé API (OpenStreetMap static). */
    fun previewImageUrl(lat: Double, lon: Double, width: Int = 580, height: Int = 236): String =
        String.format(
            Locale.US,
            "https://staticmap.openstreetmap.de/staticmap.php" +
                "?center=%.6f,%.6f&zoom=15&size=%dx%d&maptype=mapnik",
            lat,
            lon,
            width,
            height,
        )

    fun isMapsLink(text: String): Boolean =
        text.contains("google.com/maps", ignoreCase = true) ||
            text.contains("maps.google.", ignoreCase = true)
}
