package com.example.kj_updates;

public class StoryItem {

    private final String name;
    private final String initials;
    private final int colorRes;

    public StoryItem(String name, String initials, int colorRes) {
        this.name = name;
        this.initials = initials;
        this.colorRes = colorRes;
    }

    public String getName() {
        return name;
    }

    public String getInitials() {
        return initials;
    }

    public int getColorRes() {
        return colorRes;
    }
}
