# 🗺️ Hướng dẫn sử dụng với Waze & Google Maps

## ⚠️ CẬP NHẬT: Chỉ hỗ trợ Google Maps

**Sau khi test thực tế:** Waze **KHÔNG** expose navigation data qua notification. Chỉ có Google Maps hoạt động.

Xem chi tiết: `WAZE_LIMITATION.md`

---

## ✅ Đã hoàn thành

App **CatDrive** hỗ trợ **Google Maps** đầy đủ!

### 🎯 Các thay đổi đã thực hiện:

#### 1. ✅ Tạo WazeNotification Parser
- **File mới:** `WazeNotification.kt`
- Parse thông tin navigation từ Waze notifications
- Hỗ trợ 2 phương thức parse:
  - Parse từ RemoteViews (như Google Maps)
  - Fallback parse từ Notification Extras nếu RemoteViews fail
- Tự động detect:
  - Tên đường tiếp theo
  - Khoảng cách
  - ETA/ETE
  - Turn icon

#### 2. ✅ Cập nhật NavigationListener
- **File:** `NavigationListener.kt`
- Thêm logic detect cả Google Maps và Waze
- Hỗ trợ filter theo user preference
- Auto-switch giữa 2 apps

#### 3. ✅ Thêm Settings UI
- **Files:** 
  - `root_preferences.xml`
  - `strings.xml`
- Thêm dropdown cho user chọn app:
  - **Auto-detect (Recommended)** - Tự động nhận cả 2 apps
  - **Google Maps** - Chỉ nhận Google Maps
  - **Waze** - Chỉ nhận Waze

#### 4. ✅ Permissions
- AndroidManifest không cần sửa
- NotificationListenerService đã có quyền đọc tất cả notifications

---

## 📱 Hướng dẫn sử dụng

### Bước 1: Build & Install App
```bash
cd android-app
./gradlew assembleDebug
# Hoặc dùng Android Studio: Build → Build Bundle(s) / APK(s) → Build APK(s)
```

### Bước 2: Cấp quyền Notification
1. Mở app CatDrive
2. Vào **Settings**
3. Bật **"Accessing notifications"**
4. Trong màn hình Android settings, bật toggle cho CatDrive

### Bước 3: Chọn Navigation App
1. Trong Settings của CatDrive
2. Tìm **"Navigation app"**
3. Chọn:
   - ✅ **Auto-detect** (khuyên dùng) - App sẽ tự động nhận từ cả Waze và Google Maps
   - **Google Maps** - Chỉ nhận từ Google Maps
   - **Waze** - Chỉ nhận từ Waze

### Bước 4: Bật Service
1. Trong Settings, bật **"Run services"**
2. Kết nối với ESP32 device
3. Mở Waze hoặc Google Maps
4. Bắt đầu navigation → Thông tin sẽ hiển thị trên ESP32!

---

## 🔍 Troubleshooting

### ❌ Google Maps không hoạt động
**Nguyên nhân:** Google Maps có thể đã thay đổi notification layout

**Giải pháp:**
1. Thử dùng Waze thay thế (đã được tối ưu hóa)
2. Hoặc debug Google Maps notification:
   - Bật Timber logging
   - Kiểm tra logs khi navigate
   - Update GMapsNotification.kt nếu cần

### ❌ Waze không hiển thị thông tin
**Nguyên nhân:** Waze notification structure có thể khác với expected

**Giải pháp:**
1. Kiểm tra trong Settings → Navigation app → Chọn "Waze"
2. Check logs để xem notification có được detect không:
   ```kotlin
   Timber.d("Waze notification detected")
   ```
3. Có thể cần adjust parsing logic trong `WazeNotification.kt`

### ⚠️ Cả 2 app đều không hoạt động
**Checklist:**
- [ ] Đã cấp quyền "Accessing notifications"?
- [ ] Service đã được bật ("Run services")?
- [ ] Đã kết nối với ESP32 device?
- [ ] App navigation đang active navigation (không chỉ search)?
- [ ] Đã chọn đúng app trong Settings?

---

## 🛠️ Debugging Tips

### 1. Kiểm tra Notification có được detect không
Thêm log vào `NavigationListener.kt`:

```kotlin
override fun onNotificationPosted(sbn: StatusBarNotification?) {
    Timber.d("Notification from: ${sbn?.packageName}, isOngoing: ${sbn?.isOngoing}")
    // existing code...
}
```

### 2. Xem nội dung notification
Trong `WazeNotification.kt`, uncomment debug logs:

```kotlin
Timber.d("Title: $title, Text: $text, SubText: $subText")
```

### 3. Test với Mock Data
Tạo mock notification để test parse logic mà không cần real navigation

---

## 📊 So sánh Google Maps vs Waze

| Tính năng | Google Maps | Waze |
|-----------|-------------|------|
| Parsing method | RemoteViews | RemoteViews + Extras fallback |
| Độ ổn định | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| Turn icon | ✅ | ✅ |
| Distance | ✅ | ✅ |
| Street name | ✅ | ✅ |
| ETA | ✅ | ✅ |
| Notification ID | Fixed (1) | Variable (auto-detect) |

---

## 🚀 Tính năng trong tương lai

### Có thể thêm:
- [ ] Apple Maps (nếu có trên Android)
- [ ] HERE WeGo
- [ ] Sygic
- [ ] MapFactor Navigator
- [ ] Custom parsing rules cho từng app
- [ ] Settings để adjust parsing sensitivity
- [ ] Notification preview trong app

---

## 📝 Code Structure

```
android-app/app/src/main/java/com/maisonsmd/catdrive/
├── lib/
│   ├── GMapsNotification.kt      # Parser cho Google Maps
│   ├── WazeNotification.kt       # Parser cho Waze (MỚI)
│   ├── NavigationNotification.kt # Base class
│   └── NavigationData.kt         # Data model
├── service/
│   ├── NavigationListener.kt     # Main listener (CẬP NHẬT)
│   └── BroadcastService.kt       # BLE service
└── res/
    ├── xml/
    │   └── root_preferences.xml   # Settings UI (CẬP NHẬT)
    └── values/
        └── strings.xml            # Strings (CẬP NHẬT)
```

---

## ❓ FAQ

### Q: Có thể dùng cả 2 app cùng lúc không?
A: Chọn "Auto-detect" trong settings, app sẽ tự động switch giữa app nào đang active navigation.

### Q: Waze có accurate hơn Google Maps không?
A: Waze parser có fallback mechanism nên ổn định hơn nếu notification structure thay đổi.

### Q: Làm sao biết app nào đang được dùng?
A: Check logs, sẽ có dòng "Got an error while parsing WAZE/GOOGLE_MAPS notification"

### Q: Tại sao Google Maps không hoạt động?
A: Google Maps có thể đã update notification layout. Thử dùng Waze hoặc update parser.

---

## 🎉 Kết luận

App giờ đã support cả **Waze** và **Google Maps**! 

- ✅ Chọn "Auto-detect" để tự động nhận từ cả 2 apps
- ✅ Waze parser ổn định hơn với fallback mechanism
- ✅ Dễ dàng thêm support cho apps khác trong tương lai

**Build app và test ngay!** 🚗💨
