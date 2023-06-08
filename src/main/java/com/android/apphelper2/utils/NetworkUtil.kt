package com.android.apphelper2.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.text.TextUtils
import com.android.apphelper2.app.AppHelperManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.URL

/**
 * network util
 */
class NetworkUtil private constructor() {

    companion object {
        const val TAG = "NetworkUtil : "
        val instance: NetworkUtil by lazy {
            return@lazy NetworkUtil()
        }
    }

    private var mIpAddress = ""
    private val mScope: CoroutineScope by lazy {
        return@lazy CoroutineScope(Dispatchers.IO)
    }
    private val mStateFlow: MutableStateFlow<String> by lazy {
        return@lazy MutableStateFlow("")
    }
    private val mConnectivityManager: ConnectivityManager by lazy {
        return@lazy AppHelperManager.context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    private val mRequest: NetworkRequest by lazy {
        return@lazy NetworkRequest.Builder()
            // add Specified requirement，this specifies that networking is required
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .build()
    }
    private val mCallBack = object : ConnectivityManager.NetworkCallback() {

        // called the method when the network is lost
        override fun onLost(network: Network) {
            super.onLost(network)
            LogUtil.e(TAG, "this $network is lost!")
            mIpAddress = ""
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
                                            mScope.launch {
                                                mStateFlow.emit(hostAddress)
                                                mIpAddress = hostAddress
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
                                if (hostAddress != null) {
                                    mScope.launch {
                                        mStateFlow.emit(hostAddress)
                                        mIpAddress = hostAddress
                                    }
                                    LogUtil.e(TAG, "hostAddress: mobile: $hostAddress")
                                    return
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
    suspend fun getIPAddress(block: (ipAddress: String) -> Unit): NetworkUtil {
        mStateFlow.first {
            if (!TextUtils.isEmpty(it)) {
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
    suspend fun getSingleIpAddress(block: (String) -> Unit): NetworkUtil {
        if (TextUtils.isEmpty(mIpAddress)) {
            runCatching {
                mStateFlow.first {
                    if (!TextUtils.isEmpty(it)) {
                        LogUtil.e(TAG, "hostAddress:------:> result--->block---> $it")
                        block(it)
                        return@first true
                    }
                    return@first false
                }
            }.onFailure {
                LogUtil.e(TAG, "network ---> error :" + it.message)
                block("")
            }
        } else {
            block(mIpAddress)
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
        mConnectivityManager.requestNetwork(mRequest, mCallBack)
        return this
    }

    fun unregister() {
        LogUtil.e(TAG, "unregister network !")
        mConnectivityManager.unregisterNetworkCallback(mCallBack)
    }
}