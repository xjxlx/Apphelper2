package com.android.apphelper2.utils.httpclient

import com.android.apphelper2.utils.LogUtil
import com.android.apphelper2.utils.httpclient.listener.HttpCallBackListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

/**
 * @author : 流星
 * @CreateDate: 2023/3/15-14:22
 * @Description:
 */
object HttpClient {

    /**
     * @param T Api的类型
     * @param Result 数据返回的类型
     * @param block Api的具体方法
     * @return 返回个Flow的流，一般用来转换数据，这个是不用参数的类型
     */
    @JvmStatic
    suspend inline fun <reified T, Result> http(crossinline block: suspend T.() -> Result) = callbackFlow {
        try {
            val bean = RetrofitHelper.create(T::class.java)
                .block()
            // send request data
            trySend(bean)
        } catch (exception: Throwable) {
            exception.printStackTrace()
            close(exception)
        }
        // close callback
        awaitClose()
    }.flowOn(Dispatchers.IO)

    /**
     * @param T Api的类型
     * @param Parameter 参数的具体类型，一般传递map集合，例如：MutableMap<String, Any>
     * @param Result 数据返回的类型
     * @param block Api的具体方法
     * @return 返回个Flow的流，一般用来转换数据，这个是使用参数的类型
     */
    @JvmStatic
    suspend inline fun <reified T, Parameter, Result> http(crossinline block: suspend T.(Parameter) -> Result, p: Parameter) =
        callbackFlow {
            try {
                // LogUtil.e("http thread callbackFlow :" + Thread.currentThread().name)
                val bean = RetrofitHelper.create(T::class.java)
                    .block(p)
                // send request data
                trySend(bean)
            } catch (exception: Throwable) {
                exception.printStackTrace()
                close(exception)
            }
            // close callback
            awaitClose()
        }.flowOn(Dispatchers.IO)

    /**
     * @param T Api的对象
     * @param Parameter 参数的具体类型，一般传递map集合，例如：MutableMap<String, Any>
     * @param Result 数据类型
     * @param block 里面传递Api具体的方法
     * @return 这个方法用来请求单参数的方法
     */
    @JvmStatic
    suspend inline fun <reified T, Parameter, Result> http(crossinline block: suspend T.(Parameter) -> Result, p: Parameter,
                                                           callback: HttpCallBackListener<Result>) {

        http<T, Parameter, Result>({ block(it) }, p).onStart {
            LogUtil.e("http thread started :" + Thread.currentThread().name)
            callback.onStart()
        }
            .catch {
                it.printStackTrace()
                callback.onFailure(it)
            }
            .onCompletion {
                callback.onCompletion()
            }
            .collect {
                callback.onSuccess(it)
            }
    }

    /**
     * @param T Api的对象
     * @param Result 数据类型
     * @param block 里面传递Api具体的方法
     * @return 这个方法用来请求不带单参数的方法
     */
    @JvmStatic
    suspend inline fun <reified T, Result> http(crossinline block: suspend T.() -> Result, callback: HttpCallBackListener<Result>) {
        http<T, Result> { block() }.onStart {
            LogUtil.e("http thread started :" + Thread.currentThread().name)
            callback.onStart()
        }
            .catch {
                it.printStackTrace()
                callback.onFailure(it)
            }
            .onCompletion {
                callback.onCompletion()
            }
            .collect {
                callback.onSuccess(it)
            }
    }
}
