package com.android.apphelper2.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

        fun initSocketService() {
            mServerSend = ""
            mServerResult = ""

            mScope.launch {
                runCatching {
                    mServerSend += "server create server socket !\n\n"
                    mServiceListener?.callBack(mServerSend, mServerResult)
                    mServerSocket = ServerSocket(PORT)

                    mServerSocket?.let { server ->
                        mServerSend += "server wait client connect !\n\n"
                        mServiceListener?.callBack(mServerSend, mServerResult)

                        while (true) {
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
                                            while (mRead!!.readLine()
                                                    .also { mServerResult = it } != null) {
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

        fun sendServerData(content: String) {
            try {
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
                    } else {
                        mServerSend += "server is not connect ! ${"\n\n"}"
                        mServiceListener?.callBack(mServerSend, mServerResult)
                    }
                }
            } catch (e: Exception) {
                mServerSend = "server 发送失败：${e.message}\n\n"
                mServiceListener?.callBack(mServerSend, mServerResult)
                mWrite?.close()
                mWrite = null
            }
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
                mRead?.close()
                mWrite?.close()
                mSocket?.close()
                mServerSocket?.close()
                log("释放了 server!")
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

        fun initClientSocket(ip: String) {
            mClientSend = ""
            mClientResult = ""
            mScope.launch(Dispatchers.IO) {
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

                            while (mRead!!.readLine()
                                    .also { mClientResult = it } != null) {
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
        fun sendClientData(content: String) {
            runCatching {
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
        }

        interface ClientCallBackListener {
            fun callBack(send: String, result: String)
        }

        private var mClientListener: ClientCallBackListener? = null
        public fun setServiceCallBackListener(clientListener: ClientCallBackListener) {
            mClientListener = clientListener
        }

        fun stop() {
            runCatching {
                mRead?.close()
                mWrite?.close()
                mSocket?.close()
                log("释放了 client!")
            }.onFailure {
                log("释放了 client error: ${it.message}")
            }
        }
    }
}