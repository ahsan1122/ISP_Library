package com.esp.library.utilities.common;

import android.util.Log;

public class CustomLogs {

    static String TAG="ESP";

    public static void displayLogs(String msg)
    {
        Log.i(TAG,msg+"");
    }
}
