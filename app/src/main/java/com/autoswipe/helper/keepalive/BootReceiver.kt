package com.autoswipe.helper.keepalive

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import com.autoswipe.helper.config.PreferencesManager
import com.autoswipe.helper.util.LogUtil

/**
 * 开机自启广播接收器
 *
 * 在设备启动完成后自动启动保活服务
 * 需要用户手动开启「开机自启」选项
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "com.htc.intent.action.QUICKBOOT_POWERON"
        ) {
            val prefs = PreferencesManager(context)

            if (!prefs.isAutoStartEnabled()) {
                LogUtil.d("BootReceiver: auto start disabled, skip")
                return
            }

            LogUtil.d("BootReceiver: starting services after boot")

            // 启动前台保活服务
            val serviceIntent = Intent(context, KeepAliveForegroundService::class.java)
            try {
                ContextCompat.startForegroundService(context, serviceIntent)
                LogUtil.d("BootReceiver: KeepAliveForegroundService started")
            } catch (e: Exception) {
                LogUtil.e("BootReceiver: failed to start service", e)
            }

            // 如果悬浮窗已开启，也启动悬浮窗
            if (prefs.isFloatingWindowEnabled()) {
                val floatingIntent = Intent(context, com.autoswipe.helper.ui.FloatingWindowService::class.java)
                try {
                    ContextCompat.startForegroundService(context, floatingIntent)
                } catch (e: Exception) {
                    LogUtil.e("BootReceiver: failed to start floating window", e)
                }
            }
        }
    }
}
