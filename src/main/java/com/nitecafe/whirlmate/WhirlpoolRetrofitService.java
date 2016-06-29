package com.nitecafe.whirlmate;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.nitecafe.whirlmate.Constants.WHIRLPOOL_API_URL;

public class WhirlpoolRetrofitService {

    private final IWhirlpoolService whirlpoolService;
    private ThreadScraper scraper;

    public WhirlpoolRetrofitService(final String apiKey, final String agentName) {
        Interceptor interceptor = createInterceptor(apiKey, agentName);
        OkHttpClient client = createOkHttpClient(interceptor);
        Retrofit retrofit = createRetrofit(client);
        whirlpoolService = retrofit.create(IWhirlpoolService.class);
    }

    public IWhirlpoolService getWhirlpoolService() {
        return whirlpoolService;
    }

    public IThreadScraper getWhirlpoolScraper() {
        if (scraper == null)
            scraper = new ThreadScraper();

        return scraper;
    }

    private Retrofit createRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl(WHIRLPOOL_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client)
                .build();
    }

    private OkHttpClient createOkHttpClient(Interceptor interceptor) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.interceptors().add(interceptor);
        return builder.build();
    }

    private Interceptor createInterceptor(final String apiKey, final String agentName) {
        return chain -> {
            final HttpUrl url = chain.request().url().newBuilder()
                    .addQueryParameter("key", apiKey)
                    .addQueryParameter("output", "json")
                    .build();

            Request newRequest = chain.request().newBuilder().url(url)
                    .addHeader("User-Agent", agentName)
                    .build();
            return chain.proceed(newRequest);
        };
    }
}
