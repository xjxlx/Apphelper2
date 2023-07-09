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

    private val mServiceBuffer: StringBuffer = StringBuffer()
    private val mClientBuffer: StringBuffer = StringBuffer()

    interface ResultCallBackListener {
        fun onCall(send: String, result: String)
    }

    private var resultListener: ResultCallBackListener? = null
    fun setResultCallBackListener(listener: ResultCallBackListener?) {
        this.resultListener = listener
    }

    private var mResultSendData = ""
    private var mResultResultData = ""
    private var mResultFlag = false

    /**
     * 接收端代码
     */
    fun initResultZmq(ipAddress: String) {
        mServiceBuffer.setLength(0)
        mResultSendData = ""
        mResultResultData = ""

        initZContext()
        log("初始化服务端---> :  $ipAddress")

        mScope.launch {
            try {
                // Socket to talk to clients
                var bind: Boolean? = false
                if (!mResultFlag) {
                    socketResult = mContext?.createSocket(SocketType.PAIR)
                    log("创建 socketService !")
                    bind = socketResult?.bind(ipAddress)
                }
                log("服务端初始化成功 $bind")
                mResultSendData = mServiceBuffer.toString()
                resultListener?.onCall(mResultSendData, mResultResultData)

                while (!Thread.currentThread().isInterrupted) {
                    // Block until a message is received
                    val reply: ByteArray = socketResult!!.recv(0)
                    mResultFlag = true
                    // Print the message
                    val content = String(reply, ZMQ.CHARSET)
                    mResultResultData = "接收端-->接收：$content"
                }
            } catch (e: ZMQException) {
                log("初始化服务端异常--->$e")
                mResultSendData = mServiceBuffer.toString()
                resultListener?.onCall(mResultSendData, mResultResultData)
                e.printStackTrace()
                mResultFlag = false
            }
        }
    }

    suspend fun sendResult() {
        try {
            if (mResultFlag) {
                val msg = "接收端-->发送--> $number"
                socketResult?.send(msg.toByteArray(ZMQ.CHARSET), 0)
                number++
                mResultSendData = msg
                resultListener?.onCall(mResultSendData, mResultResultData)
            } else {
                mResultSendData = "发送端还没有connect成功，请等待！"
                resultListener?.onCall(mResultSendData, mResultResultData)
            }
        } catch (e: ZMQException) {
            mResultSendData = "接收端发送一场：$e"
            resultListener?.onCall(mResultSendData, mResultResultData)
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

    fun log(content: String) {
        e("ZMQ", content)
        mServiceBuffer.append(content)
            .append("\r\n")
        mClientBuffer.append(content)
            .append("\r\n")
    }

    private fun initZContext() {
        log("创建 context ---->")
        if (mContext == null) {
            mContext = ZContext(1)
        }
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