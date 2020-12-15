package com.lyk.mymap.activity;

/**
 * coding = utf-8
 *@Author : Li Yunkai(黎耘恺)
 *@Email : 2049721941@qq.com
 *@File : l
 *@Software : Android Studio
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.api.entity.OnEntityListener;
import com.baidu.trace.api.track.DistanceRequest;
import com.baidu.trace.api.track.DistanceResponse;
import com.baidu.trace.api.track.HistoryTrackResponse;
import com.baidu.trace.api.track.LatestPoint;
import com.baidu.trace.api.track.LatestPointResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.api.track.SupplementMode;
import com.baidu.trace.api.track.TrackPoint;
import com.baidu.trace.model.CoordType;
import com.baidu.trace.model.LocationMode;
import com.baidu.trace.model.OnTraceListener;
import com.baidu.trace.model.ProcessOption;
import com.baidu.trace.model.PushMessage;
import com.baidu.trace.model.SortType;
import com.baidu.trace.model.StatusCodes;
import com.baidu.trace.model.TraceLocation;
import com.baidu.trace.model.TransportMode;
import com.lyk.mymap.R;
import com.lyk.mymap.User.Trace;
import com.lyk.mymap.TrackApplication;
import com.lyk.mymap.User.User;
import com.lyk.mymap.model.CurrentLocation;
import com.lyk.mymap.utils.CommonUtil;
import com.lyk.mymap.utils.Constants;
import com.lyk.mymap.utils.MapUtil;
import com.lyk.mymap.utils.TrackReceiver;
import com.lyk.mymap.utils.ViewUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

//轨迹时时追踪
//Track from time to time

public class TracingActivity extends BaseActivity implements View.OnClickListener, SensorEventListener {

    //轨迹监听器（用于接收历史轨迹回调）
    //Track listener (for receiving historical track callbacks)
    private OnTrackListener mTrackListener = null;

    private TrackApplication trackApp = null;

    private ViewUtil viewUtil = null;

    private Button traceBtn = null;

    private Button gatherBtn = null;

    private PowerManager powerManager = null;

    private PowerManager.WakeLock wakeLock = null;

    private TrackReceiver trackReceiver = null;

    private SensorManager mSensorManager;

    private Double lastX = 0.0;
    private int mCurrentDirection = 0;

    private String startDate;
    private String endDate;
    private String startTime;
    private String endTime;

    //地图工具
    //Map Tools
    private MapUtil mapUtil = null;

    //轨迹服务监听器
    //Trace service listener
    private OnTraceListener traceListener = null;

    //轨迹监听器(用于接收纠偏后实时位置回调)
    //Track listener (used to receive real-time position callback after rectifying deviation)
    private OnTrackListener trackListener = null;

    //Entity监听器(用于接收实时定位回调)
    //Entity Listener (for receiving real-time positioning callbacks)
    private OnEntityListener entityListener = null;

    //实时定位任务
    //Real-time positioning task
    private RealTimeHandler realTimeHandler = new RealTimeHandler();

    private RealTimeLocRunnable realTimeLocRunnable = null;

    //打包周期
    //Packaging cycle
    public int packInterval = Constants.DEFAULT_PACK_INTERVAL;

    //轨迹点集合
    //Collection of locus points
    private List<LatLng> trackPoints;

    private boolean firstLocate = true;

    private Long startTimeMillis;
    private Long endTimeMillis;

    private User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化地图界面，设置标题、按钮等
        //Initializes the map interface, sets the title, buttons, etc
        setTitle("Tracking");
        setOptionsText();
        setOnClickListener(this);

        mUser = (User) getIntent().getSerializableExtra("user");

        //防止熄灭屏幕
        //Prevent off screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        init();
    }

    //初始化设置界面
    //Initializes the setup interface
    public void setOptionsText() {
        LinearLayout layout = findViewById(R.id.layout_top);
        TextView textView = layout.findViewById(R.id.tv_options);
        textView.setText("Setting");
    }

    private void init() {
        //初始化位置监听服务
        //Initializes the location listening service
        initListener();
        trackApp = (TrackApplication) getApplicationContext();
        viewUtil = new ViewUtil();
        //初始化显示地图界面
        //Initializes the display map interface
        mapUtil = MapUtil.getInstance();
        mapUtil.init(findViewById(R.id.tracing_mapView));
        //设置地图中心点
        //Set the center of the map
        mapUtil.setCenter(mCurrentDirection);
        //电池管理
        //battery management
        powerManager = (PowerManager) trackApp.getSystemService(Context.POWER_SERVICE);
        //获取传感器管理服务
        //Get sensor management services
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //创建按钮
        //create button
        traceBtn = findViewById(R.id.btn_trace);
        gatherBtn = findViewById(R.id.btn_gather);
        traceBtn.setOnClickListener(this);
        gatherBtn.setOnClickListener(this);
        //设置按钮样式
        //Set button style
        setTraceBtnStyle();
        setGatherBtnStyle();
        //创建轨迹记录list
        //Create a list of track records
        trackPoints = new ArrayList<>();

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //每次方向改变，重新给地图设置定位数据，用上一次的经纬度
        //For each change of direction, reset the location data of the map, using the previous latitude and longitude
        double x = sensorEvent.values[SensorManager.DATA_X];
        if (Math.abs(x - lastX) > 1.0) {
            // 方向改变大于1度才设置，以免地图上的箭头转动过于频繁
            // Change the direction by more than 1 degree, so that the arrows on the map don't rotate too often
            mCurrentDirection = (int) x;
            if (!CommonUtil.isZeroPoint(CurrentLocation.latitude, CurrentLocation.longitude)) {
                mapUtil.updateMapLocation(new LatLng(CurrentLocation.latitude, CurrentLocation.longitude), (float) mCurrentDirection);
            }
        }
        lastX = x;

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // 追踪选项设置
            // Tracing options Settings
            case R.id.btn_activity_options:
                ViewUtil.startActivityForResult(this, TracingOptionsActivity.class, Constants.REQUEST_CODE);
                break;
            case R.id.btn_trace:
                if (trackApp.isTraceStarted) {
                    trackApp.mClient.stopTrace(trackApp.mTrace, traceListener);
                } else {
                    //开始服务
                    //Start Service
                    trackApp.mClient.startTrace(trackApp.mTrace, traceListener);
                }
                break;
            case R.id.btn_gather:
                if (trackApp.isGatherStarted) {
                    trackApp.mClient.stopGather(traceListener);
                    //获取系统时间
                    endTimeMillis = System.currentTimeMillis();
                    endTimeMillis = endTimeMillis / 1000;

                    SimpleDateFormat date = new SimpleDateFormat("yyyy/MM/dd");
                    endDate = date.format(new Date());
                    SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
                    endTime = time.format(new Date());

                    Trace trace = new Trace();
                    trace.setStartDate(startDate);
                    trace.setStartTime(startTime);
                    trace.setEndDate(endDate);
                    trace.setEndTime(endTime);
                    trace.setStartTimeMillis(startTimeMillis.toString());
                    trace.setEndTimeMillis(endTimeMillis.toString());
                    trace.setUser(mUser);
                    trace.save(new SaveListener<String>() {
                        @Override
                        public void done(String s, BmobException e) {
                            if(e==null){

                            }else{
                                Toast.makeText(TracingActivity.this,"时间保存失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    trackApp.mClient.setInterval(Constants.DEFAULT_GATHER_INTERVAL, packInterval);
                    //开启采集
                    //Open to collect
//                    //获取系统时间
                    startTimeMillis = System.currentTimeMillis();
                    startTimeMillis = startTimeMillis / 1000;

                    SimpleDateFormat date = new SimpleDateFormat("yyyy/MM/dd");
                    startDate = date.format(new Date());
                    SimpleDateFormat time = new SimpleDateFormat("HH:mm:ss");
                    startTime = time.format(new Date());
                    trackApp.mClient.startGather(traceListener);
                }
                break;

            default:
                break;
        }

    }

    /*
     * 设置服务按钮样式
     * Set the service button style
     */
    private void setTraceBtnStyle() {
        boolean isTraceStarted = trackApp.trackConf.getBoolean("is_trace_started", false);
        if (isTraceStarted) {
            traceBtn.setText(R.string.stop_trace);
            traceBtn.setTextColor(ResourcesCompat.getColor(getResources(), R.color.red, null));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                traceBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.mipmap.bg_btn_sure, null));
            } else {
                traceBtn.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.mipmap.bg_btn_sure, null));
            }
        } else {
            traceBtn.setText(R.string.start_trace);
            traceBtn.setTextColor(ResourcesCompat.getColor(getResources(), R.color.black, null));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                traceBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.mipmap.bg_btn_cancel, null));
            } else {
                traceBtn.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.mipmap.bg_btn_cancel, null));
            }
        }
    }


    /*
     * 设置采集按钮样式
     * Sets the collection button style
     */
    private void setGatherBtnStyle() {
        boolean isGatherStarted = trackApp.trackConf.getBoolean("is_gather_started", false);
        if (isGatherStarted) {
            gatherBtn.setText(R.string.stop_gather);
            gatherBtn.setTextColor(ResourcesCompat.getColor(getResources(), R.color.red, null));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                gatherBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.mipmap.bg_btn_sure, null));
            } else {
                gatherBtn.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.mipmap.bg_btn_sure, null));
            }
        } else {
            gatherBtn.setText(R.string.start_gather);
            gatherBtn.setTextColor(ResourcesCompat.getColor(getResources(), R.color.black, null));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                gatherBtn.setBackground(ResourcesCompat.getDrawable(getResources(), R.mipmap.bg_btn_cancel, null));
            } else {
                gatherBtn.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(), R.mipmap.bg_btn_cancel, null));
            }
        }
    }

    /*
     * 实时定位任务
     * Real-time positioning task
     */
    class RealTimeLocRunnable implements Runnable {

        private int interval = 0;

        public RealTimeLocRunnable(int interval) {
            this.interval = interval;
        }

        @Override
        public void run() {
            trackApp.getCurrentLocation(entityListener, trackListener);
            realTimeHandler.postDelayed(this, interval * 500);
        }
    }

    public void startRealTimeLoc(int interval) {
        realTimeLocRunnable = new RealTimeLocRunnable(interval);
        realTimeHandler.post(realTimeLocRunnable);
    }

    public void stopRealTimeLoc() {
        if (null != realTimeHandler && null != realTimeLocRunnable) {
            realTimeHandler.removeCallbacks(realTimeLocRunnable);
        }
        trackApp.mClient.stopRealTimeLoc();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null == data) {
            return;
        }

        if (data.hasExtra("locationMode")) {
            LocationMode locationMode = LocationMode.valueOf(data.getStringExtra("locationMode"));
            trackApp.mClient.setLocationMode(locationMode);//定位模式
        }
        trackApp.mTrace.setNeedObjectStorage(false);

        if (data.hasExtra("gatherInterval") && data.hasExtra("packInterval")) {
            int gatherInterval = data.getIntExtra("gatherInterval", Constants.DEFAULT_GATHER_INTERVAL);
            int packInterval = data.getIntExtra("packInterval", Constants.DEFAULT_PACK_INTERVAL);
            TracingActivity.this.packInterval = packInterval;
            //设置频率
            //Set the frequency
            trackApp.mClient.setInterval(gatherInterval, packInterval);
        }

    }

    private void initListener() {
        trackListener = new OnTrackListener() {

            /*
             *在定位监听接口查询历史轨迹，通过历史轨迹绘制
             *可以通过查询历史轨迹筛选有价值的轨迹点进行绘制。
             */
            @Override
            public void onLatestPointCallback(LatestPointResponse response) {
                //经过服务端纠偏后的最新的一个位置点，回调
                //The last position point after the server rectifies the deviation, the callback
                try {
                    if (StatusCodes.SUCCESS != response.getStatus()) {
                        return;
                    }
                    LatestPoint point = response.getLatestPoint();
                    if (null == point || CommonUtil.isZeroPoint(point.getLocation().getLatitude(), point.getLocation()
                            .getLongitude())) {
                        return;
                    }
                    LatLng currentLatLng = mapUtil.convertTrace2Map(point.getLocation());
                    if (null == currentLatLng) {
                        return;
                    }
                    //获取起点，标记在地图上
                    //Get the starting point and mark it on the map
                    if(firstLocate){
                        firstLocate = false;
                        Toast.makeText(TracingActivity.this,"In starting point acquisition...", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //当前经纬度
                    //Current latitude and longitude
                    CurrentLocation.locTime = point.getLocTime();
                    CurrentLocation.latitude = currentLatLng.latitude;
                    CurrentLocation.longitude = currentLatLng.longitude;

                    if (trackPoints == null) {
                        return;
                    }
                    trackPoints.add(currentLatLng);

                    //endTime = CommonUtil.getCurrentTime();
                    //ToastUtils.showToastBottom(TracingActivity.this, "差值：" + (endTime - startTime));
                    //queryHistoryTrack();

                    //时时动态的画出运动轨迹
                    //draw the trajectory dynamically
                    mapUtil.drawHistoryTrack(trackPoints, false, mCurrentDirection);
                } catch (Exception x) {

                }


            }
        };

        entityListener = new OnEntityListener() {

            @Override
            public void onReceiveLocation(TraceLocation location) {
                //本地LBSTraceClient客户端获取的位置
                //Location obtained by the local LBSTraceClient client
                try {
                    if (StatusCodes.SUCCESS != location.getStatus() || CommonUtil.isZeroPoint(location.getLatitude(),
                            location.getLongitude())) {
                        return;
                    }
                    LatLng currentLatLng = mapUtil.convertTraceLocation2Map(location);
                    if (null == currentLatLng) {
                        return;
                    }
                    CurrentLocation.locTime = CommonUtil.toTimeStamp(location.getTime());
                    CurrentLocation.latitude = currentLatLng.latitude;
                    CurrentLocation.longitude = currentLatLng.longitude;
                    if (null != mapUtil) {
                        mapUtil.updateMapLocation(currentLatLng, mCurrentDirection);//显示当前位置
                        mapUtil.animateMapStatus(currentLatLng);//缩放
                    }
                } catch (Exception x) {

                }
            }

        };

        traceListener = new OnTraceListener() {

            @Override
            public void onBindServiceCallback(int errorNo, String message) {
                viewUtil.showToast(TracingActivity.this, String.format("onBindServiceCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            @Override
            public void onStartTraceCallback(int errorNo, String message) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.START_TRACE_NETWORK_CONNECT_FAILED <= errorNo) {
                    trackApp.isTraceStarted = true;
                    SharedPreferences.Editor editor = trackApp.trackConf.edit();
                    editor.putBoolean("is_trace_started", true);
                    editor.apply();
                    setTraceBtnStyle();
                    registerReceiver();
                }
                viewUtil.showToast(TracingActivity.this,
                        String.format("onStartTraceCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            @Override
            public void onStopTraceCallback(int errorNo, String message) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.CACHE_TRACK_NOT_UPLOAD == errorNo) {
                    trackApp.isTraceStarted = false;
                    trackApp.isGatherStarted = false;
                    // 停止成功后，直接移除is_trace_started记录（便于区分用户没有停止服务，直接杀死进程的情况）
                    //After the stop is successful, the is_trace_Started record is removed (to make it easier to tell if the user killed the process without stopping the service)
                    SharedPreferences.Editor editor = trackApp.trackConf.edit();
                    editor.remove("is_trace_started");
                    editor.remove("is_gather_started");
                    editor.apply();
                    setTraceBtnStyle();
                    setGatherBtnStyle();
                    unregisterPowerReceiver();
                    firstLocate = true;
                }
                viewUtil.showToast(TracingActivity.this,
                        String.format("onStopTraceCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            @Override
            public void onStartGatherCallback(int errorNo, String message) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.GATHER_STARTED == errorNo) {
                    trackApp.isGatherStarted = true;
                    SharedPreferences.Editor editor = trackApp.trackConf.edit();
                    editor.putBoolean("is_gather_started", true);
                    editor.apply();
                    setGatherBtnStyle();

                    stopRealTimeLoc();
                    startRealTimeLoc(packInterval);
                }
                viewUtil.showToast(TracingActivity.this, String.format("onStartGatherCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            @Override
            public void onStopGatherCallback(int errorNo, String message) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.GATHER_STOPPED == errorNo) {
                    trackApp.isGatherStarted = false;
                    SharedPreferences.Editor editor = trackApp.trackConf.edit();
                    editor.remove("is_gather_started");
                    editor.apply();
                    setGatherBtnStyle();

                    firstLocate = true;
                    stopRealTimeLoc();
                    startRealTimeLoc(Constants.LOC_INTERVAL);

                    if (trackPoints.size() >= 1) {
                        try {
                            mapUtil.drawEndPoint(trackPoints.get(trackPoints.size() - 1));
                        } catch (Exception e) {

                        }
                    }
                }
                viewUtil.showToast(TracingActivity.this,
                        String.format("onStopGatherCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            @Override
            public void onPushCallback(byte messageType, PushMessage pushMessage) {

            }

            @Override
            public void onInitBOSCallback(int i, String s) {

            }
        };

    }

    static class RealTimeHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    //注册广播（电源锁、GPS状态）
    //Registered broadcast (power lock, GPS status)
    @SuppressLint("InvalidWakeLockTag")
    private void registerReceiver() {
        if (trackApp.isRegisterReceiver) {
            return;
        }

        if (null == wakeLock) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "track upload");
        }
        if (null == trackReceiver) {
            trackReceiver = new TrackReceiver(wakeLock);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(StatusCodes.GPS_STATUS_ACTION);
        trackApp.registerReceiver(trackReceiver, filter);
        trackApp.isRegisterReceiver = true;

    }

    private void unregisterPowerReceiver() {
        if (!trackApp.isRegisterReceiver) {
            return;
        }
        if (null != trackReceiver) {
            trackApp.unregisterReceiver(trackReceiver);
        }
        trackApp.isRegisterReceiver = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (trackApp.trackConf.contains("is_trace_started")
                && trackApp.trackConf.contains("is_gather_started")
                && trackApp.trackConf.getBoolean("is_trace_started", false)
                && trackApp.trackConf.getBoolean("is_gather_started", false)) {
            startRealTimeLoc(packInterval);
        } else {
            startRealTimeLoc(Constants.LOC_INTERVAL);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapUtil.onResume();

        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);

        // 在Android 6.0及以上系统，请求将应用添加到白名单。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = trackApp.getPackageName();
            boolean isIgnoring = powerManager.isIgnoringBatteryOptimizations(packageName);
            if (!isIgnoring) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                try {
                    startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onDestroy时执行onDestroy()，实现地图生命周期管理
        mapUtil.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 停止定位
        stopRealTimeLoc();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        // 在activity执行onDestroy时执行onDestroy()，实现地图生命周期管理
        super.onDestroy();
        stopRealTimeLoc();
        trackPoints.clear();
        trackPoints = null;
        mapUtil.clear();
        finish();
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_tracing;
    }

}
