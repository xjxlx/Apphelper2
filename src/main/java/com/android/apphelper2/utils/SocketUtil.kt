package com.android.apphelper2.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean

class SocketUtil {

    companion object {
        const val PORT = 6666
        const val ENCODING = "UTF-8"
        const val CLIENT_BIND_CLIENT = "client:bind:"
        private const val TAG = "Socket-Util"
        fun log(content: String) {
            LogUtil.e(TAG, content)
        }
    }

    class SocketClient {
        private val mScope: CoroutineScope by lazy {
            return@lazy CoroutineScope(Dispatchers.IO)
        }
        private var mSocket: Socket? = null
        private var mRead: BufferedReader? = null
        private var mWrite: PrintStream? = null
        private var mClientSend = ""
        private var mClientResult = ""
        private var isStop: AtomicBoolean = AtomicBoolean()
        private var mJob: Job? = null
        private var mBindServerFlag: AtomicBoolean = AtomicBoolean()

        fun initClientSocket(ip: String) {
            mClientSend = ""
            mClientResult = ""
            isStop.set(false)

            mJob = mScope.launch(Dispatchers.IO) {
                runCatching {
                    mClientSend += "client ip：$ip\n"
                    mClientSend += "client port: $PORT \n\n"
                    mClientListener?.callBack(mClientSend, mClientResult)
                    log(mClientSend)

                    try {
                        mClientSend += "client create socket ...\n\n"
                        mSocket = Socket(ip, PORT)
                    } catch (e: Exception) {
                        val msg = "client create socket error:${e.message}!\n\n"
                        mClientSend += msg
                    }
                    mClientListener?.callBack(mClientSend, mClientResult)

                    mSocket?.let { socket ->
                        val connected = socket.isConnected
                        mClientSend += "client connect: $connected ${"\n\n"}"
                        log(mClientSend)
                        mClientListener?.callBack(mClientSend, mClientResult)

                        if (connected) {
                            mRead = BufferedReader(InputStreamReader(socket.getInputStream(), ENCODING))

                            // read data
                            mClientSend += "loop wait server send message ...${"\n\n"}"
                            mClientListener?.callBack(mClientSend, mClientResult)
                            log(mClientSend)

                            while (mRead?.readLine()
                                    .also {
                                        if (it != null) {
                                            mBindServerFlag.set(true)
                                            mClientResult = it
                                            if (it.contains(CLIENT_BIND_CLIENT)) {
                                                val split = it.split(CLIENT_BIND_CLIENT)
                                                mClientSend += "server bind client success! \n"
                                                mClientSend += "bind address:${split[1]} ${"\n\n"}"
                                                mClientListener?.callBack(mClientSend, mClientResult)
                                            }
                                        } else {
                                            mBindServerFlag.set(false)
                                            mClientSend += "server disconnect the link ! ${"\n\n"}"
                                            mClientListener?.callBack(mClientSend, mClientResult)
                                        }
                                    } != null) {
                                mClientListener?.callBack(mClientSend, mClientResult)
                                log("client read server data: $mClientResult")
                            }
                        } else {
                            mClientSend += "client is not connected! ${"\n\n"}"
                            mClientListener?.callBack(mClientSend, mClientResult)
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
                    mClientSend += "${"\n\n"}client link error: ${it.message} ${"\n\n"}"
                    log(mClientSend)
                    mClientListener?.callBack(mClientSend, mClientResult)
                }
            }
        }

        /**
         * 发送数据的时候，必须是在异步线程中
         */
        fun sendClientData(content: String): Boolean {
            runCatching {
                if (isStop.get()) {
                    mClientSend += "the socket have stopped, do not send message !${"\n\n"}"
                    mClientListener?.callBack(mClientSend, mClientResult)
                    log(mClientSend)
                    return false
                }

                if (!mBindServerFlag.get()) {
                    mClientSend += "the server have stopped, do not send message !${"\n\n"}"
                    mClientListener?.callBack(mClientSend, mClientResult)
                    log(mClientSend)
                    return false
                }

                mSocket?.let {
                    val connected = it.isConnected
                    if (connected) {
                        if (mWrite == null) {
                            mWrite = PrintStream(it.getOutputStream(), true, ENCODING)
                        }

                        // send data
                        mWrite?.println(content)

                        mClientSend = content
                        mClientListener?.callBack(mClientSend, mClientResult)
                        return true
                    } else {
                        mClientSend += "socket is not connected! ${"\n\n"}"
                        mClientListener?.callBack(mClientSend, mClientResult)
                        log(mClientSend)
                    }
                }
            }.onFailure {
                mClientSend = "socket snd error: ${it.message} ${"\n\n"}"
                log(mClientSend)
                mClientListener?.callBack(mClientSend, mClientResult)
            }
            return false
        }

        interface ClientCallBackListener {
            fun callBack(send: String, result: String)
        }

        private var mClientListener: ClientCallBackListener? = null
        fun setServiceCallBackListener(clientListener: ClientCallBackListener) {
            mClientListener = clientListener
        }

        fun stop() {
            mScope.launch(Dispatchers.IO) {
                runCatching {
                    isStop.set(true)
                    runCatching {
                        mSocket?.close()
                        mSocket = null
                    }.onFailure {
                        log("server -- socket close failure!")
                    }
                    runCatching {
                        mRead?.close()
                        mRead = null
                    }.onFailure {
                        log("client -- read steam close failure!")
                    }
                    runCatching {
                        mWrite?.close()
                        mWrite = null
                    }.onFailure {
                        log("client -- send steam close failure!")
                    }
                    mJob?.cancel()
                    log("release client!")
                    mClientSend += "release server!\n\n"
                    mClientListener?.callBack(mClientSend, mClientResult)
                }.onFailure {
                    log("release client error: ${it.message}")
                }
            }
        }
    }
}