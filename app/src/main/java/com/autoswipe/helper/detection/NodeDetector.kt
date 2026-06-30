package com.autoswipe.helper.detection

import android.view.accessibility.AccessibilityNodeInfo
import com.autoswipe.helper.accessibility.AccessibilityConfig

/**
 * UI节点树检测器
 *
 * 红果短剧广告特征：
 * - 广告倒计时区域显示数字（如"5"、"4"..."0"）
 * - 底部区域显示"上滑继续观看短剧"
 * - 倒计时结束后文本保持不变，但页面状态改变
 *
 * 核心策略：只要在页面上找到"上滑继续观看短剧"相关文本就判定为广告结束
 */
class NodeDetector {

    /**
     * 检测广告是否已结束
     */
    fun detectAdFinished(rootNode: AccessibilityNodeInfo): Boolean {
        // 收集页面上所有可见文本
        val allVisibleTexts = collectVisibleTexts(rootNode)
        val fullPageText = allVisibleTexts.joinToString(" ")

        // 红果短剧专属关键词检测
        for (pattern in AccessibilityConfig.HONGGUO_AD_FINISHED_PATTERNS) {
            if (fullPageText.contains(pattern)) {
                return true
            }
        }

        // 通用广告结束检测
        for (pattern in AccessibilityConfig.GENERAL_AD_FINISHED_PATTERNS) {
            if (fullPageText.contains(pattern)) {
                // 同时需要确认在广告场景中
                val hasAdIndicator = AccessibilityConfig.AD_LABEL_PATTERNS.any {
                    fullPageText.contains(it)
                }
                if (hasAdIndicator) return true

                // 或者检测到倒计时数字结束（有数字0且附近有滑动提示）
                val hasCountdownEnd = AccessibilityConfig.COUNTDOWN_END_PATTERNS.any {
                    fullPageText.contains(it)
                }
                if (hasCountdownEnd) return true
            }
        }

        // 倒计时结束检测
        for (pattern in AccessibilityConfig.COUNTDOWN_END_PATTERNS) {
            if (fullPageText.contains(pattern)) {
                return true
            }
        }

        return false
    }

    /**
     * 检测当前是否在广告中
     */
    fun isInAd(rootNode: AccessibilityNodeInfo): Boolean {
        val fullPageText = collectVisibleTexts(rootNode).joinToString(" ")

        // 有广告标识
        val hasAdLabel = AccessibilityConfig.AD_LABEL_PATTERNS.any { fullPageText.contains(it) }
        if (hasAdLabel) return true

        // 有滑动继续观看提示
        val hasSwipeHint = (AccessibilityConfig.HONGGUO_AD_FINISHED_PATTERNS +
                AccessibilityConfig.GENERAL_AD_FINISHED_PATTERNS).any { fullPageText.contains(it) }
        if (hasSwipeHint) return true

        return false
    }

    /**
     * 获取匹配文本（用于调试）
     */
    fun getMatchedTexts(rootNode: AccessibilityNodeInfo): List<String> {
        val fullPageText = collectVisibleTexts(rootNode).joinToString(" ")
        val allPatterns = AccessibilityConfig.HONGGUO_AD_FINISHED_PATTERNS +
                AccessibilityConfig.GENERAL_AD_FINISHED_PATTERNS +
                AccessibilityConfig.COUNTDOWN_END_PATTERNS +
                AccessibilityConfig.AD_LABEL_PATTERNS

        return allPatterns.filter { fullPageText.contains(it) }
    }

    /**
     * 收集页面上所有可见节点的文本
     */
    private fun collectVisibleTexts(node: AccessibilityNodeInfo): List<String> {
        val results = mutableListOf<String>()
        collectTextsRecursive(node, results)
        return results
    }

    private fun collectTextsRecursive(
        node: AccessibilityNodeInfo,
        results: MutableList<String>
    ) {
        // 收集节点的文本
        val text = node.text?.toString()?.trim()
        if (!text.isNullOrEmpty() && node.isVisibleToUser) {
            results.add(text)
        }

        // 收集内容描述
        val contentDesc = node.contentDescription?.toString()?.trim()
        if (!contentDesc.isNullOrEmpty()) {
            results.add(contentDesc)
        }

        // 递归子节点
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectTextsRecursive(child, results)
            child.recycle()
        }
    }
}