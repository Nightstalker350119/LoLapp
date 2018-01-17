package com.norbertotaveras.game_companion_app;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

/**
 * Created by Norberto Taveras on 12/4/2017.
 */

class UIHelper {
    public static void showToast(Context context, String text, int duration) {
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public static Handler createRunnableLooper() {
        return new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Runnable work = (Runnable)msg.obj;
                work.run();
            }
        };
    }
}
