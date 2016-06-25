package com.nitecafe.whirlmate;

import com.nitecafe.whirlmate.models.*;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;

public interface IWhirlpoolService {

    @GET("?get=news")
    Observable<NewsList> GetNews();

    @GET("?get=forum")
    Observable<ForumList> GetForum();

    @GET("?get=recent")
    Observable<RecentList> GetRecent();

    @GET("?get=watched")
    Observable<WatchedList> GetWatched(@Query("watchedmode") int watchedMode);

    @GET("?get=threads")
    Observable<ForumThreadList> GetThreads(@Query("forumIds") int forumId, @Query("threadcount") int threadCount);

    @GET("?get=watchedremove")
    Observable<Void> SetThreadAsUnwatched(@Query("watchedremove") int threadId);

    @GET("?get=watchedadd")
    Observable<Void> SetThreadAsWatched(@Query("watchedadd") int threadId);

    @GET("?get=watchedread")
    Observable<Void> SetThreadAsRead(@Query("watchedread") int threadId);

    @GET("?get=whims")
    Observable<WhimsList> GetWhims();

    @GET("?get=whim")
    Observable<Void> MarkWhimAsRead(@Query("whimid") int whimId);

    @GET("?get=user")
    Observable<UserDetailsList> GetUserDetails();

    @GET("?get=contacts")
    Observable<ContactList> GetContacts();
}
