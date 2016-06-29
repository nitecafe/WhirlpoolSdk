package com.nitecafe.whirlmate.customModels;

public class ScrapedPost {

    private String id;
    private User user;
    private String postedTime;
    private String content;
    private boolean edited;
    private boolean op;
    private boolean deleted;
    private String shortCode;

    public ScrapedPost(String id, User user, String postedTime, String content, boolean edited, boolean op) {
        this.id = id;
        this.user = user;
        this.postedTime = postedTime;
        this.content = content;
        this.edited = edited;
        this.op = op;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isOp() {
        return op;
    }

    public boolean isEdited() {
        return edited;
    }

    public String getContent() {
        return content;
    }

    public String getPostedTime() {
        return postedTime;
    }

    public User getUser() {
        return user;
    }

    public String getId() {
        return id;
    }

    public int getIdInteger() {
        return Integer.parseInt(id);
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }
}
