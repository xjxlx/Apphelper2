package com.android.apphelper2.utils

import android.content.Context
import android.net.wifi.WifiManager

class WifiUtil(val context: Context) {

    private val wifiManager: WifiManager by lazy {
        return@lazy context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    fun getWifiSsId(context: Context): String {
        var ssid: String = ""
        val wifiInfo = wifiManager.connectionInfo
        if (wifiInfo != null) {
            ssid = wifiInfo.ssid
        }
        return ssid
    }
}