package com.chiraitori.ganyu.ui.home

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.scale
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chiraitori.ganyu.MainActivity
import com.chiraitori.ganyu.R
import com.chiraitori.ganyu.databinding.FragmentHomeBinding
import com.chiraitori.ganyu.lib.BitmapHelper
import com.chiraitori.ganyu.lib.NavigationData
import com.chiraitori.ganyu.ui.ActivityViewModel
import com.chiraitori.ganyu.utils.AutoBlePairing
import com.chiraitori.ganyu.utils.ServiceManager
import timber.log.Timber


class HomeFragment : Fragment() {
    private var mUiBinding: FragmentHomeBinding? = null
    private var mDebugImage = false
    private var autoBlePairing: AutoBlePairing? = null
    private var foundDevice: BluetoothDevice? = null
    private var viewModel: ActivityViewModel? = null

    private val binding get() = mUiBinding!!

    private enum class UIState {
        EMPTY,              // No device found - show ripple circles
        DEVICE_FOUND,       // Device found - show connect button
        DEVICE_CONNECTED,   // Device connected but no navigation - show disconnect button
        NAVIGATING          // Device connected with navigation data - show navigation info
    }

    private fun updateUIState(state: UIState) {
        Timber.d("updateUIState: $state")
        
        binding.emptyStateLayout.visibility = if (state == UIState.EMPTY) View.VISIBLE else View.GONE
        binding.deviceFoundCard.visibility = if (state == UIState.DEVICE_FOUND) View.VISIBLE else View.GONE
        binding.deviceConnectedCard.visibility = if (state == UIState.DEVICE_CONNECTED) View.VISIBLE else View.GONE
        binding.deviceCard.visibility = if (state == UIState.NAVIGATING) View.VISIBLE else View.GONE
    }

    private fun displayNavigationData(data: NavigationData?) {
        val bitmap =
            if (!mDebugImage) data?.actionIcon?.bitmap
            else null

        binding.imgTurnIcon.setImageBitmap(bitmap)

        if (data == null) {
            // No navigation data - check if device is connected
            val isConnected = viewModel?.connectedDevice?.value != null
            if (isConnected) {
                // Device connected but no navigation data - show compact connected card
                updateUIState(UIState.DEVICE_CONNECTED)
                
                // Update connected device info
                val device = viewModel?.connectedDevice?.value
                binding.txtConnectedDeviceName.text = device?.name ?: "Unknown Device"
                binding.txtConnectedDeviceAddress.text = device?.address ?: "N/A"
            } else {
                // No device connected
                updateUIState(UIState.EMPTY)
            }
            return
        }

        // Show navigation card when navigation data is available
        updateUIState(UIState.NAVIGATING)
        binding.txtRoadName.text = data.nextDirection.nextRoad
        binding.txtRoadAdditionalInfo.text = data.nextDirection.nextRoadAdditionalInfo
        binding.txtDistance.text = data.nextDirection.distance
        binding.txtTime.text = data.eta.ete
    }

    private fun onDeviceFound(device: BluetoothDevice) {
        Timber.d("onDeviceFound called: ${device.name} (${device.address})")
        
        // Stop refresh animation
        mUiBinding?.swipeRefresh?.isRefreshing = false
        
        // Save found device
        foundDevice = device
        
        // Update UI to show device found card with connect button
        updateUIState(UIState.DEVICE_FOUND)
        
        // Update device info in card
        binding.txtFoundDeviceName.text = device.name ?: "Unknown Device"
        binding.txtFoundDeviceAddress.text = device.address
    }
    
    private fun connectToDevice() {
        val device = foundDevice ?: return
        
        Timber.d("Connecting to device: ${device.name} (${device.address})")
        
        Toast.makeText(
            requireContext(),
            "Connecting to ${device.name}...",
            Toast.LENGTH_SHORT
        ).show()
        
        val mainActivity = activity as? MainActivity
        if (mainActivity != null) {
            mainActivity.connectToDevice(device)
        } else {
            Timber.e("MainActivity is null, cannot connect to device")
        }
    }
    
    private fun disconnectDevice() {
        Timber.d("Disconnecting device")
        
        Toast.makeText(
            requireContext(),
            "Disconnecting...",
            Toast.LENGTH_SHORT
        ).show()
        
        // Call disconnect via Intent
        val intent = android.content.Intent(requireContext(), com.chiraitori.ganyu.service.BleService::class.java)
        intent.action = com.chiraitori.ganyu.lib.Intents.DISCONNECT_DEVICE
        requireContext().startService(intent)
        
        // Clear found device and return to empty state
        foundDevice = null
        updateUIState(UIState.EMPTY)
    }
    
    private fun onScanTimeout() {
        Timber.w("onScanTimeout called - no device found")
        
        // Stop refresh animation
        mUiBinding?.swipeRefresh?.isRefreshing = false
        
        // Show toast indicating no device found
        Toast.makeText(
            requireContext(),
            "No device found. Make sure your device is turned on and nearby.",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mUiBinding = FragmentHomeBinding.inflate(inflater, container, false)

        // Setup swipe refresh to auto-scan for BLE device
        binding.swipeRefresh.setOnRefreshListener {
            // Create auto pairing helper
            autoBlePairing = AutoBlePairing(
                requireContext(),
                onDeviceFound = ::onDeviceFound,
                onTimeout = ::onScanTimeout
            )
            
            // Start scanning - keep refresh animation while scanning
            val started = autoBlePairing?.startScan() ?: false
            if (!started) {
                // Failed to start scan
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(
                    requireContext(),
                    "Failed to start Bluetooth scan. Check permissions.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Setup connect button (on device found card)
        binding.btnConnectDevice.setOnClickListener {
            connectToDevice()
        }
        
        // Setup disconnect button (on device connected card)
        binding.btnDisconnectDevice.setOnClickListener {
            disconnectDevice()
        }

        // Observe ViewModel
        viewModel = ViewModelProvider(requireActivity())[ActivityViewModel::class.java]
        
        viewModel?.navigationData?.observe(viewLifecycleOwner) {
            displayNavigationData(it)
        }
        
        // Observe connected device to update UI state
        viewModel?.connectedDevice?.observe(viewLifecycleOwner) { device ->
            Timber.d("Connected device changed: $device")
            
            if (device != null) {
                // Device connected
                // Update connected device info
                binding.txtConnectedDeviceName.text = device.name ?: "Unknown Device"
                binding.txtConnectedDeviceAddress.text = device.address
                
                // If we're showing device found card, switch to connected state
                if (binding.deviceFoundCard.visibility == View.VISIBLE) {
                    // Check if there's navigation data
                    val hasNavData = viewModel?.navigationData?.value != null
                    updateUIState(if (hasNavData) UIState.NAVIGATING else UIState.DEVICE_CONNECTED)
                }
            } else {
                // Device disconnected
                // If no navigation data, show empty state
                if (viewModel?.navigationData?.value == null) {
                    updateUIState(UIState.EMPTY)
                }
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        autoBlePairing?.stopScan()
        autoBlePairing = null
        mUiBinding = null
    }
}