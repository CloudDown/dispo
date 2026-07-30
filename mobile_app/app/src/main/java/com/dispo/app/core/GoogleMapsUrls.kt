package com.dispo.app.core

import java.util.Locale

/** Liens et vignettes Google Maps (Static Maps + URL partageable). */
object GoogleMapsUrls {

    fun placeLink(lat: Double, lon: Double): String =
        String.format(Locale.US, "https://www.google.com/maps?q=%.6f,%.6f", lat, lon)

    /**
     * Image associée au lieu (Maps Static API).
     * @param apiKey valeur de `MAPS_API_KEY` / `R.string.google_maps_key`
     */
    fun staticImageUrl(
        lat: Double,
        lon: Double,
        apiKey: String,
        width: Int = 580,
        height: Int = 236,
    ): String? {
        if (apiKey.isBlank()) return null
        return String.format(
            Locale.US,
            "https://maps.googleapis.com/maps/api/staticmap" +
                "?center=%.6f,%.6f&zoom=15&size=%dx%d&scale=2&maptype=roadmap" +
                "&markers=color:0x2ECC71%%7C%.6f,%.6f&key=%s",
            lat,
            lon,
            width,
            height,
            lat,
            lon,
            apiKey,
        )
    }

    fun isMapsLink(text: String): Boolean =
        text.contains("google.com/maps", ignoreCase = true) ||
            text.contains("maps.google.", ignoreCase = true)
}
