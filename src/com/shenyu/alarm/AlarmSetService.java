package com.shenyu.alarm;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * 沈钰闹钟 · 设置服务（绕过广播队列，直接 am startservice 写闹钟）
 * 用法: am startservice -n com.shenyu.alarm/.AlarmSetService --ei hour 8 --ei minute 0 --es label "..." [--eia days 1,2,3,4,5,6,7]
 * 取消: am startservice -n com.shenyu.alarm/.AlarmSetService --es action cancel
 */
public class AlarmSetService extends Service {

    private static final String TAG = "ShenYuAlarm";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;
        String action = intent.getStringExtra("action");
        if ("cancel".equalsIgnoreCase(action)) {
            AlarmHelper.cancel(this);
            Log.i(TAG, "alarm cancelled via service");
        } else {
            int hour = intent.getIntExtra("hour", -1);
            int minute = intent.getIntExtra("minute", -1);
            String label = intent.getStringExtra("label");
            int[] days = intent.getIntArrayExtra("days");
            boolean ok = AlarmHelper.set(this, hour, minute, label, days);
            Log.i(TAG, "alarm set via service: " + hour + ":" + minute + " ok=" + ok);
        }
        // 设置完成后无需常驻
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}