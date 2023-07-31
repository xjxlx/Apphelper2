package com.android.apphelper2.utils.zmq

import android.text.TextUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.android.apphelper2.utils.DebounceUtil
import com.android.common.utils.LogUtil
import com.android.common.utils.LogWriteUtil
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import org.zeromq.SocketType
import org.zeromq.ZMQ
import java.util.concurrent.atomic.AtomicBoolean

object ZmqUtil2 {

    private val mAtomicStatus: AtomicBoolean by lazy {
        return@lazy AtomicBoolean()
    }
    private val mContext: ZMQ.Context by lazy {
        return@lazy ZMQ.context(1)
    }
    private val mSocket: ZMQ.Socket by lazy {
        return@lazy mContext.socket(SocketType.PAIR)
    }
    private val mScope: CoroutineScope by lazy {
        return@lazy CoroutineScope(Dispatchers.IO + CoroutineName(javaClass.simpleName))
    }
    private val mShardFlow: MutableSharedFlow<String> by lazy {
        return@lazy MutableSharedFlow<String>()
    }
    private var mBinding = false
    private const val TAG = "ZMQ"
    private var mWriter: LogWriteUtil? = null
    private var mSendMsg: String? = null

    // true:open  false:close
    var ZMQ_SWITCH = true
    var IP_ADDRESS = ""
    private val TCP_ADDRESS: String
        get() {
            if (TextUtils.isEmpty(IP_ADDRESS)) {
                return ""
            }
            return "tcp://${IP_ADDRESS}:6667"
        }

    private val mDebounceUtil: DebounceUtil<Boolean> by lazy { return@lazy DebounceUtil(15 * 1000) }
    private val mTraceBuffer: StringBuffer = StringBuffer()
    private var mTraceListener: TraceListener? = null

    init {
        mScope.launch {
            runCatching {
                mDebounceUtil.listener {
                    log("ST socket connect lost ！")
                }
            }.onFailure {
                log("debounce listener error: ${it.message}")
            }
        }
    }

    private fun bind() {
        if (mBinding) {
            log("bind socket success , break !")
            return
        }

        if (TextUtils.isEmpty(TCP_ADDRESS)) {
            log("tcp is empty！")
            return
        }

        runCatching {
            mBinding = mSocket.bind(TCP_ADDRESS)
            log("bind address:[ $TCP_ADDRESS ]   ---> bind success：$mBinding")

            // loop data
            mAtomicStatus.set(false)
            loopData()
        }.onFailure {
            mBinding = false
            log("bind address  failure：:${it.message}")
        }
    }

    fun resume() {
        if (ZMQ_SWITCH) {
            runCatching {
                if (mAtomicStatus.get()) {
                    mAtomicStatus.set(false)
                    log("resume --->")
                    loopData()
                }
            }.onFailure {
                log("resume - error: ${it.message}")
            }
        }
    }

    fun pause() {
        if (ZMQ_SWITCH) {
            runCatching {
                if (!mAtomicStatus.get()) {
                    mAtomicStatus.set(true)
                    log("pause --->")
                }
            }.onFailure {
                log("pause - error: ${it.message}")
            }
        }
    }

    fun start() {
        if (ZMQ_SWITCH) {
            runCatching {
                if (mBinding) {
                    log("binding is true , stop execute!")
                } else {
                    bind()
                }
            }.onFailure {
                log("start - error: ${it.message}")
            }
        }
    }

    fun stop() {
        if (ZMQ_SWITCH) {
            runCatching {
                // unbind
                log("stop ---> binding status :$mBinding")
                if (mBinding) {
                    val unbind = mSocket.unbind(TCP_ADDRESS)
                    log("stop ---> unbind --->: $unbind")

                    log("stop ---> socket ---> close ! ")
                    mSocket.close()

                    val contextClosed = mContext.isClosed
                    log("stop ---> context is close : $contextClosed")

                    if (!contextClosed) {
                        mContext.close()
                        log("stop ---> context close !")
                    }
                }
                mScope.cancel()
                log("stop ---> success !")
            }.onFailure {
                log("stop ---> error: ${it.message}")
            }
            mBinding = false
        }
    }

    private fun loopData() {
        mScope.launch {
            runCatching {
                log("loopData ---> isPause:${mAtomicStatus.get()} context: ${!mContext.isTerminated}")
                //  the Zmq context  is not stop
                while (!mContext.isTerminated) {
                    mDebounceUtil.send(true)
                    // get the result from the zmq context
                    val reply = mSocket.recv(0)
                    if (reply != null) {
                        val content = reply.toString(Charsets.UTF_8)
                        LogUtil.e(TAG, "reply :$content")
                        if (!mAtomicStatus.get()) {
                            if (mSendMsg == null) {
                                log("数据收集中...")
                                mSendMsg = content
                            }
                            // val bean = GsonUtil.fromJsonNested<HttpRequest<ZmqBean>>(content)
                            if (!mAtomicStatus.get()) {
                                LogUtil.e(TAG, "reply --- send ...")
                                mShardFlow.emit(content)
                            }
                        }
                    } else {
                        LogUtil.e(TAG, "reply : null")
                    }
                }
                log("while break ...")
            }.onFailure {
                log("loopData failure：:${it.message}")
            }
        }
    }

    fun isBindSocket(): Boolean {
        return mBinding
    }

    fun setCallBackListener(block: (String) -> Unit) {
        if (ZMQ_SWITCH) {
            mScope.launch {
                mShardFlow.collect {
                    block(it)
                }
            }
        }
    }

    suspend fun setConnectionLostListener(block: (Boolean) -> Unit) {
        mDebounceUtil.listener {
            log("ST socket connect lost ！")
            block(it)
        }
    }

    interface TraceListener {
        fun trace(content: String)
    }

    fun setTraceListener(listener: TraceListener) {
        this.mTraceListener = listener
    }

    fun log(content: String) {
        LogUtil.e(TAG, content)
        mWriter?.send(content)
        mTraceBuffer.append(content)
            .append("\r\n\n")
        mTraceListener?.trace(mTraceBuffer.toString())
    }

    fun initLog(fragment: Fragment) {
        mWriter = LogWriteUtil("$TAG.txt")
        mWriter?.init(fragment)
    }

    fun initLog(activity: FragmentActivity) {
        mWriter = LogWriteUtil("$TAG.txt")
        mWriter?.init(activity)
    }
}
