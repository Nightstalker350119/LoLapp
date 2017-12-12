package com.norbertotaveras.game_companion_app;

import android.provider.Telephony;
import android.util.Log;

import com.norbertotaveras.game_companion_app.DTO.StaticData.ProfileIconDataDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.ProfileIconDetailsDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.RealmDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Norberto Taveras on 11/15/2017.
 */

public class RiotAPI {
    private static RiotAPI instance;
    private static final String riotApiKey =
            "RGAPI-468cf58b-0219-4946-a7bb-755c9385cfab";
    private static final String rootEndpoint =
            "https://na1.api.riotgames.com/";

    private Retrofit retrofit;
    private RiotGamesService apiService;
    private OkHttpClient client;

    private ProfileIconDataDTO profileIconData;
    private RealmDTO realmData;

    private final Object pendingProfileIconLock;
    private final ArrayList<AsyncCallback<ProfileIconDataDTO>> pendingProfileIconRequests;
    private final Object pendingRealmLock;
    private final ArrayList<AsyncCallback<RealmDTO>> pendingRealmRequests;

    private final Timer rateLimitTimer;
    long lastRateLimitRequest;
    ArrayList<Long> rateLimitHistory;
    int rateLimitPerSecond;
    int rateLimitLongTermSeconds;
    int rateLimitLongTermLimit;
    Object rateLimitLock;

    public static RiotGamesService getInstance() {
        if (instance == null) {
            instance = new RiotAPI();
            instance.initRiotApi(riotApiKey);
        }
        return instance.apiService;
    }

    RiotAPI() {
        pendingProfileIconLock = new Object();
        pendingRealmLock = new Object();
        pendingProfileIconRequests = new ArrayList<>();
        pendingRealmRequests = new ArrayList<>();
        rateLimitTimer = new Timer();
        lastRateLimitRequest = 0;
        rateLimitHistory = new ArrayList<>();
        // 20 requests every 1 seconds
        // 100 requests every 2 minutes
        rateLimitPerSecond = 20;
        rateLimitLongTermSeconds = 120;
        rateLimitLongTermLimit = 100;
        rateLimitLock = new Object();
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
                .baseUrl(rootEndpoint)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(RiotGamesService.class);

        initialRequests();
    }

    private void fetchBinaryUrl(String url, final okhttp3.Callback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public static void fetchProfileIcon(final long id, final okhttp3.Callback callback) {
        instance.getProfileIconData(new AsyncCallback<ProfileIconDataDTO>() {
            @Override
            public void invoke(ProfileIconDataDTO iconData) {
                String url = String.format(
                        "http://ddragon.leagueoflegends.com/cdn/%s/img/profileicon/%d.png",
                        iconData.version, id);
                instance.fetchBinaryUrl(url, callback);
            }
        });
    }

    private void initialRequests() {
        Call<ProfileIconDataDTO> getProfileIconsRequest = apiService.getProfileIcons();

        RiotAPI.rateLimitRequest(getProfileIconsRequest, new Callback<ProfileIconDataDTO>() {
            @Override
            public void onResponse(Call<ProfileIconDataDTO> call,
                                   retrofit2.Response<ProfileIconDataDTO> response) {
                handleGetProfileIconData(response.body());
            }

            @Override
            public void onFailure(Call<ProfileIconDataDTO> call, Throwable t) {

            }
        });

        Call<RealmDTO> getRealmsRequest = apiService.getRealms();

        RiotAPI.rateLimitRequest(getRealmsRequest, new Callback<RealmDTO>() {
            @Override
            public void onResponse(Call<RealmDTO> call, retrofit2.Response<RealmDTO> response) {
                handleGetRealmsRequest(response.body());
            }

            @Override
            public void onFailure(Call<RealmDTO> call, Throwable t) {

            }
        });
    }

    private void handleGetRealmsRequest(RealmDTO realmDTO) {
        synchronized (pendingRealmLock) {
            realmData = realmDTO;
            for (AsyncCallback<RealmDTO> callback : pendingRealmRequests)
                callback.invoke(realmData);
            pendingRealmRequests.clear();
        }
    }

    public void getRealmData(AsyncCallback<RealmDTO> callback) {
        synchronized (pendingRealmLock) {
            if (realmData != null)
                callback.invoke(realmData);
            else
                pendingRealmRequests.add(callback);
        }
    }

    public void getProfileIconData(AsyncCallback<ProfileIconDataDTO> callback) {
        synchronized (pendingProfileIconLock) {
            if (profileIconData != null)
                callback.invoke(profileIconData);
            else
                pendingProfileIconRequests.add(callback);
        }
    }

    private void handleGetProfileIconData(ProfileIconDataDTO iconDataDto) {
        synchronized (pendingProfileIconLock) {
            profileIconData = iconDataDto;
            for (AsyncCallback<ProfileIconDataDTO> callback : pendingProfileIconRequests)
                callback.invoke(profileIconData);
            pendingProfileIconRequests.clear();
        }
    }

    // Returns how many milliseconds to wait before issuing another request
    // Returns 0 when we should issue a new request immediately
    private long rateLimit(long now) {
        // Calculate the timestamp which is so old we need to throw it away
        long expired = now - rateLimitLongTermSeconds * 1000;

        // Discard entries further back than long term limit
        int i, e;
        for (i = 0, e = rateLimitHistory.size();
             i < e && rateLimitHistory.get(i) < expired; ++i);
        rateLimitHistory.subList(0, i).clear();

        // Nothing in history, start request immediately
        if (rateLimitHistory.isEmpty())
            return 0;

        long oldestEntry = rateLimitHistory.get(0);

        // If we are at the long term limit...
        if (rateLimitHistory.size() >= rateLimitLongTermLimit) {
            // ...calculate how long to wait based on oldest history entry
            return oldestEntry + rateLimitLongTermLimit * 1000 - now;
        }

        // If there aren't enough history entries to hit the short term limit,
        // then start the request immediately
        if (rateLimitHistory.size() < rateLimitPerSecond)
            return 0;

        return rateLimitHistory.get(rateLimitHistory.size() - rateLimitPerSecond) + 1000 - now;
    }

    public static <T> void rateLimitRequest(final Call<T> call, final Callback<T> callback) {
        synchronized (instance.rateLimitLock) {
            instance.rateLimitRequestLocked(call, callback);
        }
    }

    private <T> void rateLimitRequestLocked(final Call<T> call, final Callback<T> callback) {
        long now = new Date().getTime();

        long delay = rateLimit(now);

        if (delay <= 0) {
            Log.v("RateLimiter", "running request immediately");
            rateLimitHistory.add(now);
            call.enqueue(callback);
        } else {
            Log.v("RateLimiter", "running request after " + String.valueOf(delay) + "ms");
            rateLimitHistory.add(now + delay);
            rateLimitTimer.schedule(new RateLimitedTask<>(call, callback), delay);
        }
    }

    private class RateLimitedTask<T> extends TimerTask {
        Call<T> call;
        Callback<T> callback;

        public RateLimitedTask(Call<T> call, Callback<T> callback) {
            this.call = call;
            this.callback = callback;
        }

        @Override
        public void run() {
            call.enqueue(callback);
        }
    }

    public interface AsyncCallback<T> {
        void invoke(T item);
    }
}
