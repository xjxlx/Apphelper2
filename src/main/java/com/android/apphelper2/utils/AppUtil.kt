package com.android.apphelper2.utils

import android.content.Context

object AppUtil {

    /**
     * 通过context去获取完整的包名
     */
    fun getPackageName(context: Context): String {
        var result = ""
        runCatching {
            val manager = context.packageManager
            val packageInfo = manager.getPackageInfo(context.packageName, 0)
            result = packageInfo.packageName
        }
        return result
    }
}