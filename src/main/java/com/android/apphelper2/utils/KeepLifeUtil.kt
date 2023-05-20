package com.android.apphelper2.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import kotlinx.coroutines.*

object KeepLifeUtil {
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
    private val mKeepScope: CoroutineScope by lazy {
        return@lazy CoroutineScope(Dispatchers.IO)
    }
    private var mJob: Job? = null

    /**
     * 发送一个停止的广播
     */
    fun sendAppRunningBroadcast(context: Context, packageName: String) {
        this.mKeepLifePackage = packageName
        context.sendBroadcast(mKeepLifeIntent)
    }

    fun sendAppRunningReceiver(packageName: String, delay: Long) {
        // first cancel
        mJob?.cancel()
        // second send broadcast
        mJob = mKeepScope.launch {
            delay(delay)
            LogUtil.e("开始执行重启操作！！！")
        }
    }
}