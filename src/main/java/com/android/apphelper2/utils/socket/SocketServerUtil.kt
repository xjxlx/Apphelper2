package com.android.apphelper2.utils.socket

import com.android.apphelper2.utils.SocketUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

class SocketServerUtil {

    private val mScope: CoroutineScope by lazy {
        return@lazy CoroutineScope(Dispatchers.IO)
    }
    private var mServerSocket: ServerSocket? = null
    private var mSocket: Socket? = null
    private var mRead: BufferedReader? = null
    private var mWrite: PrintStream? = null
    private var mTraceInfo = ""
    private var mServerResult = ""
    private var mLoopFlag: AtomicBoolean = AtomicBoolean()
    private var isStop: AtomicBoolean = AtomicBoolean()
    private var mJob: Job? = null
    private var mClientConnected = false
    private var mResultListener: SocketListener? = null
    private var mTraceListener: SocketListener? = null

    fun initSocketService() {
        mTraceInfo = ""
        mServerResult = ""
        isStop.set(false)
        mLoopFlag.set(true)

        trace("initSocketService--->")

        mJob = mScope.launch(Dispatchers.IO) {
            runCatching {
                mServerSocket = ServerSocket(SocketUtil.PORT)
                trace("server create socket success.")
            }.onFailure {
                trace("server create socket failure: ${it.message}")
            }

            runCatching {
                mServerSocket?.let { server ->
                    trace("loop wait client connect ...")
                    while (mLoopFlag.get()) {
                        // block thread ,wait client connect
                        runCatching {
                            mSocket = server.accept()
                            mClientConnected = true
                        }.onFailure {
                            runCatching {
                                trace("server socket accept error: ${it.message}")
                                mClientConnected = true
                                mSocket?.close()
                                mSocket = null
                            }
                        }
                        mSocket?.let {
                            runCatching {
                                if (mClientConnected) {
                                    mRead = BufferedReader(InputStreamReader(mSocket?.getInputStream(), SocketUtil.ENCODING))
                                    mWrite = PrintStream(mSocket?.getOutputStream(), true, SocketUtil.ENCODING)

                                    val address = it.inetAddress
                                    if (address != null) {
                                        trace("client connect success, address：${address.hostAddress}")
                                        // send a message to the client,tell client bind address info
                                        mWrite?.println(SocketUtil.CLIENT_BIND_CLIENT + address.hostAddress)
                                    }

                                    trace("loop wait start read data ...")

                                    // loop wait for a message sent by client
                                    mScope.launch(Dispatchers.IO) {
                                        if (it.isConnected) {
                                            while (mRead?.readLine()
                                                    .also { read ->
                                                        if (read != null) {
                                                            mServerResult = read
                                                            mClientConnected = true
                                                        } else {
                                                            mClientConnected = false
                                                            trace("the client connect lost !")
                                                        }
                                                    } != null) {
                                                mResultListener?.callBackListener(mServerResult)
                                            }
                                        } else {
                                            trace("client is not connect!")
                                        }
                                    }
                                } else {
                                    trace("server socket is closed!")
                                }
                            }.onFailure {
                                trace("server socket read failure: ${it.message}")
                                runCatching {
                                    mRead?.close()
                                    mRead = null
                                }
                                runCatching {
                                    mWrite?.close()
                                    mWrite = null
                                }
                            }
                        }
                    }
                }
            }.onFailure {
                trace("server socket failure: ${it.message}")
            }
        }
    }

    fun send(content: String): Boolean {
        try {
            if (isStop.get()) {
                trace("socket is stop, cant not send data ")
                return false
            }

            if (!mClientConnected) {
                trace("client connect is lost.")
                return false
            }

            mSocket?.let {
                val connected = it.isConnected
                if (connected) {
                    mWrite?.println(content)
                    trace(content, false)
                    return true
                } else {
                    trace("server is not connect!")
                }
            }
        } catch (e: Exception) {
            runCatching {
                mWrite?.close()
                mWrite = null
            }
        }
        return false
    }

    fun stop() {
        mScope.launch(Dispatchers.IO) {
            runCatching {
                isStop.set(true)
                mLoopFlag.set(false)
                runCatching {
                    mRead?.close()
                    mRead = null
                }.onFailure {
                    trace("server -- close reading steam failure！")
                }
                runCatching {
                    mWrite?.close()
                    mWrite = null
                }.onFailure {
                    trace("server -- close send steam failure ！")
                }
                runCatching {
                    mSocket?.close()
                    mSocket = null
                }.onFailure {
                    trace("server -- socket close failure！")
                }
                runCatching {
                    mServerSocket?.close()
                    mServerSocket = null
                }.onFailure {
                    trace("server -- close failure！")
                }
                mJob?.cancel()
                trace("release server!")
            }.onFailure {
                SocketUtil.log("release server error: ${it.message}")
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
        SocketUtil.log(content)
    }

    fun setTraceListener(serviceListener: SocketListener) {
        mTraceListener = serviceListener
    }

    fun setResultListener(listener: SocketListener) {
        this.mResultListener = listener
    }
}