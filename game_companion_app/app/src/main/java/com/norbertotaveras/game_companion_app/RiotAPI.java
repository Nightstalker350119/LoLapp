package com.norbertotaveras.game_companion_app;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Norberto Taveras on 11/15/2017.
 */

public class RiotAPI {
    private static RiotAPI instance;
    private static final String riotApiKey =
            "RGAPI-0d7654a4-d8b6-4be4-ac30-dda50f460c40";

    private Retrofit retrofit;
    private RiotGamesService apiService;
    private OkHttpClient client;

    public static RiotGamesService getInstance() {
        if (instance == null) {
            instance = new RiotAPI();
            instance.initRiotApi(riotApiKey);
        }
        return instance.apiService;
    }

    public static void releaseInstance() {
        instance = null;
    }

    private void initRiotApi(final String riotApiKey) {
        OkHttpClient.Builder httpClient =
                new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                HttpUrl originalHttpUrl = original.url();

                HttpUrl url = originalHttpUrl.newBuilder()
                        .addQueryParameter("api_key", riotApiKey)
                        .build();

                // Request customization: add request headers
                Request.Builder requestBuilder = original.newBuilder()
                        .url(url);

                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });

        client = httpClient.build();

        retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl("https://na1.api.riotgames.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(RiotGamesService.class);
    }

    private void fetchBinaryUrl(String url, final okhttp3.Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void fetchProfileIcon(String version, long id, okhttp3.Callback callback) {
        String url = String.format(
                "http://ddragon.leagueoflegends.com/cdn/%s/img/profileicon/%d.png",
                version, id);
        instance.fetchBinaryUrl(url, callback);
    }
}
