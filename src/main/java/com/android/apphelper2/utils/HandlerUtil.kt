package com.android.apphelper2.utils

import android.os.Handler
import android.os.Looper
import android.os.Message

class HandlerUtil {

    private var mListener: HandlerMessageListener? = null
    private val mHandler: Handler by lazy {
        return@lazy Handler(Looper.getMainLooper()) { msg ->
            mListener?.handleMessage(msg)
            false
        }
    }

    fun getMessage(): Message {
        return mHandler.obtainMessage()
    }

    fun send(msg: Message) {
        mHandler.sendMessage(msg)
    }

    public interface HandlerMessageListener {
        fun handleMessage(msg: Message)
    }

    public fun setHandlerCallBackListener(listener: HandlerMessageListener) {
        this.mListener = listener
    }
}