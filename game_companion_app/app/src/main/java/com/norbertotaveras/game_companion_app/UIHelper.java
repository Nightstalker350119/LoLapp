package com.norbertotaveras.game_companion_app;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Norberto Taveras on 12/4/2017.
 */

public class UIHelper {
    public static void showToast(Context context, String text, int duration) {
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
