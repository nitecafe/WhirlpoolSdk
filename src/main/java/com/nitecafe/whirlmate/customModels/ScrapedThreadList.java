package com.nitecafe.whirlmate.customModels;

import java.util.ArrayList;
import java.util.Map;

public class ScrapedThreadList {
    private int forum_id;
    private String forum_title;
    private int pageCount;
    private Map<String, Integer> groups;
    private ArrayList<ScrapedThread> threads;

    public ScrapedThreadList(int forumId, String forumTitle) {
        this.forum_id = forumId;
        this.forum_title = forumTitle;
    }

    public int getForum_id() {
        return forum_id;
    }

    public String getForum_title() {
        return forum_title;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public Map<String, Integer> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, Integer> groups) {
        this.groups = groups;
    }

    public ArrayList<ScrapedThread> getThreads() {
        return threads;
    }

    public void setThreads(ArrayList<ScrapedThread> threads) {
        this.threads = threads;
    }
}
