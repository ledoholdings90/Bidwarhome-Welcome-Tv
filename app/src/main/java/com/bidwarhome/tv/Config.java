package com.bidwarhome.tv;

import android.content.Context;
import android.content.SharedPreferences;

/** Guarda en la tele qué pantalla abrir y si debe arrancar al encender. */
final class Config {
    private static final String PREFS = "bidwar_tv";
    private static final String KEY_INPUT = "input";
    private static final String KEY_BOOT = "boot";

    private Config() {}

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /** Lo que se escribió en la configuración: un código de tele o un enlace completo. */
    static String input(Context c) {
        return prefs(c).getString(KEY_INPUT, "");
    }

    static boolean bootEnabled(Context c) {
        return prefs(c).getBoolean(KEY_BOOT, true);
    }

    static void save(Context c, String input, boolean boot) {
        prefs(c).edit().putString(KEY_INPUT, input).putBoolean(KEY_BOOT, boot).apply();
    }

    static boolean hasServer() {
        return BuildConfig.WEB_APP_URL != null && BuildConfig.WEB_APP_URL.startsWith("https://");
    }

    /** Convierte lo guardado en la dirección que abre la tele, o null si falta configurar. */
    static String url(Context c) {
        return toUrl(input(c));
    }

    static String toUrl(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;
        if (s.startsWith("https://") || s.startsWith("http://")) return s;
        String code = s.toUpperCase().replaceAll("[^A-Z0-9]", "");
        if (code.isEmpty() || !hasServer()) return null;
        String base = BuildConfig.WEB_APP_URL;
        return base + (base.contains("?") ? "&" : "?") + "t=" + code;
    }
}
