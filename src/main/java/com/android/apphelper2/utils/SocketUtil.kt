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

class SocketUtil {

    companion object {
        private const val PORT = 6666
        private const val TAG = "Socket-Util"
        private const val ENCODING = "UTF-8"
        fun log(content: String) {
            LogUtil.e(TAG, content)
        }
    }

    class SocketService {
        private val mScope: CoroutineScope by lazy {
            return@lazy CoroutineScope(Dispatchers.IO)
        }
        private var mServerSocket: ServerSocket? = null
        private var mSocket: Socket? = null
        private var mRead: BufferedReader? = null
        private var mWrite: PrintStream? = null
        private var mServerSend = ""
        private var mServerResult = ""
        private var mLoopFlag = true
        private var isStop = false
        private var mJob: Job? = null

        fun initSocketService() {
            mServerSend = ""
            mServerResult = ""
            isStop = false
            mLoopFlag = true

            mJob = mScope.launch {
                runCatching {
                    mServerSend += "server create server socket !\n\n"
                    mServiceListener?.callBack(mServerSend, mServerResult)
                    mServerSocket = ServerSocket(PORT)

                    mServerSocket?.let { server ->
                        mServerSend += "server wait client connect !\n\n"
                        mServiceListener?.callBack(mServerSend, mServerResult)

                        while (mLoopFlag) {
                            // block thread ,wait client connect
                            mSocket = server.accept()
                            if (mSocket != null) {
                                val address = mSocket!!.inetAddress
                                if (address != null) {
                                    mServerSend += "客户端链接成功，客户端地址：${address.hostAddress} 客户端名字：${address.hostName}\n\n"
                                    log(mServerSend)
                                }

                                mScope.launch(Dispatchers.IO) {
                                    val connected = mSocket!!.isConnected
                                    mServerSend += "客户端链接成功：$connected\n\n"
                                    log(mServerSend)
                                    mServiceListener?.callBack(mServerSend, mServerResult)

                                    if (connected) {
                                        mRead = BufferedReader(InputStreamReader(mSocket!!.getInputStream(), ENCODING))

                                        try {
                                            while (mRead?.readLine()
                                                    .also {
                                                        if (it != null) {
                                                            mServerResult = it
                                                        }
                                                    } != null) {
                                                mServiceListener?.callBack(mServerSend, mServerResult)
                                            }
                                            mServerSend += "客户端断开了链接！\n\n"
                                            mServiceListener?.callBack(mServerSend, mServerResult)
                                        } catch (e: IOException) {
                                            mServerSend += "客户端读取数据异常！\n\n"
                                            mServiceListener?.callBack(mServerSend, mServerResult)
                                        }
                                    } else {
                                        mServerSend += "客户端链接失败！\n\n"
                                        mServiceListener?.callBack(mServerSend, mServerResult)
                                    }
                                }
                            }
                        }
                    }
                }.onFailure {
                    runCatching {
                        mRead?.close()
                        mRead = null
                    }
                    val msg = "server socket error: " + it.message + "\r\n"
                    log(msg)
                    mServerSend += msg
                    mServiceListener?.callBack(mServerSend, mServerResult)
                }
            }
        }

        fun sendServerData(content: String): Boolean {
            try {
                if (isStop) {
                    mServerSend += "socket is stop! \n\n"
                    mServiceListener?.callBack(mServerSend, mServerResult)
                    return false
                }

                if (mSocket != null) {
                    mSocket?.let {
                        val connected = it.isConnected
                        mServerSend = "socket is connect: $connected\n\n"
                        mServiceListener?.callBack(mServerSend, mServerResult)
                        if (connected) {
                            if (mWrite == null) {
                                mWrite = PrintStream(it.getOutputStream(), true, ENCODING)
                            }
                            mWrite?.println(content)
                            mServerSend = content
                            mServiceListener?.callBack(mServerSend, mServerResult)
                            return true
                        } else {
                            mServerSend += "server is not connect ! ${"\n\n"}"
                            mServiceListener?.callBack(mServerSend, mServerResult)
                        }
                    }
                } else {
                    mServerSend += "please wait socket connect ! ${"\n\n"}"
                    mServiceListener?.callBack(mServerSend, mServerResult)
                }
            } catch (e: Exception) {
                mServerSend = "server 发送失败：${e.message}\n\n"
                mServiceListener?.callBack(mServerSend, mServerResult)
                mWrite?.close()
                mWrite = null
            }
            return false
        }

        interface ServerCallBackListener {
            fun callBack(send: String, result: String)
        }

        private var mServiceListener: ServerCallBackListener? = null
        fun setServiceCallBackListener(serviceListener: ServerCallBackListener) {
            mServiceListener = serviceListener
        }

        fun stop() {
            runCatching {
                isStop = true
                mLoopFlag = false

                runCatching {
                    mSocket?.close()
                    mSocket = null
                }.onFailure {
                    log("server -- socket关闭异常！")
                }
                runCatching {
                    mServerSocket?.close()
                    mServerSocket = null
                }.onFailure {
                    log("server -- 关闭异常！")
                }

                runCatching {
                    mRead?.close()
                    mRead = null
                }.onFailure {
                    log("server -- 读取流关闭异常！")
                }
                runCatching {
                    mWrite?.close()
                    mWrite = null
                }.onFailure {
                    log("server -- 发送流关闭异常！")
                }

                mJob?.cancel()
                log("释放了 server!")
                mServerSend += "释放了 server!\n\n"
                mServiceListener?.callBack(mServerSend, mServerResult)
            }.onFailure {
                log("释放了 server error: ${it.message}")
            }
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
        private var isStop = false
        private var mJob: Job? = null

        fun initClientSocket(ip: String) {
            mClientSend = ""
            mClientResult = ""
            isStop = false

            mJob = mScope.launch(Dispatchers.IO) {
                runCatching {
                    mSocket = Socket(ip, PORT)
                    mClientSend += "client 创建 socket: ip：$ip port: $PORT ${"\n\n"}"
                    mClientListener?.callBack(mClientSend, mClientResult)
                    log(mClientSend)

                    mSocket?.let { socket ->
                        val connected = socket.isConnected
                        mClientSend += "client connect: $connected ${"\n\n"}"
                        log(mClientSend)
                        mClientListener?.callBack(mClientSend, mClientResult)

                        if (connected) {
                            mRead = BufferedReader(InputStreamReader(socket.getInputStream(), ENCODING))
                            // read data

                            mClientSend += "client wait ...${"\n\n"}"
                            mClientListener?.callBack(mClientSend, mClientResult)
                            log(mClientSend)

                            while (mRead?.readLine()
                                    .also {
                                        if (it != null) {
                                            mClientResult = it
                                        }
                                    } != null) {
                                mClientListener?.callBack(mClientSend, mClientResult)
                                log("client read data: $mClientResult")
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
                    mClientSend += "client error: ${it.message} ${"\n\n"}"
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
                if (isStop) {
                    mClientSend += "the socket has been stopped \n ! ${"\n\n"}"
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
            runCatching {
                isStop = true

                runCatching {
                    mSocket?.close()
                    mSocket = null
                }.onFailure {
                    log("server -- socket关闭异常！")
                }

                runCatching {
                    mRead?.close()
                    mRead = null
                }.onFailure {
                    log("client -- 读取流关闭异常！")
                }
                runCatching {
                    mWrite?.close()
                    mWrite = null
                }.onFailure {
                    log("client -- 发送流关闭异常！")
                }

                mJob?.cancel()
                log("释放了 client!")

                mClientSend += "释放了 server!\n\n"
                mClientListener?.callBack(mClientSend, mClientResult)
            }.onFailure {
                log("释放了 client error: ${it.message}")
            }
        }
    }
}