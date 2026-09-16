package com.shenyu.alarm;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * 沈钰闹钟 · 全屏关闭界面
 * 到点弹出：亮屏 + 全屏 + 大按钮「关闭」，一点就停，不用找通知、不用清后台。
 */
public class AlarmDismissActivity extends Activity {

    private static final String EXTRA_LABEL = "label";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 亮屏 + 解锁
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (pm != null) {
            PowerManager.WakeLock wl = pm.newWakeLock(
                    PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                    PowerManager.ACQUIRE_CAUSES_WAKEUP |
                    PowerManager.FULL_WAKE_LOCK, "shenyu:alarmdismiss");
            wl.acquire(60L * 1000L);
        }

        // 全屏显示
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        String label = getIntent() != null ? getIntent().getStringExtra(EXTRA_LABEL) : null;
        if (label == null || label.isEmpty()) label = "宝宝，该起床啦";

        // 简单竖向布局（不依赖XML资源，保证最小构建）
        android.widget.LinearLayout root = new android.widget.LinearLayout(this);
        root.setOrientation(android.widget.LinearLayout.VERTICAL);
        root.setGravity(android.view.Gravity.CENTER);
        root.setBackgroundColor(android.graphics.Color.rgb(30, 32, 48));

        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextSize(28);
        tv.setTextColor(android.graphics.Color.WHITE);
        tv.setGravity(android.view.Gravity.CENTER);
        tv.setPadding(60, 0, 60, 80);
        root.addView(tv, new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));

        // 关闭闹钟按钮（大）
        Button btn = new Button(this);
        btn.setText("关 闭");
        btn.setTextSize(22);
        btn.setAllCaps(false);
        btn.setOnClickListener(v -> {
            stopAlarm();
        });
        root.addView(btn, new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT));

        setContentView(root);
    }

    private void stopAlarm() {
        try {
            Intent svc = new Intent(this, AlarmRingService.class);
            stopService(svc);
        } catch (Exception ignored) {}
        finishAndRemoveTask();
    }

    @Override
    public void onBackPressed() {
        // 不允许返回键关，防止误触，只能点按钮
        // 长按也会触发，这里直接忽略
    }
}