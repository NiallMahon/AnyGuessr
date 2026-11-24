package com.anyguessr.anyguessrbackend.social.infrastructure.repository;

import com.anyguessr.anyguessrbackend.social.domain.BlockedUser;
import com.anyguessr.anyguessrbackend.social.domain.BlockedUser.BlockedUserId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockedUserRepository extends JpaRepository<BlockedUser, BlockedUserId> {

    List<BlockedUser> findByIdBlockerId(Long blockerId);

    boolean existsByIdBlockerIdAndIdBlockedId(Long blockerId, Long blockedId);

    void deleteByIdBlockerIdAndIdBlockedId(Long blockerId, Long blockedId);
}