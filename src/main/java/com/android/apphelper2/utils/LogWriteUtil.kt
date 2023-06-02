package com.android.apphelper2.utils

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream

class LogWriteUtil(private val fileName: String) {

    private var mNumber = 0
    private var mIsFirstWrite = false  // is first write
    private var mRootPath: String = ""
    private val mFile: File? by lazy {
        return@lazy checkFile()
    }
    private val mPrintStream: PrintStream? by lazy {
        return@lazy PrintStream(FileOutputStream(mFile, true)) // 追加文件
    }

    fun initWrite(context: Context) {
        runCatching {
            mRootPath = context.filesDir.path
            LogUtil.e("root path: $mRootPath")
            val mkdirsDate = FileUtil.instance.mkdirs(mRootPath)
            LogUtil.e("create root success ：${mkdirsDate != null}")
        }.onFailure {
            it.printStackTrace()
            LogUtil.e("create root failure!")
        }
    }

    private fun checkFile(): File? {
        var result: File? = null
        runCatching {
            val datePath = FileUtil.instance.getPathForCalendar()
            val parentFile = FileUtil.instance.mkdirs(mRootPath, datePath)
            LogUtil.e("create parent file ：$parentFile")
            if (parentFile != null) {
                result = FileUtil.instance.createFile(parentFile.path, fileName)
            }
        }.onFailure {
            it.printStackTrace()
            LogUtil.e("create parent failure !")
        }
        return result
    }

    fun write(content: String) {
        var value: String
        runCatching {
            if (mFile != null && mPrintStream != null) {
                val currentDateStr = DateUtil.format(DateUtil.YYYY_MM_DD_HH_MM_SS)
                if (!mIsFirstWrite) {
                    value = "\n-----------------   $currentDateStr 重新开始   -----------------\n"
                    mIsFirstWrite = true
                    mPrintStream?.println(value)
                }

                value = "[ $currentDateStr ]【${++mNumber}】$content"
                mPrintStream?.println(value)
            }
        }.onFailure {
            it.printStackTrace()
            mPrintStream?.close()
            mIsFirstWrite = false
        }
    }

    fun init(fragment: Fragment) {
        fragment.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    mPrintStream?.close()
                }
            }
        })
    }

    fun init(activity: FragmentActivity) {
        activity.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    mPrintStream?.close()
                }
            }
        })
    }
}

