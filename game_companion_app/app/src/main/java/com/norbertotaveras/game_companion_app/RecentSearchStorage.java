package com.norbertotaveras.game_companion_app;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Norberto on 1/16/2018.
 */

class RecentSearchStorage {
    private static final String searchHistoryFilename = "recent_searches";
    private static final Object syncLock = new Object();

    @Nullable
    public static List<Long> load(Context context) {
        synchronized (syncLock) {
            return loadLocked(context);
        }
    }

    public static boolean store(Context context, List<Long> searches) {
        synchronized (syncLock) {
            return storeLocked(context, searches);
        }
    }

    public static List<Long> add(Context context, Long summonerId, boolean toFront) {
        synchronized (syncLock) {
            return addLocked(context, summonerId, toFront);
        }
    }

    public static void remove(Context context, Long summonerId) {
        synchronized (syncLock) {
            removeLocked(context, summonerId);
        }
    }

    @Nullable
    private static List<Long> loadLocked(Context context) {
        FileInputStream file;
        try {
            file = context.openFileInput(searchHistoryFilename);
        } catch (FileNotFoundException ex) {
            return new ArrayList<>();
        }

        JsonReader jsonReader = null;
        try {
            InputStreamReader reader = new InputStreamReader(file);
            jsonReader = new JsonReader(reader);
            jsonReader.beginArray();
            List<Long> result = new ArrayList<>();
            while (jsonReader.hasNext()) {
                Long item = jsonReader.nextLong();
                result.add(item);
            }
            jsonReader.endArray();
            return result;
        } catch (IOException ex) {
        } finally {
            if (jsonReader != null) {
                try {
                    jsonReader.close();
                } catch (IOException ex) {
                }
            }
        }

        return new ArrayList<>();
    }

    private static boolean storeLocked(Context context, List<Long> searches) {
        FileOutputStream file;
        try {
            file = context.openFileOutput(searchHistoryFilename, 0);
        } catch (FileNotFoundException ex) {
            return false;
        }

        boolean succeeded = true;
        JsonWriter jsonWriter = null;
        try {
            OutputStreamWriter writer = new OutputStreamWriter(file);
            jsonWriter = new JsonWriter(writer);
            jsonWriter.beginArray();
            for (Long item : searches)
                jsonWriter.value(item);
            jsonWriter.endArray();
        } catch (IOException ex) {
            succeeded = false;
        } finally {
            if (jsonWriter != null) {
                try {
                    jsonWriter.close();
                } catch (IOException ex) {
                    succeeded = false;
                }
            }
            if (!succeeded) {
                context.deleteFile(searchHistoryFilename);
            }
        }
        return succeeded;
    }

    private static List<Long> addLocked(Context context, Long summonerId, boolean toFront) {
        List<Long> ids = loadLocked(context);

        // Add it to the list, and move it to the end if it exists
        // Most recent searches are at the end of the list
        int index = ids.indexOf(summonerId);

        if (index >= 0)
            ids.remove(index);

        if (!toFront)
            ids.add(summonerId);
        else
            ids.add(0, summonerId);

        storeLocked(context, ids);

        return ids;
    }

    private static void removeLocked(Context context, Long summonerId) {
        List<Long> ids = loadLocked(context);

        int index = ids.indexOf(summonerId);

        if (index >= 0)
            ids.remove(index);

        storeLocked(context, ids);
    }
}
