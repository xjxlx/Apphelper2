package com.android.apphelper2.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.android.apphelper2.app.AppHelperManager
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream

/**
 * 日志写入工具
 */
class LogWriteUtil(private val fileName: String) : ApplicationCheck() {

    private val mRootPath: String by lazy {
        return@lazy FileUtils.instance.getRootPath(AppHelperManager.context)
    }
    private val mFileName: String by lazy {
        return@lazy FileUtils.instance.getFileName(fileName)
    }
    private var mFile: File? = null
    private var mNumber = 0 // 每一行的编号
    private var printStream: PrintStream? = null
    private var mIsFirstWrite = false  // 是否首次写入收据

    init {
        runCatching {
            LogUtil.e("root path: $mRootPath")
            val mkdirsDate = FileUtils.instance.mkdirs(mRootPath)
            LogUtil.e("create root success ：${mkdirsDate != null}")
        }.onFailure {
            it.printStackTrace()
            LogUtil.e("create root failure!")
        }
    }

    private fun checkFile(): File? {
        var result: File? = null
        runCatching {
            val datePath = FileUtils.instance.getPathForCalendar()
            val parentFile = FileUtils.instance.mkdirs(mRootPath, datePath)
            LogUtil.e("create parent file ：$parentFile")
            if (parentFile != null) {
                result = FileUtils.instance.createFile(parentFile.path, mFileName)
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
            if (mFile == null) {
                mFile = checkFile()
            }
            if (mFile != null) {
                if (printStream == null) {
                    printStream = PrintStream(FileOutputStream(mFile, true)) // 追加文件
                }

                // 获取当前的时间
                val currentDateStr = DataUtil.format(Pattern.YYYY_MM_DD_HH_MM_SS)
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
            printStream?.close() // 关闭打印流
            mIsFirstWrite = false
        }
    }

    fun init(fragment: Fragment) {
        fragment.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    mFile = null
                    printStream?.close() // 关闭打印流
                }
            }
        })
    }

    fun init(activity: FragmentActivity) {
        activity.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    mFile = null
                    printStream?.close() // 关闭打印流
                }
            }
        })
    }

}

