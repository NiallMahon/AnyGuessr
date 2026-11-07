// The region a player clicked, with optional click coordinates.
package com.anyguessr.anyguessrbackend.gameplay.ui;

public record SubmitGuessRequest(
        String selectedRegionId,
        Double clickedLat,
        Double clickedLng
) {
}