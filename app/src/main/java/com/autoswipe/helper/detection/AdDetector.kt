package com.autoswipe.helper.detection

import android.view.accessibility.AccessibilityNodeInfo
import com.autoswipe.helper.config.PreferencesManager
import com.autoswipe.helper.util.LogUtil

/**
 * 广告检测调度器
 */
class AdDetector(private val prefs: PreferencesManager) {

    private val nodeDetector = NodeDetector()
    private val timerDetector = TimerBasedDetector(
        defaultAdDurationMs = prefs.getFixedAdDuration()
    )

    private var lastDetectionMethod: DetectionMethod? = null
    private var debugCount = 0

    fun detect(rootNode: AccessibilityNodeInfo): ScreenState {
        // 周期性输出调试信息（每 20 次事件）
        debugCount++
        val shouldLog = debugCount % 20 == 0

        val inAd = nodeDetector.isInAd(rootNode)

        if (shouldLog && inAd) {
            val matched = nodeDetector.getMatchedTexts(rootNode)
            LogUtil.d("AdDetector: inAd=true, matchedTexts=$matched")
        }

        // 优先级1：UI节点文本检测
        if (nodeDetector.detectAdFinished(rootNode)) {
            if (shouldLog || lastDetectionMethod != DetectionMethod.NODE_TEXT) {
                LogUtil.d("AdDetector: ✅ ad finished (NODE_TEXT), matched=${nodeDetector.getMatchedTexts(rootNode)}")
            }
            lastDetectionMethod = DetectionMethod.NODE_TEXT
            return ScreenState.AdFinished(DetectionMethod.NODE_TEXT)
        }

        // 优先级2：在广告中且定时器触发
        if (inAd) {
            timerDetector.onAdDetected()
        }

        if (timerDetector.shouldSwipe()) {
            LogUtil.d("AdDetector: ✅ ad finished (TIMER)")
            lastDetectionMethod = DetectionMethod.TIMER
            return ScreenState.AdFinished(DetectionMethod.TIMER)
        }

        if (inAd) {
            return ScreenState.AdPlaying(timerDetector.getRemainingSeconds())
        }

        return ScreenState.Normal
    }

    fun getDebugInfo(rootNode: AccessibilityNodeInfo): Map<String, Any> {
        return mapOf(
            "matchedTexts" to nodeDetector.getMatchedTexts(rootNode),
            "isInAd" to nodeDetector.isInAd(rootNode),
            "lastDetectionMethod" to (lastDetectionMethod?.name ?: "none"),
            "timerRemaining" to timerDetector.getRemainingSeconds(),
        )
    }

    fun reset() {
        timerDetector.reset()
        lastDetectionMethod = null
    }
}