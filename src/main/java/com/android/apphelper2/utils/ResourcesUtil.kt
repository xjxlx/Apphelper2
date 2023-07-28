package com.android.apphelper2.utils

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.util.TypedValue
import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import com.android.apphelper2.app.AppHelper2

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
            val metaData = context.packageManager.getApplicationInfo(AppHelper2.mPackageName, PackageManager.GET_META_DATA).metaData
            if (metaData != null) {
                val value = metaData.getString(key)
                result = value ?: ""
            }
        } catch (ex: PackageManager.NameNotFoundException) {
            LogUtil.e("getStringMetaData ---> error: " + ex.message)
        }
        return result
    }

    /**
     * @param dp 具体的dp值
     * @return 使用标准的dp值
     */
    fun toDp(dp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().displayMetrics)
    }

    /**
     * @param px px的值
     * @return 返回一个标准的px的值
     */
    fun toPx(px: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, px, Resources.getSystem().displayMetrics)
    }

    /**
     * @param sp sp的值
     * @return 返回一个标准的sp的值
     */
    fun toSp(sp: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, Resources.getSystem().displayMetrics)
    }
}