package com.shenyu.alarm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 沈钰闹钟 · 主页
 * 显示当前闹钟设置 + 测试响铃按钮
 */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sp = getSharedPreferences("shenyu_alarm", Context.MODE_PRIVATE);
        int hour = sp.getInt("hour", -1);
        int minute = sp.getInt("minute", -1);
        String daysStr = sp.getString("days", "");
        String label = sp.getString("label", "");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(0xFF14161F);
        root.setPadding(48, 48, 48, 48);

        TextView title = new TextView(this);
        title.setText("🔔 沈钰闹钟");
        title.setTextSize(28);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView status = new TextView(this);
        StringBuilder sb = new StringBuilder();
        if (hour < 0 || minute < 0) {
            sb.append("\n\n还没有设定的闹钟\n");
            sb.append("跟哥哥说一声，哥哥就从后台写上去 🖤\n");
        } else {
            sb.append("\n\n⏰ 已设定：");
            sb.append(String.format(java.util.Locale.CHINA, "%02d:%02d", hour, minute));
            sb.append("\n");
            if (daysStr != null && !daysStr.isEmpty()) {
                sb.append("📅 重复：周").append(daysStr.replace(",", "、"));
                sb.append("\n");
            } else {
                sb.append("📅 单次（响完就睡）\n");
            }
            if (label != null && !label.isEmpty()) {
                sb.append("💌 ").append(label).append("\n");
            }
        }
        status.setText(sb.toString());
        status.setTextSize(16);
        status.setTextColor(0xFFE0E0E0);
        status.setGravity(Gravity.CENTER);
        root.addView(status);

        Button btnTest = new Button(this);
        btnTest.setText("🔔 测试响铃一下");
        btnTest.setTextSize(16);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent svc = new Intent(MainActivity.this, AlarmRingService.class);
                svc.putExtra("label", "宝宝，哥哥叫你～（测试）");
                startForegroundService(svc);
            }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = 48;
        btnTest.setLayoutParams(lp);
        root.addView(btnTest);

        setContentView(root);
    }
}