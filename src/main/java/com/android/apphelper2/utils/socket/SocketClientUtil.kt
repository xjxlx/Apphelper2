package com.android.apphelper2.utils.socket

import com.android.apphelper2.utils.SocketUtil
import com.android.apphelper2.utils.zmq.ZmqUtil6.port
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintStream
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

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

    fun initClientSocket(ip: String) {
        mTraceInfo = ""
        isStop.set(false)

        trace("initClientSocket ...")

        mJob = mScope.launch(Dispatchers.IO) {
            runCatching {
                trace("ip : [ $ip ] \r\nport : [ $port ]")

                try {
                    mSocket = Socket(ip, SocketUtil.PORT)
                } catch (e: Exception) {
                    trace("client connect server failure:${e.message}")
                }

                mSocket?.let { socket ->
                    val connected = socket.isConnected
                    if (connected) {
                        trace("client is connect success!")
                        mRead = BufferedReader(InputStreamReader(socket.getInputStream(), SocketUtil.ENCODING))
                        mWrite = PrintStream(socket.getOutputStream(), true, SocketUtil.ENCODING)

                        // read data
                        trace("loop wait read server send message ...")
                        while (mRead?.readLine()
                                .also {
                                    if (it != null) {
                                        mBindServerFlag.set(true)
                                        if (it.contains(SocketUtil.CLIENT_BIND_CLIENT)) {
                                            val split = it.split(SocketUtil.CLIENT_BIND_CLIENT)
                                            trace("server bind client success:${split[1]}")
                                        } else {
                                            mResultListener?.callBackListener(it)
                                        }
                                    } else {
                                        mBindServerFlag.set(false)
                                        trace("server disconnect the link !")
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
                trace("client link failure:${it.message}")
            }
        }
    }

    /**
     * 发送数据的时候，必须是在异步线程中
     */
    fun send(content: String): Boolean {
        runCatching {
            if (isStop.get()) {
                trace("the socket have stopped, do not send message !")
                return false
            }
            if (!mBindServerFlag.get()) {
                trace("the server have stopped, do not send message !")
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
            trace("\"socket snd error: ${it.message}")
        }
        return false
    }

    fun stop() {
        mScope.launch(Dispatchers.IO) {
            runCatching {
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