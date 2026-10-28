package com.dj.mcpeoverlay;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class MainActivity extends Activity {
    private boolean launched = false;

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(28), dp(24), dp(24));
        root.setBackgroundColor(0xFF10151D);

        TextView title = new TextView(this);
        title.setText("MCPE Overlay");
        title.setTextSize(28);
        title.setTextColor(0xFFFFFFFF);
        root.addView(title);

        TextView description = new TextView(this);
        description.setText(
            "Плавающее меню Minecraft Bedrock. " +
            "Разреши отображение поверх других приложений."
        );
        description.setTextSize(16);
        description.setTextColor(0xFFB9C4D0);
        description.setPadding(0, dp(12), 0, dp(20));
        root.addView(description);

        Button start = new Button(this);
        start.setText("Разрешить и запустить меню");
        start.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 23
                    && !Settings.canDrawOverlays(this)) {
                Intent permission = new Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())
                );
                startActivity(permission);
            } else {
                launchOverlay();
            }
        });

        root.addView(start, new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView info = new TextView(this);
        info.setText(
            "После выдачи разрешения меню запустится автоматически. " +
            "Открой Minecraft и нажми плавающую кнопку MC. " +
            "Игровые переключатели пока демонстрационные. " +
            "Импорт аддона сам по себе не подключает приложение к движку."
        );
        info.setTextSize(14);
        info.setTextColor(0xFF8997A8);
        info.setPadding(0, dp(18), 0, 0);
        root.addView(info);

        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Build.VERSION.SDK_INT < 23
                || Settings.canDrawOverlays(this)) {
            launchOverlay();
        }
    }

    private void launchOverlay() {
        if (launched) return;
        launched = true;

        try {
            startService(new Intent(this, OverlayService.class));
            finish();
        } catch (RuntimeException e) {
            launched = false;
            android.widget.Toast.makeText(
                this,
                "Ошибка запуска меню: " + e.getMessage(),
                android.widget.Toast.LENGTH_LONG
            ).show();
        }
    }
}
