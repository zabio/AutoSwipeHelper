package com.autoswipe.helper.util

import android.util.Log

/**
 * 日志工具类
 *
 * 统一管理日志输出，Release 版本可关闭 DEBUG 和 VERBOSE 日志
 */
object LogUtil {
    private const val TAG = "AutoSwipeHelper"

    /** 是否启用详细日志 */
    var isDebugEnabled = true

    fun v(message: String) {
        if (isDebugEnabled) Log.v(TAG, message)
    }

    fun d(message: String) {
        if (isDebugEnabled) Log.d(TAG, message)
    }

    fun i(message: String) {
        Log.i(TAG, message)
    }

    fun w(message: String) {
        Log.w(TAG, message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e(TAG, message, throwable)
        } else {
            Log.e(TAG, message)
        }
    }
}
