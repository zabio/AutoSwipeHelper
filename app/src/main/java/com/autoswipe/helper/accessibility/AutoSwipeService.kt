package com.autoswipe.helper.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import com.autoswipe.helper.config.PreferencesManager
import com.autoswipe.helper.detection.AdDetector
import com.autoswipe.helper.detection.ScreenState
import com.autoswipe.helper.gesture.GestureController
import com.autoswipe.helper.util.LogUtil
import com.autoswipe.helper.util.NotificationHelper

/**
 * 自动上滑无障碍服务
 *
 * 核心功能：
 * - 监听目标 App（红果短剧等）的窗口变化
 * - 检测广告结束状态
 * - 自动执行上滑手势
 *
 * 红果短剧广告特征：
 * - 广告页面底部显示 "上滑继续观看短剧"
 * - 通常有 5-15 秒倒计时
 * - 倒计时结束后可上滑跳过
 */
class AutoSwipeService : AccessibilityService() {

    private lateinit var adDetector: AdDetector
    private lateinit var gestureController: GestureController
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var notificationHelper: NotificationHelper

    /** 服务是否启用 */
    private var isServiceEnabled = false

    /** 用户是否暂停了自动上滑（通过悬浮窗/通知栏） */
    private var isUserPaused = false

    /** 上次执行上滑的时间戳 */
    private var lastSwipeTime = 0L

    /** 执行上滑的次数统计 */
    private var swipeCount = 0

    override fun onCreate() {
        super.onCreate()
        instance = this
        preferencesManager = PreferencesManager(this)
        adDetector = AdDetector(preferencesManager)
        gestureController = GestureController(this, preferencesManager)
        notificationHelper = NotificationHelper(this)
        LogUtil.d("AutoSwipeService created")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        // 配置服务信息
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                    AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            notificationTimeout = 100
        }
        serviceInfo = info

        isServiceEnabled = true
        notificationHelper.showServiceRunningNotification()
        LogUtil.d("AutoSwipeService connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!isServiceEnabled || isUserPaused) return

        val eventType = event.eventType
        if (eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            eventType != AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED
        ) {
            return
        }

        val packageName = event.packageName?.toString() ?: return
        if (!preferencesManager.isTargetApp(packageName)) return

        // 防抖
        val now = System.currentTimeMillis()
        if (now - lastSwipeTime < preferencesManager.getMinSwipeInterval()) {
            return
        }

        checkAndPerformSwipe()
    }

    private var eventCount = 0
    private var adDetectedCount = 0

    private fun checkAndPerformSwipe() {
        val rootNode = rootInActiveWindow ?: return

        eventCount++

        try {
            val screenState = adDetector.detect(rootNode)

            // 每 50 次事件打印一次调试信息
            if (eventCount % 50 == 0) {
                val debugInfo = adDetector.getDebugInfo(rootNode)
                LogUtil.d("AutoSwipeService: events=$eventCount, adDetected=$adDetectedCount, debug=$debugInfo")
            }

            when (screenState) {
                is ScreenState.AdFinished -> {
                    adDetectedCount++
                    LogUtil.d("🚀 AutoSwipeService: performing swipe #$adDetectedCount (method=${screenState.method})")
                    performSwipe(screenState.method.name)
                }
                is ScreenState.AdPlaying -> {}
                is ScreenState.Normal -> {}
                is ScreenState.Unknown -> {}
            }
        } catch (e: Exception) {
            LogUtil.e("checkAndPerformSwipe error", e)
        } finally {
            rootNode.recycle()
        }
    }

    /**
     * 执行上滑手势
     */
    private fun performSwipe(method: String) {
        lastSwipeTime = System.currentTimeMillis()

        gestureController.performSwipeUp { success ->
            if (success) {
                swipeCount++
                LogUtil.d("Swipe #$swipeCount completed successfully (method: $method)")

                // 更新通知
                notificationHelper.updateSwipeCount(swipeCount)

                // 重置检测器状态
                adDetector.reset()
            } else {
                LogUtil.w("Swipe #${swipeCount + 1} failed or cancelled (method: $method)")
            }
        }
    }

    /**
     * 暂停自动上滑（用户手动暂停）
     */
    fun pause() {
        isUserPaused = true
        notificationHelper.showPausedNotification()
        LogUtil.d("AutoSwipeService paused by user")
    }

    /**
     * 恢复自动上滑
     */
    fun resume() {
        isUserPaused = false
        adDetector.reset()
        notificationHelper.showServiceRunningNotification()
        LogUtil.d("AutoSwipeService resumed by user")
    }

    /**
     * 获取服务运行状态
     */
    fun isRunning(): Boolean = isServiceEnabled && !isUserPaused

    /**
     * 获取上滑次数
     */
    fun getSwipeCount(): Int = swipeCount

    /**
     * 手动触发一次上滑
     */
    fun manualSwipe(callback: ((Boolean) -> Unit)? = null) {
        gestureController.performSwipeUp { success ->
            if (success) swipeCount++
            callback?.invoke(success)
        }
    }

    override fun onInterrupt() {
        isServiceEnabled = false
        LogUtil.d("AutoSwipeService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceEnabled = false
        notificationHelper.cancelAll()
        LogUtil.d("AutoSwipeService destroyed, total swipes: $swipeCount")
    }

    companion object {
        /** 全局服务实例（用于外部通信） */
        @Volatile
        var instance: AutoSwipeService? = null
            private set
    }
}