package com.example.kj_updates;

import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

public class StoryItem {

    private String storyId;
    private final String userId;
    private final String username;
    private final String storyText;
    private final Timestamp timestamp;
    private int colorRes;

    public StoryItem(String storyId, String userId, String username, String storyText, Timestamp timestamp) {
        this.storyId = storyId;
        this.userId = userId;
        this.username = username;
        this.storyText = storyText;
        this.timestamp = timestamp;
    }

    public String getStoryId() {
        return storyId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getStoryText() {
        return storyText;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public int getColorRes() {
        return colorRes;
    }

    public void setColorRes(int colorRes) {
        this.colorRes = colorRes;
    }

    public String getInitials() {
        String name = username == null ? "U" : username.trim();
        String[] parts = name.split("\\s+");
        if (parts.length == 0 || parts[0].isEmpty()) return "U";
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("username", username);
        data.put("storyText", storyText);
        data.put("timestamp", timestamp);
        return data;
    }
}
