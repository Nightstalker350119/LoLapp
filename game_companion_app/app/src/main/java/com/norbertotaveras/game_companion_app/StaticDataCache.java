package com.norbertotaveras.game_companion_app;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;

/**
 * Created by Norberto Taveras on 1/12/2018.
 */

class StaticDataCache<T> {
    public RiotAPI.DeferredRequest<T> tryCache(
            final Context context, final Gson gson,
            final String filename, StaticCacheFetcher<T> fetcher, Type typeOfT) {

        try {
            RiotAPI.Deferred<T> request = new RiotAPI.Deferred<>();

            FileInputStream file = context.openFileInput(filename);
            InputStreamReader reader = null;
            try {
                reader = new InputStreamReader(file, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // Come on, this will never happen
            }

            T obj;
            try {
                obj = gson.fromJson(reader, typeOfT);
                file.close();
            } catch (Exception e) {
                // If JSON was malformed, we got nothing
                obj = null;
            }

            if (obj == null)
                throw new FileNotFoundException();

            request.setResult(obj);

            return request;
        } catch (FileNotFoundException e) {
            RiotAPI.DeferredRequest<T> request;

            request = fetcher.fetch();

            request.getData(new RiotAPI.AsyncCallback<T>() {
                @Override
                public void invoke(T item) {
                    String json = gson.toJson(item);
                    FileOutputStream file = null;
                    try {
                        file = context.openFileOutput(filename, 0);
                        OutputStreamWriter writer = null;
                        try {
                            writer = new OutputStreamWriter(file, "UTF-8");
                        } catch (UnsupportedEncodingException e1) {
                            // Come on, this will never happen
                        }
                        writer.write(json, 0, json.length());
                        writer.flush();
                        file.close();
                        // This is the successful exit code path
                        return;
                    } catch (Exception fnfe) {
                        Log.e("RiotStaticCache", "Could not create cache file " +
                                filename + "!");
                        try {
                            // This is the successful exit code path
                            if (file != null)
                                file.close();
                            return;
                        } catch (IOException ioe) {
                            Log.e("RiotStaticCache", "Error closing cache file " +
                                    filename + "!");
                        }
                    }

                    // If execution made it here, it failed
                    context.deleteFile(filename);

                    Log.e("RiotStaticCache", "Discarded unsuccessfully created" +
                                    "cache file " + filename + "!");
                }
            });

            return request;
        }
    }
}

interface StaticCacheFetcher<T> {
    RiotAPI.DeferredRequest<T> fetch();
}
