package com.example.kj_updates;

import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

public class LikeRecord {

    private final String userId;
    private final Timestamp likedAt;

    public LikeRecord(String userId) {
        this.userId = userId;
        this.likedAt = Timestamp.now();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("likedAt", likedAt);
        return data;
    }
}
