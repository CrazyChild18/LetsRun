package com.lyk.mymap.utils;

/**
 * Copyright from Baidu.com
 * Extract from the official documents
 * Change for this app by LiYunkai
 * Utility class code
 */

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.SyncStateContract;
import android.util.Log;

public class NetUtil {

    /**
     * 检测网络状态是否联通
     *
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (null != info && info.isConnected() && info.isAvailable()) {
                return true;
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "current network is not available");
            return false;
        }
        return false;
    }
}
