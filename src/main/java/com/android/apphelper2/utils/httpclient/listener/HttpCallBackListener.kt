package com.android.apphelper2.utils.httpclient.listener

abstract class HttpCallBackListener<T> : BaseHttpCallBackListener<T> {

    override fun onStart() {
    }

    override fun onCompletion() {
    }

    override fun onFailure(exception: Throwable) {
    }
}