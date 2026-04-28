package com.example.kj_updates;

import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

public class Post {

    private final String userId;
    private final String username;
    private final String postText;
    private final String postImage;
    private final Timestamp timestamp;
    private final int likesCount;

    public Post(
            String userId,
            String username,
            String postText,
            String postImage,
            Timestamp timestamp,
            int likesCount
    ) {
        this.userId = userId;
        this.username = username;
        this.postText = postText;
        this.postImage = postImage;
        this.timestamp = timestamp;
        this.likesCount = likesCount;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("username", username);
        data.put("postText", postText);
        data.put("postImage", postImage);
        data.put("timestamp", timestamp);
        data.put("likesCount", likesCount);
        return data;
    }
}
