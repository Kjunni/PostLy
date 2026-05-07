package com.example.kj_updates;

public class FeedItem {

    public enum Type {
        SOCIAL,
        NEWS
    }

    private final String postId;
    private final String userId;
    private final Type type;
    private final String author;
    private final String meta;
    private final String title;
    private final String body;
    private final String bannerText;
    private final String initials;
    private final int likes;
    private final int comments;
    private final int shares;
    private final int colorRes;
    private final boolean likedByCurrentUser;
    private final boolean ownedByCurrentUser;

    public FeedItem(
            String postId,
            String userId,
            Type type,
            String author,
            String meta,
            String title,
            String body,
            String bannerText,
            String initials,
            int likes,
            int comments,
            int shares,
            int colorRes,
            boolean likedByCurrentUser,
            boolean ownedByCurrentUser
    ) {
        this.postId = postId;
        this.userId = userId;
        this.type = type;
        this.author = author;
        this.meta = meta;
        this.title = title;
        this.body = body;
        this.bannerText = bannerText;
        this.initials = initials;
        this.likes = likes;
        this.comments = comments;
        this.shares = shares;
        this.colorRes = colorRes;
        this.likedByCurrentUser = likedByCurrentUser;
        this.ownedByCurrentUser = ownedByCurrentUser;
    }

    public String getPostId() {
        return postId;
    }

    public String getUserId() {
        return userId;
    }

    public Type getType() {
        return type;
    }

    public String getAuthor() {
        return author;
    }

    public String getMeta() {
        return meta;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getBannerText() {
        return bannerText;
    }

    public String getInitials() {
        return initials;
    }

    public int getLikes() {
        return likes;
    }

    public int getComments() {
        return comments;
    }

    public int getShares() {
        return shares;
    }

    public int getColorRes() {
        return colorRes;
    }

    public boolean isLikedByCurrentUser() {
        return likedByCurrentUser;
    }

    public boolean isOwnedByCurrentUser() {
        return ownedByCurrentUser;
    }
}
