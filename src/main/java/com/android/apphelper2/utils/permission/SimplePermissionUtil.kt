package com.android.apphelper2.utils.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.android.apphelper2.utils.LogUtil

class SimplePermissionUtil {
    private val mRequestCode = 100

    fun checkPermission(activity: FragmentActivity, permission: String) {

        val result = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                LogUtil.e("拥有 $permission 权限 !")
            } else {
                LogUtil.e("没有 $permission 权限 !")
            }
        }

        if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            LogUtil.e("检测发现有权限--->")

        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            LogUtil.e("qqqq ---> 没有 开始请求  ----》》 ---》")

            result.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

    }

    /**
     * Manifest.permission.CAMERA
     */
    fun checkPermission(context: Context, permission: String): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(context, permission)
    }

    /**
     * 发起请求
     * Manifest.permission.CAMERA
     */
    fun requestPermission(activity: FragmentActivity, vararg params: String) {
        ActivityCompat.requestPermissions(activity, params, mRequestCode)
    }

    fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<String?>, @NonNull grantResults: IntArray) {
        var isAllGranted = false
        if (requestCode == mRequestCode) {
            for (item in permissions.indices) {
                if (grantResults[item] == PackageManager.PERMISSION_GRANTED) {

                    //已授权
                } else {
                    //未授权
                }
            }
        }
    }

    private var activity: FragmentActivity? = null
    val requestPermissionLauncher = activity?.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your
            // app.
        } else {
            // Explain to the user that the feature is unavailable because the
            // feature requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their
            // decision.
        }
    }

}
