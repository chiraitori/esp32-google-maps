# 🚗 ESP32 Google Maps Navigation Display

Dự án hiển thị thông tin chỉ đường từ **Waze** hoặc **Google Maps** lên màn hình LCD ESP32 qua Bluetooth.

## 🎯 Tính năng

### ✅ Đã hoàn thành:
- **Hỗ trợ 2 apps navigation:**
  - ✅ Google Maps
  - ✅ Waze (mới thêm!)
- **Auto-detect:** Tự động nhận từ app nào đang navigate
- **Hiển thị thông tin:**
  - Tên đường tiếp theo
  - Khoảng cách đến điểm rẽ
  - ETA (thời gian đến)
  - Turn icon (biểu tượng rẽ)
  - Tốc độ hiện tại
  - Cảnh báo quá tốc độ
- **ESP32 LCD Display:**
  - Màn hình 320x172 hoặc 172x320
  - LVGL GUI framework
  - Light/Dark theme
  - Brightness control

## 📦 Cấu trúc Project

```
esp32-google-maps/
├── android-app/           # Android app để đọc navigation data
│   └── app/src/main/
│       ├── java/com/maisonsmd/catdrive/
│       │   ├── lib/
│       │   │   ├── GMapsNotification.kt      # Parser Google Maps
│       │   │   ├── WazeNotification.kt       # Parser Waze ⭐ MỚI
│       │   │   └── NavigationData.kt
│       │   └── service/
│       │       ├── NavigationListener.kt     # Main listener
│       │       └── BleService.kt             # BLE communication
│       └── res/
│           └── xml/root_preferences.xml      # Settings UI
│
└── esp32/                 # ESP32 firmware
    ├── esp32.ino          # Main sketch
    ├── ui.h               # LVGL UI
    ├── ble.h              # BLE handler
    ├── lcd.cpp/h          # LCD driver
    └── *.c                # Font files (1.3MB!)
```

## 🚀 Quick Start

### 1️⃣ Build Android App

```bash
cd android-app
./gradlew assembleDebug
```

Hoặc dùng Android Studio: **Build → Build APK(s)**

### 2️⃣ Flash ESP32

1. Mở `esp32/esp32.ino` trong Arduino IDE
2. **QUAN TRỌNG:** Đổi Partition Scheme:
   ```
   Tools → Partition Scheme → "Huge APP (3MB No OTA/1MB SPIFFS)"
   ```
3. Upload lên ESP32

### 3️⃣ Cài đặt App

1. Install APK lên điện thoại
2. Cấp quyền:
   - ✅ Notification access
   - ✅ Location
   - ✅ Bluetooth
3. Trong Settings:
   - **Navigation app**: Google Maps (Waze không hỗ trợ)
   - Bật **Run services**
   - Connect với ESP32 device

### 4️⃣ Sử dụng

1. Mở **Google Maps**
2. Bắt đầu navigation
3. Thông tin sẽ hiển thị trên màn hình ESP32! 🎉

> ⚠️ **Lưu ý:** Chỉ Google Maps được hỗ trợ. Waze không expose navigation data qua notification.

## 📱 App Settings

Trong app CatDrive → Settings:

| Setting | Mô tả |
|---------|-------|
| **Navigation app** | Google Maps (chỉ app này hỗ trợ) |
| **Run services** | Bật/tắt service nhận navigation data |
| **LCD light theme** | Chuyển theme sáng/tối cho ESP32 |
| **Display brightness** | Điều chỉnh độ sáng màn hình (0-100) |
| **Speed warning limit** | Giới hạn tốc độ để cảnh báo (km/h) |
| **Select a device** | Chọn ESP32 device để kết nối |

## 🔧 Troubleshooting

### ❌ ESP32: "Sketch too big" error

**Giải pháp:**
1. Tools → Partition Scheme → **"Huge APP (3MB)"**
2. Tools → Optimize → **"Optimize for size (-Os)"**

📖 **Chi tiết:** Xem file `GIAM_DUNG_LUONG.md`

### ❌ Google Maps không hoạt động

**Giải pháp:**
- Dùng **Waze** thay thế (đã được optimize)
- Waze có fallback parser ổn định hơn

📖 **Chi tiết:** Xem file `WAZE_SUPPORT.md`

### ❌ App không nhận navigation data

**Checklist:**
- [ ] Đã cấp quyền Notification access?
- [ ] Service đã được bật?
- [ ] Đã kết nối ESP32?
- [ ] App navigation đang active navigate?
- [ ] Chọn đúng app trong Settings?

## 🛠️ Hardware Requirements

### ESP32 Board
- ESP32-S2/S3 hoặc ESP32 DevKit
- Flash: **4MB minimum** (recommend 8MB)
- RAM: 320KB

### LCD Display
- ST7789 display (320x172 hoặc 172x320)
- SPI interface
- Backlight control

### Kết nối:
```
ESP32          ST7789 LCD
-----          ----------
GPIO_SPI_MOSI  → SDA
GPIO_SPI_CLK   → SCL
PIN_LCD_CS     → CS
PIN_LCD_DC     → DC
PIN_LCD_RST    → RST
PIN_BACKLIGHT  → BL
```

Xem chi tiết trong `esp32/config.h`

## 📚 Documentation

- **GIAM_DUNG_LUONG.md** - Hướng dẫn giải quyết lỗi "Sketch too big"
- **WAZE_SUPPORT.md** - Chi tiết về Waze & Google Maps support
- **LICENSE** - MIT License

## 🎨 Screenshots

*(Thêm screenshots của app và ESP32 display)*

## 🔮 Roadmap

### Đã hoàn thành ✅
- [x] Google Maps support
- [x] Waze support
- [x] Auto-detect navigation app
- [x] ESP32 LCD display
- [x] BLE communication
- [x] Speed warning
- [x] Light/Dark theme

### Sắp tới 🚧
- [ ] HERE WeGo support
- [ ] Apple Maps (nếu có trên Android)
- [ ] Custom parsing rules
- [ ] OTA firmware update
- [ ] Battery level display
- [ ] Weather info

## 🤝 Contributing

Contributions are welcome! 

### Thêm support cho navigation app mới:

1. Tạo class parser mới (theo template `WazeNotification.kt`)
2. Update `NavigationListener.kt` để detect app
3. Thêm option vào Settings UI
4. Test và tạo Pull Request

## 📄 License

MIT License - xem file `LICENSE`

## 👨‍💻 Author

**maisonsmd**

## ⭐ Credits

- **LVGL** - GUI framework cho ESP32
- **SimpleSt7789** - ST7789 LCD driver
- **Android NotificationListenerService** - Notification access API

---

## 🆘 Support

Nếu gặp vấn đề:
1. Kiểm tra **Troubleshooting** section ở trên
2. Đọc file `GIAM_DUNG_LUONG.md` hoặc `WAZE_SUPPORT.md`
3. Tạo Issue trên GitHub
4. Check logs trong Android Logcat

---

**Enjoy your navigation display!** 🚗💨🗺️
