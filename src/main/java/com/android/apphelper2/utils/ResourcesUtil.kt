package com.android.apphelper2.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import com.android.apphelper2.app.AppHelperManager

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

    fun getDimension(context: Context, @DimenRes id: Int): Float {
        kotlin.runCatching {
            context.resources.getDimension(id)
        }
            .onSuccess {
                return it
            }
            .onFailure {
                return 0f
            }
        return 0f
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

    fun getStringMetaData(context: Context, key: String): String {
        var result: String = ""
        try {
            val metaData = context.packageManager.getApplicationInfo(AppHelperManager.mPackageName, PackageManager.GET_META_DATA).metaData
            if (metaData != null) {
                val value = metaData.getString(key)
                result = value ?: ""
            }
        } catch (ex: PackageManager.NameNotFoundException) {
            LogUtil.e("getStringMetaData ---> error: " + ex.message)
        }
        return result
    }
}