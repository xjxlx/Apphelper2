package com.android.apphelper2.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import kotlinx.coroutines.*

object KeepLifeBroadCast {

    private var mPackageName: String = ""
    private val mKeepLifeIntent: Intent by lazy {
        return@lazy Intent().apply {
            if (!TextUtils.isEmpty(mPackageName)) {
                action = mPackageName
                setPackage(mPackageName)
                component = ComponentName(mPackageName, "${mPackageName}.keeplife.KeepLifeReceiver")
            }
        }
    }
    private val mKeepScope: CoroutineScope by lazy {
        return@lazy CoroutineScope(Dispatchers.IO)
    }
    private var mJob: Job? = null

    /**
     * 发送一个异常停止的广播
     */
    fun sendAppErrorBroadcast(context: Context, packageName: String) {
        this.mPackageName = packageName
        mKeepLifeIntent.putExtra("type", "close")
        LogUtil.e("发送一个异常停止的广播！")
        context.sendBroadcast(mKeepLifeIntent)
    }

    /**
     * 发送一个轮询侦听的广播
     */
    fun sendAppPollListenerBroadcast(context: Context, packageName: String, delay: Long) {
        this.mPackageName = packageName
        LogUtil.e("发送一个轮询侦听的广播！")
        // first cancel
        mJob?.cancel()
        // second send broadcast
        mJob = mKeepScope.launch {
            while (true) {
                mKeepLifeIntent.putExtra("type", "listener")
                context.sendBroadcast(mKeepLifeIntent)
                delay(delay)
            }
        }
    }

    /**
     * 关闭监听
     */
    fun closeListener() {
        mJob?.cancel()
        mKeepScope.cancel()
    }
}