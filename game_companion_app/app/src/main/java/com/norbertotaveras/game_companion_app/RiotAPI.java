package com.norbertotaveras.game_companion_app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.norbertotaveras.game_companion_app.DTO.StaticData.ChampionDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.ChampionListDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.ProfileIconDataDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.RealmDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.SummonerSpellDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.SummonerSpellListDTO;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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
    private static final String riotApiKey = "RGAPI-8cef106f-6455-44cb-abec-3971fa917389";
    private static final String rootEndpoint = "https://na1.api.riotgames.com/";
    private static final String staticCdn = "http://ddragon.leagueoflegends.com/cdn";

    private Retrofit retrofit;
    private RiotGamesService apiService;
    private OkHttpClient client;

    private DeferredRequest<ProfileIconDataDTO> deferredProfileIconData;
    private DeferredRequest<RealmDTO> deferredRealm;
    private DeferredRequest<SummonerSpellListDTO> deferredSpellList;
    private DeferredRequest<ChampionListDTO> deferredChampionList;

    public final RateLimiter rateLimiter;

    private Context context;

    final HashMap<String, DeferredRequest<Drawable>> drawableCache;
    final Object drawableCacheLock;

    public static RiotGamesService getInstance(Context context) {
        if (instance == null) {
            instance = new RiotAPI(context);
            instance.initRiotApi(riotApiKey);
        }
        return instance.apiService;
    }

    RiotAPI(Context context) {
        rateLimiter = new RateLimiter(20, 120, 100);
        drawableCache = new HashMap<>();
        drawableCacheLock = new Object();
        this.context = context;
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

                for (int retries = 0; ; ) {
                    HttpUrl url = originalHttpUrl.newBuilder()
                            .addQueryParameter("api_key", riotApiKey)
                            .build();

                    // Request customization: add request headers
                    Request.Builder requestBuilder = original.newBuilder()
                            .url(url);

                    Request request = requestBuilder.build();
                    try {
                        Response response = chain.proceed(request);
                        return response;
                    } catch (EOFException ex) {
                        if (retries++ < 3) {
                            Log.e("RiotAPI", "Unexpected EOF! Retry " +
                                    String.valueOf(retries));
                            continue;
                        }

                        Log.e("RiotAPI", "Unexpected EOF! Retry not working, giving up");

                        throw ex;
                    }
                }
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

    public static void drawableFromUrl(final String url,
                                       final AsyncCallback<Drawable> callback)
    {
        synchronized (instance.drawableCacheLock) {
            DeferredRequest<Drawable> request = instance.drawableCache.get(url);

            if (request == null) {
                request = cachedDrawableFileRead(url);

                if (request == null) {
                    request = new DeferredDrawable(fetchUrlCall(url));
                    request.getData(new AsyncCallback<Drawable>() {
                        @Override
                        public void invoke(Drawable item) {
                            if (item != null)
                                cachedDrawableFileWrite(url, item);
                        }
                    });
                }

                instance.drawableCache.put(url, request);
            }

            request.getData(callback);
        }
    }

    private static String filenameFromUrl(String url) {
        final int snip = url.indexOf('?');
        if (snip >= 0)
            url = url.substring(0, snip);

        final int urlLength = staticCdn.length();

        if (urlLength > staticCdn.length())
            return null;

        final String prefix = url.substring(0, staticCdn.length());
        if (!prefix.equals(staticCdn))
            return null;

        final String suffix = url.substring(staticCdn.length());

        if (suffix.length() < 1)
            return null;

        final String result = suffix.replaceAll("[^0-9A-Za-z]", "_");

        return "cached_drawable_" + result;
    }

    @Nullable
    private static DeferredRequest<Drawable> cachedDrawableFileRead(String url) {
        String filename = filenameFromUrl(url);
        if (filename == null)
            return null;

        FileInputStream file;
        try {
            file = instance.context.openFileInput(filename);
        } catch (FileNotFoundException ex) {
            return null;
        }

        Log.v("RiotAPI", "Opened cached URL file");

        Drawable drawable = null;
        try {
            drawable = Drawable.createFromStream(file, "");
        } catch (Exception ex) {
            // Decoding the file failed, get rid of it
            try {
                file.close();
            } catch (Exception ex2) {
                // Don't care, deleting it
            }
            instance.context.deleteFile(filename);
            drawable = null;
        }

        try {
            file.close();
        } catch (Exception ex) {
            // What could have possibly happened here? We only read it!
        }

        if (drawable == null) {
            Log.e("RiotAPI", "Unable to decode cached URL file!");
            return null;
        }

        Log.v("RiotAPI", "Loaded cached URL file");

        Deferred<Drawable> request = new Deferred<>();
        request.setResult(drawable);
        return request;
    }

    private static boolean cachedDrawableFileWrite(String url, Drawable item) {
        String filename = filenameFromUrl(url);

        if (filename == null)
            return false;

        FileOutputStream file;
        try {
            file = instance.context.openFileOutput(filename, 0);
        } catch (FileNotFoundException ex) {
            Log.e("RiotAPI", "Could not create cached url file");
            return false;
        }

        Log.v("RiotAPI", "Created cached URL file");

        try {
            BitmapDrawable bmp = (BitmapDrawable) item;
            if (!bmp.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, file))
                throw new Exception("Compression failed");

            file.close();
        } catch (Exception ex) {
            Log.e("RiotAPI", "Failed to encode bitmap file: " + ex.getMessage());
            try {
                file.close();
            } catch (Exception ex2) {
                // Don't care, deleting it anyway
            }
            instance.context.deleteFile(filename);
            return false;
        }

        Log.v("RiotAPI", "Write cached URL file");

        return true;
    }

    public static void fetchProfileIcon(final long id, final AsyncCallback<Drawable> callback) {
        instance.getProfileIconData(new AsyncCallback<ProfileIconDataDTO>() {
            @Override
            public void invoke(ProfileIconDataDTO iconData) {
            String url = String.format(
                    "%s/%s/img/profileicon/%d.png",
                    staticCdn, iconData.version, id);
            drawableFromUrl(url, callback);
            }
        });
    }

    public static String beautifyQueueName(String queueName) {
        queueName = transformQueueName(queueName);
        queueName = titleCaseFromUnderscores(queueName);
        queueName = queueName.replaceFirst("(\\d+)x(\\d+)$", "$1:$2");
        return queueName;
    }

    public static String transformQueueName(String queueName) {
        queueName = queueName.replaceFirst("_SR$", "_5x5");
        queueName = queueName.replaceFirst("_TT$", "_3x3");
        return queueName;
    }

    public static String beautifyTierName(String tierName) {
        return titleCaseFromUnderscores(tierName);
    }

    public static String titleCaseFromUnderscores(String input) {
        String[] parts = input.split("_");
        StringBuilder sb = new StringBuilder(input.length() * 2);

        for (int i = 0; i < parts.length; ++i) {
            sb.append(parts[i].substring(0, 1).toUpperCase());
            sb.append(parts[i].substring(1).toLowerCase());
            if (i + 1 < parts.length)
                sb.append(' ');
        }

        return sb.toString();
    }

    public static int tierNameToResourceId(String tierName) {
        switch (tierName) {
            case "SILVER": return R.drawable.silver;
            case "CHALLENGER": return R.drawable.challenger;
            case "DIAMOND": return R.drawable.diamond;
            case "GOLD": return R.drawable.gold;
            case "MASTER": return R.drawable.master;
            case "PLATINUM": return R.drawable.platinum;
            case "PROVISIONAL": return R.drawable.provisional;
            case "BRONZE": return R.drawable.bronze;
            default: return android.R.color.transparent;
        }
    }


    private void initialRequests() {
        final Gson gson = new Gson();

        deferredProfileIconData = new StaticDataCache<ProfileIconDataDTO>().tryCache(
                context, gson, "riot_icondata",
                new StaticCacheFetcher<ProfileIconDataDTO>() {
                    @Override
                    public DeferredRequest<ProfileIconDataDTO> fetch() {
                        return new RiotAPI.DeferredRequest<>(apiService.getProfileIcons());
                    }
                }, new TypeToken<ProfileIconDataDTO>(){}.getType());

        deferredRealm = new StaticDataCache<RealmDTO>().tryCache(
                context, gson, "riot_realmdata",
                new StaticCacheFetcher<RealmDTO>() {
                    @Override
                    public DeferredRequest<RealmDTO> fetch() {
                        return new RiotAPI.DeferredRequest<>(apiService.getRealms());
                    }
                }, new TypeToken<RealmDTO>(){}.getType());

        deferredSpellList = new StaticDataCache<SummonerSpellListDTO>().tryCache(
                context, gson, "riot_spelldata",
                new StaticCacheFetcher<SummonerSpellListDTO>() {
                    @Override
                    public DeferredRequest<SummonerSpellListDTO> fetch() {
                        return new RiotAPI.DeferredRequest<>(
                                apiService.getSummonerSpellList("image"));
                    }
                }, new TypeToken<SummonerSpellListDTO>(){}.getType());

        deferredChampionList = new StaticDataCache<ChampionListDTO>().tryCache(
                context, gson, "riot_championdata",
                new StaticCacheFetcher<ChampionListDTO>() {
                    @Override
                    public DeferredRequest<ChampionListDTO> fetch() {
                        return new RiotAPI.DeferredRequest<>(apiService.getChampionList());
                    }
                }, new TypeToken<ChampionListDTO>(){}.getType());
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

    public static void fetchChampionIcon(
            final long championId, final AsyncCallback<Drawable> callback) {
        Log.v("RiotAPI", "Fetching champion icon, id=" + String.valueOf(championId));

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

                String url = null;
                try {
                    String championFilename = playerChampion.name.replace(" ", "");
                    url = String.format("%s/%s/img/champion/%s.png", staticCdn, list.version,
                            URLEncoder.encode(championFilename, "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    // Will never happen in a million years
                }

                drawableFromUrl(url, callback);
            }
        });
    }

    public static void fetchSpellIcon(
            final long spellId, final AsyncCallback<Drawable> callback) {
        Log.v("RiotAPI", "Fetching spell icon, id=" + String.valueOf(spellId));

        getSpellList(new RiotAPI.AsyncCallback<SummonerSpellListDTO>() {
            @Override
            public void invoke(SummonerSpellListDTO list) {
                SummonerSpellDTO spellMatch = null;
                for (Map.Entry<String, SummonerSpellDTO> spell : list.data.entrySet()) {
                    if (spell.getValue().id == spellId) {
                        spellMatch = spell.getValue();
                        break;
                    }
                }

                // Sanity check
                if (spellMatch == null)
                    return;

                String url = String.format("%s/%s/img/spell/%s.png",
                        staticCdn, list.version, spellMatch.key);

                drawableFromUrl(url, callback);
            }
        });
    }

    public static void fetchRuneIcon(
            final long runeId, final AsyncCallback<Drawable> callback) {
        Log.v("RiotAPI", "Fetching rune icon, id=" + String.valueOf(runeId));

        // Reusing spell list just for the version
        getSpellList(new AsyncCallback<SummonerSpellListDTO>() {
            @Override
            public void invoke(SummonerSpellListDTO list) {
                String url = String.format("%s/%s/img/rune/%s.png",
                        staticCdn, list.version, runeId);

                drawableFromUrl(url, callback);
            }
        });
    }

    public static void fetchItemIcon(
            final long itemId, final AsyncCallback<Drawable> callback) {
        Log.v("RiotAPI", "Fetching item icon, id=" + String.valueOf(itemId));

        // Reusing spell list just for the version
        getSpellList(new AsyncCallback<SummonerSpellListDTO>() {
            @Override
            public void invoke(SummonerSpellListDTO list) {
                String url = String.format("%s/%s/img/item/%s.png",
                        staticCdn, list.version, itemId);

                drawableFromUrl(url, callback);
            }
        });
    }

    public static <T> void rateLimitRequest(Call<T> call, Callback<T> callback) {
        instance.rateLimiter.request(call, callback, 0);
    }

    private static RequestCache requestCache = new RequestCache(64);

    public static <T> void cachedRequest(Call<T> call, AsyncCallback<T> callback) {
        requestCache.request(call, callback);
    }

    public static class RequestCache {
        private static Object syncLock = new Object();
        private static LinkedHashMap<String, DeferredRequest<?>> cache;

        public RequestCache(final int maxEntries) {
            cache = new LinkedHashMap<String, DeferredRequest<?>>() {
                @Override
                protected boolean removeEldestEntry(Entry eldest) {
                    return size() >= maxEntries;
                }
            };
        }

        public static <T> void request(Call<T> call, AsyncCallback<T> callback) {
            synchronized (syncLock) {
                String url = call.request().url().toString();
                Log.v("RequestCache", "Looking up " + url);
                if (!requestImpl(cache.get(url), callback)) {
                    Log.v("RequestCache", "Cache miss");
                    cacheMiss(url, call, callback);
                } else {
                    Log.v("RequestCache", "Cache hit");
                }
            }
        }

        private static <T, U> boolean requestImpl(DeferredRequest<T> entry,
                                                     AsyncCallback<U> callback) {
            if (entry != null) {
                entry.getData((AsyncCallback<T>)callback);
                return true;
            }
            return false;
        }

        private static <T> void cacheMiss(String url, Call<T> call, final AsyncCallback<?> callback) {
            DeferredRequest<T> request = new DeferredRequest<>(call);
            cache.put(url, request);
            request.getData((AsyncCallback<T>)callback);
        }
    }

    public static class DeferredRequest<T> {
        private final Object pendingRequestLock;
        private ArrayList<AsyncCallback<T>> pendingRequests;
        private T data = null;

        // Deferred request with REST wrapper
        public DeferredRequest(Call<T> call) {
            Log.v("RiotAPI","Creating rate limited request, url=" +
                    call.request().url().toString());

            pendingRequestLock = new Object();
            pendingRequests = new ArrayList<>();

            instance.rateLimiter.request(call, new Callback<T>() {
                @Override
                public void onResponse(Call<T> call, retrofit2.Response<T> response) {
                    Log.v("RiotAPI","Got rate limiter response, url=" +
                            call.request().url().toString());
                    handleResponse(response.body());
                }

                @Override
                public void onFailure(Call<T> call, Throwable t) {
                    Log.e("DeferredRequest", "Request failed! " + call.toString());
                }
            }, 0);
        }

        // Deferred raw HTTP request
        public DeferredRequest(okhttp3.Call call) {
            pendingRequestLock = new Object();
            pendingRequests = new ArrayList<>();

            call.enqueue(new okhttp3.Callback() {
                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    handleResponse(transformResponse(response));
                }

                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    Log.e("DeferredRequest", "Request failed! " + call.toString());
                }
            });
        }

        private DeferredRequest() {
            pendingRequestLock = new Object();
            pendingRequests = new ArrayList<>();
        }

        public void getData(AsyncCallback<T> callback) {
            synchronized (pendingRequestLock) {
                if (pendingRequests != null) {
                    pendingRequests.add(callback);
                    return;
                }
            }

            // Immediately invoke callback outside the lock
            callback.invoke(data);
        }

        protected T transformResponse(Response response) {
            throw new UnsupportedOperationException();
        }

        protected void handleResponse(T response) {
            ArrayList<AsyncCallback<T>> requests;

            synchronized (pendingRequestLock) {
                // It doesn't make sense for this to be called when data is not null
                assert(data == null);

                // Store the data for future requests of the same thing
                data = response;

                // Grab the list of callbacks and replace it with null,
                // it won't be needed anymore
                requests = pendingRequests;
                pendingRequests = null;
            }

            // Invoke queued callbacks outside the lock
            for (AsyncCallback<T> callback : requests)
                callback.invoke(data);
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

    public static class Deferred<T>
        extends DeferredRequest<T>
    {
        public void setResult(T result) {
            handleResponse(result);
        }
    }

    public interface AsyncCallback<T> {
        void invoke(T item);
    }
}
