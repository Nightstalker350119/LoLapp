package com.norbertotaveras.game_companion_app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

    public static void confirm(Context context, String question, String yesButton, String noButton,
                               final ConfirmCallback callback) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        callback.onChoice(true);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        callback.onChoice(false);
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(question).setPositiveButton(yesButton, dialogClickListener)
                .setNegativeButton(noButton, dialogClickListener).show();
    }

    interface ConfirmCallback {
        void onChoice(boolean choice);
    }
}
