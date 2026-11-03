package com.anyguessr.anyguessrbackend.social.ui;

import com.anyguessr.anyguessrbackend.social.domain.Friend;
import com.anyguessr.anyguessrbackend.social.app.FriendService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/friends")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class FriendController {
    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @GetMapping("/{userId}")
    public List<Friend> getFriends(@PathVariable String userId) {
        return friendService.getFriends(userId);
    }

    @PostMapping
    public Friend addFriend(@RequestBody Friend friend) {
        return friendService.addFriend(friend);
    }

    @DeleteMapping("/{id}")
    public void deleteFriend(@PathVariable Long id) {
        friendService.deleteFriend(id);
    }
}