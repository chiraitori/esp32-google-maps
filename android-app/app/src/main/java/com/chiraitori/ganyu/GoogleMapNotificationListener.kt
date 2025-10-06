package com.chiraitori.ganyu

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.chiraitori.ganyu.lib.Intents
import com.chiraitori.ganyu.lib.NavigationNotification
import com.chiraitori.ganyu.service.NavigationListener
import timber.log.Timber

class GoogleMapNotificationListener : NavigationListener() {
    private val mBinder = LocalBinder()

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): GoogleMapNotificationListener = this@GoogleMapNotificationListener
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Bind by activity
        if (intent?.action == Intents.BIND_LOCAL_SERVICE) {
            return mBinder
        }
        // Bind by OS
        return super.onBind(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand: action=${intent?.action}")
        if (intent?.action == Intents.ENABLE_SERVICES) {
            Timber.i("Enabling NavigationListener service")
            enabled = true
        }
        if (intent?.action == Intents.DISABLE_SERVICES) {
            Timber.i("Disabling NavigationListener service")
            enabled = false
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onNavigationNotificationUpdated(navNotification: NavigationNotification) {
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(
            Intent(Intents.NAVIGATION_UPDATE).apply {
                putExtra("navigation_data", navNotification.navigationData)
            }
        )
    }

    override fun onNavigationNotificationRemoved(navNotification: NavigationNotification) {
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(
            // Empty data (no extras)
            Intent(Intents.NAVIGATION_UPDATE)
        )
    }
}