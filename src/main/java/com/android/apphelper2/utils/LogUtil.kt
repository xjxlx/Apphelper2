package com.android.apphelper2.utils

import android.text.TextUtils
import com.android.apphelper2.app.AppHelper2.isDebug
import com.orhanobut.logger.Logger

/**
 * @author XJX 日志工具类
 */
object LogUtil : ApplicationCheck() {

    @JvmStatic
    fun e(tag: String = "", value: Any) {
        val string = value.toString()
        e(tag, string)
    }

    @JvmStatic
    fun e(tag: String = "", value: String) {
        if (isDebug) {
            if (!TextUtils.isEmpty(value)) {
                if (TextUtils.isEmpty(tag)) {
                    Logger.e(value)
                } else {
                    Logger.t(tag)
                        .e(value)
                }
            }
        }
    }

    @JvmStatic
    fun e(value: Any) {
        e(value = value, tag = "")
    }

    @JvmStatic
    fun e(value: String) {
        e(value = value, tag = "")
    }

    @JvmStatic
    fun d(tag: String = "", value: Any) {
        if (isDebug) {
            val contentValue = if (value is String) {
                value
            } else {
                value.toString()
            }

            if (!TextUtils.isEmpty(contentValue)) {
                if (TextUtils.isEmpty(tag)) {
                    Logger.d(contentValue)
                } else {
                    Logger.t(tag)
                        .d(contentValue)
                }
            }
        }
    }

    @JvmStatic
    fun d(value: Any) {
        d(value = value, tag = "")
    }

    @JvmStatic
    fun i(tag: String = "", value: Any) {
        if (isDebug) {
            val contentValue = if (value is String) {
                value
            } else {
                value.toString()
            }

            if (!TextUtils.isEmpty(contentValue)) {
                if (TextUtils.isEmpty(tag)) {
                    Logger.i(contentValue)
                } else {
                    Logger.t(tag)
                        .i(contentValue)
                }
            }
        }
    }

    fun i(value: Any) {
        i(value = value, tag = "")
    }

    @JvmStatic
    fun w(tag: String = "", value: Any) {
        if (isDebug) {
            val contentValue = if (value is String) {
                value
            } else {
                value.toString()
            }

            if (!TextUtils.isEmpty(contentValue)) {
                if (TextUtils.isEmpty(tag)) {
                    Logger.w(contentValue)
                } else {
                    Logger.t(tag)
                        .w(contentValue)
                }
            }
        }
    }

    @JvmStatic
    fun w(value: Any) {
        w(value = value, tag = "")
    }

    @JvmStatic
    fun json(value: String) {
        if (isDebug) {
            Logger.json(value)
        }
    }

    @JvmStatic
    fun xml(value: String) {
        if (isDebug) {
            Logger.xml(value)
        }
    }
}

