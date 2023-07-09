package com.android.apphelper2.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
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
        private var mBufferedReader: BufferedReader? = null
        private var mServerSend = ""
        private var mServerResult = ""
        private var mPrintStream: PrintStream? = null

        fun initSocketService() {
            mScope.launch {
                mServerSend = ""
                mServerResult = ""

                runCatching {
                    mServerSocket = ServerSocket(PORT)
                    mServerSocket?.let { server ->
                        mServerSend += "server create server socket !\n\n"
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
                                        mBufferedReader = BufferedReader(InputStreamReader(mSocket!!.getInputStream(), ENCODING))
                                        while (mBufferedReader!!.readLine()
                                                .also { mServerResult = it } != null) {
                                            mServiceListener?.callBack(mServerSend, mServerResult)
                                        }
                                        mServerSend += "客户端断开了链接！\n\n"
                                        mServiceListener?.callBack(mServerSend, mServerResult)
                                    }
                                }
                            }
                        }
                    }
                }.onFailure {
                    runCatching {
                        mBufferedReader?.close()
                        mBufferedReader = null
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
                        if (mPrintStream == null) {
                            mPrintStream = PrintStream(it.getOutputStream(), true, ENCODING)
                        }
                        mPrintStream?.println(content)
                        mServerSend = content
                        mServiceListener?.callBack(mServerSend, mServerResult)
                    } else {
                        mServerSend += "server is not connect ! ${"\n\n"}"
                        mServiceListener?.callBack(mServerSend, mServerResult)
                    }
                }
            } catch (e: Exception) {
                mServerSend += "server 发送失败：${e.message}\n\n"
                mServiceListener?.callBack(mServerSend, mServerResult)
                mPrintStream?.close()
                mPrintStream = null
            }
        }

        interface ServerCallBackListener {
            fun callBack(send: String, result: String)
        }

        private var mServiceListener: ServerCallBackListener? = null
        public fun setServiceCallBackListener(serviceListener: ServerCallBackListener) {
            mServiceListener = serviceListener
        }
    }

    class SocketClient {
        private val mScope: CoroutineScope by lazy {
            return@lazy CoroutineScope(Dispatchers.IO)
        }
        private var mSocket: Socket? = null
        private var mBufferedReader: BufferedReader? = null
        private var mClientSend = ""
        private var mClientResult = ""
        private var mClientPrintStream: PrintStream? = null

        fun initClientSocket(ip: String) {
            mClientSend = ""
            mClientResult = ""
            mScope.launch {
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
                            mBufferedReader = BufferedReader(InputStreamReader(socket.getInputStream(), ENCODING))

                            while (mBufferedReader!!.readLine()
                                    .also { mClientResult = it } != null) {
                                mClientListener?.callBack(mClientSend, mClientResult)
                            }
                        } else {
                            mClientSend += "client is not connected! ${"\n\n"}"
                            mClientListener?.callBack(mClientSend, mClientResult)
                        }
                    }
                }.onFailure {
                    runCatching {
                        mBufferedReader?.close()
                        mBufferedReader = null
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

        fun sendClientData(content: String) {
            runCatching {
                mSocket?.let {
                    val connected = it.isConnected
                    if (connected) {
                        if (mClientPrintStream == null) {
                            mClientPrintStream = PrintStream(it.getOutputStream(), true, ENCODING)
                        }
                        // send client data
                        mClientPrintStream?.println(content)
                        mClientResult = content
                        mClientListener?.callBack(mClientSend, mClientResult)
                    } else {
                        mClientSend += "socket is not connected! ${"\n\n"}"
                        mClientListener?.callBack(mClientSend, mClientResult)
                        log(mClientSend)
                    }
                }
            }.onFailure {
                mClientSend += "socket snd error: ${it.message} ${"\n\n"}"
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
    }
}