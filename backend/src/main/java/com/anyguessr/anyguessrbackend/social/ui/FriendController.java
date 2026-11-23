package com.anyguessr.anyguessrbackend.social.ui;

import com.anyguessr.anyguessrbackend.social.app.FriendService;
import com.anyguessr.anyguessrbackend.social.domain.Friend;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "*")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @GetMapping("/user/{userId}")
    public List<Friend> getFriendsByUserId(@PathVariable String userId) {
        return friendService.getFriendsByUserId(userId);
    }

    @PostMapping
    public Friend addFriend(@RequestBody Friend friend) {
        return friendService.saveFriend(friend);
    }

    @GetMapping("/lookup/{userId}/{username}")
    public ResponseEntity<Friend> getFriendByUserIdAndUsername(@PathVariable String userId, @PathVariable String username) {
        return friendService.getFriendByUserIdAndUsername(userId, username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFriend(@PathVariable Long id) {
        friendService.deleteFriend(id);
        return ResponseEntity.noContent().build();
    }
}