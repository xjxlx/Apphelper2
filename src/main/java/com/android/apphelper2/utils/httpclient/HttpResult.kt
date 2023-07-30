package com.android.apphelper2.utils.httpclient

data class HttpResult<T>(var msg: String = "", var code: Int = 0, var `data`: T? = null)
