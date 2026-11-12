package com.bidwarhome.tv;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** Pantalla de configuración: qué alojamiento es esta tele y si arranca al encender. */
public class SetupActivity extends Activity {

    private static final int RIA = Color.rgb(14, 58, 68);
    private static final int RIA_DEEP = Color.rgb(9, 44, 52);
    private static final int ESPUMA = Color.rgb(234, 242, 239);
    private static final int GRANITO = Color.rgb(147, 166, 164);
    private static final int SOL = Color.rgb(240, 185, 74);

    private EditText input;
    private CheckBox boot;
    private TextView error;
    private Button overlayBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(RIA_DEEP);
        scroll.setFillViewport(true);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setPadding(dp(64), dp(40), dp(64), dp(40));
        scroll.addView(box);

        TextView brand = text("BIDWAR HOME", 14, SOL);
        brand.setLetterSpacing(0.12f);
        box.addView(brand);

        TextView title = text("Configurar esta tele", 34, ESPUMA);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
        title.setPadding(0, dp(6), 0, dp(10));
        box.addView(title);

        String help = Config.hasServer()
                ? "Escribe el código de la tele de este alojamiento. Lo encuentras en la hoja de Google, en el menú Pantallas TV → Ver enlaces de las teles."
                : "Escribe o pega el enlace completo de la tele de este alojamiento (Pantallas TV → Ver enlaces de las teles).";
        TextView helpView = text(help, 17, GRANITO);
        helpView.setMaxWidth(dp(720));
        box.addView(helpView);

        input = new EditText(this);
        input.setSingleLine(true);
        input.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        input.setTextColor(ESPUMA);
        input.setHintTextColor(GRANITO);
        input.setHint(Config.hasServer() ? "Código de la tele" : "https://…");
        input.setInputType(Config.hasServer()
                ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setText(Config.input(this));
        input.setBackground(field());
        input.setPadding(dp(18), dp(14), dp(18), dp(14));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(560), LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(22);
        box.addView(input, lp);

        error = text("", 16, Color.rgb(240, 138, 126));
        error.setVisibility(View.GONE);
        error.setPadding(0, dp(8), 0, 0);
        box.addView(error);

        boot = new CheckBox(this);
        boot.setText("Abrir la pantalla automáticamente al encender");
        boot.setTextColor(ESPUMA);
        boot.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        boot.setButtonTintList(ColorStateList.valueOf(SOL));
        boot.setChecked(Config.bootEnabled(this));
        boot.setBackground(focusable(Color.TRANSPARENT));
        boot.setPadding(dp(8), dp(10), dp(16), dp(10));
        LinearLayout.LayoutParams lpc = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpc.topMargin = dp(18);
        box.addView(boot, lpc);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lpr = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpr.topMargin = dp(22);
        box.addView(row, lpr);

        Button save = button("Guardar y abrir", true);
        save.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { save(); }
        });
        row.addView(save);

        overlayBtn = button("Permitir arranque automático", false);
        overlayBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { askOverlay(); }
        });
        LinearLayout.LayoutParams lpb = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lpb.leftMargin = dp(14);
        row.addView(overlayBtn, lpb);

        TextView foot = text("Para volver a esta pantalla: en el mando pulsa arriba, arriba, abajo, abajo.\nVersión " + BuildConfig.VERSION_NAME, 14, GRANITO);
        foot.setPadding(0, dp(30), 0, 0);
        box.addView(foot);

        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent e) {
                if (actionId == EditorInfo.IME_ACTION_DONE) { save.requestFocus(); return false; }
                return false;
            }
        });

        setContentView(scroll);
        if (Config.input(this).isEmpty()) input.requestFocus(); else save.requestFocus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean needs = Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this);
        overlayBtn.setVisibility(needs ? View.VISIBLE : View.GONE);
    }

    private void save() {
        String raw = input.getText().toString().trim();
        String url = Config.toUrl(raw);
        if (url == null) {
            error.setText(Config.hasServer() ? "Escribe el código de la tele." : "Escribe el enlace completo, empezando por https://");
            error.setVisibility(View.VISIBLE);
            input.requestFocus();
            return;
        }
        Config.save(this, raw, boot.isChecked());
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    private void askOverlay() {
        if (Build.VERSION.SDK_INT < 23) return;
        try {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
        } catch (Exception e) {
            try {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
            } catch (Exception e2) {
                Toast.makeText(this, "Este dispositivo no tiene ese ajuste. Prueba a reiniciarlo para comprobar si la pantalla se abre sola.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /* ---------- estilos ---------- */

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private TextView text(String s, int sp, int color) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        t.setLineSpacing(0, 1.2f);
        return t;
    }

    private GradientDrawable rounded(int fill, int stroke) {
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dp(10));
        g.setColor(fill);
        if (stroke != 0) g.setStroke(dp(2), stroke);
        return g;
    }

    private StateListDrawable field() {
        StateListDrawable s = new StateListDrawable();
        s.addState(new int[]{android.R.attr.state_focused}, rounded(RIA, SOL));
        s.addState(new int[]{}, rounded(RIA, Color.argb(90, 234, 242, 239)));
        return s;
    }

    private StateListDrawable focusable(int normal) {
        StateListDrawable s = new StateListDrawable();
        s.addState(new int[]{android.R.attr.state_focused}, rounded(Color.argb(60, 240, 185, 74), SOL));
        s.addState(new int[]{}, rounded(normal, 0));
        return s;
    }

    private Button button(String label, boolean primary) {
        final Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        b.setPadding(dp(26), dp(12), dp(26), dp(12));
        StateListDrawable s = new StateListDrawable();
        s.addState(new int[]{android.R.attr.state_focused}, rounded(SOL, SOL));
        s.addState(new int[]{}, primary ? rounded(ESPUMA, 0) : rounded(Color.TRANSPARENT, Color.argb(120, 234, 242, 239)));
        b.setBackground(s);
        b.setTextColor(new ColorStateList(
                new int[][]{{android.R.attr.state_focused}, {}},
                new int[]{RIA_DEEP, primary ? RIA_DEEP : ESPUMA}));
        b.setStateListAnimator(null);
        return b;
    }
}
