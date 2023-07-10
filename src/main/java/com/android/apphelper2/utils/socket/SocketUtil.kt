package com.android.apphelper2.utils.socket

import com.android.apphelper2.utils.LogUtil

object SocketUtil {

    const val PORT = 6666
    const val ENCODING = "UTF-8"
    const val CLIENT_BIND_CLIENT = "client:bind:"
    private const val TAG = "Socket-Util"
    fun log(content: String) {
        LogUtil.e(TAG, content)
    }
}