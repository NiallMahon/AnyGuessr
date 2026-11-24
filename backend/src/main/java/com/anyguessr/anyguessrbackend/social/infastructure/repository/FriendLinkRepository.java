package com.anyguessr.anyguessrbackend.social.infrastructure.repository;

import com.anyguessr.anyguessrbackend.social.domain.FriendLink;
import com.anyguessr.anyguessrbackend.social.domain.FriendLink.FriendLinkId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FriendLinkRepository extends JpaRepository<FriendLink, FriendLinkId> {

    @Query("SELECT f FROM FriendLink f WHERE (f.id.userIdA = :userId OR f.id.userIdB = :userId) AND f.status = :status")
    List<FriendLink> findAllByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Query("SELECT f FROM FriendLink f WHERE (f.id.userIdA = :userId OR f.id.userIdB = :userId) AND f.requestedBy <> :userId AND f.status = 'PENDING'")
    List<FriendLink> findIncomingPending(@Param("userId") Long userId);

    @Query("SELECT f FROM FriendLink f WHERE (f.id.userIdA = :userId OR f.id.userIdB = :userId) AND f.requestedBy = :userId AND f.status = 'PENDING'")
    List<FriendLink> findOutgoingPending(@Param("userId") Long userId);

    @Query("SELECT f FROM FriendLink f WHERE f.id.userIdA = :minId AND f.id.userIdB = :maxId")
    Optional<FriendLink> findBetween(@Param("minId") Long minId, @Param("maxId") Long maxId);
}