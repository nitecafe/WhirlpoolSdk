package com.nitecafe.whirlmate;

import com.nitecafe.whirlmate.customModels.ScrapedPostList;
import com.nitecafe.whirlmate.customModels.ScrapedThread;
import com.nitecafe.whirlmate.customModels.ScrapedThreadList;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

public interface IThreadScraper {

    boolean isPublicForum(int forumId);

    Observable<ScrapedThreadList> scrapeThreadsFromForumObservable(int forumId, int pageNumber, int groupId);

    Observable<ScrapedPostList> scrapePostsFromThreadObservable(int threadId, int page);

    Observable<ArrayList<ScrapedThread>> ScrapPopularThreadsObservable();

    Observable<List<ScrapedThread>> searchThreadsObservable(int forumId, int groupId, String query);
}
