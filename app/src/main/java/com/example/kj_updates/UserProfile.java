package com.example.kj_updates;

import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

public class UserProfile {

    private final String username;
    private final String email;
    private final String bio;
    private final String profileImage;
    private final Timestamp createdAt;

    public UserProfile(String username, String email, String bio, String profileImage, Timestamp createdAt) {
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.profileImage = profileImage;
        this.createdAt = createdAt;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("email", email);
        data.put("bio", bio);
        data.put("profileImage", profileImage);
        data.put("createdAt", createdAt);
        return data;
    }
}
