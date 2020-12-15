package com.lyk.mymap.utils;

/**
 * Copyright from Baidu.com
 * Extract from the official documents
 * Change for this app by LiYunkai
 * Utility class code
 */

import android.app.ActivityManager;
import android.content.Context;
import android.telephony.TelephonyManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CommonUtil {

    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return "";
    }

    //获取当前时间戳(单位：秒)
    //Gets the current timestamp (in seconds)
    public static long getCurrentTime() {
        return System.currentTimeMillis() / 1000;
    }

    //校验double数值是否为0
    //Verify that the double value is 0
    public static boolean isEqualToZero(double value) {
        return Math.abs(value - 0.0) < 0.01 ? true : false;
    }

    //经纬度是否为(0,0)点
    //Is the latitude and longitude (0,0) point
    public static boolean isZeroPoint(double latitude, double longitude) {
        return isEqualToZero(latitude) && isEqualToZero(longitude);
    }

    //将字符串转为时间戳
    //Converts the string to a timestamp
    public static long toTimeStamp(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.CHINA);
        Date date;
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
        return date.getTime() / 1000;
    }

    //获取设备IMEI码
    //Obtain equipment IMEI code
    public static String getImei(Context context) {
        String imei;
        try {
            imei = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        } catch (Exception e) {
            imei = "myTrace";
        }
        return imei;
    }

}
