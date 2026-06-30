package com.autoswipe.helper.keepalive

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.autoswipe.helper.config.AppConfig
import com.autoswipe.helper.util.LogUtil
import com.autoswipe.helper.util.NotificationHelper

/**
 * 前台服务保活
 *
 * 通过前台服务机制防止系统杀死应用进程。
 * 前台服务在状态栏显示一个低优先级的持久通知。
 *
 * Android 8.0+ 要求使用前台服务通道 (Foreground Service Type)
 * 这里使用 specialUse 类型（Android 14+ 需要声明）
 */
class KeepAliveForegroundService : Service() {

    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        LogUtil.d("KeepAliveForegroundService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = notificationHelper.createKeepAliveNotification()
        startForeground(AppConfig.NOTIFY_ID_KEEP_ALIVE, notification)

        LogUtil.d("KeepAliveForegroundService started")

        // START_STICKY: 服务被杀死后自动重启
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        LogUtil.d("KeepAliveForegroundService destroyed")
    }

    companion object {
        const val ACTION_STOP = "com.autoswipe.helper.STOP_KEEP_ALIVE"
    }
}