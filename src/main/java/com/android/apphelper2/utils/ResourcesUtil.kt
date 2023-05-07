package com.android.apphelper2.utils

import android.content.Context
import androidx.annotation.StringRes

object ResourcesUtil {

    /**
     * Returns the res ---> string content
     */
    fun getString(context: Context, @StringRes id: Int): String {
        kotlin.runCatching {
            context.resources.getString(id)
        }
            .onSuccess {
                return it
            }
            .onFailure {
                return ""
            }
        return ""
    }

    fun getStringForReflect(context: Context, id: String): String {
        runCatching {
            // 获取包名
            val packageName: String = context.packageName
            val resources = context.resources
            // 获取 R.string 对应的资源 ID
            val resId: Int = resources.getIdentifier(id, "string", packageName)
            // 获取字符串
            if (resId != 0) {
                return resources.getString(resId)
            } else {
                return ""
            }
        }.onFailure {
            return ""
        }
        return ""
    }
}