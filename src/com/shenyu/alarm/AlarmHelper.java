package com.shenyu.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

/**
 * 闹钟核心逻辑（共享给 Receiver / SetService / 测试）
 */
public final class AlarmHelper {

    public static final String PREFS = "shenyu_alarm";
    public static final String KEY_HOUR = "hour";
    public static final String KEY_MINUTE = "minute";
    public static final String KEY_DAYS = "days";
    public static final String KEY_LABEL = "label";
    public static final int REQ_CODE_BASE = 9100;

    private AlarmHelper() {}

    public static boolean set(Context context, int hour, int minute, String label, int[] days) {
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) return false;
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit()
                .putInt(KEY_HOUR, hour)
                .putInt(KEY_MINUTE, minute)
                .putString(KEY_DAYS, days == null ? "" : join(days))
                .putString(KEY_LABEL, label == null ? "" : label)
                .apply();
        // 一次性排好未来8天，每个独立requestCode——防中途被清后台/force-stop导致后续闹钟全丢
        scheduleFutureDays(context, hour, minute, label, days, 8);
        return true;
    }

    public static void cancel(Context context) {
        // 取消前先读配置，清掉未来8天所有已排的
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        int hour = sp.getInt(KEY_HOUR, -1);
        int minute = sp.getInt(KEY_MINUTE, -1);
        int[] days = parseDays(sp.getString(KEY_DAYS, ""));
        if (hour >= 0 && minute >= 0) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            for (int i = 0; i < 8; i++) {
                Calendar c = (Calendar) cal.clone();
                c.add(Calendar.DAY_OF_MONTH, i);
                long t = c.getTimeInMillis();
                if (t > System.currentTimeMillis()) {
                    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    if (am != null) am.cancel(buildServicePending(context, t, null));
                }
            }
        }
        sp.edit().clear().apply();
    }

    public static void scheduleRepeatIfNeeded(Context context) {
        // 已改为"设置时一次性排8天"，不再依赖响铃后自续，保留空实现兜底
    }

    private static void scheduleFutureDays(Context context, int hour, int minute, String label, int[] days, int n) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        for (int i = 0; i < n; i++) {
            Calendar c = (Calendar) cal.clone();
            c.add(Calendar.DAY_OF_MONTH, i);
            long t = c.getTimeInMillis();
            if (t <= System.currentTimeMillis()) continue;
            if (days != null && days.length > 0 && !contains(days, c.get(Calendar.DAY_OF_WEEK))) continue;
            scheduleNext(context, t, label);
        }
    }

    public static void scheduleNext(Context context, long triggerAt, String label) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = buildServicePending(context, triggerAt, label);
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }

    /** 每个触发时间生成独立requestCode，互不覆盖；可复算——取消时用同一算法即可精确取消 */
    private static PendingIntent buildServicePending(Context context, long triggerAt, String label) {
        Intent it = new Intent(context, AlarmRingService.class);
        it.putExtra("label", label);
        int code = REQ_CODE_BASE + (int) ((triggerAt / 60000) % 9000);
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            return PendingIntent.getForegroundService(context, code, it,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        }
        return PendingIntent.getService(context, code, it,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static String join(int[] arr) {
        if (arr == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(arr[i]);
        }
        return sb.toString();
    }

    private static int[] parseDays(String s) {
        String[] parts = s.split(",");
        int[] out = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            out[i] = Integer.parseInt(parts[i].trim());
        }
        return out;
    }

    private static boolean contains(int[] arr, int v) {
        for (int x : arr) if (x == v) return true;
        return false;
    }
}