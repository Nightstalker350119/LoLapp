package com.norbertotaveras.game_companion_app;

import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;

/**
 * Created by Norberto on 1/11/2018.
 */

public class RateLimiter {
    private final Timer rateLimitTimer;
    private final Object rateLimitLock;

//    private final ArrayList<Long> rateLimitHistory;
//    private final int rateLimitPerSecond;
//    private final int rateLimitLongTermSeconds;
//    private final int rateLimitLongTermLimit;

    RateLimiter(int perSec, int longTermSec, int longTermLimit) {
        rateLimitTimer = new Timer();
//        rateLimitHistory = new ArrayList<>();
//        rateLimitPerSecond = perSec;
//        rateLimitLongTermSeconds = longTermSec;
//        rateLimitLongTermLimit = longTermLimit;
        rateLimitLock = new Object();
    }

//    // Returns how many milliseconds to wait before issuing another request
//    // Returns 0 when we should issue a new request immediately
//    private long rateLimit(long now) {
//        // Calculate the timestamp which is so old we need to throw it away
//        long expired = now - rateLimitLongTermSeconds * 1000;
//
//        // Discard entries further back than long term limit
//        int i, e;
//        for (i = 0, e = rateLimitHistory.size();
//             i < e && rateLimitHistory.get(i) < expired; ++i);
//        Log.v("RateLimiter", "Removing " + String.valueOf(i) + " entries from history");
//        rateLimitHistory.subList(0, i).clear();
//
//        // Nothing in history, start request immediately
//        if (rateLimitHistory.isEmpty()) {
//            Log.v("RateLimiter", "History is empty, no wait");
//            return 0;
//        }
//
//        // Get entry long term limit entries ago, or first, if not enough history entries
//        // This becomes relevant when we have drastically exceeded the limit because
//        // we just started up and we don't know about the requests in a previous run
//        long oldestEntry = rateLimitHistory.get(
//                Math.max(0, rateLimitHistory.size() - rateLimitLongTermLimit));
//
//        if (overrun)
//            return oldestEntry + rateLimitLongTermLimit * 1000 - now;
//
//        // If we are at the long term limit...
//        if (rateLimitHistory.size() >= rateLimitLongTermLimit) {
//            // ...calculate how long to wait based on oldest history entry
//            return oldestEntry + rateLimitLongTermLimit * 1000 - now;
//        }
//
//        // If there aren't enough history entries to hit the short term limit,
//        // then start the request immediately
//        if (rateLimitHistory.size() < rateLimitPerSecond)
//            return 0;
//
//        // If we are at the long term limit,
//        // wait the full longTermSeconds before issuing another request
//        if (rateLimitHistory.size() == rateLimitLongTermLimit)
//            return rateLimitHistory.get(0) + rateLimitLongTermSeconds * 1000 - now;
//
//        // We will exceed the short term limit, calculate time until next short term time window
//        return rateLimitHistory.get(rateLimitHistory.size() - rateLimitPerSecond) + 1000 - now;
//    }

    public <T> void request(final Call<T> call, final Callback<T> callback, long delayMs) {
        synchronized (rateLimitLock) {
            requestLocked(call, callback, delayMs);
        }
    }

    private <T> void requestLocked(final Call<T> call, final Callback<T> callback, long delayMs) {
        //long now = new Date().getTime();

//        if (delay < 0)
//            delay = rateLimit(now);

        final Callback<T> wrapper = new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, retrofit2.Response<T> response) {
                // We hit rate limit unexpectedly, reschedule
                if (response.code() != 429) {
                    callback.onResponse(call, response);
                } else {
                    String retryAfter = response.headers().get("Retry-After");
                    Log.w("RateLimiter", "Unexpected 429 error, rescheduling after " +
                        retryAfter + " seconds");
                    if (retryAfter != null) {
                        int waitSeconds = Integer.parseInt(retryAfter);
                        request(call.clone(), callback, waitSeconds * 1000);
                    } else {
                        Log.e("RateLimiter", "Retry-After HTTP response header missing");
                    }
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable t) {
                callback.onFailure(call, t);
            }
        };

        if (delayMs <= 0) {
            Log.v("RateLimiter", "running request immediately");
            //rateLimitHistory.add(now);
            call.enqueue(wrapper);
        } else {
            Log.v("RateLimiter", "running request after " + String.valueOf(delayMs) + "ms");
            //rateLimitHistory.add(now + delay);
            rateLimitTimer.schedule(new RateLimitedTask<>(call, wrapper), delayMs);
        }
    }

    private class RateLimitedTask<T> extends TimerTask {
        final Call<T> call;
        final Callback<T> callback;

        public RateLimitedTask(Call<T> call, Callback<T> callback) {
            Log.v("RiotAPI", "Creating rate limiter task, url=" +
                    call.request().url().toString());

            this.call = call;
            this.callback = callback;
        }

        @Override
        public void run() {
            Log.v("RiotAPI", "Enqueueing rate limiter task, url=" +
                    call.request().url().toString());
            call.enqueue(callback);
        }
    }
}
