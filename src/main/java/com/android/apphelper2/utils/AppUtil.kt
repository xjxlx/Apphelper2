package com.android.apphelper2.utils

import android.content.Context
import android.content.pm.PackageManager
import com.android.apphelper2.app.AppHelperManager.packageName

class AppUtil {

    fun getStringMetaData(context: Context, key: String): String {
        var result: String = ""
        try {
            val metaData = context.packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA).metaData
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