package com.autoswipe.helper.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.autoswipe.helper.R
import com.autoswipe.helper.config.AppConfig
import com.autoswipe.helper.ui.MainActivity

/**
 * 通知管理助手
 *
 * 管理所有通知的创建和更新：
 * - 服务运行状态通知
 * - 服务暂停通知
 * - 更新上滑次数
 */
class NotificationHelper(private val context: Context) {

    private val notificationManager: NotificationManager
        get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 服务保活渠道
            val keepAliveChannel = NotificationChannel(
                AppConfig.CHANNEL_KEEP_ALIVE,
                context.getString(R.string.notification_channel_keepalive),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "保持自动上滑服务在后台运行"
                setShowBadge(false)
            }

            // 服务状态渠道
            val statusChannel = NotificationChannel(
                AppConfig.CHANNEL_SERVICE_STATUS,
                context.getString(R.string.notification_channel_status),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示自动上滑服务运行状态"
                setShowBadge(false)
            }

            notificationManager.createNotificationChannel(keepAliveChannel)
            notificationManager.createNotificationChannel(statusChannel)
        }
    }

    /**
     * 显示服务运行中通知
     */
    fun showServiceRunningNotification() {
        val notification = buildNotification(
            channelId = AppConfig.CHANNEL_SERVICE_STATUS,
            title = context.getString(R.string.app_name),
            content = context.getString(R.string.notification_service_running),
            ongoing = true
        )
        notificationManager.notify(AppConfig.NOTIFY_ID_SERVICE_STATUS, notification)
    }

    /**
     * 显示服务暂停通知
     */
    fun showPausedNotification() {
        val notification = buildNotification(
            channelId = AppConfig.CHANNEL_SERVICE_STATUS,
            title = context.getString(R.string.app_name),
            content = context.getString(R.string.notification_service_paused),
            ongoing = true
        )
        notificationManager.notify(AppConfig.NOTIFY_ID_SERVICE_STATUS, notification)
    }

    /**
     * 更新上滑次数
     */
    fun updateSwipeCount(count: Int) {
        val notification = buildNotification(
            channelId = AppConfig.CHANNEL_SERVICE_STATUS,
            title = context.getString(R.string.app_name),
            content = "已自动上滑 $count 次",
            ongoing = true
        )
        notificationManager.notify(AppConfig.NOTIFY_ID_SERVICE_STATUS, notification)
    }

    /**
     * 创建前台服务保活通知
     */
    fun createKeepAliveNotification(): Notification {
        return buildNotification(
            channelId = AppConfig.CHANNEL_KEEP_ALIVE,
            title = context.getString(R.string.app_name),
            content = context.getString(R.string.notification_service_running),
            ongoing = true
        )
    }

    /**
     * 取消所有通知
     */
    fun cancelAll() {
        notificationManager.cancel(AppConfig.NOTIFY_ID_SERVICE_STATUS)
        notificationManager.cancel(AppConfig.NOTIFY_ID_KEEP_ALIVE)
    }

    private fun buildNotification(
        channelId: String,
        title: String,
        content: String,
        ongoing: Boolean = false
    ): Notification {
        val pendingIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(ongoing)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(false)
            .build()
    }
}
