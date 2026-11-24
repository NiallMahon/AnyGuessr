package com.anyguessr.anyguessrbackend.social.app;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anyguessr.anyguessrbackend.social.domain.BlockedUser;
import com.anyguessr.anyguessrbackend.social.infrastructure.repository.BlockedUserRepository;
import com.anyguessr.anyguessrbackend.social.infrastructure.repository.FriendLinkRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @Mock
    private FriendLinkRepository friendLinkRepository;

    @Mock
    private BlockedUserRepository blockedUserRepository;

    @Mock
    private UserLookupService userLookupService;

    private FriendService friendService;

    @BeforeEach
    void setUp() {
        friendService = new FriendService(
                friendLinkRepository,
                blockedUserRepository,
                userLookupService
        );
    }

    @Test
    void blockUser_removesFriendAndSavesBlock() {
        when(friendLinkRepository.findBetween(1L, 2L)).thenReturn(Optional.empty());
        when(blockedUserRepository.existsByIdBlockerIdAndIdBlockedId(1L, 2L)).thenReturn(false);

        friendService.blockUser(1L, 2L);

        verify(blockedUserRepository).save(any(BlockedUser.class));
    }

    @Test
    void blockUser_selfBlockThrowsBadRequest() {
        assertThrows(ResponseStatusException.class, () -> friendService.blockUser(1L, 1L));
        verify(blockedUserRepository, never()).save(any());
    }
}