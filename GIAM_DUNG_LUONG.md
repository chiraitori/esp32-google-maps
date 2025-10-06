# 🔧 Hướng dẫn giảm dung lượng ESP32 Sketch

## ⚠️ Vấn đề hiện tại
```
Sketch uses 1,665,313 bytes (127%) of program storage space. Maximum is 1,310,720 bytes.
❌ VỰT QUÁ 354KB (27%)
```

## 🎯 Giải pháp (Làm theo thứ tự từ dễ đến khó)

---

## ✅ PHƯƠNG ÁN 1: ĐỔI PARTITION SCHEME (KHUYÊN DÙNG)

### Cách làm:
1. Mở Arduino IDE
2. Chọn board ESP32 của bạn
3. **Tools → Partition Scheme → "Huge APP (3MB No OTA/1MB SPIFFS)"**
4. Upload lại

### Các tùy chọn partition:
- **Huge APP (3MB No OTA/1MB SPIFFS)** - 3MB cho code ✅ Chọn cái này
- **Minimal SPIFFS (1.9MB APP with OTA/190KB SPIFFS)** - 1.9MB cho code
- **No OTA (2MB APP/2MB SPIFFS)** - 2MB cho code

### Ưu điểm:
- ✅ Dễ nhất, chỉ 1 click
- ✅ Không phải sửa code
- ✅ Hiệu quả 100%

---

## ✅ PHƯƠNG ÁN 2: TẮT LOGGING (Tiết kiệm ~50-100KB)

### Sửa file `lv_conf.h`:

```cpp
// Dòng 278: Tắt hẳn log
#define LV_USE_LOG 0

// HOẶC giảm log level
#define LV_LOG_LEVEL LV_LOG_LEVEL_ERROR  // Chỉ log lỗi
```

### Sửa file `config.h` (nếu có):
```cpp
#define CORE_DEBUG_LEVEL 0  // Tắt debug ESP32
```

---

## ✅ PHƯƠNG ÁN 3: GIẢM SỐ FONT (Tiết kiệm ~200-800KB)

### Phân tích font hiện tại:
| File | Kích thước | Sử dụng |
|------|-----------|---------|
| montserrat_bold_32.c | 387KB | Khoảng cách đến đường tiếp theo |
| montserrat_semibold_28.c | 281KB | Tên đường tiếp theo |
| montserrat_number_bold_48.c | 252KB | Tốc độ |
| montserrat_semibold_24.c | 222KB | Mô tả đường |
| montserrat_24.c | 202KB | Đơn vị tốc độ, ETA |
| **TỔNG** | **1.34MB** | **90% dung lượng!** |

### Cách 3A: Xóa font không cần thiết

1. **XÓA:** `montserrat_bold_32.c` (387KB) 
   - Dùng `montserrat_semibold_28.c` thay thế

2. **Sửa file `ui.h`:**
```cpp
// Dòng 183: Thay bold_32 → semibold_28
lv_obj_set_style_text_font(lblDistanceToNextRoad, get_montserrat_semibold_28(), LV_STATE_DEFAULT);
```

3. **Xóa các file không dùng:**
```powershell
Remove-Item "d:\Code\esp32-google-maps\esp32\montserrat_bold_32.c"
```

4. **Sửa `local_fonts.h`:** Xóa dòng `get_montserrat_bold_32()`

### Cách 3B: Dùng font built-in của LVGL (Tiết kiệm 1.34MB!)

1. **Sửa `lv_conf.h` - Bật font có sẵn:**
```cpp
#define LV_FONT_MONTSERRAT_24 1  // Thay montserrat_24.c
#define LV_FONT_MONTSERRAT_28 1  // Thay montserrat_semibold_28.c
#define LV_FONT_MONTSERRAT_32 1  // Thay montserrat_bold_32.c
#define LV_FONT_MONTSERRAT_48 1  // Thay montserrat_number_bold_48.c
```

2. **Sửa `ui.h` - Dùng font built-in:**
```cpp
// Thay tất cả:
get_montserrat_24()              → &lv_font_montserrat_24
get_montserrat_semibold_24()     → &lv_font_montserrat_24
get_montserrat_semibold_28()     → &lv_font_montserrat_28
get_montserrat_bold_32()         → &lv_font_montserrat_32
get_montserrat_number_bold_48()  → &lv_font_montserrat_48
```

3. **Xóa TẤT CẢ file .c:**
```powershell
Remove-Item "d:\Code\esp32-google-maps\esp32\montserrat_*.c"
```

4. **Xóa `local_fonts.h`** (không cần nữa)

⚠️ **Lưu ý:** Font built-in chỉ có ASCII + ký tự Latin cơ bản (không có tiếng Việt có dấu)

---

## ✅ PHƯƠNG ÁN 4: GIẢM LVGL FEATURES (Tiết kiệm ~100-200KB)

### Sửa `lv_conf.h`:

```cpp
// Tắt các tính năng không dùng
#define LV_USE_LOG 0                    // Đã nói ở trên
#define LV_USE_ASSERT_MALLOC 0          // Tắt assert
#define LV_USE_ASSERT_NULL 0
#define LV_USE_OBJ_PROPERTY 0           // Tắt property system
#define LV_USE_OBJ_ID 0                 // Tắt object ID

// Widgets không dùng (nếu có)
#define LV_USE_ANIMIMG 0
#define LV_USE_CALENDAR 0
#define LV_USE_CHART 0
#define LV_USE_KEYBOARD 0
#define LV_USE_LIST 0
#define LV_USE_MENU 0
#define LV_USE_SLIDER 0
#define LV_USE_TEXTAREA 0
// ... tắt các widget không dùng
```

---

## ✅ PHƯƠNG ÁN 5: COMPILER OPTIMIZATION

### Cách 1: Trong Arduino IDE
Tools → **Optimize → "Optimize for size (-Os)"**

### Cách 2: Tạo file `platform.local.txt`

**Vị trí:** `C:\Users\Asus\AppData\Local\Arduino15\packages\esp32\hardware\esp32\<version>\platform.local.txt`

**Nội dung:**
```properties
# Optimize for size
compiler.c.extra_flags=-Os -DCORE_DEBUG_LEVEL=0 -DDEBUG_ESP_PORT=
compiler.cpp.extra_flags=-Os -DCORE_DEBUG_LEVEL=0 -DDEBUG_ESP_PORT=
compiler.c.elf.extra_flags=-Wl,--gc-sections

# Remove unused functions
compiler.c.extra_flags=-ffunction-sections -fdata-sections
compiler.cpp.extra_flags=-ffunction-sections -fdata-sections
```

---

## ✅ PHƯƠNG ÁN 6: TẠO LẠI FONT VỚI RANGE NHỎ HƠN

### Dùng LVGL Font Converter online:
https://lvgl.io/tools/fontconverter

### Thiết lập:
1. **Font:** Montserrat
2. **Size:** 24, 28, 32, 48
3. **Bpp:** 1 bit (thay vì 4 bit) - Giảm 75% dung lượng
4. **Range:** 
   - `0x20-0x7E` (ASCII cơ bản)
   - `0x30-0x39` (Chỉ số, dùng cho font số)
5. **Format:** C array

Thay thế các file `.c` hiện tại.

---

## 📊 So sánh hiệu quả:

| Phương án | Tiết kiệm | Độ khó | Khuyên dùng |
|-----------|-----------|--------|-------------|
| 1. Đổi partition | 0KB (tăng giới hạn) | ⭐ | ✅✅✅ |
| 2. Tắt logging | ~50-100KB | ⭐ | ✅✅ |
| 3A. Xóa 1 font | ~387KB | ⭐⭐ | ✅✅ |
| 3B. Dùng font built-in | ~1.34MB | ⭐⭐⭐ | ✅ (mất tiếng Việt) |
| 4. Giảm LVGL | ~100-200KB | ⭐⭐⭐ | ✅ |
| 5. Compiler opt | ~50-150KB | ⭐⭐ | ✅✅ |
| 6. Tạo lại font | ~75% font size | ⭐⭐⭐⭐ | ✅ (tốn thời gian) |

---

## 🚀 KHUYẾN NGHỊ CUỐI CÙNG:

### Giải pháp nhanh (5 phút):
1. ✅ Đổi partition → **Huge APP (3MB)**
2. ✅ Tools → Optimize → **"Optimize for size (-Os)"**
3. ✅ Upload → XONG!

### Giải pháp tối ưu (30 phút):
1. ✅ Đổi partition → Huge APP
2. ✅ Tắt logging trong `lv_conf.h`
3. ✅ Xóa `montserrat_bold_32.c`, dùng semibold_28 thay thế
4. ✅ Compiler optimization
5. ✅ Upload → Còn dư >1MB!

### Giải pháp cực đoan (nếu cần):
- Dùng font built-in LVGL (mất tiếng Việt có dấu)
- Hoặc tạo lại font với 1-bit depth

---

## 🔍 Kiểm tra kết quả:

Sau khi compile, xem dòng:
```
Sketch uses XXXXXX bytes (XX%) of program storage space.
```

**Mục tiêu:** < 90% (< 1,179,648 bytes nếu dùng partition 1.3MB)

---

## ❓ Cần giúp thêm?

Nếu vẫn lỗi, cho tôi biết:
1. Đã thử phương án nào?
2. Kích thước sau khi compile (XX bytes, XX%)
3. Board ESP32 cụ thể (ESP32-S3, C3, ...)?
