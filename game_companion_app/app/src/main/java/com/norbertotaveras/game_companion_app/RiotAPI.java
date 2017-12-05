package com.norbertotaveras.game_companion_app;

import com.norbertotaveras.game_companion_app.DTO.StaticData.ProfileIconDataDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.ProfileIconDetailsDTO;
import com.norbertotaveras.game_companion_app.DTO.StaticData.RealmDTO;

import java.io.IOException;
import java.util.ArrayList;

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
            "RGAPI-ecc58539-f3f7-4a49-82c0-ebd30618a796";
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
        retrofit2.Call<ProfileIconDataDTO> getProfileIconsRequest = apiService.getProfileIcons();

        getProfileIconsRequest.enqueue(new Callback<ProfileIconDataDTO>() {
            @Override
            public void onResponse(retrofit2.Call<ProfileIconDataDTO> call,
                                   retrofit2.Response<ProfileIconDataDTO> response) {
                handleGetProfileIconData(response.body());
            }

            @Override
            public void onFailure(retrofit2.Call<ProfileIconDataDTO> call, Throwable t) {

            }
        });

        retrofit2.Call<RealmDTO> getRealmsRequest = apiService.getRealms();

        getRealmsRequest.enqueue(new Callback<RealmDTO>() {
            @Override
            public void onResponse(retrofit2.Call<RealmDTO> call, retrofit2.Response<RealmDTO> response) {
                handleGetRealmsRequest(response.body());
            }

            @Override
            public void onFailure(retrofit2.Call<RealmDTO> call, Throwable t) {

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

    public interface AsyncCallback<T> {
        void invoke(T item);
    }
}
