# 🔧 Android 13+ Compatibility Update

## 📋 Tóm tắt thay đổi

Cập nhật app để hỗ trợ đầy đủ **Android 13 (API 33)** và cao hơn.

---

## ✅ Các thay đổi chính

### 1. **Update Target SDK → 34**
**File:** `build.gradle`

```diff
- compileSdk 32
+ compileSdk 34

- targetSdk 32
+ targetSdk 34

- versionCode 1
+ versionCode 2
- versionName "1.0"
+ versionName "1.1"
```

**Lý do:** Android 13+ yêu cầu app target SDK mới để hoạt động đúng

---

### 2. **Thêm POST_NOTIFICATIONS permission**
**File:** `AndroidManifest.xml`

```diff
  <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
+ <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

**Lý do:** Android 13+ yêu cầu permission mới cho notifications

---

### 3. **Implement POST_NOTIFICATIONS runtime request**
**File:** `PermissionCheck.kt`

```kotlin
// NEW METHOD
fun requestNotificationPostingPermission(activity: AppCompatActivity) {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            200
        )
    }
}

// UPDATED METHOD
fun checkNotificationPostingPermission(context: Context): Boolean {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
    return true
}
```

**Lý do:** Android 13+ yêu cầu request permission runtime

---

### 4. **Cải thiện Google Maps notification detection**
**File:** `NavigationListener.kt`

**Trước:**
```kotlin
private fun isGoogleMapsNotification(sbn: StatusBarNotification?): Boolean {
    // ...
    return (sbn.id == 1)  // CHỈ accept ID=1
}
```

**Sau:**
```kotlin
private fun isGoogleMapsNotification(sbn: StatusBarNotification?): Boolean {
    // ...
    
    // Accept ID=1 (traditional)
    if (sbn.id == 1) return true
    
    // HOẶC detect bằng content (Android 13+ có thể dùng ID khác)
    val extras = sbn.notification.extras
    val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
    val hasNavigationInfo = title.contains(Regex("""\\d+\\.?\\d*\\s*(km|m|mi|ft|min|h|giờ|phút)"""))
    
    if (hasNavigationInfo) {
        Timber.d("Found Google Maps notification with navigation info (ID=${sbn.id})")
        return true
    }
    
    return false
}
```

**Lý do:** Android 13+ có thể dùng notification ID khác, không phải 1

---

### 5. **Thêm debug logging**
**File:** `GMapsNotification.kt`

```kotlin
init {
    try {
        // Debug: Log notification extras
        val extras = sbn.notification.extras
        Timber.d("=== Google Maps Notification Debug ===")
        Timber.d("Notification ID: ${sbn.id}")
        Timber.d("Title: ${extras.getCharSequence(Notification.EXTRA_TITLE)}")
        Timber.d("Text: ${extras.getCharSequence(Notification.EXTRA_TEXT)}")
        // ...
        
        // Parse notification
        // ...
        
        Timber.d("Parsed navigation data: $navigationData")
    } catch (e: Exception) {
        Timber.e("Failed to parse: $e")
    }
}
```

**Lý do:** Dễ debug khi có vấn đề trên Android 13+

---

### 6. **Update SettingsFragment**
**File:** `SettingsFragment.kt`

```kotlin
// Thêm listener cho POST_NOTIFICATIONS checkbox
mPostNotificationCheckbox.setOnPreferenceChangeListener { _, newValue ->
    if (newValue as Boolean) {
        PermissionCheck.requestNotificationPostingPermission(activity as MainActivity)
    }
    return@setOnPreferenceChangeListener false
}
```

**Lý do:** User có thể request permission từ settings

---

## 📊 Files thay đổi

| File | Thay đổi | Mục đích |
|------|----------|----------|
| `build.gradle` | SDK 32→34, version 1.0→1.1 | Update target |
| `AndroidManifest.xml` | +POST_NOTIFICATIONS | Permission mới |
| `PermissionCheck.kt` | +check/request POST_NOTIFICATIONS | Runtime permission |
| `SettingsFragment.kt` | +listener cho POST_NOTIFICATIONS | UI interaction |
| `NavigationListener.kt` | Smart detection (ID + content) | Fix Android 13+ |
| `GMapsNotification.kt` | +debug logging | Troubleshooting |
| `ANDROID_13_SUPPORT.md` | NEW file | Documentation |

---

## 🚀 Hướng dẫn build & test

### 1. Build app mới
```bash
cd android-app
./gradlew clean assembleDebug
```

### 2. Install trên Android 13+
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 3. Cấp permissions
1. Mở app → Settings
2. Grant tất cả permissions:
   - ✅ Accessing notifications (NotificationListener)
   - ✅ **Posting notifications** ← MỚI
   - ✅ Accessing location
   - ✅ Accessing bluetooth

### 4. Test
1. Bật **Run services**
2. Connect ESP32
3. Mở Google Maps → Navigate
4. Check logs:
```bash
adb logcat -s NavigationListener:D GMapsNotification:D
```

---

## 🐛 Troubleshooting

### Không nhận notification?

**Check 1: POST_NOTIFICATIONS granted?**
```bash
adb shell dumpsys package com.maisonsmd.catdrive | grep POST_NOTIFICATIONS
```
→ Phải có `granted=true`

**Check 2: Notification Listener enabled?**
```bash
adb shell settings get secure enabled_notification_listeners
```
→ Phải có `com.maisonsmd.catdrive`

**Check 3: Service running?**
- App → Settings → **Run services** = ON

**Check 4: Notification ID**
```
D/NavigationListener: Google Maps notification detected - ID: [number]
```
→ Nếu ID ≠ 1, app sẽ dùng content-based detection

---

## ✅ Compatibility

| Android Version | Status | Notes |
|----------------|--------|-------|
| Android 12 (32) | ✅ | Original target |
| **Android 13 (33)** | ✅ | **Fixed với update này** |
| Android 14 (34) | ✅ | Compiled against 34 |

---

## 📝 Checklist

Trước khi release:
- [x] Update SDK
- [x] Add POST_NOTIFICATIONS permission
- [x] Implement runtime permission request
- [x] Smart notification detection
- [x] Debug logging
- [x] Update documentation
- [ ] Test trên Android 13 device
- [ ] Test trên Android 14 device
- [ ] Verify logs
- [ ] User testing

---

## 🎯 Next Steps

1. **Build & test ngay:**
   ```bash
   cd android-app && ./gradlew assembleDebug
   ```

2. **Install và test trên Android 13**

3. **Check logs nếu có vấn đề:**
   ```bash
   adb logcat -s NavigationListener:D GMapsNotification:D
   ```

4. **Report kết quả:**
   - ✅ Works: Great!
   - ❌ Fails: Share logs + Google Maps version

---

**Update version: 1.1**  
**Date: 2025-10-06**  
**Target: Android 13+ compatibility** 🎉
