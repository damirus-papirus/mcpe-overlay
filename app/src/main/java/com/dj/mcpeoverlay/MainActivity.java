package com.dj.mcpeoverlay;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class MainActivity extends Activity {
    private int dp(float value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(28), dp(24), dp(24));
        root.setBackgroundColor(0xFF10151D);

        TextView title = new TextView(this);
        title.setText("MCPE Overlay"); title.setTextSize(28); title.setTextColor(0xFFFFFFFF);
        root.addView(title);
        TextView desc = new TextView(this);
        desc.setText("Плавающее меню для Android. Переключатели пока управляют только интерфейсом — они не изменяют игровой движок Minecraft.");
        desc.setTextSize(15); desc.setTextColor(0xFFB9C4D0); desc.setPadding(0, dp(12), 0, dp(20));
        root.addView(desc);

        Button start = new Button(this); start.setText("Разрешение и запуск меню");
        start.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
                Intent permission = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivity(permission);
            } else {
                startService(new Intent(this, OverlayService.class));
                finish();
            }
        });
        root.addView(start, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView note = new TextView(this);
        note.setText("Функции меню: координаты экрана, режим яркости интерфейса, Zoom-панель, Speed, ESP и Fly — демонстрационные переключатели. Для настоящих игровых функций нужен отдельный мод, совместимый с конкретной сборкой.");
        note.setTextSize(13); note.setTextColor(0xFF8997A8); note.setPadding(0, dp(18), 0, 0);
        root.addView(note);
        setContentView(root);
    }

    @Override protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 23 && Settings.canDrawOverlays(this)) {
            // Permission granted; user can tap the launch button to start the service.
        }
    }
}
