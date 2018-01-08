package com.norbertotaveras.game_companion_app;

import android.graphics.drawable.Drawable;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.util.Log;

import com.norbertotaveras.game_companion_app.DTO.StaticData.ChampionDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.ChampionListDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.ProfileIconDataDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.ProfileIconDetailsDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.RealmDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.SummonerSpellListDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
            "RGAPI-feb20962-a035-43f1-99b7-53f7bfc85e77";
    private static final String rootEndpoint =
            "https://na1.api.riotgames.com/";

    private Retrofit retrofit;
    private RiotGamesService apiService;
    private OkHttpClient client;

    private DeferredRequest<ProfileIconDataDTO> deferredProfileIconData;
    private DeferredRequest<RealmDTO> deferredRealm;
    private DeferredRequest<SummonerSpellListDTO> deferredSpellList;
    private DeferredRequest<ChampionListDTO> deferredChampionList;

    private final Timer rateLimitTimer;
    private final ArrayList<Long> rateLimitHistory;
    private final int rateLimitPerSecond;
    private final int rateLimitLongTermSeconds;
    private final int rateLimitLongTermLimit;
    private final Object rateLimitLock;

    final HashMap<String, DeferredDrawable> drawableCache;
    final Object drawableCacheLock;

    public static RiotGamesService getInstance() {
        if (instance == null) {
            instance = new RiotAPI();
            instance.initRiotApi(riotApiKey);
        }
        return instance.apiService;
    }

    RiotAPI() {
        rateLimitTimer = new Timer();
        rateLimitHistory = new ArrayList<>();
        // 20 requests every 1 seconds
        // 100 requests every 2 minutes
        rateLimitPerSecond = 19;
        rateLimitLongTermSeconds = 121;
        rateLimitLongTermLimit = 99;
        rateLimitLock = new Object();

        drawableCache = new HashMap<>();
        drawableCacheLock = new Object();
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

    private static okhttp3.Call fetchUrlCall(String url) {
        Request request = new Request.Builder().url(url).build();
        return instance.client.newCall(request);
    }

    public static void fetchBinaryUrl(String url, final okhttp3.Callback callback) {
        okhttp3.Call call = fetchUrlCall(url);
        call.enqueue(callback);
    }

    public static void drawableFromUrl(String url, final AsyncCallback<Drawable> callback) {
        synchronized (instance.drawableCacheLock) {
            DeferredDrawable request = instance.drawableCache.get(url);

            if (request == null)
                request = new DeferredDrawable(fetchUrlCall(url));

            instance.drawableCache.put(url, request);
            request.getData(callback);
        }

        fetchBinaryUrl(url, new okhttp3.Callback() {
            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                Drawable drawable = Drawable.createFromStream(
                        response.body().byteStream(), null);
                callback.invoke(drawable);
            }

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                callback.invoke(null);
            }
        });
    }

    public static void fetchProfileIcon(final long id, final AsyncCallback<Drawable> callback) {
        instance.getProfileIconData(new AsyncCallback<ProfileIconDataDTO>() {
            @Override
            public void invoke(ProfileIconDataDTO iconData) {
            String url = String.format(
                    "http://ddragon.leagueoflegends.com/cdn/%s/img/profileicon/%d.png",
                    iconData.version, id);
            drawableFromUrl(url, callback);
            }
        });
    }

    private void initialRequests() {
        deferredProfileIconData = new DeferredRequest<>(apiService.getProfileIcons());
        deferredRealm = new DeferredRequest<>(apiService.getRealms());
        deferredSpellList = new DeferredRequest<>(apiService.getSummonerSpellList("image"));
        deferredChampionList = new DeferredRequest<>(apiService.getChampionList());
    }

    public void getRealms(AsyncCallback<RealmDTO> callback) {
        deferredRealm.getData(callback);
    }

    public static void getProfileIconData(AsyncCallback<ProfileIconDataDTO> callback) {
        instance.deferredProfileIconData.getData(callback);
    }

    public static void getSpellList(AsyncCallback<SummonerSpellListDTO> callback) {
        instance.deferredSpellList.getData(callback);
    }

    public static void getChampionList(AsyncCallback<ChampionListDTO> callback) {
        instance.deferredChampionList.getData(callback);
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

        final Callback<T> wrapper = new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, retrofit2.Response<T> response) {
                // We hit rate limit unexpectedly, reschedule
                if (response.code() == 429) {
                    Log.w("RateLimiter", "Unexpected 429 error, rescheduling...");
                    rateLimitRequest(call.clone(), callback);
                } else {
                    callback.onResponse(call, response);
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                callback.onFailure(call, t);
            }
        };

        if (delay <= 0) {
            Log.v("RateLimiter", "running request immediately");
            rateLimitHistory.add(now);
            //rateLimitTimer.schedule(new RateLimitedTask<>(call, wrapper), 1);
            call.enqueue(wrapper);
        } else {
            Log.v("RateLimiter", "running request after " + String.valueOf(delay) + "ms");
            rateLimitHistory.add(now + delay);
            rateLimitTimer.schedule(new RateLimitedTask<>(call, wrapper), delay);
        }
    }

    public static void fetchChampionIcon(
            final long championId, final AsyncCallback<Drawable> callback) {
        getChampionList(new RiotAPI.AsyncCallback<ChampionListDTO>() {
            @Override
            public void invoke(ChampionListDTO list) {
                ChampionDTO playerChampion = null;
                for (Map.Entry<String, ChampionDTO> champion : list.data.entrySet()) {
                    if (champion.getValue().id == championId) {
                        playerChampion = champion.getValue();
                        break;
                    }
                }

                // Sanity check
                if (playerChampion == null)
                    return;

                String url = String.format("http://ddragon.leagueoflegends.com/" +
                        "cdn/%s/img/champion/%s.png", list.version, playerChampion.name);

                drawableFromUrl(url, callback);
            }
        });
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

    public static class DeferredRequest<T> {
        private Object pendingRequestLock;
        private ArrayList<AsyncCallback<T>> pendingRequests;
        private T data;

        public DeferredRequest(Call<T> call) {
            pendingRequestLock = new Object();
            pendingRequests = new ArrayList<>();

            RiotAPI.rateLimitRequest(call, new Callback<T>() {
                @Override
                public void onResponse(Call<T> call, retrofit2.Response<T> response) {
                    handleResponse(response.body());
                }

                @Override
                public void onFailure(Call<T> call, Throwable t) {

                }
            });
        }

        public DeferredRequest(okhttp3.Call call) {
            pendingRequestLock = new Object();
            pendingRequests = new ArrayList<>();

            call.enqueue(new okhttp3.Callback() {
                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    transformResponse(response);
                }

                @Override
                public void onFailure(okhttp3.Call call, IOException e) {

                }
            });
        }

        public void getData(AsyncCallback<T> callback) {
            synchronized (pendingRequestLock) {
                if (data != null)
                    callback.invoke(data);
                else
                    pendingRequests.add(callback);
            }
        }

        protected T transformResponse(Response response) {
            throw new UnsupportedOperationException();
        }

        private void handleResponse(T response) {
            synchronized (pendingRequestLock) {
                data = response;
                for (AsyncCallback<T> callback : pendingRequests)
                    callback.invoke(data);
                pendingRequests.clear();
            }
        }
    }

    public static class DeferredDrawable extends DeferredRequest<Drawable> {
        DeferredDrawable(okhttp3.Call call) {
            super(call);
        }

        @Override
        protected Drawable transformResponse(Response response) {
            return Drawable.createFromStream(response.body().byteStream(), null);
        }
    }

    public interface AsyncCallback<T> {
        void invoke(T item);
    }
}
