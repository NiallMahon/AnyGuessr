package com.anyguessr.anyguessrbackend.social.app;

import com.anyguessr.anyguessrbackend.social.domain.Friend;
import com.anyguessr.anyguessrbackend.social.infrastructure.repository.FriendRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FriendService {

    private final FriendRepository friendRepository;

    public FriendService(FriendRepository friendRepository) {
        this.friendRepository = friendRepository;
    }

    public List<Friend> getFriendsByUserId(String userId) {
        return friendRepository.findByUserId(userId);
    }

    public Optional<Friend> getFriendByUserIdAndUsername(String userId, String username) {
        return friendRepository.findByUserIdAndUsername(userId, username);
    }

    public Friend saveFriend(Friend friend) {
        return friendRepository.save(friend);
    }

    public void deleteFriend(Long id) {
        friendRepository.deleteById(id);
    }
}