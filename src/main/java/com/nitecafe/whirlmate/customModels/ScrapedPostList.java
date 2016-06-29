package com.nitecafe.whirlmate.customModels;

import java.util.ArrayList;

public class ScrapedPostList {

    private int threadId;
    private String threadTitle;
    private ArrayList<ScrapedPost> scrapedPosts = new ArrayList<>();
    private String notebar;
    private int pageCount;

    public ScrapedPostList(int threadId, String threadTitle) {
        this.threadId = threadId;
        this.threadTitle = threadTitle;
    }

    public int getThreadId() {
        return threadId;
    }

    public String getThreadTitle() {
        return threadTitle;
    }

    public ArrayList<ScrapedPost> getScrapedPosts() {
        return scrapedPosts;
    }

    public void setScrapedPosts(ArrayList<ScrapedPost> scrapedPosts) {
        this.scrapedPosts = scrapedPosts;
    }

    public String getNotebar() {
        return notebar;
    }

    public void setNotebar(String notebar) {
        this.notebar = notebar;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }
}
