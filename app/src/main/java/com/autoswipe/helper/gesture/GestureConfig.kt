package com.autoswipe.helper.gesture

/**
 * 手势配置参数
 */
data class GestureConfig(
    /** 滑动持续时间（毫秒），默认 300ms */
    val swipeDurationMs: Long = 300L,

    /** 检测到广告结束后延迟执行上滑（毫秒），默认 500ms */
    val swipeDelayMs: Long = 500L,

    /** 滑动起始位置（屏幕高度比例），默认从 80% 处开始 */
    val swipeStartRatio: Float = 0.8f,

    /** 滑动结束位置（屏幕高度比例），默认到 20% 处结束 */
    val swipeEndRatio: Float = 0.2f,

    /** 滑动 X 轴位置（屏幕宽度比例），默认屏幕中央 */
    val swipeXRatio: Float = 0.5f,

    /** 两次滑动最小间隔（毫秒） */
    val minSwipeIntervalMs: Long = 2000L,
)
