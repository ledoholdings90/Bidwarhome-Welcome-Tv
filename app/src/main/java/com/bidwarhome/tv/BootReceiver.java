package com.bidwarhome.tv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Abre la pantalla de bienvenida cuando se enciende el dispositivo o se actualiza la app. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Config.bootEnabled(context) || Config.url(context) == null) return;
        try {
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(i);
        } catch (Exception ignored) {
            // Algunos sistemas bloquean abrir apps al arrancar sin el permiso de superposición.
        }
    }
}
