package com.anyguessr.anyguessrbackend.identity.social;

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