package com.bidwarhome.tv;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;

/** Muestra la pantalla de bienvenida a pantalla completa y la mantiene siempre viva. */
public class MainActivity extends Activity {

    private static final int RETRY_MS = 30_000;
    private static final long FULL_RELOAD_MS = 12L * 60 * 60 * 1000; // recarga completa cada 12 h
    private static final int[] SECRET = {
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_UP,
            KeyEvent.KEYCODE_DPAD_DOWN, KeyEvent.KEYCODE_DPAD_DOWN};

    private final Handler handler = new Handler(Looper.getMainLooper());
    private FrameLayout root;
    private WebView web;
    private TextView overlay;
    private String url;
    private boolean loadFailed;
    private int secretPos;
    private long secretTime;

    private final Runnable retry = new Runnable() {
        @Override public void run() { load(); }
    };
    private final Runnable fullReload = new Runnable() {
        @Override public void run() {
            load();
            handler.postDelayed(this, FULL_RELOAD_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_FULLSCREEN);

        url = Config.url(this);
        if (url == null) {
            openSetup();
            finish();
            return;
        }

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(14, 58, 68));

        overlay = new TextView(this);
        overlay.setTextColor(Color.rgb(234, 242, 239));
        overlay.setBackgroundColor(Color.rgb(9, 44, 52));
        overlay.setTextSize(28);
        overlay.setGravity(Gravity.CENTER);
        overlay.setPadding(80, 80, 80, 80);
        overlay.setVisibility(View.GONE);

        createWebView();
        root.addView(overlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);

        load();
        handler.postDelayed(fullReload, FULL_RELOAD_MS);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void createWebView() {
        web = new WebView(this);
        web.setBackgroundColor(Color.rgb(14, 58, 68));
        web.setFocusable(false);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);

        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(web, true);

        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false; // todo se abre dentro de la app
            }

            @Override
            public void onPageFinished(WebView view, String u) {
                if (!loadFailed) overlay.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) failed();
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (Build.VERSION.SDK_INT < 23) failed();
            }

            @Override
            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                // El motor web se ha caído: se crea uno nuevo en vez de cerrar la app.
                root.removeView(web);
                web.destroy();
                createWebView();
                load();
                return true;
            }
        });
        root.addView(web, 0, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void load() {
        handler.removeCallbacks(retry);
        loadFailed = false;
        if (web != null && url != null) web.loadUrl(url);
    }

    private void failed() {
        loadFailed = true;
        overlay.setText("Sin conexión a internet\n\nReintentando cada 30 segundos…");
        overlay.setVisibility(View.VISIBLE);
        handler.removeCallbacks(retry);
        handler.postDelayed(retry, RETRY_MS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
        if (web == null) return;
        web.onResume();
        String current = Config.url(this);
        if (current == null) {
            openSetup();
        } else if (!current.equals(url)) {
            url = current;
            load();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) hideSystemUi();
    }

    @SuppressWarnings("deprecation")
    private void hideSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    /** Arriba, arriba, abajo, abajo en el mando (o el botón de menú) abre la configuración. */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int code = event.getKeyCode();
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
            if (code == KeyEvent.KEYCODE_MENU || code == KeyEvent.KEYCODE_SETTINGS) {
                openSetup();
                return true;
            }
            long now = System.currentTimeMillis();
            if (now - secretTime > 3000) secretPos = 0;
            if (code == SECRET[secretPos]) {
                secretPos++;
                secretTime = now;
                if (secretPos == SECRET.length) {
                    secretPos = 0;
                    openSetup();
                    return true;
                }
            } else {
                secretPos = (code == SECRET[0]) ? 1 : 0;
                secretTime = now;
            }
        }
        // El botón Atrás no cierra la pantalla, para que el huésped no la quite sin querer.
        if (code == KeyEvent.KEYCODE_BACK) return true;
        return super.dispatchKeyEvent(event);
    }

    private void openSetup() {
        startActivity(new Intent(this, SetupActivity.class));
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (web != null) {
            root.removeView(web);
            web.destroy();
            web = null;
        }
        super.onDestroy();
    }
}
