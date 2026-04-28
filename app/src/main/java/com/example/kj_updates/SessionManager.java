package com.example.kj_updates;

import android.content.Context;
import android.content.SharedPreferences;

public final class SessionManager {

    private static final String PREFS_NAME = "pulse_social_session";
    private static final String KEY_DEMO_MODE = "demo_mode";

    private SessionManager() {
    }

    public static boolean isDemoMode(Context context) {
        return prefs(context).getBoolean(KEY_DEMO_MODE, false);
    }

    public static void setDemoMode(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_DEMO_MODE, enabled).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
