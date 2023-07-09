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
        private const val CLIENT_BIND_CLIENT = "client:bind:"
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
        private var mClientConnectFlag = false;

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
                                mClientConnectFlag = true
                                mRead = BufferedReader(InputStreamReader(mSocket?.getInputStream(), ENCODING))
                                mWrite = PrintStream(mSocket?.getOutputStream(), true, ENCODING)

                                val address = mSocket?.inetAddress
                                if (address != null) {
                                    mServerSend += "client connect success, client host address：${address.hostAddress} client host name：${address.hostName}\n\n"
                                    log(mServerSend)
                                    // when the binding is successful ,send a message to the client ,telling it that link was successful
                                    mWrite?.println(CLIENT_BIND_CLIENT + address.hostAddress)
                                }

                                mScope.launch(Dispatchers.IO) {
                                    val connected = mSocket!!.isConnected
                                    mServerSend += "loop wait client send the message ...：\n\n"
                                    log(mServerSend)
                                    mServiceListener?.callBack(mServerSend, mServerResult)

                                    if (connected) {
                                        try {
                                            while (mRead?.readLine()
                                                    .also {
                                                        if (it != null) {
                                                            mServerResult = it
                                                        } else {
                                                            mClientConnectFlag = false
                                                            mServerSend += "client disconnect the link ！\n\n"
                                                            mServiceListener?.callBack(mServerSend, mServerResult)
                                                        }
                                                    } != null) {
                                                mServiceListener?.callBack(mServerSend, mServerResult)
                                            }
                                        } catch (e: IOException) {
                                            mServerSend += "loop reading the client message abnormal！\n\n"
                                            mServiceListener?.callBack(mServerSend, mServerResult)
                                        }
                                    } else {
                                        mServerSend += "the client link failure！\n\n"
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
                    mServerSend += "socket is stop , do not send data ! \n\n"
                    mServiceListener?.callBack(mServerSend, mServerResult)
                    return false
                }

                if (!mClientConnectFlag) {
                    mServerSend += "client connect is lost ! \n\n"
                    mServiceListener?.callBack(mServerSend, mServerResult)
                    return false
                }

                if (mSocket != null) {
                    mSocket?.let {
                        val connected = it.isConnected
                        mServerSend = "socket is connect: $connected\n\n"
                        mServiceListener?.callBack(mServerSend, mServerResult)
                        if (connected) {
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
                mServerSend = "server send data failure：${e.message}\n\n"
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
                    log("server -- socket close failure！")
                }
                runCatching {
                    mServerSocket?.close()
                    mServerSocket = null
                }.onFailure {
                    log("server -- close failure！")
                }

                runCatching {
                    mRead?.close()
                    mRead = null
                }.onFailure {
                    log("server -- close reading steam failure！")
                }
                runCatching {
                    mWrite?.close()
                    mWrite = null
                }.onFailure {
                    log("server -- close send steam failure ！")
                }

                mJob?.cancel()
                log("release server!")
                mServerSend += "release server!\n\n"
                mServiceListener?.callBack(mServerSend, mServerResult)
            }.onFailure {
                log("release server error: ${it.message}")
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
        private var mBindServerFlag = false;

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
                                            mBindServerFlag = true
                                            mClientResult = it
                                            if (it.contains(CLIENT_BIND_CLIENT)) {
                                                val split = it.split(CLIENT_BIND_CLIENT)
                                                mClientSend += "server bind client success, address:${split[1]} ${"\n\n"}"
                                                mClientListener?.callBack(mClientSend, mClientResult)
                                            }
                                        } else {
                                            mBindServerFlag = false
                                            mClientSend += "server disconnect the client ! ${"\n\n"}"
                                            mClientListener?.callBack(mClientSend, mClientResult)
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
                    mClientSend += "${"\n\n"}client error: ${it.message} ${"\n\n"}"
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