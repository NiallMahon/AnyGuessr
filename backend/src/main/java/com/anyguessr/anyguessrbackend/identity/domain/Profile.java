// A player's public profile, with initials worked out from their name.
package com.anyguessr.anyguessrbackend.identity.domain;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
/**
 * Represents a player's public profile.
 *
 * @param id the user's ID
 * @param name the player's display name
 * @param location the player's location
 * @param initials the player's initials
 * @param gamesPlayed the number of games the player has played
 */
public record Profile(
    Long id,
    String name,
    String location,
    String initials,
    int gamesPlayed
) {
    /**
     * Creates uppercase initials from the first two words of a name.
     *
     * @param name the name to use
     * @return the initials, or an empty string if the name is blank
     */
    public static String initialsOf(String name) {
        return Arrays.stream(name.trim().split("\\s+"))
                .filter(part -> !part.isBlank())
                .limit(2)
                .map(part -> part.substring(0, 1)
                        .toUpperCase(Locale.ROOT))
                .collect(Collectors.joining());
    }
}
