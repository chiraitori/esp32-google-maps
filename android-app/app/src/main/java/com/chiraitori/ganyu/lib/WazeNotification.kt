package com.chiraitori.ganyu.lib

import android.app.Notification
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.service.notification.StatusBarNotification
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.view.children
import timber.log.Timber

const val WAZE_PACKAGE = "com.waze"

enum class WazeContentViewType {
    NORMAL,
    BIG,
    BEST,
}

internal class WazeNotification(cx: Context, sbn: StatusBarNotification) : NavigationNotification(cx, sbn) {
    init {
        try {
            val normalContent = getContentView(WazeContentViewType.NORMAL)
            if (normalContent != null)
                parseRemoteView(getRemoteViewGroup(normalContent))

            val bestContentView = getContentView(WazeContentViewType.BEST)
            if (bestContentView != normalContent)
                parseRemoteView(getRemoteViewGroup(bestContentView))
        } catch (e: Exception) {
            Timber.e("Failed to parse Waze notification: $e")
            // Try to parse from notification extras as fallback
            parseFromExtras()
        }
    }

    private fun getContentView(type: WazeContentViewType = WazeContentViewType.BEST): RemoteViews? {
        if (type == WazeContentViewType.BIG || type == WazeContentViewType.BEST) {
            val remoteViews = Notification.Builder.recoverBuilder(mContext, mNotification).createBigContentView()

            if (remoteViews != null || type == WazeContentViewType.BIG)
                return remoteViews
        }

        return Notification.Builder.recoverBuilder(mContext, mNotification).createContentView()
    }

    private fun getRemoteViewGroup(remoteViews: RemoteViews?): ViewGroup {
        if (remoteViews == null) {
            throw Exception("Impossible to create notification view")
        }

        val layoutInflater = mAppSourceContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val viewGroup = layoutInflater.inflate(remoteViews.layoutId, null) as ViewGroup?
            ?: throw Exception("Impossible to inflate viewGroup")

        remoteViews.reapply(mAppSourceContext, viewGroup)

        return viewGroup
    }

    private fun getEntryName(item: View): String {
        val entryName: String = try {
            if (item.id > 0)
                mAppSourceContext.resources.getResourceEntryName(item.id)
            else ""
        } catch (e: Exception) {
            ""
        }

        return entryName
    }

    private fun findChildByName(group: ViewGroup, name: CharSequence): View? {
        for (child in group.children) {
            val entryName = getEntryName(child)

            if (entryName == name)
                return child

            if (child is ViewGroup) {
                val c = findChildByName(child, name)
                if (c != null)
                    return c
            }
        }

        return null
    }

    private fun findViewByClass(group: ViewGroup, className: Class<*>): View? {
        for (child in group.children) {
            if (child.javaClass == className)
                return child

            if (child is ViewGroup) {
                val c = findViewByClass(child, className)
                if (c != null)
                    return c
            }
        }

        return null
    }

    private fun parseRemoteView(group: ViewGroup): NavigationData {
        val data = navigationData

        // Waze notification structure is different from Google Maps
        // Try to find text views and image views with common names
        
        // Try common field names for Waze
        val titleText = findChildByName(group, "title") as TextView?
            ?: findChildByName(group, "text") as TextView?
        val subText = findChildByName(group, "text1") as TextView?
            ?: findChildByName(group, "text2") as TextView?
        val timeText = findChildByName(group, "time") as TextView?
            ?: findChildByName(group, "chronometer") as TextView?
        val iconView = findChildByName(group, "icon") as ImageView?
            ?: findChildByName(group, "right_icon") as ImageView?

        // Parse title - usually contains street name or next turn
        if (titleText != null && titleText.text.trim().isNotEmpty()) {
            val titleStr = titleText.text.toString().trim()
            
            // Check if title contains distance info (e.g., "200 m", "1.5 km")
            val distanceRegex = """(\d+(?:\.\d+)?\s*(?:m|km|mi|ft))""".toRegex(RegexOption.IGNORE_CASE)
            val distanceMatch = distanceRegex.find(titleStr)
            
            if (distanceMatch != null) {
                data.nextDirection = NavigationDirection(
                    nextRoad = titleStr.replace(distanceMatch.value, "").trim(),
                    nextRoadAdditionalInfo = "",
                    distance = distanceMatch.value
                )
            } else {
                data.nextDirection = NavigationDirection(
                    nextRoad = titleStr,
                    nextRoadAdditionalInfo = "",
                    distance = ""
                )
            }
        }

        // Parse subtitle - usually contains additional info
        if (subText != null && subText.text.trim().isNotEmpty()) {
            val subStr = subText.text.toString().trim()
            
            // Check if it contains ETA or time info
            if (subStr.contains("ETA", ignoreCase = true) || subStr.contains("min", ignoreCase = true)) {
                // Try to extract time and distance
                val parts = subStr.split("·", "-", "•")
                if (parts.size >= 2) {
                    data.eta = NavigationEta(
                        eta = parts[0].trim(),
                        ete = if (parts.size > 1) parts[1].trim() else "",
                        distance = if (parts.size > 2) parts[2].trim() else ""
                    )
                }
            } else {
                // It's probably a road description
                val currentDirection = data.nextDirection
                if (currentDirection != null) {
                    data.nextDirection = currentDirection.copy(nextRoadAdditionalInfo = subStr)
                }
            }
        }

        // Parse time/chronometer
        if (timeText != null && timeText.text.trim().isNotEmpty()) {
            val currentEta = data.eta
            if (currentEta != null) {
                data.eta = currentEta.copy(ete = timeText.text.toString().trim())
            } else {
                data.eta = NavigationEta(
                    eta = "",
                    ete = timeText.text.toString().trim(),
                    distance = ""
                )
            }
        }

        // Parse turn icon
        if (iconView != null) {
            (iconView.drawable as? BitmapDrawable?)?.bitmap?.also {
                data.actionIcon = NavigationIcon(it.copy(it.config, false))
            }
        }

        Timber.d("Parsed Waze notification: $data")

        return data
    }

    /**
     * Fallback method to parse notification from extras when RemoteViews parsing fails
     */
    private fun parseFromExtras() {
        val data = navigationData
        val extras = mNotification.extras

        try {
            // Try to get text from notification extras
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""
            val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""

            Timber.d("Waze notification extras - Title: $title, Text: $text, SubText: $subText")

            // Parse title for road name and distance
            if (title.isNotEmpty()) {
                val distanceRegex = """(\d+(?:\.\d+)?\s*(?:m|km|mi|ft))""".toRegex(RegexOption.IGNORE_CASE)
                val distanceMatch = distanceRegex.find(title)
                
                if (distanceMatch != null) {
                    data.nextDirection = NavigationDirection(
                        nextRoad = title.replace(distanceMatch.value, "").trim(),
                        nextRoadAdditionalInfo = if (text.isNotEmpty()) text else subText,
                        distance = distanceMatch.value
                    )
                } else {
                    data.nextDirection = NavigationDirection(
                        nextRoad = title,
                        nextRoadAdditionalInfo = if (text.isNotEmpty()) text else subText,
                        distance = ""
                    )
                }
            }

            // Try to parse ETA from text or bigText
            val etaText = if (bigText.isNotEmpty()) bigText else text
            if (etaText.isNotEmpty()) {
                val parts = etaText.split("·", "-", "•")
                if (parts.size >= 2) {
                    data.eta = NavigationEta(
                        eta = parts[0].trim(),
                        ete = if (parts.size > 1) parts[1].trim() else "",
                        distance = if (parts.size > 2) parts[2].trim() else ""
                    )
                }
            }

            // Try to get icon from extras
            val largeIcon = extras.getParcelable<android.graphics.Bitmap>(Notification.EXTRA_LARGE_ICON)
            if (largeIcon != null) {
                data.actionIcon = NavigationIcon(largeIcon.copy(largeIcon.config, false))
            }

        } catch (e: Exception) {
            Timber.e("Failed to parse Waze notification from extras: $e")
        }
    }
}
