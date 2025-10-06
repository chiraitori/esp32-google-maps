from PIL import Image
import os

# Đọc ảnh gốc
input_image = r"d:\Code\esp32-google-maps\ganyutss.png"
img = Image.open(input_image).convert('RGBA')

# Các kích thước icon cho Android
icon_sizes = {
    'mdpi': 48,
    'hdpi': 72,
    'xhdpi': 96,
    'xxhdpi': 144,
    'xxxhdpi': 192
}

# Base path
base_path = r"d:\Code\esp32-google-maps\android-app\app\src\main\res"

# Tạo icons
for density, size in icon_sizes.items():
    # ic_launcher.png
    output_dir = os.path.join(base_path, f'mipmap-{density}')
    os.makedirs(output_dir, exist_ok=True)
    
    output_path = os.path.join(output_dir, 'ic_launcher.webp')
    resized = img.resize((size, size), Image.Resampling.LANCZOS)
    resized.save(output_path, 'WEBP', quality=90)
    print(f"Created: {output_path}")
    
    # ic_launcher_round.webp (same as regular for now)
    output_path_round = os.path.join(output_dir, 'ic_launcher_round.webp')
    resized.save(output_path_round, 'WEBP', quality=90)
    print(f"Created: {output_path_round}")

# Tạo foreground cho adaptive icon (108dp)
foreground_sizes = {
    'mdpi': 108,
    'hdpi': 162,
    'xhdpi': 216,
    'xxhdpi': 324,
    'xxxhdpi': 432
}

for density, size in foreground_sizes.items():
    output_dir = os.path.join(base_path, f'mipmap-{density}')
    output_path = os.path.join(output_dir, 'ic_launcher_foreground.webp')
    
    # Tạo ảnh vuông với padding cho adaptive icon
    foreground = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    # Scale ảnh gốc xuống 66% (theo guideline của adaptive icon)
    icon_size = int(size * 0.66)
    resized_icon = img.resize((icon_size, icon_size), Image.Resampling.LANCZOS)
    # Paste vào giữa
    offset = (size - icon_size) // 2
    foreground.paste(resized_icon, (offset, offset), resized_icon if resized_icon.mode == 'RGBA' else None)
    
    foreground.save(output_path, 'WEBP', quality=90)
    print(f"Created: {output_path}")

print("\n✅ All icons generated successfully!")
