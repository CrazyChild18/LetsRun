package com.lyk.mymap;

/**
 * coding = utf-8
 *@Author : Li Yunkai(黎耘恺)
 *@Email : 2049721941@qq.com
 *@File : l
 *@Software : Android Studio
 */


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.Trace;
import com.baidu.trace.api.entity.LocRequest;
import com.baidu.trace.api.entity.OnEntityListener;
import com.baidu.trace.api.track.LatestPointRequest;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.model.BaseRequest;
import com.baidu.trace.model.OnCustomAttributeListener;
import com.baidu.trace.model.ProcessOption;
import com.baidu.trace.model.TransportMode;
import com.lyk.mymap.utils.CommonUtil;
import com.lyk.mymap.utils.NetUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class TrackApplication extends Application {

    private AtomicInteger mSequenceGenerator = new AtomicInteger();

    private LocRequest locRequest = null;

    public Context mContext = null;

    public SharedPreferences trackConf = null;

    //轨迹客户端
    //Trajectory client
    public LBSTraceClient mClient = null;

    //轨迹服务
    //Track service
    public Trace mTrace = null;

    //轨迹服务ID
    //Trace service ID
    public long serviceId = 222078;

    //Entity标识
    //The Entity identifier
    public String entityName = "My Tracks";

    public boolean isRegisterReceiver = false;

    //服务是否开启标识
    //Whether the service opens the logo
    public boolean isTraceStarted = false;

    //采集是否开启标识
    //Collection whether to open the identification
    public boolean isGatherStarted = false;

    public static int screenWidth = 0;

    public static int screenHeight = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        entityName = CommonUtil.getImei(this);


        // 若为创建独立进程，则不初始化成员变量
        //If a separate process is created, the member variable is not initialized
        if ("com.baidu.track:remote".equals(CommonUtil.getCurProcessName(mContext))) {
            return;
        }

        //调用SDKInitializer进行初始化
        //SDKInitializer is called for initialization
        SDKInitializer.initialize(mContext);
        //获取屏幕尺寸
        //Get screen size
        getScreenSize();
        //创建LBSTraceClient和Trace
        //Create LBSTraceClient and Trace
        mClient = new LBSTraceClient(mContext);
        mTrace = new Trace(serviceId, entityName);

        trackConf = getSharedPreferences("track_conf", MODE_PRIVATE);

        locRequest = new LocRequest(serviceId);

        mClient.setOnCustomAttributeListener(new OnCustomAttributeListener() {
            @Override
            public Map<String, String> onTrackAttributeCallback() {
                Map<String, String> map = new HashMap<>();
                map.put("key1", "value1");
                map.put("key2", "value2");
                return map;
            }

            @Override
            public Map<String, String> onTrackAttributeCallback(long l) {
                return null;
            }
        });

        clearTraceStatus();
    }

    //获取当前位置
    //Obtaining the Current Location
    public void getCurrentLocation(OnEntityListener entityListener, OnTrackListener trackListener) {
        // 网络连接正常，开启服务及采集，则查询纠偏后实时位置；否则进行实时定位
        // If the network connection is normal and service and collection are started,
        // the real-time position after rectifying the deviation will be inquired.
        // Otherwise, real-time positioning is performed
        if (NetUtil.isNetworkAvailable(mContext)
                && trackConf.contains("is_trace_started")
                && trackConf.contains("is_gather_started")
                && trackConf.getBoolean("is_trace_started", false)
                && trackConf.getBoolean("is_gather_started", false)) {
            LatestPointRequest request = new LatestPointRequest(getTag(), serviceId, entityName);
            ProcessOption processOption = new ProcessOption();
            processOption.setRadiusThreshold(50);
            processOption.setTransportMode(TransportMode.walking);
            processOption.setNeedDenoise(true);
            processOption.setNeedMapMatch(true);
            request.setProcessOption(processOption);
            mClient.queryLatestPoint(request, trackListener);
        } else {
            mClient.queryRealTimeLoc(locRequest, entityListener);
        }
    }

    //获取屏幕尺寸
    //Get screen size
    private void getScreenSize() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;
    }

    /*
     * 清除Trace状态：初始化app时，判断上次是正常停止服务还是强制杀死进程，根据trackConf中是否有is_trace_started字段进行判断。
     * 停止服务成功后，会将该字段清除；若未清除，表明为非正常停止服务。
     *
     * Clear Trace status: When initializing your app, determine whether the last time the service was stopped or the process was forced to die, based on whether the IS_trace_started field is in trackConf.
     * This field will be cleared after the service is stopped successfully; If not cleared, it is an abnormal stop of service.
     */
    private void clearTraceStatus() {
        if (trackConf.contains("is_trace_started") || trackConf.contains("is_gather_started")) {
            SharedPreferences.Editor editor = trackConf.edit();
            editor.remove("is_trace_started");
            editor.remove("is_gather_started");
            editor.apply();
        }
    }

    //初始化请求公共参数
    //Initialize the request common parameters
    public void initRequest(BaseRequest request) {
        request.setTag(getTag());
        request.setServiceId(serviceId);
    }

    //获取请求标识
    //Get the request id
    public int getTag() {
        return mSequenceGenerator.incrementAndGet();
    }

}
