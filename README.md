CatDrive is a fun project that displays Google Map navigation on a small screen that I attach to my motorbike.

# How it works

The Android app must be installed and running on your phone, it will read the current navigation data from Google Maps and send it to the ESP32 over Bluetooth Low Energy (BLE).

## Hardware

You will need a ESP32-C6 (or other with BLE capabilities) with a small screen.

Mine is [ESP32-C6 1.47inch Display Development Board](https://www.waveshare.com/esp32-c6-lcd-1.47.htm) from Waveshare. If you use a different screen, you will need to adapt the code to your screen.

## Software

### ESP32

- The code is built with Arduino (v2), you need to install the ESP32 board support in Arduino IDE first.
- Install the required libraries: `lvgl`
- Copy `lv_conf.h` to Arduino's libraries folder (not inside `lvgl` folder).
- Make sure the screen works by using the example sketch provided by [Waveshare](https://www.waveshare.com/wiki/ESP32-C6-LCD-1.47)
- Build the code and upload it to your ESP32.

### Android

Currently I make the app specificly for my Pixel 4a phone running Android 13, so most APIs are level 33 or above.

- Install Android Studio and SDK for Android 13.
- Open `android-app` folder in Android Studio.


## Modifications

### Font

