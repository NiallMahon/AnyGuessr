package com.anyguessr.anyguessrbackend.identity.social;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class FriendService {
    private final List<Friend> friends = new ArrayList<>();

    public FriendService() {
        friends.add(new Friend(1L, "Alice Smith", "New York, USA", "AS", 42, "Online"));
        friends.add(new Friend(2L, "Bob Jones", "London, UK", "BJ", 15, "Offline"));
        friends.add(new Friend(3L,"Danny Johnson","Galway Ireland", "DJ", 73, "Online"));
    }

    public List<Friend> getFriends(String userId) {
        return friends;
    }

    public Friend addFriend(Friend friend) {
        friends.add(friend);
        return friend;
    }

    public void deleteFriend(Long id) {
        friends.removeIf(friend -> friend.getId().equals(id));
    }
}