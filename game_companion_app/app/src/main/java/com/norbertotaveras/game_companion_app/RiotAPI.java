package com.norbertotaveras.game_companion_app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.LongSparseArray;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.norbertotaveras.game_companion_app.DTO.ChampionMastery.ChampionMasteryDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.MatchDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.ParticipantDTO;
import com.norbertotaveras.game_companion_app.DTO.Match.ParticipantIdentityDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.ChampionDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.ChampionListDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.ProfileIconDataDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.RealmDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.SkinDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.SummonerSpellDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.SummonerSpellListDTO;
import com.norbertotaveras.game_companion_app.DTO.Summoner.SummonerDTO;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
    private static final String riotApiKey = "RGAPI-fa48945f-1cf2-4f8f-88ce-a08fb212db4a";
    private static final String rootEndpoint = "https://na1.api.riotgames.com/";
    private static final String staticCdn = "http://ddragon.leagueoflegends.com/cdn";

    private static FirebaseAuth firebaseAuth;
    private static FirebaseDatabase firebase;
    private static DatabaseReference firebaseDB;

    private static LocalDBCache localDBCache;

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

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            firebase = FirebaseDatabase.getInstance();
            firebaseDB = firebase.getReference();
        }

        localDBCache = LocalDBCache.getInstance(context);

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

    //  for i in $(ls); do t=${i#*_}; t=${t%.*}; echo "case $t: return R.drawable.perk_$t;" ; done
    public static int perkIdToResourceId(long perkId) {
        if (perkId < Integer.MIN_VALUE)
            perkId = 0;
        if (perkId > Integer.MAX_VALUE)
            perkId = 0;
        switch ((int)perkId) {
            case 8005:
                return R.drawable.perk_8005;
            case 8008:
                return R.drawable.perk_8008;
            case 8009:
                return R.drawable.perk_8009;
            case 8014:
                return R.drawable.perk_8014;
            case 8017:
                return R.drawable.perk_8017;
            case 8021:
                return R.drawable.perk_8021;
            case 8105:
                return R.drawable.perk_8105;
            case 8112:
                return R.drawable.perk_8112;
            case 8120:
                return R.drawable.perk_8120;
            case 8124:
                return R.drawable.perk_8124;
            case 8126:
                return R.drawable.perk_8126;
            case 8128:
                return R.drawable.perk_8128;
            case 8134:
                return R.drawable.perk_8134;
            case 8135:
                return R.drawable.perk_8135;
            case 8136:
                return R.drawable.perk_8136;
            case 8138:
                return R.drawable.perk_8138;
            case 8139:
                return R.drawable.perk_8139;
            case 8143:
                return R.drawable.perk_8143;
            case 8210:
                return R.drawable.perk_8210;
            case 8214:
                return R.drawable.perk_8214;
            case 8224:
                return R.drawable.perk_8224;
            case 8226:
                return R.drawable.perk_8226;
            case 8229:
                return R.drawable.perk_8229;
            case 8230:
                return R.drawable.perk_8230;
            case 8232:
                return R.drawable.perk_8232;
            case 8233:
                return R.drawable.perk_8233;
            case 8234:
                return R.drawable.perk_8234;
            case 8236:
                return R.drawable.perk_8236;
            case 8237:
                return R.drawable.perk_8237;
            case 8242:
                return R.drawable.perk_8242;
            case 8243:
                return R.drawable.perk_8243;
            case 8299:
                return R.drawable.perk_8299;
            case 8304:
                return R.drawable.perk_8304;
            case 8306:
                return R.drawable.perk_8306;
            case 8313:
                return R.drawable.perk_8313;
            case 8316:
                return R.drawable.perk_8316;
            case 8321:
                return R.drawable.perk_8321;
            case 8326:
                return R.drawable.perk_8326;
            case 8339:
                return R.drawable.perk_8339;
            case 8345:
                return R.drawable.perk_8345;
            case 8347:
                return R.drawable.perk_8347;
            case 8351:
                return R.drawable.perk_8351;
            case 8359:
                return R.drawable.perk_8359;
            case 8410:
                return R.drawable.perk_8410;
            case 8429:
                return R.drawable.perk_8429;
            case 8430:
                return R.drawable.perk_8430;
            case 8435:
                return R.drawable.perk_8435;
            case 8437:
                return R.drawable.perk_8437;
            case 8439:
                return R.drawable.perk_8439;
            case 8444:
                return R.drawable.perk_8444;
            case 8446:
                return R.drawable.perk_8446;
            case 8451:
                return R.drawable.perk_8451;
            case 8453:
                return R.drawable.perk_8453;
            case 8463:
                return R.drawable.perk_8463;
            case 8465:
                return R.drawable.perk_8465;
            case 9101:
                return R.drawable.perk_9101;
            case 9103:
                return R.drawable.perk_9103;
            case 9104:
                return R.drawable.perk_9104;
            case 9105:
                return R.drawable.perk_9105;
            case 9111:
                return R.drawable.perk_9111;
            default:
                return android.R.color.transparent;
        }
    }

    //  for i in $(ls); do t=${i#*perk_style_}; t=${t%.*}; echo "case $t: return R.drawable.perk_style_$t;" ; done
    public static int perkStyleIdToResourceId(long perkStyleId) {
        if (perkStyleId < Integer.MIN_VALUE)
            perkStyleId = 0;
        if (perkStyleId > Integer.MAX_VALUE)
            perkStyleId = 0;
        switch ((int)perkStyleId) {
            case 8000:
                return R.drawable.perk_style_8000;
            case 8100:
                return R.drawable.perk_style_8100;
            case 8200:
                return R.drawable.perk_style_8200;
            case 8300:
                return R.drawable.perk_style_8300;
            case 8400:
                return R.drawable.perk_style_8400;
            default:
                return android.R.color.transparent;
        }
    }

    public static int tierNameToResourceId(String tierName, String rank) {
        //switch (tierName + rank.toLowerCase()) {
        switch (tierName + "_" + rank) {
            // Challenger/Master/Provisional always have rank 1
            case "CHALLENGER_I": return R.drawable.challenger;
            case "MASTER_I": return R.drawable.master;
            case "PROVISIONAL_I": return R.drawable.provisional;

            case "SILVER_I": return R.drawable.silver_i;
            case "SILVER_II": return R.drawable.silver_ii;
            case "SILVER_III": return R.drawable.silver_iii;
            case "SILVER_IV": return R.drawable.silver_iv;
            case "SILVER_V": return R.drawable.silver_v;

            case "DIAMOND_I": return R.drawable.diamond_i;
            case "DIAMOND_II": return R.drawable.diamond_ii;
            case "DIAMOND_III": return R.drawable.diamond_iii;
            case "DIAMOND_IV": return R.drawable.diamond_iv;
            case "DIAMOND_V": return R.drawable.diamond_v;

            case "GOLD_I": return R.drawable.gold_i;
            case "GOLD_II": return R.drawable.gold_ii;
            case "GOLD_III": return R.drawable.gold_iii;
            case "GOLD_IV": return R.drawable.gold_iv;
            case "GOLD_V": return R.drawable.gold_v;

            case "PLATINUM_I": return R.drawable.platinum_i;
            case "PLATINUM_II": return R.drawable.platinum_ii;
            case "PLATINUM_III": return R.drawable.platinum_iii;
            case "PLATINUM_IV": return R.drawable.platinum_iv;
            case "PLATINUM_V": return R.drawable.platinum_v;

            case "BRONZE_I": return R.drawable.bronze_i;
            case "BRONZE_II": return R.drawable.bronze_ii;
            case "BRONZE_III": return R.drawable.bronze_iii;
            case "BRONZE_IV": return R.drawable.bronze_iv;
            case "BRONZE_V": return R.drawable.bronze_v;

            default: return android.R.color.transparent;
        }
    }
    
    public static int championLevelToResourceId(int level) {
        switch (level) {
            case 1: return R.drawable.mastery_icon_i;
            case 2: return R.drawable.mastery_icon_ii;
            case 3: return R.drawable.mastery_icon_iii;
            case 4: return R.drawable.mastery_icon_iv;
            case 5: return R.drawable.mastery_icon_v;
            case 6: return R.drawable.mastery_icon_vi;
            case 7: return R.drawable.mastery_icon_vii;
            default: return android.R.color.transparent;
        }
    }

    public static String formatMinSec(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / 3600);
        if (h == 0)
            return String.valueOf(m) + "m " + String.valueOf(s) + "s";
        return String.valueOf(h) + "h " + String.valueOf(m) + "m " + String.valueOf(s) + "s";
    }

    public static String formatKda(ParticipantDTO participant) {
        return String.format(Locale.US, "%d / %d / %d",
                participant.stats.kills, participant.stats.deaths,
                participant.stats.assists);
    }

    public static String formatKdaRatio(ParticipantDTO participant) {
        return formatKdaRatio(participant.stats.kills + participant.stats.assists,
                participant.stats.deaths);
    }

    public static String formatKdaRatio(long killsPlusAssists, long deaths) {
        String kdaText;

        if (deaths > 0) {
            long kdaRatioGcd = gcd(killsPlusAssists, deaths);
            final double numer = kdaRatioGcd != 0
                    ? (double)killsPlusAssists / kdaRatioGcd : killsPlusAssists;
            final double denom = kdaRatioGcd != 0
                    ? (double)deaths / kdaRatioGcd : deaths;

            kdaText = simpleDouble(numer) + ":" + simpleDouble(denom);
        } else {
            kdaText = "Perfect";
        }

        return kdaText;
    }

    public static String simpleDouble(double n) {
        if (n == Math.floor(n))
            return String.valueOf((int)n);
        return String.format(Locale.US, "%.2f", n);
    }

    // Find greatest common divisor using simple Euclid's algorithm
    public static long gcd(long a, long b)
    {
        if (a == 0 || b == 0)
            return 0;

        while (a != b) {
            if (a > b)
                a -= b;
            else
                b -= a;
        }

        return a;
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
                        return new RiotAPI.DeferredRequest<>(apiService.getChampionList(
                                "all", "all"));
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
                    String championFilename = playerChampion.key;
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
        RequestCache.request(call, callback);
    }

    private static void getNetworkMatch(final Long gameId, final String gameIdStr,
                                        final AsyncCallback<MatchDTO> callback) {
        Log.v("RiotFirebaseCache", "Cache miss for match id=" + gameIdStr);
        Call<MatchDTO> matchRequest = instance.apiService.getMatch(gameId);

        cachedRequest(matchRequest, new AsyncCallback<MatchDTO>() {
            @Override
            public void invoke(MatchDTO match) {
                // Make callback ASAP
                callback.invoke(match);

                // Insert it into Firebase
                if (firebaseAuth.getCurrentUser() != null) {
                    Map<String, Object> ins = new HashMap<>();
                    ins.put("/match/" + gameIdStr, match);
                    firebaseDB.updateChildren(ins);
                }

                // Insert it into local database
                localDBCache.insertObject("match", gameId, match, null);
            }
        });
    }

    private static void getFirebaseMatch(final Long gameId, final String gameIdStr,
                                         final AsyncCallback<MatchDTO> callback) {
        if (firebaseAuth.getCurrentUser() == null) {
            getNetworkMatch(gameId, gameIdStr, callback);
            return;
        }

        firebaseDB.child("match").child(gameIdStr).addListenerForSingleValueEvent(
                new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Cache hit
                    Log.v("RiotFirebaseCache", "Cache hit for match id=" + gameIdStr);
                    MatchDTO match = dataSnapshot.getValue(MatchDTO.class);

                    callback.invoke(match);

                    localDBCache.insertObject("match", gameId, match, null);
                } else {
                    getNetworkMatch(gameId, gameIdStr, callback);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                getNetworkMatch(gameId, gameIdStr, callback);
            }
        });
    }

    public static void getCachedMatch(final Long gameId, final AsyncCallback<MatchDTO> callback) {
        final String gameIdStr = String.valueOf(gameId);

        // Try local database first
        localDBCache.lookupObject("match", gameId, MatchDTO.class,
                new AsyncCallback<MatchDTO>() {
            @Override
            public void invoke(MatchDTO item) {
                if (item != null) {
                    callback.invoke(item);
                } else {
                    getFirebaseMatch(gameId, gameIdStr, callback);
                }
            }
        });
    }

    public static boolean durationIsRemake(long gameDuration) {
        return gameDuration < 300;
    }

    public static ParticipantIdentityDTO participantIdentityFromSummoner(
            List<ParticipantIdentityDTO> participantIdentities, SummonerDTO summoner) {
        ParticipantIdentityDTO summonerIdentity = null;
        for (ParticipantIdentityDTO participantIdentity : participantIdentities) {
            if (participantIdentity.player.accountId == summoner.accountId) {
                summonerIdentity = participantIdentity;
                break;
            }
        }
        return summonerIdentity;
    }

    public static ParticipantDTO participantFromParticipantId(
            List<ParticipantDTO> participants, int participantId)
    {
        ParticipantDTO participantFind = null;
        for (ParticipantDTO participantSearch : participants) {
            if (participantSearch.participantId == participantId) {
                participantFind = participantSearch;
                break;
            }
        }
        return participantFind;
    }

    public static void populateSpellIcons(final View view, final int rowId,
                                          final Handler uiThreadHandler,
                                          final ImageView[] spellIcons,
                                          final ParticipantDTO participant)
    {
        long[] spellIds = new long[] {
                participant.spell1Id,
                participant.spell2Id
        };

        for (int i = 0; i < spellIcons.length; ++i) {
            spellIcons[i].setImageDrawable(null);

            final int tempI = i;

            RiotAPI.fetchSpellIcon(spellIds[i], new RiotAPI.AsyncCallback<Drawable>() {
                @Override
                public void invoke(final Drawable drawable) {
                    if ((int)view.getTag() != rowId)
                        return;

                    uiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if ((int)view.getTag() != rowId)
                                return;

                            spellIcons[tempI].setImageDrawable(drawable);
                        }
                    });
                }
            });
        }
    }

    public static void populateRuneIcons(final View view, final int rowId,
                                         final Handler uiThreadHandler,
                                         final ImageView[] runeIcons,
                                         final ParticipantDTO participant)
    {
        uiThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                runeIcons[0].setImageResource(
                        RiotAPI.perkIdToResourceId(participant.stats.perk0));

                runeIcons[1].setImageResource(
                        RiotAPI.perkStyleIdToResourceId(
                                participant.stats.perkSubStyle));
            }
        });
    }

    public static void populateItemIcons(final View view, final int rowId,
                                         final Handler uiThreadHandler,
                                         final ImageView[] itemIcons,
                                         final ParticipantDTO participant)
    {
        long[] itemIds = new long[] {
                participant.stats.item0,
                participant.stats.item1,
                participant.stats.item2,
                participant.stats.item3,
                participant.stats.item4,
                participant.stats.item5,
                participant.stats.item6
        };

        for (int i = 0; i < itemIds.length; ++i) {
            itemIcons[i].setImageDrawable(null);

            final int tempI = i;

            RiotAPI.fetchItemIcon(itemIds[i], new RiotAPI.AsyncCallback<Drawable>() {
                @Override
                public void invoke(final Drawable drawable) {
                    if ((int)view.getTag() != rowId)
                        return;

                    uiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if ((int)view.getTag() != rowId)
                                return;

                            itemIcons[tempI].setImageDrawable(drawable);
                        }
                    });
                }
            });
        }
    }

    public static void populateChampionIcon(final View view, final int rowId,
                                            final Handler uiThreadHandler,
                                            final ImageView championIcon,
                                            final ParticipantDTO participant)
    {
        championIcon.setImageDrawable(null);
        RiotAPI.fetchChampionIcon(participant.championId, new AsyncCallback<Drawable>() {
            @Override
            public void invoke(final Drawable drawable) {
                if ((int)view.getTag() != rowId)
                    return;

                uiThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if ((int)view.getTag() != rowId)
                            return;

                        championIcon.setImageDrawable(drawable);
                    }
                });
            }
        });
    }

    public static String formatSpecialKills(ParticipantDTO participant) {
        if (participant.stats.pentaKills > 0) {
            return "Penta-kill!";
        } else if (participant.stats.tripleKils > 0) {
            return "Triple-kill";
        } else if (participant.stats.doubleKills > 0) {
            return "Double-kill";
        }
        return null;
    }

    public static void fetchChampionSplash(final long championId,
                                           final AsyncCallback<Drawable> callback) {
        ChampionDTO champion = ChampionLookup.championById(championId);

        if (champion.skins.isEmpty()) {
            callback.invoke(null);
            return;
        }

        SkinDTO skin = champion.skins.get(0);

        String url = String.format(Locale.ENGLISH,
                "%s/img/champion/splash/%s_%d.jpg",
                staticCdn, champion.key, skin.num);

        drawableFromUrl(url, callback);
    }

    // Singleton uses double checked lock to lazily
    // asynchronously initialize id->champion lookup table
    public static class ChampionLookup {
        private LongSparseArray<ChampionDTO> lookup;
        private String version;

        private static Object lock = new Object();
        private static volatile ChampionLookup instance;

        // Double checked locked singleton
        public static ChampionLookup getInstance() {
            if (instance == null || instance.lookup == null) {
                synchronized (lock) {
                    // Create the instance, if it has not been created yet
                    if (instance == null)
                        instance = new ChampionLookup();

                    // The lookup table is populated asynchronously,
                    // wait for that the first time, too
                    try {
                        while (instance.lookup == null)
                            lock.wait();
                    }
                    catch (InterruptedException ex) {
                        return null;
                    }
                }
            }
            return instance;
        }

        static String getVersion() {
            ChampionLookup inst = getInstance();
            if (inst != null)
                return inst.version;
            return null;
        }

        static ChampionDTO championById(long id) {
            ChampionLookup inst = getInstance();
            if (inst != null)
                return inst.lookup.get(id);
            return null;
        }

        private ChampionLookup() {
            RiotAPI.getChampionList(new RiotAPI.AsyncCallback<ChampionListDTO>() {
                @Override
                public void invoke(ChampionListDTO item) {
                    version = item.version;
                    LongSparseArray<ChampionDTO> lookupInit =
                            new LongSparseArray<>(item.data.size());

                    for (Map.Entry<String, ChampionDTO> entry : item.data.entrySet())
                        lookupInit.put(entry.getValue().id, entry.getValue());

                    lookup = lookupInit;
                    lock.notify();
                }
            });
        }
    }

    public static class ChampionMasteryComparators {
        public static final Comparator<ChampionMasteryDTO> byChampion =
                new Comparator<ChampionMasteryDTO>() {
                    @Override
                    public int compare(ChampionMasteryDTO lhs, ChampionMasteryDTO rhs) {
                        ChampionDTO lhsChamp = RiotAPI.ChampionLookup.championById(lhs.championId);
                        ChampionDTO rhsChamp = RiotAPI.ChampionLookup.championById(rhs.championId);

                        return lhsChamp.name.compareTo(rhsChamp.name);
                    }
                };

        public static final Comparator<ChampionMasteryDTO> byPoints =
                new Comparator<ChampionMasteryDTO>() {
                    @Override
                    public int compare(ChampionMasteryDTO lhs, ChampionMasteryDTO rhs) {
                        return -Long.compare(lhs.championPoints, rhs.championPoints);
                    }
                };

        public static final Comparator<ChampionMasteryDTO> byLevel =
                new Comparator<ChampionMasteryDTO>() {
                    @Override
                    public int compare(ChampionMasteryDTO lhs, ChampionMasteryDTO rhs) {
                        return -Integer.compare(lhs.championLevel, rhs.championLevel);
                    }
                };
    }

    public enum QueueId {
        all(-1, "All"),
        normal(400, "Normal"),
        rankedSolo(420, "Ranked Solo"),
        rankedFlex(440, "Ranked Flex"),
        aram(450, "ARAM"),
        snowUrf(1010, "Snow Urf");

        public final int queueId;
        public final String text;

        QueueId(int id, String text) {
            this.queueId = id;
            this.text = text;
        }

        public static String textFromInt(int queueId) {
            if (queueId == all.queueId) return all.text;
            if (queueId == normal.queueId) return normal.text;
            if (queueId == rankedSolo.queueId) return rankedSolo.text;
            if (queueId == rankedFlex.queueId) return rankedFlex.text;
            if (queueId == aram.queueId) return aram.text;
            if (queueId == snowUrf.queueId) return snowUrf.text;
            return "queueId=" + queueId;
        }
    }

    public static String queueIdToQueueName(int queueId) {
        return QueueId.textFromInt(queueId);
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
