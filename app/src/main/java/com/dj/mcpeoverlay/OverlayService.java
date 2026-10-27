package com.dj.mcpeoverlay;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Switch;
import android.widget.Toast;

public final class OverlayService extends Service {
    private WindowManager wm;
    private View bubble;
    private View panel;
    private WindowManager.LayoutParams bubbleParams;
    private WindowManager.LayoutParams panelParams;
    private int dp(float x) { return (int) (x * getResources().getDisplayMetrics().density + 0.5f); }

    @Override public void onCreate() {
        super.onCreate();
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return; }
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        createBubble();
    }

    private WindowManager.LayoutParams params(int width, int height, int gravity) {
        int type = android.os.Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        return new WindowManager.LayoutParams(width, height, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT, gravity);
    }

    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color); return t;
    }

    private void createBubble() {
        TextView b = text("MC", 15, Color.WHITE); b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setGravity(Gravity.CENTER); b.setBackgroundColor(0xEE176B52); b.setPadding(dp(14), dp(10), dp(14), dp(10));
        bubble = b;
        bubbleParams = params(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.START);
        bubbleParams.x = dp(18); bubbleParams.y = dp(160);
        b.setOnClickListener(v -> togglePanel());
        b.setOnTouchListener(new View.OnTouchListener() {
            float downX, downY; int startX, startY; boolean moved;
            @Override public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_DOWN) { downX=e.getRawX(); downY=e.getRawY(); startX=bubbleParams.x; startY=bubbleParams.y; moved=false; return false; }
                if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    int dx=(int)(e.getRawX()-downX), dy=(int)(e.getRawY()-downY);
                    if (Math.abs(dx)>dp(4)||Math.abs(dy)>dp(4)) moved=true;
                    if (moved) { bubbleParams.x=startX+dx; bubbleParams.y=startY+dy; wm.updateViewLayout(bubble,bubbleParams); }
                    return moved;
                }
                return false;
            }
        });
        wm.addView(bubble, bubbleParams);
    }

    private void togglePanel() {
        if (panel != null) { wm.removeView(panel); panel=null; return; }
        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12)); card.setBackgroundColor(0xF21B2430);
        TextView header = text("MCPE Overlay 26.52", 18, Color.WHITE); header.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(header);
        TextView sub = text("ДЕМО-ПАНЕЛЬ · без подключения к движку", 10, 0xFF9CAFC0);
        sub.setPadding(0, dp(4), 0, dp(8)); card.addView(sub);
        ScrollView scroll = new ScrollView(this);
        LinearLayout list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL);
        addSwitch(list, "Coords", "Координаты экрана телефона");
        addSwitch(list, "Fullbright", "Переключатель яркости меню");
        addSwitch(list, "Zoom", "Демонстрационный режим приближения");
        addSwitch(list, "Speed", "Переключатель скорости (демо)");
        addSwitch(list, "ESP", "Переключатель подсветки (демо)");
        addSwitch(list, "Fly", "Переключатель полёта (демо)");
        scroll.addView(list); card.addView(scroll, new LinearLayout.LayoutParams(dp(270), dp(310)));
        TextView close = text("ЗАКРЫТЬ МЕНЮ", 13, 0xFF69F0AE); close.setGravity(Gravity.CENTER); close.setPadding(0, dp(12), 0, dp(4));
        close.setOnClickListener(v -> togglePanel()); card.addView(close);
        panel=card;
        panelParams=params(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, Gravity.TOP|Gravity.START);
        panelParams.x=Math.max(dp(8), bubbleParams.x); panelParams.y=Math.max(dp(8), bubbleParams.y+dp(48));
        wm.addView(panel,panelParams);
    }

    private void addSwitch(LinearLayout parent, String name, String detail) {
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.VERTICAL); row.setPadding(0, dp(5), 0, dp(5));
        Switch sw = new Switch(this); sw.setText(name); sw.setTextColor(Color.WHITE); sw.setTextSize(15);
        TextView info = text(detail, 11, 0xFF9CAFC0);
        row.addView(sw); row.addView(info); parent.addView(row);
        sw.setOnCheckedChangeListener((button, checked) -> Toast.makeText(this,
            name + (checked ? " включён" : " выключен") + " (режим интерфейса)", Toast.LENGTH_SHORT).show());
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }
    @Override public void onDestroy() {
        if (wm != null) { if (panel != null) { wm.removeView(panel); panel=null; } if (bubble != null) { wm.removeView(bubble); bubble=null; } }
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent) { return null; }
}
