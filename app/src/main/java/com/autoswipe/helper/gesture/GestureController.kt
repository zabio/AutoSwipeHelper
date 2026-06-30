package com.autoswipe.helper.gesture

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import com.autoswipe.helper.config.PreferencesManager
import com.autoswipe.helper.util.LogUtil

/**
 * 手势控制器
 *
 * 利用 AccessibilityService.dispatchGesture() API
 * 在目标应用窗口上执行模拟的上滑手势
 */
class GestureController(
    private val service: AccessibilityService,
    private val prefs: PreferencesManager
) {
    private val config: GestureConfig
        get() = GestureConfig(
            swipeDurationMs = prefs.getSwipeDuration(),
            swipeDelayMs = prefs.getSwipeDelay(),
            swipeStartRatio = prefs.getSwipeStartRatio(),
            swipeEndRatio = prefs.getSwipeEndRatio(),
            minSwipeIntervalMs = prefs.getMinSwipeInterval(),
        )

    /** 是否正在执行手势 */
    @Volatile
    private var isPerforming = false

    /**
     * 执行上滑手势
     *
     * 从屏幕底部向上滑动，模拟用户的上滑操作
     *
     * @param onResult 结果回调（true=成功，false=失败/取消）
     */
    fun performSwipeUp(onResult: ((Boolean) -> Unit)? = null) {
        if (isPerforming) {
            LogUtil.d("GestureController: already performing, skip")
            onResult?.invoke(false)
            return
        }

        val displayMetrics = service.resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels.toFloat()
        val screenHeight = displayMetrics.heightPixels.toFloat()

        val cfg = config

        // 计算滑动起止坐标
        val startX = screenWidth * cfg.swipeXRatio
        val startY = screenHeight * cfg.swipeStartRatio
        val endY = screenHeight * cfg.swipeEndRatio

        // 添加随机偏移（±5px），避免完全相同的滑动轨迹
        val jitterX = ((Math.random() - 0.5) * 10).toFloat()
        val jitterStartY = ((Math.random() - 0.5) * 10).toFloat()
        val jitterEndY = ((Math.random() - 0.5) * 10).toFloat()

        val path = Path().apply {
            moveTo(startX + jitterX, startY + jitterStartY)
            lineTo(startX + jitterX, endY + jitterEndY)
        }

        val gestureBuilder = GestureDescription.Builder()

        // Android 10+ 支持 clickable 回调和更精细的控制
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val stroke = GestureDescription.StrokeDescription(
                path,
                cfg.swipeDelayMs,
                cfg.swipeDurationMs,
                false // willContinue = true for smoother gesture
            )
            gestureBuilder.addStroke(stroke)
        } else {
            @Suppress("DEPRECATION")
            val stroke = GestureDescription.StrokeDescription(
                path,
                cfg.swipeDelayMs,
                cfg.swipeDurationMs
            )
            gestureBuilder.addStroke(stroke)
        }

        isPerforming = true

        val resultCallback = object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                isPerforming = false
                LogUtil.d("GestureController: swipe completed " +
                        "(${cfg.swipeStartRatio * 100}% → ${cfg.swipeEndRatio * 100}%, " +
                        "duration=${cfg.swipeDurationMs}ms)")
                onResult?.invoke(true)
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                isPerforming = false
                LogUtil.w("GestureController: swipe cancelled")
                onResult?.invoke(false)
            }
        }

        val success = service.dispatchGesture(gestureBuilder.build(), resultCallback, null)
        if (!success) {
            isPerforming = false
            LogUtil.e("GestureController: dispatchGesture returned false")
            onResult?.invoke(false)
        }
    }

    /**
     * 执行自定义手势
     *
     * @param startX 起始 X 坐标
     * @param startY 起始 Y 坐标
     * @param endX 结束 X 坐标
     * @param endY 结束 Y 坐标
     * @param durationMs 手势持续时间
     * @param delayMs 延迟执行时间
     * @param onResult 结果回调
     */
    fun performCustomSwipe(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        durationMs: Long = 300L,
        delayMs: Long = 0L,
        onResult: ((Boolean) -> Unit)? = null
    ) {
        if (isPerforming) {
            onResult?.invoke(false)
            return
        }

        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }

        val gestureBuilder = GestureDescription.Builder()
        val stroke = GestureDescription.StrokeDescription(path, delayMs, durationMs)
        gestureBuilder.addStroke(stroke)

        isPerforming = true

        service.dispatchGesture(gestureBuilder.build(), object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                isPerforming = false
                onResult?.invoke(true)
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                isPerforming = false
                onResult?.invoke(false)
            }
        }, null)
    }

    /** 获取当前手势执行状态 */
    fun isBusy(): Boolean = isPerforming
}
