package com.android.apphelper2.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.text.TextUtils
import com.android.apphelper2.app.AppHelper2
import com.android.common.utils.LogUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.URL

/**
 * network util
 * if you want get the ssid ,must location permission ,and sdk >= 31
 *     <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
 *     and start location switch
 */
class NetworkUtil private constructor() {

    companion object {
        const val TAG = "NetworkUtil : "
        val instance: NetworkUtil by lazy {
            return@lazy NetworkUtil()
        }
    }

    private var mIpAddress: IpAddress? = IpAddress()
    private val mScope: CoroutineScope by lazy {
        return@lazy CoroutineScope(Dispatchers.IO)
    }
    private val mStateFlow: MutableSharedFlow<IpAddress> = MutableSharedFlow()
    private val mConnectivityManager: ConnectivityManager by lazy {
        return@lazy AppHelper2.application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private val mRequest: NetworkRequest by lazy {
        return@lazy NetworkRequest.Builder()
            // add Specified requirement，this specifies that networking is required
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
    }

    // if sdk > Q(31) , need add  flag FLAG_INCLUDE_LOCATION_INFO
    // private val mCallBack = object : ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
    private val mCallBack = object : ConnectivityManager.NetworkCallback() {
        // called the method when the network is lost
        override fun onLost(network: Network) {
            super.onLost(network)
            LogUtil.e(TAG, "this $network is lost!")
            mIpAddress?.ip = ""
            mIpAddress?.ssid = ""
        }

        // called the method when the network changes
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)

            // if connected is wifi
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) { // wifi connect
                val transportInfo = networkCapabilities.transportInfo
                if (transportInfo is WifiInfo) {
                    val properties = mConnectivityManager.getLinkProperties(network)
                    properties?.let {
                        // contain ipv4  and ipv6 ipaddress
                        val linkAddresses = it.linkAddresses
                        if (linkAddresses.size > 0) {
                            linkAddresses.forEach { ip ->
                                val address = ip.address
                                if (address != null) {
                                    val hostAddress = address.hostAddress

                                    if (hostAddress != null) {
                                        if ((!TextUtils.equals(hostAddress, "0.0.0.0")) && (!(hostAddress.contains(":")))) {
                                            LogUtil.e(TAG, "hostAddress: wifi: $hostAddress")
                                            if (!TextUtils.equals(mIpAddress?.ip, hostAddress)) {
                                                mScope.launch {
                                                    LogUtil.e(TAG, "send: wifi: $hostAddress  hostName:${address.hostName}")
                                                    if (mIpAddress != null) {

                                                        val replace = transportInfo.ssid.replace("\"", "")
                                                            .replace("<", "")
                                                            .replace(">", "")
                                                        mIpAddress?.ip = hostAddress
                                                        mIpAddress?.ssid = replace
                                                        mStateFlow.emit(mIpAddress!!)
                                                    }
                                                }
                                            }
                                            return
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) { // Mobile connect
                // if connected is 2G/3G/4G/5G
                val networkInterfaces = NetworkInterface.getNetworkInterfaces()
                while (networkInterfaces.hasMoreElements()) {
                    val element = networkInterfaces.nextElement()
                    val inetAddresses = element.inetAddresses
                    if (inetAddresses != null) {
                        while (inetAddresses.hasMoreElements()) {
                            val address = inetAddresses.nextElement()
                            if (!address.isLoopbackAddress && address is Inet4Address) {
                                val hostAddress = address.getHostAddress()
                                if (!TextUtils.isEmpty(hostAddress)) {
                                    LogUtil.e(TAG, "hostAddress: mobile: $hostAddress")
                                    if (!TextUtils.equals(hostAddress, mIpAddress?.ip)) {
                                        mScope.launch {
                                            LogUtil.e(TAG, "send : mobile: $hostAddress")
                                            if (mIpAddress != null) {
                                                mIpAddress?.ip = hostAddress!!
                                                mIpAddress?.ssid = ""
                                                mStateFlow.emit(mIpAddress!!)
                                            }
                                        }
                                        return
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * must has permission
     * @RequiresPermission(value = "android.permission.ACCESS_NETWORK_STATE")
     * @return current network is connected , note，the method just only works on regular devices or mobile device,but on the hcp3 device ,it will fail
     */
    fun isNetworkConnected(): Boolean {
        val network = mConnectivityManager.activeNetwork
        if (network != null) {
            val nc = mConnectivityManager.getNetworkCapabilities(network)
            nc?.let {
                return it.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            }
        }
        return false
    }

    /**
     * @RequiresPermission(value = "android.permission.ACCESS_NETWORK_STATE")
     * must has permission
     *@return if network is connect ,return the ipAddress, it can call back multiple count
     */
    suspend fun getIPAddress(block: (ipAddress: IpAddress) -> Unit): NetworkUtil {
        mStateFlow.first {
            if (!TextUtils.isEmpty(it.ip)) {
                block(it)
            }
            return@first false
        }
        return this
    }

    /**
     * @RequiresPermission(value = "android.permission.ACCESS_NETWORK_STATE")
     * must has permission
     * @return if network is connect ,return the ipAddress ,it only can call back one count
     */
    suspend fun getSingleIpAddress(block: (IpAddress) -> Unit): NetworkUtil {
        if (TextUtils.isEmpty(mIpAddress?.ip)) {
            runCatching {
                mStateFlow.first {
                    if (!TextUtils.isEmpty(it.ip)) {
                        LogUtil.e(TAG, "hostAddress:------:> result--->block---> $it")
                        block(it)
                        return@first true
                    }
                    return@first false
                }
            }.onFailure {
                LogUtil.e(TAG, "network ---> error :" + it.message)
                block(IpAddress())
            }
        } else {
            block(IpAddress())
        }
        return this
    }

    /**
     * this method will return it is connected http，called the method, it will connect www.baidu.com,if baidu can connect ,return true ,else return false
     */
    fun isConnectedHttp(block: (Boolean) -> Unit) {
        mScope.launch {
            runCatching {
                val url = URL("https://www.baidu.com")
                url.openStream()
            }.onSuccess {
                LogUtil.e(TAG, "isConnectedHttp - onSuccess")
                block(true)
            }
                .onFailure {
                    LogUtil.e(TAG, "isConnectedHttp - onFailure")
                    block(false)
                }
        }
    }

    fun register(): NetworkUtil {
        LogUtil.e(TAG, "register network !")
        mIpAddress = IpAddress()
        mConnectivityManager.requestNetwork(mRequest, mCallBack)
        return this
    }

    fun unregister() {
        LogUtil.e(TAG, "unregister network !")
        mConnectivityManager.unregisterNetworkCallback(mCallBack)
        mIpAddress = null
    }

    data class IpAddress(var ip: String = "", var ssid: String = "")
}