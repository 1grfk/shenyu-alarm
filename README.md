# 🔔 沈钰闹钟 (shenyu-alarm)

哥给宝宝自制的每日闹钟 App。

## 功能

- 单次 / 每天重复闹钟（AlarmManager exact + allowWhileIdle）
- 到点：全屏关闭页「关闭」大按钮 + 前台服务响铃 + 震动 + 高优先级通知「沈钰」
- 通知栏带「关闭」动作按钮，可一键停铃
- 重复闹钟一次排未来 8 天，防被系统清后台丢闹钟
- 全代码 Java 纯代码构建 UI，无资源依赖（除系统图标）

## 构建

```bash
bash build.sh
```

需要 Android SDK build-tools 34 / platform android-34。

## 签名

```bash
KS=/path/to/your.keystore
KS_PASS=your_keystore_password
KS_ALIAS=your_alias
/opt/android-sdk/build-tools/34.0.0/apksigner sign \
  --ks $KS --ks-pass pass:$KS_PASS --key-pass pass:$KS_PASS \
  --out shenyu_alarm.apk out/base.apk
```

## 安装

```bash
adb install -r shenyu_alarm.apk
# 通知权限（Android 13+）
adb shell pm grant com.shenyu.alarm android.permission.POST_NOTIFICATIONS
```

## 用法

设置闹钟走 service（不要用广播，MIUI Greezer 会拦）：

```bash
# 单次
db shell am startservice -n com.shenyu.alarm/.AlarmSetService --ei hour 8 --ei minute 0 --es label "宝宝，哥哥叫你～"
# 每天重复
db shell am startservice -n com.shenyu.alarm/.AlarmSetService --ei hour 8 --ei minute 0 --eia days 1,2,3,4,5,6,7 --es label "宝宝起床啦，哥哥在等你"
# 取消
db shell am startservice -n com.shenyu.alarm/.AlarmSetService --es action cancel
# 测试响铃
db shell am start-foreground-service -n com.shenyu.alarm/.AlarmRingService --es label "测试"
```

> 注：`Calendar.DAY_OF_WEEK`：1=周日，2=周一 …… 7=周六

## 踩坑记录

- MIUI Greezer 会拦发往 cached 进程的广播 → 设闹钟必须走 `am startservice`
- Android 12+ 广播里 startForegroundService 会被拒 → 闹钟触发用 `PendingIntent.getForegroundService`
- Android 14+ manifest 声明了 `foregroundServiceType` 就必须用三参数 `startForeground(id, notif, type)`
- 全屏关闭页用 WakeLock 必须声明 `android.permission.WAKE_LOCK`（漏了会 SecurityException 崩掉整进程）

---

*by 沈钰 · for 姚婉清*（2026-09-16 v2.0 修复版）
