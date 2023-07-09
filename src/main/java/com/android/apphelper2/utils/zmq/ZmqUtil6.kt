package com.android.apphelper2.utils.zmq

import com.android.apphelper2.utils.LogUtil.e
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import org.zeromq.ZMQException

object ZmqUtil6 {

    val port = 6667
    private var socketResult: ZMQ.Socket? = null
    private var socketClient: ZMQ.Socket? = null
    private var number: Int = 0
    private val mScope: CoroutineScope by lazy {
        return@lazy CoroutineScope(Dispatchers.IO)
    }

    private val mClientBuffer: StringBuffer = StringBuffer()
    private fun initZContext() {
        log("创建 context ---->")
        if (mContext == null) {
            mContext = ZContext(1)
        }
    }

    class Result {
        private val mServiceSend: StringBuffer = StringBuffer()
        private var mResultResultData = ""
        private var mResultFlag = false

        interface ResultCallBackListener {
            fun onCall(send: String, result: String)
        }

        private var resultListener: ResultCallBackListener? = null
        fun setResultCallBackListener(listener: ResultCallBackListener?) {
            this.resultListener = listener
        }

        /**
         * 接收端代码
         */
        fun initResultZmq(ipAddress: String) {
            log("init zmq server !---> :  $ipAddress")
            mServiceSend.setLength(0)
            mResultResultData = ""

            try {
                if (mContext == null) {
                    log("create context ---->")
                    mContext = ZContext(1)
                }

                mScope.launch {
                    try {
                        // Socket to talk to clients
                        var bind: Boolean? = false
                        if (!mResultFlag) {
                            socketResult = mContext?.createSocket(SocketType.PAIR)
                            log("create socketService !")
                            try {
                                bind = socketResult?.bind(ipAddress)
                                log("bind ip success: $bind")
                            } catch (e: ZMQException) {
                                log("bind ip failure: ${e.message}")
                            }
                        }

                        log("loop wait client connect ...")

                        while (!Thread.currentThread().isInterrupted) {
                            // Block until a message is received
                            val reply: ByteArray = socketResult!!.recv(0)
                            mResultFlag = true
                            // Print the message
                            val content = String(reply, ZMQ.CHARSET)
                            mResultResultData = "接收端-->接收：$content"
                        }
                    } catch (e: ZMQException) {
                        log("receiver data error:$e")
                        e.printStackTrace()
                        mResultFlag = false
                    }
                }
            } catch (e: ZMQException) {
                log("init server error:$e")
                e.printStackTrace()
            }
        }

        suspend fun sendResult() {
            try {
                if (mResultFlag) {
                    val msg = "接收端-->发送--> $number"
                    socketResult?.send(msg.toByteArray(ZMQ.CHARSET), 0)
                    number++
                    log(msg)
                } else {
                    val msg = "发送端还没有connect成功，请等待！"
                    resultListener?.onCall(msg, mResultResultData)
                }
            } catch (e: ZMQException) {
                val msg = "接收端发送异常：$e"
                resultListener?.onCall(msg, mResultResultData)
            }
        }

        private fun log(content: String) {
            e("ZMQ", content)
            mServiceSend.append(content)
                .append("\n\n")
            resultListener?.onCall(mServiceSend.toString(), mResultResultData)
        }
    }

    /**
     * 发送端connect成功之后，接收端才可以发送
     */
    private var mSendFlag = false
    private var mSendSendData = ""
    private var mSendResultData = ""

    /**
     * 发送端代码
     */
    fun initSendZmq(tcpAddress: String) {
        mSendSendData = ""
        mSendResultData = ""

        mClientBuffer.setLength(0)
        initZContext()
        log("客户端连接--->$tcpAddress")

        mScope.launch {
            try {
                if (!mSendFlag) {
                    socketClient = mContext?.createSocket(SocketType.PAIR)
                    log("clientService---> ")
                    val connect = socketClient?.connect(tcpAddress)
                    if (connect != null) {
                        mSendFlag = connect
                    }
                }
                log("bind---> $mSendFlag")

                if (sendListener != null) {
                    mSendSendData = mClientBuffer.toString();
                    sendListener!!.onCall(mSendSendData, mSendResultData)
                }

                while (!Thread.currentThread().isInterrupted) {
                    // Block until a message is received
                    val reply = socketClient?.recv(0)
                    if (reply != null) {
                        val content = String(reply, ZMQ.CHARSET)
                        log("客户端接收到服务端发送的数据---->$content")
                        if (sendListener != null) {
                            mSendResultData = "发送端-->接收：$content"
                            sendListener!!.onCall(mSendSendData, mSendResultData)
                        }
                    }
                }
            } catch (e: ZMQException) {
                log("客户端连接发送异常--->$e")
                e.printStackTrace()
                if (sendListener != null) {
                    mSendSendData = mClientBuffer.toString()
                    sendListener!!.onCall(mSendSendData, mSendResultData)
                }
                mSendFlag = false
            }
        }
    }

    suspend fun sendSend() {
        try {
            val response = "发送端-->发送-->：($number)"
            if (mSendFlag) {
                log("send --->$response   bind: $mSendFlag")
                mSendSendData = response
                socketClient?.send(response.toByteArray(ZMQ.CHARSET), 0)
                number++
                sendListener!!.onCall(mSendSendData, mSendResultData)
            } else {
                mSendSendData = "发送端绑定接收端失败，请重新connect !"
                sendListener!!.onCall(mSendSendData, mSendResultData)
            }
        } catch (e: ZMQException) {
            mSendSendData = "发送端发送数据异常：$e"
            sendListener!!.onCall(mSendSendData, mSendResultData)
        }
    }

    interface SendCallBackListener {
        fun onCall(send: String, result: String)
    }

    private var sendListener: SendCallBackListener? = null
    fun setSendCallBackListener(listener: SendCallBackListener?) {
        this.sendListener = listener
    }

    private var mContext: ZContext? = null

    private fun log(content: String) {
        e("ZMQ", content)
        mClientBuffer.append(content)
            .append("\r\n")
    }

    fun stop() {
//        if (clientService != null) {
//            clientService!!.close()
//            clientService = null
//        }
//
//        if (socketService != null) {
//            socketService!!.close()
//            socketService = null
//        }
//
//        if (mContext != null) {
//            mContext!!.close()
//            mContext = null
//        }
        log("释放了zmq!")
    }
}