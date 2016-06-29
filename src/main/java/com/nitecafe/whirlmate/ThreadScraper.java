package com.nitecafe.whirlmate;

import com.nitecafe.whirlmate.customModels.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import rx.Observable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nitecafe.whirlmate.Constants.FORUM_URL;
import static com.nitecafe.whirlmate.Constants.POPULAR_URL;
import static com.nitecafe.whirlmate.Constants.THREAD_URL;

/**
 * More refactoring needed!
 */
public class ThreadScraper implements IThreadScraper {

    // these forum IDs are public, and we can scrape the data from them
    private static int[] PUBLIC_FORUMS = {92, 100, 142, 82, 9, 107, 135, 80, 136, 125, 116, 63,
            127, 139, 7, 129, 130, 131, 10, 38, 39, 91, 87, 112, 132, 8, 83, 138, 143, 133, 58, 106,
            126, 71, 118, 137, 114, 123, 128, 141, 140, 144, 18, 14, 15, 68, 72, 94, 90, 102, 105,
            109, 108, 147, 31, 67, 5, 148, 149, 150};

    private static String buildSearchUrl(int forumId, int groupId, String query) throws URISyntaxException {
        URI uri = new URI("http", "forums.whirlpool.net.au", "/forum/", "action=threads_search&f=" + forumId + "&fg=" + groupId + "&q=" + query, null);
        return uri.toASCIIString();
    }

    /**
     * Checks if a forum is public (ie. it can be scraped)
     */
    @Override
    public boolean isPublicForum(int forumId) {
        for (int PUBLIC_FORUM : ThreadScraper.PUBLIC_FORUMS) {
            if (PUBLIC_FORUM == forumId) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Observable<ScrapedThreadList> scrapeThreadsFromForumObservable(int forumId, int pageNumber, int groupId) {
        return Observable.create(subscriber -> {
            ScrapedThreadList scrapedThreads = null;
            try {
                scrapedThreads = scrapeThreadsFromForum(forumId, pageNumber, groupId);
            } catch (Exception e) {
                subscriber.onError(e);
            }
            subscriber.onNext(scrapedThreads);
            subscriber.onCompleted();
        });
    }

    private ScrapedThreadList scrapeThreadsFromForum(int forumId, int pageNumber, int groupId) throws IOException {
        String forumUrl = FORUM_URL + forumId + "?p=" + pageNumber;

        if (groupId != 0) {
            forumUrl += "&g=" + groupId;
        }

        Document doc = downloadPage(forumUrl);

        String forumTitle = getForumTitle(doc);
        ScrapedThreadList threadList = new ScrapedThreadList(forumId, forumTitle);

        int pageCount = getPageCount(doc);
        threadList.setPageCount(pageCount);

        Map<String, Integer> groups = getForumGroups(doc);
        threadList.setGroups(groups);

        ArrayList<ScrapedThread> threads = getScrapedThreads(forumId, doc);
        threadList.setThreads(threads);

        return threadList;
    }

    private ArrayList<ScrapedThread> getScrapedThreads(int forumId, Document doc) {
        ArrayList<ScrapedThread> threads = new ArrayList<>();
        Elements trs = doc.select("tr");

        for (Element tr : trs) {
            Set<String> tr_classes = tr.classNames();

            ScrapedThread t = getThreadFromTableRow(tr, null, forumId);

            if (tr_classes.contains("sticky")) {
                t.setSticky(true);
            }
            if (tr_classes.contains("closed")) {
                t.setClosed(true);
            }
            if (tr_classes.contains("deleted")) {
                t.setDeleted(true);
            }
            if (tr_classes.contains("pointer")) {
                t.setMoved(true);
            }

            if (t != null) {
                threads.add(t);
            }
        }
        return threads;
    }

    private Map<String, Integer> getForumGroups(Document doc) {
        // get the groups in this forum
        Map<String, Integer> groups = null;
        try {
            groups = new LinkedHashMap<>();

            Elements group_options = doc.select("select[name=g] option");

            for (Element group_option : group_options) {
                if (!group_option.attr("value").equals("0")) {
                    groups.put(group_option.text(), Integer.parseInt(group_option.attr("value")));
                }
            }
        } catch (NullPointerException e) {
            // no groups in this forum, do nothing
        }
        return groups;
    }

    private String getForumTitle(Document doc) {
        // get the forum title
        String forumTitle;

        try {
            forumTitle = doc.select("ul#breadcrumb li").last().text();
        } catch (NullPointerException e) {
            forumTitle = "";
        }
        return forumTitle;
    }

    private int getPageCount(Document doc) {
        Element p = doc.select("select[name=p] option").last(); // get the last option
        int pageCount = 0;
        if (p != null)
            pageCount = Integer.parseInt(p.text());

        if (pageCount == -1) {
            // no select box present, get pagination list elements
            p = doc.select("ul.pagination li").last();
            if (p != null)
                pageCount = Integer.parseInt(p.text().replace("\u00a0", "").trim());
        }
        return pageCount;
    }

    private ScrapedThread getThreadFromTableRow(Element tr, String forum, int forumId) {
        int id = 0;
        String title = "";
        String last_poster = "";
        String last_post_date = "";
        String first_poster = "";
        String first_post_date = "";
        int page_count = 1;

        Elements tds = tr.children();
        // title reps reads oldest newest
        for (Element td : tds) {
            Set<String> td_classes = td.classNames();

            if (td_classes.contains("title")) {

                String url = "";

                try {
                    for (Element child : td.children()) {
                        if (child.hasClass("title")) {
                            title = child.text();
                            url = child.attr("href");
                        }
                    }

                } catch (Exception e) {
                    title = td.text();
                    url = td.select("a").get(0).attr("href");
                }

                Pattern thread_id_regex = Pattern.compile("(t=([0-9]+))|(/archive/([0-9]+))");
                Matcher m = thread_id_regex.matcher(url);
                while (m.find()) {
                    try {
                        id = Integer.parseInt(m.group(2));
                    } catch (NumberFormatException e) {
                        id = Integer.parseInt(m.group(4));
                    }
                }

                // get thread page count
                try {
                    Element page_element = td.select("script").get(0);

                    Pattern page_count_regex = Pattern.compile("([0-9]+),([0-9]+)");
                    Matcher page_matcher = page_count_regex.matcher(page_element.html());
                    while (page_matcher.find()) {
                        page_count = Integer.parseInt(page_matcher.group(2));
                    }

                    //page_count = Integer.parseInt(page_element.text().replace(" ", ""));
                } catch (NullPointerException e) {
                    // no page list, must only be 1 page
                } catch (IndexOutOfBoundsException e) {
                    // no page list
                } catch (NumberFormatException e) {
                    // not a number, probably a deleted thread; ignore
                }
            } else if (td_classes.contains("newest")) {
                try {
                    last_poster = td.child(0).text();
                } catch (Exception e) {
                }

                last_post_date = td.ownText();
            } else if (td_classes.contains("oldest")) {
                try {
                    first_poster = td.child(0).text();
                } catch (Exception e) {
                }

                first_post_date = td.ownText();
            }
        }

        if (last_poster.equals("")) { // no replies yet
            // set the first poster as the last poster (since there's only 1 post)
            last_poster = first_poster;
            last_post_date = first_post_date;
        }

        ScrapedThread t = new ScrapedThread(id, title, null, last_poster, forum, forumId);
        t.setLastDateText(last_post_date);
        t.setPageCount(page_count);

        if (title.isEmpty()) {
            return null;
        }

        return t;
    }

    @Override
    public Observable<ScrapedPostList> scrapePostsFromThreadObservable(int threadId, int page) {
        return Observable.create(subscriber -> {
            ScrapedPostList posts = null;
            try {
                posts = scrapePostsFromThread(threadId, "", page);
            } catch (IOException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
            subscriber.onNext(posts);
            subscriber.onCompleted();
        });
    }

    private ScrapedPostList scrapePostsFromThread(int threadId, String threadTitle, int page) throws IOException {

        ArrayList<ScrapedPost> scrapedPosts = new ArrayList<>();

        String threadUrl = THREAD_URL + threadId;
        if (page != 1) {
            threadUrl += "&p=" + page;
        }

        Document doc = downloadPage(threadUrl);

        // check for an error message
        Elements alert = doc.select("#alert");
        if (alert != null && alert.size() > 0) {
            throw new IOException("Private forum");
        }

        if (threadTitle == null || threadTitle.isEmpty()) { // no thread title was passed
            // scrape the title from the page
            Elements breadcrumbElements = doc.select("#breadcrumb li");
            threadTitle = breadcrumbElements.last().text();
        }

        // get page count
        Elements paginationElements = doc.select("#top_pagination li");
        int page_count = paginationElements.size() - 2; // list elements, subtract first date and last date elements
        if (page_count <= 0) page_count = 1;

        // get notebar (thread header that mods put there sometimes)
        String notebar = null;
        try {
            notebar = doc.select(".notebar").get(0).html();
        } catch (IndexOutOfBoundsException e) {
            // no notebar
        }

        Elements replies = doc.select("#replylist > div");

        for (Element reply : replies) {
            String id;
            String userId;
            String userName;
            String posted_time = "";
            String content;
            boolean edited = false;
            boolean op = false;
            boolean deleted = false;

            // get reply ID
            id = reply.attr("id").replace("r", "");

            // get author name
            Element userNameElement;
            try {
                userNameElement = reply.select(".bu_name").get(0);
            } catch (IndexOutOfBoundsException e) {
                // username not found, probably a deleted scrapedPost
                userNameElement = reply.select(".replyuser > a > b").get(0);
            }
            userName = userNameElement.text();

            // get author ID
            Element userIdElement = reply.select(".userid").get(0);
            userId = userIdElement.text().replace("User #", "");

            // check if this author is the OP
            Elements opElement = reply.select(".op");
            if (!opElement.isEmpty()) { // user is the OP
                op = true;
                opElement.get(0).remove(); // remove element so the text doesn't show up
            }

            // check if this scrapedPost has been edited
            Elements editedElements = reply.select(".edited");
            if (!editedElements.isEmpty()) {
                edited = true;

                // remove elements so text doesn't show up
                editedElements.forEach(Node::remove);
            }

            // get the poster's user group
            String userGroup = "";
            try {
                userGroup = reply.select(".usergroup").get(0).text();
            } catch (IndexOutOfBoundsException e) {
                // no usergroup, probably a deleted scrapedPost
            }

            //get shortlink
            String shortLink = "";
            try {
                shortLink = reply.select(".shortcode").get(0).text();
            } catch (IndexOutOfBoundsException e) {
                // no shortcode
            }

            // get posted time
            try {
                Element dateElement = reply.select(".date").get(0);
                posted_time = dateElement.ownText();
            } catch (IndexOutOfBoundsException e) {
                // no scrapedPost date, probably a deleted scrapedPost
                deleted = true;
            }

            // get the reply content
            Element contentElement = reply.select(".replytext").get(0);
            content = contentElement.html();

            User user = new User(userId, userName);
            user.setGroup(userGroup);

            ScrapedPost scrapedPost = new ScrapedPost(id, user, posted_time, content, edited, op);

            scrapedPost.setDeleted(deleted);
            scrapedPost.setShortCode(shortLink);
            scrapedPosts.add(scrapedPost);
        }

        ScrapedPostList scrapedPostList = new ScrapedPostList(threadId, threadTitle);
        scrapedPostList.setPageCount(page_count);
        scrapedPostList.setNotebar(notebar);
        scrapedPostList.setScrapedPosts(scrapedPosts);

        return scrapedPostList;
    }

    private Document downloadPage(String url) throws IOException {
        return Jsoup.connect(url).get();
    }

    @Override
    public Observable<ArrayList<ScrapedThread>> ScrapPopularThreadsObservable() {
        return Observable.create(subscriber -> {
            ArrayList<ScrapedThread> scrapedThreads = null;
            try {
                scrapedThreads = downloadPopularThreads();
            } catch (IOException e) {
                e.printStackTrace();
                subscriber.onError(e);
            }
            subscriber.onNext(scrapedThreads);
            subscriber.onCompleted();
        });
    }

    private ArrayList<ScrapedThread> downloadPopularThreads() throws IOException {
        ArrayList<ScrapedThread> threads = new ArrayList<>();

        Document doc = downloadPage(POPULAR_URL);
        Elements trs = doc.select("tr");

        String current_forum = null;
        int current_forum_id = 0;

        for (Element tr : trs) {
            Set<String> tr_classes = tr.classNames();

            // section - contains a forum name
            if (tr_classes.contains("section")) {
                current_forum = tr.text();

                // get the forum ID
                String forum_url = tr.select("a").attr("href");
                Pattern forum_id_regex = Pattern.compile("/forum/([0-9]+)");
                Matcher m = forum_id_regex.matcher(forum_url);
                while (m.find()) {
                    current_forum_id = Integer.parseInt(m.group(1));
                }
            }
            // thread
            else {
                if (current_forum == null) continue;

                ScrapedThread t = getThreadFromTableRow(tr, current_forum, current_forum_id);
                threads.add(t);
            }
        }

        return threads;
    }

    @Override
    public Observable<List<ScrapedThread>> searchThreadsObservable(int forumId, int groupId, String query) {
        return Observable.create(subscriber -> {
            try {
                final List<ScrapedThread> scrapedThreads = searchThreads(forumId, groupId, query);
                subscriber.onNext(scrapedThreads);
                subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }

    private List<ScrapedThread> searchThreads(int forum_id, int group_id, String query) throws URISyntaxException, IOException {
        String search_url = buildSearchUrl(forum_id, group_id, query);

        if (search_url == null) {
            return null;
        }

        List<ScrapedThread> threads = new ArrayList<>();

        Document doc = downloadPage(search_url);
        Elements trs = doc.select("tr");

        String current_forum = null;
        int current_forum_id = 0;

        for (Element tr : trs) {
            Set<String> tr_classes = tr.classNames();

            // section - contains a forum name
            if (tr_classes.contains("section")) {
                current_forum = tr.text();

                // get the forum ID
                String forum_url = tr.select("a").attr("href");
                Pattern forum_id_regex = Pattern.compile("/forum/([0-9]+)");
                Matcher m = forum_id_regex.matcher(forum_url);
                while (m.find()) {
                    current_forum_id = Integer.parseInt(m.group(1));
                }
            }
            // thread
            else {
                if (current_forum == null) continue;

                ScrapedThread t = getThreadFromTableRow(tr, current_forum, current_forum_id);
                threads.add(t);
            }
        }

        return threads;
    }

}
