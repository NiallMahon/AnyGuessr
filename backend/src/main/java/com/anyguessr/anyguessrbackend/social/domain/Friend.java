package com.anyguessr.anyguessrbackend.social.domain;
import org.springframework.web.bind.annotation.*;

public class Friend {
    private Long id;
    private String name;
    private String location;
    private String initials;
    private int gamesPlayed;
    private String status;

    public Friend() {}

    public Friend(Long id, String name, String location, String initials, int gamesPlayed, String status) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.initials = initials;
        this.gamesPlayed = gamesPlayed;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLocation() { return location; }
    public void getLocation(String location) { this.location = location; }
    public String getInitials() { return initials; }
    public void setInitials(String initials) { this.initials = initials; }
    public int getGamesPlayed() { return gamesPlayed; }
    public void setGamesPlayed(int gamesPlayed) { this.gamesPlayed = gamesPlayed; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}