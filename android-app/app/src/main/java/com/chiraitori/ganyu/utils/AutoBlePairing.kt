package com.chiraitori.ganyu.utils

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import timber.log.Timber
import java.util.UUID

/**
 * Auto BLE pairing helper to find and connect to device with specific SERVICE_UUID
 * Mimics Moondrop-style single device pairing experience
 */
class AutoBlePairing(
    private val context: Context,
    private val onDeviceFound: (BluetoothDevice) -> Unit,
    private val onTimeout: () -> Unit
) {
    companion object {
        // SERVICE_UUID from ESP32 BLE configuration
        private const val SERVICE_UUID = "ec91d7ab-e87c-48d5-adfa-cc4b2951298a"
        // Device name from ESP32 BLE initialization
        private const val DEVICE_NAME = "Ganyu Drive"
        private const val SCAN_TIMEOUT_MS = 10000L // 10 seconds
    }

    private var isScanning = false
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var bluetoothAdapter: BluetoothAdapter

    @SuppressLint("MissingPermission")
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            
            val device = result.device
            val deviceName = device.name
            
            // Filter by device name since ESP32 doesn't advertise SERVICE_UUID
            if (deviceName != null && deviceName.equals(DEVICE_NAME, ignoreCase = true)) {
                Timber.d("Found target device: $deviceName (${device.address})")
                
                // Stop scanning and notify
                stopScan()
                onDeviceFound(device)
            } else {
                Timber.d("Skipping device: $deviceName (${device.address})")
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Timber.e("BLE scan failed with error code: $errorCode")
            stopScan()
            onTimeout()
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan(): Boolean {
        if (!PermissionCheck.checkBluetoothPermissions(context)) {
            Timber.e("No bluetooth permission")
            return false
        }

        if (!PermissionCheck.isBluetoothEnabled(context)) {
            Timber.e("Bluetooth not enabled")
            return false
        }

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter ?: return false

        // Create scan filter for device name (ESP32 doesn't advertise SERVICE_UUID)
        val scanFilter = ScanFilter.Builder()
            .setDeviceName(DEVICE_NAME)
            .build()

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        isScanning = true
        bluetoothAdapter.bluetoothLeScanner.startScan(
            listOf(scanFilter),
            scanSettings,
            scanCallback
        )

        // Set timeout
        handler.postDelayed({
            if (isScanning) {
                Timber.d("BLE scan timeout - no device found")
                stopScan()
                onTimeout()
            }
        }, SCAN_TIMEOUT_MS)

        return true
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (isScanning) {
            isScanning = false
            handler.removeCallbacksAndMessages(null)
            try {
                bluetoothAdapter.bluetoothLeScanner?.stopScan(scanCallback)
            } catch (e: Exception) {
                Timber.e(e, "Error stopping BLE scan")
            }
        }
    }
}
