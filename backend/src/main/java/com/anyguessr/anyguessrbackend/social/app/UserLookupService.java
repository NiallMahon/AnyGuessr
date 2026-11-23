package com.anyguessr.anyguessrbackend.social.app;

import java.util.List;
import java.util.Optional;

public interface UserLookupService {

    record UserSummary(
            Long userId,
            String name,
            String username,
            String location,
            String initials,
            int gamesPlayed,
            String status,
            String duelLinkId
    ) {}

    Optional<UserSummary> findById(Long userId);

    Optional<UserSummary> findByUsernameIgnoreCase(String username);

    List<UserSummary> findAllByIds(List<Long> userIds);

    List<UserSummary> searchByUsername(String query);
}