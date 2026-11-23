package com.anyguessr.anyguessrbackend.social.infrastructure.repository;

import com.anyguessr.anyguessrbackend.social.domain.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findByUserId(String userId);
    Optional<Friend> findByUserIdAndUsername(String userId, String username);
}