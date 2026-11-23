// A player's public profile, with initials worked out from their name.
package com.anyguessr.anyguessrbackend.identity.domain;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public record Profile(
    Long id,
    String name,
    String location,
    String initials,
    int gamesPlayed
) {

    public static String initialsOf(String name) {
        return Arrays.stream(name.trim().split("\\s+"))
                .filter(part -> !part.isBlank())
                .limit(2)
                .map(part -> part.substring(0, 1)
                        .toUpperCase(Locale.ROOT))
                .collect(Collectors.joining());
    }
}
