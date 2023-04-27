package com.android.apphelper2.utils.permission

import android.content.pm.PackageManager
import android.text.TextUtils
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.android.apphelper2.utils.LogUtil

class PermissionUtil private constructor() {

    companion object {
        const val TAG = "Permission"
    }

    class PermissionActivity(private val activity: FragmentActivity) {
        private var permission: String = ""
        private var rationaleCallBackListener: PermissionRationaleCallBackListener? = null
        private var callBackListener: PermissionCallBackListener? = null
        private var multipleCallBackListener: PermissionMultipleCallBackListener? = null

        private val registerResult: ActivityResultLauncher<String> =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    LogUtil.e(TAG, "请求到了 $permission 权限 !")
                } else {
                    LogUtil.e(TAG, "没有请求到 $permission 权限 !")
                }
                callBackListener?.onCallBack(permission, isGranted)
            }

        private val registerResultMultiple: ActivityResultLauncher<Array<String>> =
            activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                val all = result.all {
                    it.value
                }
                multipleCallBackListener?.onCallBack(all, result)
            }

        fun setCallBackListener(callBackListener: PermissionCallBackListener): PermissionActivity {
            this.callBackListener = callBackListener
            return this
        }

        /**
         * request permission reason
         *
         * 1：if you don't have the permission , when you first request the permission, return false,
         * 2：when you refuse for the first request and request again， return true
         * 3：when you refuse for the first time and then refuse again, return false
         */
        fun shouldShow(permission: String, listener: PermissionRationaleCallBackListener): Boolean {
            this.permission = permission
            var result = false
            this.rationaleCallBackListener = listener
            if (!TextUtils.isEmpty(permission)) {
                result = shouldShowRequestPermissionRationale(activity, permission)
            }
            listener.onCallBack(permission, result)
            return result
        }

        /**
         * @param permission Manifest.permission.READ_EXTERNAL_STORAGE ...
         */
        fun request(permission: String) {
            this.permission = permission
            if (TextUtils.isEmpty(permission)) {
                LogUtil.e(TAG, "permission is empty !")
                return
            }
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED) {
                callBackListener?.onCallBack(permission, true)
            } else {
                // request permission
                registerResult.launch(permission)
            }
        }

        /**
         * request multiple permission
         */
        fun requestArray(array: Array<String>, listener: PermissionMultipleCallBackListener) {
            multipleCallBackListener = listener
            registerResultMultiple.launch(array)
        }
    }

    class PermissionFragment(private val fragment: Fragment) {
        private var permission: String = ""
        private var rationaleCallBackListener: PermissionRationaleCallBackListener? = null
        private var callBackListener: PermissionCallBackListener? = null
        private var multipleCallBackListener: PermissionMultipleCallBackListener? = null

        private val registerResult: ActivityResultLauncher<String> =
            fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    LogUtil.e(TAG, "请求到了 $permission 权限 !")
                } else {
                    LogUtil.e(TAG, "没有请求到 $permission 权限 !")
                }
                callBackListener?.onCallBack(permission, isGranted)
            }

        private val registerResultMultiple: ActivityResultLauncher<Array<String>> =
            fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                val all = result.all {
                    it.value
                }
                multipleCallBackListener?.onCallBack(all, result)
            }

        fun setCallBackListener(callBackListener: PermissionCallBackListener): PermissionFragment {
            this.callBackListener = callBackListener
            return this
        }

        /**
         * request permission reason
         *
         * 1：if you don't have the permission , when you first request the permission, return false,
         * 2：when you refuse for the first request and request again， return true
         * 3：when you refuse for the first time and then refuse again, return false
         */
        fun shouldShow(permission: String, listener: PermissionRationaleCallBackListener): Boolean {
            this.permission = permission
            var result = false
            this.rationaleCallBackListener = listener
            if (!TextUtils.isEmpty(permission)) {
                result = shouldShowRequestPermissionRationale(fragment, permission)
            }
            listener.onCallBack(permission, result)
            return result
        }

        /**
         * @param permission Manifest.permission.READ_EXTERNAL_STORAGE ...
         */
        fun request(permission: String) {
            this.permission = permission
            if (TextUtils.isEmpty(permission)) {
                LogUtil.e(TAG, "permission is empty !")
                return
            }

            if (fragment.context != null) {
                if (ContextCompat.checkSelfPermission(fragment.requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
                    callBackListener?.onCallBack(permission, true)
                } else {
                    // request permission
                    registerResult.launch(permission)
                }
            }
        }

        /**
         * request multiple permission
         */
        fun requestArray(array: Array<String>, listener: PermissionMultipleCallBackListener) {
            multipleCallBackListener = listener
            registerResultMultiple.launch(array)
        }
    }

}