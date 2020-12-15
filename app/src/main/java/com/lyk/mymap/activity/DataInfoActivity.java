package com.lyk.mymap.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.api.track.DistanceRequest;
import com.baidu.trace.api.track.DistanceResponse;
import com.baidu.trace.api.track.HistoryTrackRequest;
import com.baidu.trace.api.track.HistoryTrackResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.api.track.SupplementMode;
import com.baidu.trace.api.track.TrackPoint;
import com.baidu.trace.model.CoordType;
import com.baidu.trace.model.ProcessOption;
import com.baidu.trace.model.SortType;
import com.baidu.trace.model.StatusCodes;
import com.baidu.trace.model.TransportMode;
import com.lyk.mymap.R;
import com.lyk.mymap.TrackApplication;
import com.lyk.mymap.User.Trace;
import com.lyk.mymap.User.User;
import com.lyk.mymap.utils.CommonUtil;
import com.lyk.mymap.utils.Constants;
import com.lyk.mymap.utils.MapUtil;
import com.lyk.mymap.utils.ViewUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

public class DataInfoActivity extends AppCompatActivity {

    private Trace mTrace;
    private TextView start_time_edit;
    private TextView end_time_edit;
    private TextView distance_edit;

    private TrackApplication trackApp = null;
    private ViewUtil viewUtil = null;

    //地图工具
    //Map Tools
    private MapUtil mapUtil = null;

    private String distance;

    //历史轨迹请求
    //History track request
    private HistoryTrackRequest historyTrackRequest = new HistoryTrackRequest();

    //轨迹监听器（用于接收历史轨迹回调）
    //Track listener (for receiving historical track callbacks)
    private OnTrackListener mTrackListener = null;

    //轨迹点集合
    //Collection of locus points
    private List<LatLng> trackPoints = new ArrayList<>();

    //轨迹排序规则
    //Trajectory collation
    private SortType sortType = SortType.asc;

    private int pageIndex = 1;

    //开始和结束时间
    ///Start time and end time
    long startTimeInMillis;
    long endTimeInMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_info);
        trackApp = (TrackApplication) getApplicationContext();
        viewUtil = new ViewUtil();
        mapUtil = MapUtil.getInstance();
        mapUtil.init(findViewById(R.id.mapShow));

        mTrace = (Trace) getIntent().getSerializableExtra("trace");
        start_time_edit = findViewById(R.id.start_time_edit);
        end_time_edit = findViewById(R.id.end_time_edit);
        distance_edit = findViewById(R.id.distance_edit);

        //得到时间 yyyy/mm/dd hh:mm:ss
        //Get time
        start_time_edit.setText(mTrace.getStartDate() + " " + mTrace.getStartTime());
        end_time_edit.setText(mTrace.getEndDate() + " " + mTrace.getEndTime());

        //得到时间格式
        //Get time format yyyy/mm/dd hh:mm:ss
        startTimeInMillis = Long.parseLong(mTrace.getStartTimeMillis());
        endTimeInMillis = Long.parseLong(mTrace.getEndTimeMillis());
        initListener();
        queryHistoryTrack();
    }

    private void initListener() {
        mTrackListener = new OnTrackListener() {
            @Override
            public void onHistoryTrackCallback(HistoryTrackResponse response) {
                try {
                    int total = response.getTotal();
                    if (StatusCodes.SUCCESS != response.getStatus()) {
                        viewUtil.showToast(DataInfoActivity.this, response.getMessage());
                    } else if (0 == total) {
                        viewUtil.showToast(DataInfoActivity.this, getString(R.string.no_track_data));
                    } else {
                        List<TrackPoint> points = response.getTrackPoints();
                        if (null != points) {
                            for (TrackPoint trackPoint : points) {
                                if (!CommonUtil.isZeroPoint(trackPoint.getLocation().getLatitude(), trackPoint.getLocation().getLongitude())) {
                                    trackPoints.add(MapUtil.convertTrace2Map(trackPoint.getLocation()));
                                }
                            }
                        }
                    }
                    //查找下一页数据
                    //Find the next page of data
                    if (total > Constants.PAGE_SIZE * pageIndex) {
                        historyTrackRequest.setPageIndex(++pageIndex);
                        queryHistoryTrack();
                    } else {
                        //画轨迹
                        //Draw trajectory
                        mapUtil.drawHistoryTrack(trackPoints, true, 0);
                    }
                    // 查询里程
                    // Query range
                    queryDistance();
                } catch (Exception e) {
                }
            }

            //通过回传函数得到运动距离
            @Override
            public void onDistanceCallback(DistanceResponse response) {
                if(mTrace.getDistance() != ""){
                    distance = mTrace.getDistance();
                    distance_edit.setText(distance);
                }else {
                    distance = String.valueOf(((float) response.getDistance())*1000/1000);
                    mTrace.setDistance(distance);
                    mTrace.update(mTrace.getObjectId(), new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if(e==null){
                                distance_edit.setText(distance);
                            }else{
                                viewUtil.showToast(DataInfoActivity.this, "Distance更新失败: " + e.getMessage());
                            }
                        }
                    });
                }
                super.onDistanceCallback(response);
            }

        };

    }

    /*
     * 查询历史轨迹
     * 筛选有价值的轨迹点进行绘制
     * 设置里程补偿方式，当轨迹中断5分钟以上，会被认为是一段中断轨迹，默认不补充
     * 比如某些原因造成两点之间的距离过大，相距100米，那么在这两点之间的轨迹如何补偿
     * SupplementMode.driving：补偿轨迹为两点之间最短驾车路线
     * SupplementMode.riding：补偿轨迹为两点之间最短骑车路线
     * SupplementMode.walking：补偿轨迹为两点之间最短步行路线
     * SupplementMode.straight：补偿轨迹为两点之间直线
     *
     * Query history track
     * Select valuable locus points for drawing
     * Set the mileage compensation mode.
     * If the track is interrupted for more than 5 minutes, it will be considered as an interrupt track and will not be supplemented by default
     * if the distance between two points is too large for some reason, 100 meters apart, how can the trajectories between these two points compensate
     * SupplementMode. Driving: The compensation trace shall be the shortest driving route between two points
     * SupplementMode. Riding: The compensation track is the shortest cycling route between two points
     * SupplementMode. Walking: The compensation trail is the shortest walking route between two points
     * SupplementMode. Straight: The compensation trace is a straight line between two points
     */
    private void queryHistoryTrack() {
        trackPoints.clear();
        pageIndex = 1;
        trackApp.initRequest(historyTrackRequest);
        historyTrackRequest.setSupplementMode(SupplementMode.walking);
        //设置返回结果的排序规则，默认升序排序；升序：集合中index=0代表起始
        //Set the collation rules of return results, default ascending sorting; Ascending: Index =0 in the set represents the start
        historyTrackRequest.setSortType(SortType.asc);
        //设置返回结果的坐标类型，默认为百度经纬度
        //Sets the coordinate type of the returned result, which defaults to baidu latitude and longitude
        historyTrackRequest.setCoordTypeOutput(CoordType.bd09ll);
        //Trace中的entityName
        //The Trace of the entityName
        historyTrackRequest.setEntityName(trackApp.entityName);
        //设置startTime和endTime，会请求这段时间内的轨迹数据;
        //When startTime and endTime are set, trajectory data during this period is requested.
        //这里查询采集开始到采集结束之间的轨迹数据
        //Here we query the trajectory data between the beginning and the end of the collection
        historyTrackRequest.setStartTime(startTimeInMillis);
        historyTrackRequest.setEndTime(endTimeInMillis);
        historyTrackRequest.setPageIndex(pageIndex);
        historyTrackRequest.setPageSize(Constants.PAGE_SIZE);
        //发起请求，设置回调监听
        //Initiates the request, sets the callback listener
        trackApp.mClient.queryHistoryTrack(historyTrackRequest, mTrackListener);

        ProcessOption processOption = new ProcessOption();
        processOption.setRadiusThreshold(Constants.DEFAULT_RADIUS_THRESHOLD);
        processOption.setTransportMode(TransportMode.walking);
        processOption.setNeedDenoise(true);
        processOption.setNeedVacuate(true);
        processOption.setNeedMapMatch(true);
        historyTrackRequest.setProcessOption(processOption);
        historyTrackRequest.setProcessed(true);
    }

    private void queryDistance() {
        DistanceRequest distanceRequest = new DistanceRequest(trackApp.getTag(), trackApp.serviceId, trackApp.entityName);
        // 设置开始时间
        // Set the start time
        distanceRequest.setStartTime(startTimeInMillis);
        // 设置结束时间
        // Set end time
        distanceRequest.setEndTime(endTimeInMillis);
        // 纠偏
        // Corrective Actions
        distanceRequest.setProcessed(true);
        // 创建纠偏选项实例
        // Create an instance of rectifying options
        ProcessOption processOption = new ProcessOption();
        // 去噪
        // denoising
        processOption.setNeedDenoise(true);
        // 绑路
        // Tie road
        processOption.setNeedMapMatch(true);
        // 交通方式为步行
        // The transportation is on foot
        processOption.setTransportMode(TransportMode.walking);
        // 设置纠偏选项
        // Set deviation correction options
        distanceRequest.setProcessOption(processOption);
        // 里程填充方式为无
        // The mileage filling mode is null
        distanceRequest.setSupplementMode(SupplementMode.no_supplement);
        // 查询里程
        // Query range
        trackApp.mClient.queryDistance(distanceRequest, mTrackListener);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapUtil.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapUtil.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (null != trackPoints) {
            trackPoints.clear();
        }

        trackPoints = null;
        mapUtil.clear();

    }

}