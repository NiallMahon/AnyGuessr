package com.anyguessr.anyguessrbackend.social.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "friends")
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private String name;
    private String username;
    private String location;
    private String initials;
    private int gamesPlayed;
    private String status;
    private String duelLinkId;

    public Friend() {}

    public Friend(Long id, String userId, String name, String username, String location, String initials, int gamesPlayed, String status, String duelLinkId) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.username = username;
        this.location = location;
        this.initials = initials;
        this.gamesPlayed = gamesPlayed;
        this.status = status;
        this.duelLinkId = duelLinkId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getInitials() { return initials; }
    public void setInitials(String initials) { this.initials = initials; }

    public int getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDuelLinkId() { return duelLinkId; }
    public void setDuelLinkId(String duelLinkId) { this.duelLinkId = duelLinkId; }
}