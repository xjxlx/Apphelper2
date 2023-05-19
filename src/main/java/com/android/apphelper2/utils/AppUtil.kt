package com.android.apphelper2.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.text.TextUtils

object AppUtil {

    private var mKeepLifePackage: String = ""
    private val mKeepLifeIntent: Intent by lazy {
        return@lazy Intent().apply {
            if (!TextUtils.isEmpty(mKeepLifePackage)) {
                action = mKeepLifePackage
                setPackage(mKeepLifePackage)
                component = ComponentName(mKeepLifePackage, "${mKeepLifePackage}.keeplife.KeepLifeReceiver")
            }
        }
    }

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

    fun sendAppRunningBroadcast(context: Context, packageName: String) {
        this.mKeepLifePackage = packageName
        context.sendBroadcast(mKeepLifeIntent)
    }
}