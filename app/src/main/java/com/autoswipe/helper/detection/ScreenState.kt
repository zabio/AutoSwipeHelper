package com.autoswipe.helper.detection

/**
 * 屏幕状态
 */
sealed class ScreenState {
    /** 正常播放状态 */
    data object Normal : ScreenState()

    /** 广告播放中 */
    data class AdPlaying(val remainingSeconds: Int? = null) : ScreenState()

    /** 广告已结束，可上滑 */
    data class AdFinished(val method: DetectionMethod) : ScreenState()

    /** 未知状态 */
    data object Unknown : ScreenState()
}

/**
 * 检测方法
 */
enum class DetectionMethod {
    /** UI节点文本检测 */
    NODE_TEXT,

    /** 倒计时数字检测 */
    COUNTDOWN,

    /** 固定计时触发 */
    TIMER,
}

/**
 * 倒计时检测结果
 */
sealed class CountdownResult {
    /** 倒计时进行中 */
    data class Counting(val currentValue: Int) : CountdownResult()

    /** 倒计时已结束 */
    data object Finished : CountdownResult()

    /** 未检测到倒计时 */
    data object NotFound : CountdownResult()
}
