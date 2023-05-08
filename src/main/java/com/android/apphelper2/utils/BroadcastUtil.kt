package com.android.apphelper2.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object BroadcastUtil {

    fun isBroadcastReceiverRegistered(context: Context, receiverClass: Class<*>?): Boolean {
        val receiverName = ComponentName(context, receiverClass!!)
        try {
            val receiverInfo = context.packageManager.getReceiverInfo(receiverName, PackageManager.GET_META_DATA)
            return true
        } catch (e: PackageManager.NameNotFoundException) { // 广播接收器未注册，返回 false
        }
        return false
    }
}