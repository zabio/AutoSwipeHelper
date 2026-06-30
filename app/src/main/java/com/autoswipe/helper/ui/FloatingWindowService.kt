package com.autoswipe.helper.ui

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.autoswipe.helper.R
import com.autoswipe.helper.accessibility.AutoSwipeService
import com.autoswipe.helper.config.AppConfig
import com.autoswipe.helper.config.PreferencesManager
import com.autoswipe.helper.util.LogUtil

/**
 * 悬浮窗服务
 *
 * 在屏幕上显示一个可拖拽的悬浮按钮：
 * - 点击切换：启用/暂停自动上滑
 * - 长按拖拽：移动悬浮窗位置
 * - 双击：打开主界面
 */
class FloatingWindowService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var preferencesManager: PreferencesManager

    private var isAutoSwipeEnabled = true
    private var lastClickTime = 0L

    override fun onCreate() {
        super.onCreate()
        preferencesManager = PreferencesManager(this)
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AppConfig.CHANNEL_KEEP_ALIVE,
                "悬浮窗",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "悬浮窗控制服务"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(AppConfig.NOTIFY_ID_FLOATING, createNotification())
        setupFloatingWindow()
        return START_STICKY
    }

    private fun setupFloatingWindow() {
        // 如果已经添加，先移除
        try {
            windowManager.removeView(floatingView)
        } catch (_: Exception) {}

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.END
            x = 16
            y = 400
        }

        floatingView = LayoutInflater.from(this)
            .inflate(R.layout.floating_window, null)

        // 设置初始状态
        updateFloatingAppearance()

        // 点击：切换启用/暂停
        floatingView.setOnClickListener {
            val now = System.currentTimeMillis()
            if (now - lastClickTime < 300) {
                // 双击：打开主界面
                openMainActivity()
            } else {
                // 单击：切换状态
                toggleAutoSwipe()
            }
            lastClickTime = now
        }

        // 长按拖拽
        floatingView.setOnTouchListener(FloatingTouchListener(params, windowManager))

        windowManager.addView(floatingView, params)
    }

    private fun toggleAutoSwipe() {
        isAutoSwipeEnabled = !isAutoSwipeEnabled
        preferencesManager.setPaused(!isAutoSwipeEnabled)

        val service = AutoSwipeService.instance
        if (isAutoSwipeEnabled) {
            service?.resume()
        } else {
            service?.pause()
        }

        updateFloatingAppearance()
        LogUtil.d("FloatingWindow: auto swipe ${if (isAutoSwipeEnabled) "enabled" else "paused"}")
    }

    private fun updateFloatingAppearance() {
        val container = floatingView.findViewById<View>(R.id.floating_container)
        val icon = floatingView.findViewById<android.widget.TextView>(R.id.tv_floating_icon)
        val label = floatingView.findViewById<android.widget.TextView>(R.id.tv_floating_label)

        if (isAutoSwipeEnabled) {
            container.setBackgroundResource(R.drawable.floating_bg_circle)
            icon.text = "⏸"
            label.text = "自动"
        } else {
            container.setBackgroundResource(R.drawable.floating_bg_circle_paused)
            icon.text = "▶"
            label.text = "暂停"
        }
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    private fun createNotification() = NotificationCompat.Builder(this, AppConfig.CHANNEL_KEEP_ALIVE)
        .setContentTitle(getString(R.string.floating_auto_swipe))
        .setContentText(if (isAutoSwipeEnabled) "运行中" else "已暂停")
        .setSmallIcon(R.drawable.ic_notification)
        .setOngoing(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        try {
            windowManager.removeView(floatingView)
        } catch (_: Exception) {}
        super.onDestroy()
    }

    companion object {
        const val ACTION_STOP = "com.autoswipe.helper.STOP_FLOATING"

        fun toggle(activity: Activity, open: Boolean) {
            if (open)
                startFloatingWindow(activity)
            else{
                val it = Intent(ACTION_STOP)
                it.setPackage(activity.packageName)
                it.setClass(activity, FloatingWindowService::class.java)
                activity.stopService(it)
            }
        }


        private fun startFloatingWindow(activity: Activity) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
                Toast.makeText(activity, "请先开启悬浮窗权限", Toast.LENGTH_SHORT).show()
                return
            }
            try {
                val intent = Intent(activity, FloatingWindowService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity.startForegroundService(intent)
                } else {
                    activity.startService(intent)
                }
                LogUtil.d("FloatingWindowService started")
            } catch (e: Exception) {
                LogUtil.e("Failed to start FloatingWindowService", e)
            }
        }

    }
}

/**
 * 悬浮窗拖拽触摸监听器
 */
class FloatingTouchListener(
    private val params: WindowManager.LayoutParams,
    private val windowManager: WindowManager
) : View.OnTouchListener {

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    private val dragThreshold = 10f

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = params.x
                initialY = params.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                isDragging = false
                return false
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.rawX - initialTouchX
                val deltaY = event.rawY - initialTouchY

                if (!isDragging && (Math.abs(deltaX) > dragThreshold || Math.abs(deltaY) > dragThreshold)) {
                    isDragging = true
                }

                if (isDragging) {
                    params.x = initialX - deltaX.toInt()
                    params.y = initialY + deltaY.toInt()
                    windowManager.updateViewLayout(view, params)
                }
                return isDragging
            }

            MotionEvent.ACTION_UP -> {
                return isDragging
            }
        }
        return false
    }
}