# Material Design UI Update - 06/10/2025

## Tổng quan
Cập nhật toàn diện UI/UX của app với Material Design 3, thay icon app bằng hình Ganyu, và cải thiện giao diện tổng thể.

## 🎨 Thay đổi chính

### 1. Icon App - Ganyu Theme ✅
**File:** `ganyutss.png` → Tất cả mipmap densities

Đã tạo icons cho:
- `mipmap-mdpi` (48x48)
- `mipmap-hdpi` (72x72)
- `mipmap-xhdpi` (96x96)
- `mipmap-xxhdpi` (144x144)
- `mipmap-xxxhdpi` (192x192)

Bao gồm:
- ✅ `ic_launcher.png` - Icon chính
- ✅ `ic_launcher_round.png` - Icon tròn
- ✅ `ic_launcher_foreground.png` - Adaptive icon foreground

**Script:** `generate_icons.py` - Tự động generate tất cả sizes

### 2. Color Palette - Material Design 3 🎨
**File:** `res/values/colors.xml`

**Primary Colors (Blue - Google Maps inspired):**
```xml
<color name="primary">#1976D2</color>
<color name="primary_variant">#1565C0</color>
<color name="primary_light">#42A5F5</color>
```

**Secondary Colors (Teal accent):**
```xml
<color name="secondary">#00ACC1</color>
<color name="secondary_variant">#00838F</color>
<color name="secondary_light">#26C6DA</color>
```

**Dark Theme Colors:**
```xml
<color name="dark_background">#121212</color>
<color name="dark_surface">#1E1E1E</color>
<color name="dark_surface_variant">#2C2C2C</color>
```

### 3. Themes - Material Design 3 🌙
**Files:** 
- `res/values/themes.xml` - Light theme
- `res/values-night/themes.xml` - Dark theme

**Đặc điểm:**
- Hỗ trợ DayNight tự động
- Rounded corners cho tất cả components
- Elevation và shadow đúng chuẩn Material
- Custom button, card, bottom navigation styles

### 4. Layouts - Material Components 📐

#### Fragment Home (`fragment_home.xml`)
**Cải thiện:**
- ✅ **MaterialCardView** cho navigation info
  - Corner radius: 16dp
  - Elevation: 4dp
  - Dark surface background
  - Padding: 24dp

- ✅ **Gradient Overlay** trên background image
  - Tăng contrast cho text
  - Smooth transition

- ✅ **Info Layout** được tổ chức lại:
  - Turn icon: 80x80, tinted với primary_light
  - Road name: 24sp, bold, white
  - Additional info: 16sp, secondary_light
  - Distance: 32sp, bold, prominent
  - ETA & Speed: Horizontal layout với labels

- ✅ **MaterialButton** cho Disconnect:
  - OutlinedButton style
  - Red color (error)
  - Corner radius: 20dp
  - Icon included

#### Device Selection (`activity_ble_selection.xml`)
**Cải thiện:**
- ✅ Scan button → MaterialButton
  - Rounded corners: 24dp
  - Search icon
  - Larger padding (14dp vertical)

#### Device Row Item (`device_row_item.xml`)
**Cải thiện:**
- ✅ FrameLayout → MaterialCardView
  - Corner radius: 12dp
  - Elevation: 2dp
  - Margins: 8dp horizontal, 4dp vertical
  - Ripple effect

- ✅ Thêm Bluetooth icon (40x40)
  - Tinted với primary color
  - Aligned left

- ✅ Text styling:
  - Device name: 18sp, bold
  - MAC address: 14sp, grey

- ✅ Removed bottom divider line

#### Bottom Navigation
**Cải thiện:**
- ✅ Surface background với elevation: 8dp
- ✅ Color selector cho selected/unselected states
- ✅ Label visibility: labeled (always show)

### 5. Vector Icons 🎯
**New icons created:**

**`ic_navigation.xml`** - Compass icon for Home tab:
```xml
<path android:pathData="M12,10.9c-0.61,0 -1.1,0.49..."/>
```

**`ic_settings.xml`** - Gear icon for Settings tab:
```xml
<path android:pathData="M19.14,12.94c0.04,-0.3..."/>
```

**`ic_home.xml`** - House icon (backup):
```xml
<path android:pathData="M12,5.69l5,4.5V18h-2v-6..."/>
```

**Color Selector:** `res/color/bottom_nav_color.xml`
- Selected: `@color/navigation_selected` (#1976D2)
- Unselected: `@color/navigation_unselected` (#9E9E9E)

### 6. Drawables 🖼️
**`gradient_overlay.xml`:**
```xml
<gradient
    android:angle="90"
    android:startColor="#00000000"
    android:centerColor="#30000000"
    android:endColor="#80000000" />
```

Tạo overlay tối dần từ trên xuống dưới để text dễ đọc hơn.

## 📁 Files đã thay đổi

### Created (12 files):
1. `generate_icons.py` - Script tạo icons
2. `res/color/bottom_nav_color.xml` - Color selector
3. `res/drawable/gradient_overlay.xml` - Gradient overlay
4. `res/drawable/ic_home.xml` - Home icon
5. `res/drawable/ic_settings.xml` - Settings icon
6. `res/drawable/ic_navigation.xml` - Navigation icon
7-11. App icons (15 files total cho tất cả densities)

### Modified (7 files):
1. `res/values/colors.xml` - Material 3 color palette
2. `res/values/themes.xml` - Light theme
3. `res/values-night/themes.xml` - Dark theme
4. `res/layout/fragment_home.xml` - Navigation UI
5. `res/layout/activity_ble_selection.xml` - Device selection
6. `res/layout/device_row_item.xml` - Device list item
7. `res/menu/bottom_nav_menu.xml` - Bottom nav icons

## 🎯 Material Design Guidelines Applied

✅ **Shape:**
- Small components: 4dp corners
- Medium components: 8dp corners
- Large components: 12dp corners
- Cards: 12-16dp corners

✅ **Elevation:**
- Bottom navigation: 8dp
- Cards: 2-4dp
- Buttons: Follow Material defaults

✅ **Typography:**
- Titles: 24sp bold
- Subtitles: 16-18sp
- Body: 14-16sp
- Labels: 12sp

✅ **Spacing:**
- Container padding: 16-24dp
- Element spacing: 8-16dp
- Icon size: 24dp (nav), 40dp (list), 80dp (featured)

✅ **Colors:**
- Contrast ratio ≥ 4.5:1 for text
- Primary color cho actions
- Secondary color cho accents
- Error color cho destructive actions

## 🚀 Cách build và test

### Build app:
```bash
cd android-app
./gradlew assembleDebug
```

### Install:
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Kiểm tra:
1. ✅ Icon app đã đổi thành Ganyu
2. ✅ Bottom navigation có icons mới và màu đẹp hơn
3. ✅ Home screen có card navigation info đẹp
4. ✅ Device list items là cards với Bluetooth icon
5. ✅ Scan button có icon và rounded
6. ✅ Dark theme tự động theo system setting
7. ✅ Màu sắc nhất quán theo Material Design

## 📸 Thay đổi UI trước/sau

### Icon App:
- ❌ Trước: Icon mặc định Android (xanh lá)
- ✅ Sau: Ganyu cute icon (tất cả resolutions)

### Bottom Navigation:
- ❌ Trước: Icons đen trắng đơn giản
- ✅ Sau: Vector icons đẹp, màu blue khi selected

### Home Screen:
- ❌ Trước: Text trắng flat trên background
- ✅ Sau: Material card với gradient overlay, layout organized

### Device List:
- ❌ Trước: Text list với divider line
- ✅ Sau: Material cards với Bluetooth icon, no dividers

### Theme:
- ❌ Trước: Grey theme đơn điệu
- ✅ Sau: Blue/Teal Material theme, dark theme support

## 🎨 Color Reference

| Màu | Hex | Dùng cho |
|-----|-----|----------|
| Primary | #1976D2 | Main actions, selected items |
| Primary Light | #42A5F5 | Icons, accents |
| Secondary | #00ACC1 | Additional info, highlights |
| Error | #D32F2F | Disconnect, warnings |
| Success | #4CAF50 | Connected status |
| Dark Background | #121212 | App background (dark) |
| Dark Surface | #1E1E1E | Cards, sheets (dark) |

## ✨ Best Practices Implemented

1. **Consistency:** Tất cả corners đều rounded theo Material guideline
2. **Accessibility:** Contrast ratio đủ cho text on background
3. **Responsiveness:** Layout work với mọi screen sizes
4. **Performance:** Vector icons thay vì PNG (nhẹ hơn)
5. **Maintainability:** Colors và styles ở centralized files
6. **UX:** Visual hierarchy rõ ràng (size, weight, color)

## 🔧 Next Steps (Optional)

Nếu muốn tinh chỉnh thêm:
1. Add animation transitions giữa screens
2. Add splash screen với Ganyu icon
3. Add custom font (Roboto variants)
4. Add more illustrations/empty states
5. Add Material Motion transitions

---
**Version:** 1.1  
**Date:** 06/10/2025  
**Design System:** Material Design 3  
**Theme:** Ganyu x Google Maps
