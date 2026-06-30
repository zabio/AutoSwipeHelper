package com.autoswipe.helper.detection

import android.view.accessibility.AccessibilityNodeInfo

/**
 * 倒计时数字变化检测器
 *
 * 原理：
 * - 广告倒计时数字通常在屏幕固定位置显示（如右下角）
 * - 数字从 N（如 5-15）递减到 0
 * - 当数字稳定为 0 且连续多次读取一致时，判定倒计时结束
 *
 * 红果短剧的倒计时特征：
 * - 通常在右下角显示 "N 秒" 或单独的 "N"
 * - 数字范围通常是 5-10 秒
 */
class CountdownDetector {
    private var lastCountdownValue = -1
    private var sameValueCount = 0
    private var countdownStarted = false

    companion object {
        /** 连续相同读数次数阈值（防止抖动） */
        private const val STABILITY_THRESHOLD = 3

        /** 倒计时数字合理范围 */
        private const val MIN_COUNTDOWN = 1
        private const val MAX_COUNTDOWN = 60

        /** 数字匹配正则：纯数字，可能带"s"或"秒" */
        private val COUNTDOWN_REGEX = Regex("""^(\d+)\s*[s秒]?$""")
    }

    /**
     * 检测倒计时状态
     *
     * @param node 当前窗口根节点
     * @return 倒计时检测结果
     */
    fun detectCountdownEnd(node: AccessibilityNodeInfo): CountdownResult {
        val candidates = findCountdownCandidates(node)

        // 如果没有找到候选数字节点
        if (candidates.isEmpty()) {
            // 如果之前检测到倒计时但突然找不到，可能是倒计时结束了
            if (countdownStarted) {
                countdownStarted = false
                lastCountdownValue = -1
                sameValueCount = 0
                return CountdownResult.Finished
            }
            return CountdownResult.NotFound
        }

        // 取所有候选中的最小值（因为可能有多个数字显示）
        val currentValue = candidates.minOrNull() ?: return CountdownResult.NotFound

        // 稳定性检测
        if (currentValue == lastCountdownValue) {
            sameValueCount++
        } else {
            lastCountdownValue = currentValue
            sameValueCount = 1
            countdownStarted = true
        }

        // 数字为 0 且稳定 → 倒计时结束
        if (currentValue == 0 && sameValueCount >= STABILITY_THRESHOLD) {
            countdownStarted = false
            return CountdownResult.Finished
        }

        // 数字在倒计时范围内 → 倒计时进行中
        if (currentValue in MIN_COUNTDOWN..MAX_COUNTDOWN) {
            return CountdownResult.Counting(currentValue)
        }

        return CountdownResult.NotFound
    }

    /**
     * 查找可能是倒计时数字的节点
     *
     * 特征：
     * - 文本是纯数字或"数字+秒/s"
     * - 位于屏幕底部区域（通过 bounds 判断）
     * - 通常是较小的 View（宽度 < 屏幕宽度的 20%）
     */
    private fun findCountdownCandidates(node: AccessibilityNodeInfo): List<Int> {
        val results = mutableListOf<Int>()
        findCountdownCandidatesRecursive(node, results)
        return results
    }

    private fun findCountdownCandidatesRecursive(
        node: AccessibilityNodeInfo,
        results: MutableList<Int>
    ) {
        val text = node.text?.toString()?.trim() ?: ""

        // 尝试匹配倒计时数字格式
        val matchResult = COUNTDOWN_REGEX.find(text)
        if (matchResult != null) {
            val number = matchResult.groupValues[1].toIntOrNull()
            if (number != null && number in 0..MAX_COUNTDOWN) {
                // 额外检查：节点应该在可见区域
                if (node.isVisibleToUser) {
                    results.add(number)
                }
            }
        }

        // 递归子节点
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            findCountdownCandidatesRecursive(child, results)
            child.recycle()
        }
    }

    /** 重置检测状态 */
    fun reset() {
        lastCountdownValue = -1
        sameValueCount = 0
        countdownStarted = false
    }
}
