package com.anyguessr.anyguessrbackend.social.infrastructure.repository;

import com.anyguessr.anyguessrbackend.social.app.UserLookupService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Database-backed implementation of {@link UserLookupService} querying user records via JPA native SQL.
 */
@Service
public class DbUserLookupService implements UserLookupService {

    @PersistenceContext
    private EntityManager entityManager;

    private static final String BASE_SELECT =
            "SELECT u.user_id, u.username, " +
                    "(SELECT COUNT(*) FROM user_games g WHERE g.user_id = u.user_id) AS games_played " +
                    "FROM users u WHERE u.deleted_at IS NULL ";

    /**
     * Finds an active user summary by ID.
     */
    @Override
    public Optional<UserSummary> findById(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        List<?> rows = entityManager.createNativeQuery(BASE_SELECT + "AND u.user_id = :userId")
                .setParameter("userId", userId)
                .getResultList();
        return rows.stream().findFirst().map(this::mapRow);
    }

    /**
     * Finds an active user summary by username, ignoring case.
     */
    @Override
    public Optional<UserSummary> findByUsernameIgnoreCase(String username) {
        if (username == null || username.isBlank()) {
            return Optional.empty();
        }
        List<?> rows = entityManager.createNativeQuery(BASE_SELECT + "AND LOWER(u.username) = LOWER(:username)")
                .setParameter("username", username.trim())
                .getResultList();
        return rows.stream().findFirst().map(this::mapRow);
    }

    /**
     * Finds active user summaries matching any of the given user IDs.
     */
    @Override
    public List<UserSummary> findAllByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        List<?> rows = entityManager.createNativeQuery(BASE_SELECT + "AND u.user_id IN (:userIds)")
                .setParameter("userIds", userIds)
                .getResultList();
        return rows.stream().map(this::mapRow).toList();
    }

    /**
     * Searches active users whose usernames contain the query string, up to 15 results.
     */
    @Override
    public List<UserSummary> searchByUsername(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String pattern = "%" + query.trim().toLowerCase() + "%";
        List<?> rows = entityManager.createNativeQuery(
                        BASE_SELECT + "AND LOWER(u.username) LIKE :pattern ORDER BY u.username ASC LIMIT 15"
                )
                .setParameter("pattern", pattern)
                .getResultList();
        return rows.stream().map(this::mapRow).toList();
    }

    /**
     * Maps a native query result row into a {@link UserSummary}.
     */
    private UserSummary mapRow(Object raw) {
        Object[] cols = (Object[]) raw;
        Long userId = ((Number) cols[0]).longValue();
        String username = String.valueOf(cols[1]);
        int gamesPlayed = cols[2] instanceof Number n ? n.intValue() : 0;
        String initials = buildInitials(username);
        return new UserSummary(
                userId,
                username,
                username,
                "Unknown",
                initials,
                gamesPlayed,
                "Online",
                "duel-" + userId
        );
    }

    /**
     * Builds up to two uppercase initials from a username.
     */
    private String buildInitials(String username) {
        String clean = username.trim();
        String[] parts = clean.split("[._\\s-]+");
        if (parts.length >= 2 && !parts[0].isEmpty() && !parts[1].isEmpty()) {
            return ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
        }
        return clean.substring(0, Math.min(2, clean.length())).toUpperCase();
    }
}