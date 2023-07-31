package com.android.apphelper2.utils

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.android.common.utils.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream

class LogWriteUtil(private val fileName: String) {

    private var mRootPath: String = ""
    private var mFile: File? = null
    private var mNumber = 0
    private var printStream: PrintStream? = null
    private var mIsFirstWrite = false  // is first write
    private val tag = "Write"
    private var mSendChannel: SendChannel<String>? = null

    private fun checkFile(): File? {
        var result: File? = null
        runCatching {
            val datePath = FileUtil.instance.getPathForCalendar()
            val parentFile = FileUtil.instance.mkdirs(mRootPath, datePath)
            LogUtil.e(tag, "create parent file ：$parentFile")
            if (parentFile != null) {
                result = FileUtil.instance.createFile(parentFile.path, fileName)
            }
        }.onFailure {
            it.printStackTrace()
            LogUtil.e(tag, "create parent failure !")
        }
        return result
    }

    fun send(content: String) {
        mSendChannel?.trySend(content)
    }

    private fun write(content: String) {
        var value: String
        runCatching {
            if (mFile == null) {
                mFile = checkFile()
            }
            if (mFile != null) {
                if (printStream == null) {
                    printStream = PrintStream(FileOutputStream(mFile, true)) // 追加文件
                }

                val currentDateStr = DateUtil.format(DateUtil.YYYY_MM_DD_HH_MM_SS)
                if (!mIsFirstWrite) {
                    value = "\n-----------------   $currentDateStr 重新开始   -----------------\n"
                    mIsFirstWrite = true
                    printStream?.println(value)
                }

                value = "[ $currentDateStr ]【${++mNumber}】$content"
                printStream?.println(value)
            }
        }.onFailure {
            it.printStackTrace()
            printStream?.close()
            mIsFirstWrite = false
            LogUtil.e(tag, "write failed : ${it.message}")
        }
    }

    fun initContext(context: Context) {
        runCatching {
            mRootPath = context.filesDir.path
            LogUtil.e(tag, "root path: $mRootPath")
            val mkdirsDate = FileUtil.instance.mkdirs(mRootPath)
            LogUtil.e(tag, "create root success ：${mkdirsDate != null}")
        }.onFailure {
            it.printStackTrace()
            LogUtil.e(tag, "create root failure!")
        }
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    fun init(fragment: Fragment) {
        val context = fragment.context
        if (context != null) {
            initContext(context)
        }

        fragment.lifecycleScope.launch(Dispatchers.IO) {
            mSendChannel = actor {
                val iterator = iterator()
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    write(next)
                }
            }
        }

        fragment.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    mFile = null
                    printStream?.close()
                    printStream = null
                    mSendChannel = null
                }
            }
        })
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    fun init(activity: FragmentActivity) {
        initContext(activity)

        activity.lifecycleScope.launch(Dispatchers.IO) {
            mSendChannel = actor {
                val iterator = iterator()
                while (iterator.hasNext()) {
                    val next = iterator.next()
                    write(next)
                }
            }
        }

        activity.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    mFile = null
                    printStream?.close()
                    printStream = null
                    mSendChannel = null
                }
            }
        })
    }
}

