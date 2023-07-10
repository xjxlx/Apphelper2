package com.android.apphelper2.utils.socket

import com.android.apphelper2.utils.zmq.ZmqUtil6.port
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintStream
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class SocketClientUtil {

    private val mScope: CoroutineScope by lazy {
        return@lazy CoroutineScope(Dispatchers.IO)
    }
    private var mSocket: Socket? = null
    private var mRead: BufferedReader? = null
    private var mWrite: PrintStream? = null
    private var mTraceInfo = ""
    private var isStop: AtomicBoolean = AtomicBoolean()
    private var mJob: Job? = null
    private var mBindServerFlag: AtomicBoolean = AtomicBoolean()
    private var mTraceListener: SocketListener? = null
    private var mResultListener: SocketListener? = null
    private var mRestartJob: Job? = null
    private var mIp = ""

    /**
     * 1: 绑定服务端失败，极有可能是服务端网络异常
     * 2: 客户端本身的网络出现了异常，比如断网了，或者服务端的链接被重置了
     * 3: 服务端出现了异常，例如：服务端的socket close
     */
    private var mConnectErrorType: AtomicInteger = AtomicInteger()

    fun initClientSocket(ip: String) {
        this.mIp = ip
        mTraceInfo = ""
        isStop.set(false)
        mConnectErrorType.set(0)

        trace("initClientSocket ...")

        mJob = mScope.launch(Dispatchers.IO) {
            runCatching {
                trace("ip : [ $ip ] \r\nport : [ $port ]")
                try {
                    mSocket = Socket(ip, SocketUtil.PORT)
                    mSocket?.soTimeout = 3000
                } catch (e: Exception) {
                    trace("the client connect server failure:${e.message}")
                    mConnectErrorType.set(1)
                }

                mSocket?.let { socket ->
                    val connected = socket.isConnected
                    if (connected) {
                        trace("the client is connect server success!")
                        mRead = BufferedReader(InputStreamReader(socket.getInputStream(), SocketUtil.ENCODING))
                        mWrite = PrintStream(socket.getOutputStream(), true, SocketUtil.ENCODING)
                        mConnectErrorType.set(0)

                        // read data
                        trace("loop wait read server send message ...")
                        while (mRead?.readLine()
                                .also {
                                    if (it != null) {
                                        mBindServerFlag.set(true)
                                        if (it.contains(SocketUtil.CLIENT_BIND_CLIENT)) {
                                            val split = it.split(SocketUtil.CLIENT_BIND_CLIENT)
                                            trace("server bind success:${split[1]}")
                                        } else {
                                            mResultListener?.callBackListener(it)
                                        }
                                    } else {
                                        mBindServerFlag.set(false)
                                        trace("\nserver disconnect the link !")
                                        mConnectErrorType.set(3)
                                    }
                                } != null) {
                        }
                    } else {
                        trace("client is not connected!")
                    }
                }
            }.onFailure {
                runCatching {
                    mRead?.close()
                    mRead = null
                }
                runCatching {
                    mSocket?.close()
                    mSocket = null
                }
                trace("\nclient link failure:${it.message}")
                mConnectErrorType.set(2)
            }
        }
    }

    /**
     * 发送数据的时候，必须是在异步线程中
     */
    fun send(content: String): Boolean {
        runCatching {
            if (isStop.get()) {
                trace("the socket is stop, can not send message !")
                return false
            }
            if (!mBindServerFlag.get()) {
                trace("the server is stop, can not send message !")
                return false
            }

            mSocket?.let {
                val connected = it.isConnected
                if (connected) {
                    // send data
                    mWrite?.println(content)
                    trace(content, false)
                    return true
                } else {
                    trace("socket is not connected!")
                }
            }
        }.onFailure {
            trace("socket snd error: ${it.message}")
        }
        return false
    }

    fun stop() {
        mScope.launch(Dispatchers.IO) {
            runCatching {
                mRestartJob?.cancel()

                isStop.set(true)
                runCatching {
                    mSocket?.close()
                    mSocket = null
                }.onFailure {
                    trace("server -- socket close failure!")
                }
                runCatching {
                    mRead?.close()
                    mRead = null
                }.onFailure {
                    trace("client -- read steam close failure!")
                }
                runCatching {
                    mWrite?.close()
                    mWrite = null
                }.onFailure {
                    trace("client -- send steam close failure!")
                }
                mJob?.cancel()
                trace("release server!")
            }.onFailure {
                SocketUtil.log("release client error: ${it.message}")
            }
        }
    }

    fun autoRestart() {
        mRestartJob = mScope.launch(Dispatchers.IO) {
            while (true) {
                delay(5000)
                if (!isStop.get()) {
                    val type = mConnectErrorType.get()

                    if (type == 1 || type == 2 || type == 3) {
                        trace("try restart socket !")
                        initClientSocket(mIp)
                    }
                }
            }
        }
    }

    private fun trace(content: String, automaticAdd: Boolean = true) {
        if (automaticAdd) {
            mTraceInfo += (content + "\n\n")
        } else {
            mTraceInfo = content
        }
        mTraceListener?.callBackListener(mTraceInfo)
    }

    fun setTraceListener(serviceListener: SocketListener) {
        mTraceListener = serviceListener
    }

    fun setResultListener(listener: SocketListener) {
        this.mResultListener = listener
    }
}