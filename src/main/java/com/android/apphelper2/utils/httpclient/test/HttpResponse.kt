package com.android.apphelper2.utils.httpclient.test

data class HttpResponse<T>(val `data`: T?, val msg: String, val ret: Int)