package com.nitecafe.whirlmate;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

public class WhirlpoolRetrofitService implements IWhirlpoolRetrofitService {

    private static final String WHIRLPOOL_API_URL = "https://whirlpool.net.au/api/";
    private final IWhirlpoolService whirlpoolService;

    public WhirlpoolRetrofitService(final String apiKey, final String agentName) {
        Interceptor interceptor = createInterceptor(apiKey, agentName);
        OkHttpClient client = createOkHttpClient(interceptor);
        Retrofit retrofit = createRetrofit(client);
        whirlpoolService = retrofit.create(IWhirlpoolService.class);
    }

    public IWhirlpoolService getWhirlpoolService() {
        return whirlpoolService;
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
        return new Interceptor() {
            public okhttp3.Response intercept(Chain chain) throws IOException {
                final HttpUrl url = chain.request().url().newBuilder()
                        .addQueryParameter("key", apiKey)
                        .addQueryParameter("output", "json")
                        .build();

                Request newRequest = chain.request().newBuilder().url(url)
                        .addHeader("User-Agent", agentName)
                        .build();
                return chain.proceed(newRequest);
            }
        };
    }
}
