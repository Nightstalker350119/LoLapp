package com.norbertotaveras.game_companion_app;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Norberto Taveras on 2/28/2018.
 */

public class LocalDBCache {
    private static volatile LocalDBCache instance;
    private static Object lock = new Object();

    private final Executor executor;
    private SQLiteDatabase localDB;
    private final Gson gson;
    private final Object openedLock;
    private volatile boolean opened;

    public static LocalDBCache getInstance(Context context) {
        if (instance != null)
            return instance;

        synchronized (lock) {
            if (instance != null)
                return instance;

            instance = new LocalDBCache(context);
        }

        return instance;
    }

    private LocalDBCache(Context context) {
        opened = false;
        openedLock = new Object();
        executor = Executors.newCachedThreadPool();

        OpenTask openTask = new OpenTask(context, this);
        executor.execute(openTask);

        gson = new Gson();
    }

    private void notifyOpen() {
        synchronized (openedLock) {
            opened = true;
            openedLock.notifyAll();
        }
    }

    private void waitOpen() {
        // Double-checked check of opened to avoid taking the lock every time
        if (!opened) {
            synchronized (openedLock) {
                try {
                    // Wait for the database open to complete
                    while (!opened)
                        openedLock.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    public <T> void lookupObject(String kind, Long id, Type type,
                                 RiotAPI.AsyncCallback<T> callback) {
        LookupTask task = new LookupTask<>(this, kind, id, type, callback);
        executor.execute(task);
    }

    public void insertObject(String kind, Long id, Object obj,
                                    RiotAPI.AsyncCallback<Boolean> callback) {
        InsertTask task = new InsertTask(this, kind, id, gson.toJson(obj), callback);
        executor.execute(task);
    }

    private static class OpenTask implements Runnable {
        private final Context context;
        private final LocalDBCache cache;

        public OpenTask(Context context, LocalDBCache cache) {
            this.context = context;
            this.cache = cache;
        }

        @Override
        public void run() {
            SQLiteDatabase localDB = context.openOrCreateDatabase("local-cache.sqlite",
                    Context.MODE_PRIVATE, null);

            Log.v("LocalDBCache", "Initializing database");

            localDB.execSQL(
                    "CREATE TABLE IF NOT EXISTS \"match\" (" +
                            " \"type\" VARCHAR(8)," +
                            " \"id\" LONG," +
                            " \"json\" TEXT" +
                            ")"
            );

            localDB.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS \"match_type_id\"" +
                            " ON \"match\"(\"type\", \"id\")"
            );

            cache.localDB = localDB;

            cache.notifyOpen();
        }
    }

    private static class LookupTask<T> implements Runnable {
        public final LocalDBCache cache;
        public final String kind;
        public final long id;
        public final Type type;
        public final RiotAPI.AsyncCallback<T> callback;

        public LookupTask(LocalDBCache cache, String kind, long id, Type type,
                          RiotAPI.AsyncCallback<T> callback) {
            this.cache = cache;
            this.kind = kind;
            this.id = id;
            this.type = type;
            this.callback = callback;
        }

        @Override
        public void run() {
            cache.waitOpen();

            Log.v("LocalDBCache", "Looking up " + kind + " with id " + id);

            Cursor result = cache.localDB.rawQuery("SELECT json" +
                            " FROM \"match\"" +
                            " WHERE type = @kind" +
                            " AND \"id\" = @id",
                    new String[] { kind, String.valueOf(id) });

            T match = null;

            if (result.moveToNext()) {
                Log.v("LocalDBCache", "Cache hit for " + kind + " with id " + id);
                String json = result.getString(0);
                match = cache.gson.fromJson(json, type);
            } else {
                Log.v("LocalDBCache", "Cache miss for " + kind + " with id " + id);
            }

            result.close();

            callback.invoke(match);
        }
    }

    private static class InsertTask implements Runnable {
        private final LocalDBCache cache;
        private final String type;
        private final long id;
        private final String json;
        private final RiotAPI.AsyncCallback<Boolean> callback;

        public InsertTask(LocalDBCache cache, String type, long id, String json,
                          RiotAPI.AsyncCallback<Boolean> callback) {
            this.cache = cache;
            this.type = type;
            this.id = id;
            this.json = json;
            this.callback = callback;
        }

        @Override
        public void run() {
            cache.waitOpen();

            boolean result = false;
            try {
                Log.v("LocalDBCache", "Inserting" +
                        " type=" + type + "," +
                        " id=" + id +
                        " size=" + json.length());

                cache.localDB.execSQL("INSERT INTO match ( \"type\", \"id\", \"json\" )" +
                                " VALUES ( @type, @id, @json )",
                        new Object[]{ type, id, json });

                result = true;
            } catch (SQLiteConstraintException ex) {
                // Another client won a race
                Log.v("LocalDBCache", "Insert conflict," +
                        " type=" + type + "," +
                        " id=" + id);
            }

            if (callback != null)
                callback.invoke(result);
        }
    }
}
