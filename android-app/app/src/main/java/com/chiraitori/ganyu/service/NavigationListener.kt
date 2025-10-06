package com.chiraitori.ganyu.service

import android.content.Context
import android.content.SharedPreferences
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.chiraitori.ganyu.SHARED_PREFERENCES_FILE
import com.chiraitori.ganyu.lib.GMAPS_PACKAGE
import com.chiraitori.ganyu.lib.GMapsNotification
import com.chiraitori.ganyu.lib.WAZE_PACKAGE
import com.chiraitori.ganyu.lib.WazeNotification
import com.chiraitori.ganyu.lib.NavigationData
import com.chiraitori.ganyu.lib.NavigationNotification
import kotlinx.coroutines.*
import timber.log.Timber

@OptIn(DelicateCoroutinesApi::class)
open class NavigationListener : NotificationListenerService() {
    private var mNotificationParserCoroutine: Job? = null
    private lateinit var mLastNotification: StatusBarNotification

    private var mCurrentNotification: NavigationNotification? = null
    private var mEnabled = false
    private lateinit var mSharedPref: SharedPreferences

    private enum class NavApp {
        GOOGLE_MAPS,
        WAZE
    }

    protected var enabled: Boolean
        get() = mEnabled
        set(value) {
            if (value == mEnabled)
                return
            if (value.also { mEnabled = it })
                checkActiveNotifications()
            else {
                mCurrentNotification = null
            }
        }

    val lastNavigationData: NavigationData? get() = mCurrentNotification?.navigationData

    private fun getSelectedNavApp(): String {
        return mSharedPref.getString("navigation_app", "auto") ?: "auto"
    }

    private fun shouldHandleNavApp(navApp: NavApp): Boolean {
        return when (getSelectedNavApp()) {
            "auto" -> true // Handle both apps
            "google_maps" -> navApp == NavApp.GOOGLE_MAPS
            "waze" -> navApp == NavApp.WAZE
            else -> true // Default to auto
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Timber.i("NavigationListener connected!")
        mSharedPref = applicationContext.getSharedPreferences(SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE)
        val selectedApp = getSelectedNavApp()
        Timber.i("Selected navigation app: $selectedApp")
        checkActiveNotifications()
    }

    private fun checkActiveNotifications() {
        try {
            val activeCount = this.activeNotifications.size
            Timber.d("Checking for active Navigation notifications (total: $activeCount)")
            this.activeNotifications.forEach { statusBarNotification ->
                if (statusBarNotification.packageName == GMAPS_PACKAGE || 
                    statusBarNotification.packageName == WAZE_PACKAGE) {
                    Timber.d("Found notification from: ${statusBarNotification.packageName}, ID: ${statusBarNotification.id}, ongoing: ${statusBarNotification.isOngoing}")
                }
                onNotificationPosted(
                    statusBarNotification
                )
            }
        } catch (e: Throwable) {
            Timber.e("Failed to check for active notifications: $e")
        }
    }

    private fun isGoogleMapsNotification(sbn: StatusBarNotification?): Boolean {
        if (!enabled || sbn == null)
            return false

        if (GMAPS_PACKAGE !in sbn.packageName)
            return false
            
        // Debug logging for Android 13+
        Timber.d("Google Maps notification detected - ID: ${sbn.id}, isOngoing: ${sbn.isOngoing}, package: ${sbn.packageName}")
        
        if (!sbn.isOngoing) {
            Timber.w("Google Maps notification is not ongoing, skipping")
            return false
        }

        // Google Maps navigation notification usually has ID = 1
        // But on some Android versions it might be different
        if (sbn.id == 1) {
            Timber.d("Found Google Maps navigation notification (ID=1)")
            return true
        }
        
        // On Android 13+, try to detect by notification content
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString() ?: ""
        val hasNavigationInfo = title.contains(Regex("""\d+\.?\d*\s*(km|m|mi|ft|min|h|giờ|phút)""", RegexOption.IGNORE_CASE))
        
        if (hasNavigationInfo) {
            Timber.d("Found Google Maps notification with navigation info (ID=${sbn.id})")
            return true
        }
        
        Timber.w("Google Maps notification ID=${sbn.id} does not match navigation pattern")
        return false
    }

    private fun isWazeNotification(sbn: StatusBarNotification?): Boolean {
        if (!enabled || sbn == null)
            return false

        if (!sbn.isOngoing || WAZE_PACKAGE !in sbn.packageName)
            return false

        // Waze navigation notifications contain navigation info in title or text
        // Filter out the "Running" service notification
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString() ?: ""
        val subText = extras.getCharSequence(android.app.Notification.EXTRA_SUB_TEXT)?.toString() ?: ""
        
        // Check if it contains navigation-related info
        // Skip the "Running. Tap to open." notification
        if (text.contains("Đang chạy", ignoreCase = true) || 
            text.contains("Running", ignoreCase = true) ||
            text.contains("Tap to open", ignoreCase = true) ||
            text.contains("Nhấn để mở", ignoreCase = true)) {
            Timber.d("Skipping Waze service notification: $text")
            return false
        }
        
        // Accept notifications that have distance info (km, m, mi, ft) or time info (min, h)
        val hasNavigationInfo = title.contains(Regex("""\d+\.?\d*\s*(km|m|mi|ft|min|h)""", RegexOption.IGNORE_CASE)) ||
                                text.contains(Regex("""\d+\.?\d*\s*(km|m|mi|ft|min|h)""", RegexOption.IGNORE_CASE)) ||
                                subText.contains(Regex("""\d+\.?\d*\s*(km|m|mi|ft|min|h)""", RegexOption.IGNORE_CASE))
        
        if (hasNavigationInfo) {
            Timber.d("Found Waze navigation notification - Title: $title, Text: $text")
        }
        
        return hasNavigationInfo
    }

    private fun isNavigationNotification(sbn: StatusBarNotification?): Boolean {
        return isGoogleMapsNotification(sbn) || isWazeNotification(sbn)
    }

    protected open fun onNavigationNotificationAdded(navNotification: NavigationNotification) {
    }

    protected open fun onNavigationNotificationUpdated(navNotification: NavigationNotification) {
    }

    protected open fun onNavigationNotificationRemoved(navNotification: NavigationNotification) {
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn?.packageName == GMAPS_PACKAGE || sbn?.packageName == WAZE_PACKAGE) {
            Timber.v("onNotificationPosted ${sbn.packageName}, ID: ${sbn.id}, enabled: $enabled")
        }

        when {
            isGoogleMapsNotification(sbn) && shouldHandleNavApp(NavApp.GOOGLE_MAPS) -> {
                Timber.i("Handling Google Maps notification")
                handleNavigationNotification(sbn!!, NavApp.GOOGLE_MAPS)
            }
            isWazeNotification(sbn) && shouldHandleNavApp(NavApp.WAZE) -> {
                Timber.i("Handling Waze notification")
                handleNavigationNotification(sbn!!, NavApp.WAZE)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Timber.v("onNotificationRemoved ${sbn?.packageName}")

        if (isNavigationNotification(sbn)) {
            mNotificationParserCoroutine?.cancel()

            onNavigationNotificationRemoved(
                if (mCurrentNotification != null) mCurrentNotification!!
                else NavigationNotification(applicationContext, sbn!!)
            )

            mCurrentNotification = null
        }
    }

    private fun handleNavigationNotification(statusBarNotification: StatusBarNotification, navApp: NavApp) {
        mLastNotification = statusBarNotification
        if (mNotificationParserCoroutine != null && mNotificationParserCoroutine!!.isActive)
            return

        mNotificationParserCoroutine = GlobalScope.launch(Dispatchers.Main) {
            val worker = GlobalScope.async(Dispatchers.Default) {
                return@async when (navApp) {
                    NavApp.GOOGLE_MAPS -> GMapsNotification(
                        this@NavigationListener.applicationContext,
                        mLastNotification
                    )
                    NavApp.WAZE -> WazeNotification(
                        this@NavigationListener.applicationContext,
                        mLastNotification
                    )
                }
            }

            try {
                val navNotification = worker.await()
                val lastNotification = mCurrentNotification

                val updated: Boolean = if (lastNotification == null) {
                    onNavigationNotificationAdded(navNotification)
                    true
                } else {
                    lastNotification.navigationData != navNotification.navigationData
                    // Timber.v("Notification is different than previous: $updated")
                }

                if (updated) {
                    mCurrentNotification = navNotification
                    onNavigationNotificationUpdated(mCurrentNotification!!)
                }
            } catch (error: Exception) {
                if (!mNotificationParserCoroutine!!.isCancelled)
                    Timber.e("Got an error while parsing ${navApp.name} notification: $error")
            }
        }
    }

}
