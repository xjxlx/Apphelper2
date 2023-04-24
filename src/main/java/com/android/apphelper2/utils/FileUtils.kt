package com.android.apphelper2.utils

import android.content.Context
import android.os.Build
import android.os.Environment
import android.text.TextUtils
import java.io.File
import java.util.*

class FileUtils : ApplicationCheck() {

    companion object {
        private val TAG = FileUtils::class.java.simpleName
        val instance: FileUtils by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            return@lazy FileUtils()
        }
    }

    /**
     * 例子：/storage/emulated/0/Download
     * @param type The type of storage directory to return. Should be one of
     * [Environment.DIRECTORY_MUSIC],
     * [Environment.DIRECTORY_PODCASTS],
     * [Environment.DIRECTORY_RINGTONES],
     * [Environment.DIRECTORY_ALARMS],
     * [Environment.DIRECTORY_NOTIFICATIONS],
     * [Environment.DIRECTORY_PICTURES],
     * [Environment.DIRECTORY_MOVIES],
     * [Environment.DIRECTORY_DOWNLOADS],
     * [Environment.DIRECTORY_DCIM], or
     * [Environment.DIRECTORY_DOCUMENTS].
     * @return 获取SD卡下，指定公共目录的路径，该路径在android 11及以后，只能通过IO流的形式使用，直接读写不可用
     */
    fun getSdTypePublicPath(fileUtils: FileUtils, type: String): String {
        var path = ""
        if (!TextUtils.isEmpty(type) && fileUtils.checkSdStatus()) {
            val publicDirectory = Environment.getExternalStoragePublicDirectory(type)
            if (publicDirectory != null) {
                path = publicDirectory.path
            }
        }
        return path
    }

    /**
     * 根据指定的编译版本去获取根目录的空间地址，如果编译版本大于android10,则默认使用沙盒目录空间，否则使用sd卡内的公共文档目录
     */
    @JvmOverloads
    fun getRootPath(context: Context, targetVersion: Int = 30): String {
        var rootPath = ""
        if (targetVersion > Build.VERSION_CODES.Q) {
            // 大于android 10 的版本，存入到不可见的沙盒目录中
            val filesDir = context.filesDir
            rootPath = filesDir.path
        } else {
            val path = getSdTypePublicPath(this, Environment.DIRECTORY_DOCUMENTS)
            if (TextUtils.isEmpty(path)) {
                rootPath = getRootPath(context)
            }
        }
        return rootPath
    }

    /**
     * 返回一个年月日的目录，用于创建当前的父目录，例如：pathForCalendar: /2023/4/21/
     */
    fun getPathForCalendar(): String {
        val calendar = Calendar.getInstance()
        //当前年
        val year = calendar[Calendar.YEAR]
        //当前月
        val month = calendar[Calendar.MONTH] + 1
        //当前月的第几天：即当前日
        val dayOfMonth = calendar[Calendar.DAY_OF_MONTH]
        return "" + year + File.separator + (month) + File.separator + dayOfMonth + File.separator
    }

    fun getFileName(fileName: String, tag: String = ""): String {
        return if (TextUtils.isEmpty(tag)) {
            fileName
        } else {
            tag + "_" + fileName
        }
    }

    /**
     * 创建文件夹
     */
    fun mkdirs(parentFilePath: String): File? {
        var result: File? = null
        runCatching {
            val file = File(parentFilePath)
            if (file.exists()) {
                result = file
            } else {
                val mkdirs = file.mkdirs()
                if (mkdirs) {
                    result = file
                }
            }
        }.onFailure {
            it.printStackTrace()
            LogUtil.e("create file mkdirs failed !")
        }
        return result
    }

    /**
     * 创建文件夹
     */
    fun mkdirs(parentFilePath: String, childFile: String): File? {
        var result: File? = null
        runCatching {
            val file = File(parentFilePath, childFile)
            if (file.exists()) {
                result = file
            } else {
                val mkdirs = file.mkdirs()
                if (mkdirs) {
                    result = file
                }
            }
        }.onFailure {
            it.printStackTrace()
            LogUtil.e("create file mkdirs failed !")
        }
        return result
    }

    /**
     * 创建文件
     */
    fun createFile(fileName: String): File? {
        var result: File? = null
        runCatching {
            val file = File(fileName)
            if (file.exists()) {
                result = file
            } else {
                val createNewFile = file.createNewFile()
                if (createNewFile) {
                    result = file
                }
            }
        }.onFailure {
            it.printStackTrace()
            LogUtil.e("create file createNewFile failed !")
        }
        return result
    }

    /**
     * 创建文件
     */
    fun createFile(parentFile: String, fileName: String): File? {
        var result: File? = null
        runCatching {
            val file = File(parentFile, fileName)
            if (file.exists()) {
                result = file
            } else {
                val createNewFile = file.createNewFile()
                if (createNewFile) {
                    result = file
                }
            }
        }.onFailure {
            it.printStackTrace()
            LogUtil.e("create file createNewFile failed !")
        }
        return result
    }

    private fun checkSdStatus(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }
}