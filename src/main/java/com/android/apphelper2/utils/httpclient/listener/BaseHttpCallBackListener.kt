package com.android.apphelper2.utils.httpclient.listener

/**
 * @author : 流星
 * @CreateDate: 2023/3/15-17:31
 * @Description:
 */
interface BaseHttpCallBackListener<T> {
    fun onStart()
    fun onSuccess(t: T)
    fun onFailure(exception: Throwable)
    fun onCompletion()
}