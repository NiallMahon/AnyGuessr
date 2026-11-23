package com.anyguessr.anyguessrbackend.social.app;

import com.anyguessr.anyguessrbackend.social.app.UserLookupService.UserSummary;
import com.anyguessr.anyguessrbackend.social.domain.BlockedUser;
import com.anyguessr.anyguessrbackend.social.domain.FriendLink;
import com.anyguessr.anyguessrbackend.social.domain.FriendLink.Status;
import com.anyguessr.anyguessrbackend.social.infrastructure.repository.BlockedUserRepository;
import com.anyguessr.anyguessrbackend.social.infrastructure.repository.FriendLinkRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FriendService {

    public record RequestPair(
            Long otherUserId,
            Long requestedBy
    ) {}

    public record RequestView(
            Long otherUserId,
            String otherUsername,
            String otherName,
            String otherInitials,
            String status
    ) {}

    public record UserSearchResult(
            UserSummary user,
            String relationship
    ) {}

    private final FriendLinkRepository friendLinkRepository;
    private final BlockedUserRepository blockedUserRepository;
    private final UserLookupService userLookupService;

    public FriendService(
            FriendLinkRepository friendLinkRepository,
            BlockedUserRepository blockedUserRepository,
            UserLookupService userLookupService
    ) {
        this.friendLinkRepository = friendLinkRepository;
        this.blockedUserRepository = blockedUserRepository;
        this.userLookupService = userLookupService;
    }

    public List<UserSummary> getFriendsByUserId(Long userId) {
        List<Long> friendIds = friendLinkRepository.findAllByUserIdAndStatus(userId, Status.ACCEPTED.name()).stream()
                .map(link -> link.getUserIdA().equals(userId) ? link.getUserIdB() : link.getUserIdA())
                .toList();
        return userLookupService.findAllByIds(friendIds);
    }

    public List<UserSummary> getBlockedUsers(Long userId) {
        List<Long> blockedIds = blockedUserRepository.findByIdBlockerId(userId).stream()
                .map(BlockedUser::getBlockedId)
                .toList();
        return userLookupService.findAllByIds(blockedIds);
    }

    public List<RequestView> getIncomingPendingRequests(Long userId) {
        List<FriendLink> pending = friendLinkRepository.findIncomingPending(userId);
        List<Long> otherIds = pending.stream()
                .map(link -> link.getUserIdA().equals(userId) ? link.getUserIdB() : link.getUserIdA())
                .distinct()
                .toList();
        Map<Long, UserSummary> usersById = userLookupService.findAllByIds(otherIds).stream()
                .collect(Collectors.toMap(UserSummary::userId, Function.identity()));
        return pending.stream()
                .map(link -> toRequestView(
                        link.getUserIdA().equals(userId) ? link.getUserIdB() : link.getUserIdA(),
                        link.getStatus(),
                        usersById
                ))
                .toList();
    }

    public List<RequestView> getOutgoingPendingRequests(Long userId) {
        List<FriendLink> pending = friendLinkRepository.findOutgoingPending(userId);
        List<Long> otherIds = pending.stream()
                .map(link -> link.getUserIdA().equals(userId) ? link.getUserIdB() : link.getUserIdA())
                .distinct()
                .toList();
        Map<Long, UserSummary> usersById = userLookupService.findAllByIds(otherIds).stream()
                .collect(Collectors.toMap(UserSummary::userId, Function.identity()));
        return pending.stream()
                .map(link -> toRequestView(
                        link.getUserIdA().equals(userId) ? link.getUserIdB() : link.getUserIdA(),
                        link.getStatus(),
                        usersById
                ))
                .toList();
    }

    public List<UserSearchResult> searchPlayers(Long currentUserId, String query) {
        List<UserSummary> matches = userLookupService.searchByUsername(query);

        Set<Long> friendIds = friendLinkRepository.findAllByUserIdAndStatus(currentUserId, Status.ACCEPTED.name()).stream()
                .map(link -> link.getUserIdA().equals(currentUserId) ? link.getUserIdB() : link.getUserIdA())
                .collect(Collectors.toSet());

        Set<Long> outgoingIds = friendLinkRepository.findOutgoingPending(currentUserId).stream()
                .map(link -> link.getUserIdA().equals(currentUserId) ? link.getUserIdB() : link.getUserIdA())
                .collect(Collectors.toSet());

        Set<Long> incomingIds = friendLinkRepository.findIncomingPending(currentUserId).stream()
                .map(link -> link.getUserIdA().equals(currentUserId) ? link.getUserIdB() : link.getUserIdA())
                .collect(Collectors.toSet());

        Set<Long> blockedIds = new HashSet<>(
                blockedUserRepository.findByIdBlockerId(currentUserId).stream()
                        .map(BlockedUser::getBlockedId)
                        .toList()
        );

        return matches.stream()
                .filter(u -> !u.userId().equals(currentUserId))
                .filter(u -> !blockedIds.contains(u.userId()))
                .filter(u -> !blockedUserRepository.existsByIdBlockerIdAndIdBlockedId(u.userId(), currentUserId))
                .map(u -> {
                    String rel = "NONE";
                    if (friendIds.contains(u.userId())) {
                        rel = "FRIEND";
                    } else if (outgoingIds.contains(u.userId())) {
                        rel = "OUTGOING_PENDING";
                    } else if (incomingIds.contains(u.userId())) {
                        rel = "INCOMING_PENDING";
                    }
                    return new UserSearchResult(u, rel);
                })
                .toList();
    }

    @Transactional
    public FriendLink sendFriendRequest(Long requesterUserId, String targetUsername) {
        if (requesterUserId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Requester user ID is required");
        }
        if (targetUsername == null || targetUsername.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target username is required");
        }

        UserSummary requester = userLookupService.findById(requesterUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Current user not found"));

        UserSummary recipient = userLookupService.findByUsernameIgnoreCase(targetUsername.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No user found with that username"));

        if (requester.userId().equals(recipient.userId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot send a friend request to yourself");
        }

        if (blockedUserRepository.existsByIdBlockerIdAndIdBlockedId(requester.userId(), recipient.userId())
                || blockedUserRepository.existsByIdBlockerIdAndIdBlockedId(recipient.userId(), requester.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot send a friend request to this user");
        }

        long minId = Math.min(requester.userId(), recipient.userId());
        long maxId = Math.max(requester.userId(), recipient.userId());

        Optional<FriendLink> existingOpt = friendLinkRepository.findBetween(minId, maxId);
        if (existingOpt.isPresent()) {
            FriendLink existing = existingOpt.get();
            Status currentStatus = Status.valueOf(existing.getStatus());
            if (currentStatus == Status.ACCEPTED) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already friends with this user");
            }
            if (currentStatus == Status.PENDING) {
                if (requester.userId().equals(existing.getRequestedBy())) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Friend request already pending");
                }
                existing.setStatus(Status.ACCEPTED.name());
                return friendLinkRepository.save(existing);
            }
            existing.setRequestedBy(requester.userId());
            existing.setStatus(Status.PENDING.name());
            return friendLinkRepository.save(existing);
        }

        return friendLinkRepository.save(
                new FriendLink(requester.userId(), recipient.userId(), requester.userId(), Status.PENDING)
        );
    }

    @Transactional
    public void acceptRequest(Long actingUserId, Long requesterUserId) {
        long minId = Math.min(actingUserId, requesterUserId);
        long maxId = Math.max(actingUserId, requesterUserId);

        FriendLink link = friendLinkRepository.findBetween(minId, maxId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request not found"));

        if (!Status.PENDING.name().equals(link.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Request is no longer pending");
        }
        if (actingUserId.equals(link.getRequestedBy())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the recipient can accept this request");
        }

        link.setStatus(Status.ACCEPTED.name());
        friendLinkRepository.save(link);
    }

    @Transactional
    public void declineRequest(Long actingUserId, Long requesterUserId) {
        long minId = Math.min(actingUserId, requesterUserId);
        long maxId = Math.max(actingUserId, requesterUserId);

        FriendLink link = friendLinkRepository.findBetween(minId, maxId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request not found"));

        if (!Status.PENDING.name().equals(link.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Request is no longer pending");
        }
        if (actingUserId.equals(link.getRequestedBy())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the recipient can decline this request");
        }

        link.setStatus(Status.DECLINED.name());
        friendLinkRepository.save(link);
    }

    @Transactional
    public void cancelOutgoingRequest(Long actingUserId, Long recipientUserId) {
        long minId = Math.min(actingUserId, recipientUserId);
        long maxId = Math.max(actingUserId, recipientUserId);

        FriendLink link = friendLinkRepository.findBetween(minId, maxId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request not found"));

        if (!Status.PENDING.name().equals(link.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Request is no longer pending");
        }
        if (!actingUserId.equals(link.getRequestedBy())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the sender can cancel this request");
        }

        link.setStatus(Status.CANCELLED.name());
        friendLinkRepository.save(link);
    }

    @Transactional
    public void removeMutualFriend(Long userId, Long friendUserId) {
        long minId = Math.min(userId, friendUserId);
        long maxId = Math.max(userId, friendUserId);
        friendLinkRepository.findBetween(minId, maxId).ifPresent(friendLinkRepository::delete);
    }

    @Transactional
    public void blockUser(Long blockerUserId, Long blockedUserId) {
        if (blockerUserId.equals(blockedUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot block yourself");
        }

        removeMutualFriend(blockerUserId, blockedUserId);

        if (!blockedUserRepository.existsByIdBlockerIdAndIdBlockedId(blockerUserId, blockedUserId)) {
            blockedUserRepository.save(new BlockedUser(blockerUserId, blockedUserId));
        }
    }

    @Transactional
    public void unblockUser(Long blockerUserId, Long blockedUserId) {
        blockedUserRepository.deleteByIdBlockerIdAndIdBlockedId(blockerUserId, blockedUserId);
    }

    private RequestView toRequestView(Long otherUserId, String status, Map<Long, UserSummary> usersById) {
        UserSummary other = usersById.get(otherUserId);
        String username = other != null ? other.username() : String.valueOf(otherUserId);
        return new RequestView(
                otherUserId,
                username,
                username,
                other != null ? other.initials() : "U",
                status
        );
    }
}