package com.anyguessr.anyguessrbackend.social.ui;

import com.anyguessr.anyguessrbackend.social.app.FriendService;
import com.anyguessr.anyguessrbackend.social.app.FriendService.RequestView;
import com.anyguessr.anyguessrbackend.social.app.FriendService.UserSearchResult;
import com.anyguessr.anyguessrbackend.social.app.UserLookupService.UserSummary;
import com.anyguessr.anyguessrbackend.social.domain.FriendLink;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "*")
public class FriendController {

    private final FriendService friendService;

    public record SendFriendRequestBody(Long requesterUserId, String targetUsername) {}

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @GetMapping("/user/{userId}")
    public List<UserSummary> getFriendsByUserId(@PathVariable Long userId) {
        return friendService.getFriendsByUserId(userId);
    }

    @GetMapping("/search")
    public List<UserSearchResult> searchPlayers(
            @RequestParam Long userId,
            @RequestParam String query
    ) {
        return friendService.searchPlayers(userId, query);
    }

    @GetMapping("/requests/incoming/{userId}")
    public List<RequestView> getIncomingRequests(@PathVariable Long userId) {
        return friendService.getIncomingPendingRequests(userId);
    }

    @GetMapping("/requests/outgoing/{userId}")
    public List<RequestView> getOutgoingRequests(@PathVariable Long userId) {
        return friendService.getOutgoingPendingRequests(userId);
    }

    @PostMapping("/requests")
    public ResponseEntity<FriendLink> sendFriendRequest(@RequestBody SendFriendRequestBody body) {
        FriendLink created = friendService.sendFriendRequest(body.requesterUserId(), body.targetUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/requests/accept")
    public ResponseEntity<Void> acceptRequest(
            @RequestParam Long userId,
            @RequestParam Long requesterUserId
    ) {
        friendService.acceptRequest(userId, requesterUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/requests/decline")
    public ResponseEntity<Void> declineRequest(
            @RequestParam Long userId,
            @RequestParam Long requesterUserId
    ) {
        friendService.declineRequest(userId, requesterUserId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/requests/cancel")
    public ResponseEntity<Void> cancelRequest(
            @RequestParam Long userId,
            @RequestParam Long recipientUserId
    ) {
        friendService.cancelOutgoingRequest(userId, recipientUserId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/{friendUserId}")
    public ResponseEntity<Void> removeFriend(
            @PathVariable Long userId,
            @PathVariable Long friendUserId
    ) {
        friendService.removeMutualFriend(userId, friendUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/blocked/{userId}")
    public List<UserSummary> getBlockedUsers(@PathVariable Long userId) {
        return friendService.getBlockedUsers(userId);
    }

    @PostMapping("/block/{userId}/{targetUserId}")
    public ResponseEntity<Void> blockUser(
            @PathVariable Long userId,
            @PathVariable Long targetUserId
    ) {
        friendService.blockUser(userId, targetUserId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/block/{userId}/{targetUserId}")
    public ResponseEntity<Void> unblockUser(
            @PathVariable Long userId,
            @PathVariable Long targetUserId
    ) {
        friendService.unblockUser(userId, targetUserId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity> handleStatusException(ResponseStatusException ex) {
        String message = ex.getReason() != null ? ex.getReason() : "Request failed";
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("message", message));
    }
}