package com.lyk.mymap.activity;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.lyk.mymap.R;
import com.lyk.mymap.User.Friend;
import com.lyk.mymap.User.Trace;
import com.lyk.mymap.User.User;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/*
 * 这个类是用来查看和显示好友运动数据
 * 通过查询好友得到好友运动距离
 *
 * This class is used to view and display buddy movement data
 * Get the distance of friends' movement by inquiring friends
 */

public class FriendInfoActivity extends AppCompatActivity {

    private TextView friend_name;
    private Friend friendQuery;
    private ImageView exit;
    private GraphView graph;
    private TextView total_distance;
    private String total;
    private double d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_info);
        exit = findViewById(R.id.iv_exit);
        graph = findViewById(R.id.graph);
        total_distance = findViewById(R.id.friend_total_distance_edit);

        //通过传入好友用户名查询好友信息
        //Query the buddy information by passing in the buddy user name
        friendQuery = (Friend)getIntent().getSerializableExtra("queryFriend");

        //设置好友昵称
        //Set buddy nicknames
        friend_name = findViewById(R.id.friend_username);
        friend_name.setText(friendQuery.getName());

        total = getTotalDistance();
        total_distance.setText(total);
        drawGraph();

        //返回按钮
        //return push-button
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    /*
     * 此方法用于计算运动总距离
     * 通过数据库的每一次距离进行计算
     * 添加if判断分支来判断用户是否更新最近一次的运动数据
     * 若没有更新只显示后三次的结果，并提醒用户更新
     *
     * This method is used to calculate the total distance of motion
     * Calculate each distance from the database
     * Add an if judgment branch to determine if the user has updated the last motion data
     * Only the last three results are displayed if there is no update, and the user is reminded to update
     */
    public String getTotalDistance() {
        BmobQuery<User> query = new BmobQuery<>();
        //按照时间降序
        //In descending order of time
        query.addWhereEqualTo("nickname", friendQuery.getName());
        query.order("-createdAt");
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if (e == null) {
                    User u = new User();
                    u.setObjectId(list.get(0).getObjectId());
                    BmobQuery<Trace> query1 = new BmobQuery<>();
                    query1.addWhereEqualTo("user", u);
                    query1.order("-createdAt");
                    query1.findObjects(new FindListener<Trace>() {
                        @Override
                        public void done(List<Trace> list, BmobException e) {
                            if(list.size()>1){
                                if(!list.get(0).getDistance().equals(null)){
                                    if (e == null) {
                                        for (int i = 0; i < list.size(); i++) {
                                            d = d + Double.parseDouble(list.get(i).getDistance());
                                        }
                                        total_distance.setText(String.valueOf(d));
                                    } else {
                                        Toast.makeText(FriendInfoActivity.this, "Query1 error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }else {
                                    if (e == null) {
                                        for (int i = 1; i < list.size(); i++) {
                                            d = d + Double.parseDouble(list.get(i).getDistance());
                                        }
                                        total_distance.setText(String.valueOf(d));
                                    } else {
                                        Toast.makeText(FriendInfoActivity.this, "Query1 error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }else if(list.size()==1){
                                if(!list.get(0).getDistance().equals(null)){
                                    if (e == null) {
                                        for (int i = 0; i < list.size(); i++) {
                                            d = d + Double.parseDouble(list.get(i).getDistance());
                                        }
                                        total_distance.setText(String.valueOf(d));
                                    } else {
                                        Toast.makeText(FriendInfoActivity.this, "Query1 error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }else {
                                    if (e == null) {
                                        total_distance.setText("Wait for check");
                                    } else {
                                        Toast.makeText(FriendInfoActivity.this, "Query1 error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }else{
                                if (e == null) {
                                    total_distance.setText("No sport record");
                                } else {
                                    Toast.makeText(FriendInfoActivity.this, "Query1 error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                } else {
                    Toast.makeText(FriendInfoActivity.this,"Query2 error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        return String.valueOf(total_distance);
    }

    public void drawGraph(){
        BmobQuery<User> query = new BmobQuery<>();
        //按照时间降序
        //In descending order of time
        query.addWhereEqualTo("nickname", friendQuery.getName());
        query.order("-createdAt");
        query.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> list, BmobException e) {
                if(e==null){
                    User u = new User();
                    u.setObjectId(list.get(0).getObjectId());
                    BmobQuery<Trace> query1 = new BmobQuery<>();
                    query1.addWhereEqualTo("user", u);
                    query1.order("-createdAt");
                    query1.findObjects(new FindListener<Trace>() {
                        @Override
                        public void done(List<Trace> list, BmobException e) {
                            if(e==null){
                                if(list.size()>=4){
                                    //判断是否同步最近一次
                                    //Determine whether to synchronize the last time
                                    //因为如果用户没有查看最近一次数据，会导致运动距离无法更新
                                    //Because if the user does not view the last data, the motion distance cannot be updated
                                    //缺失数据无法绘制
                                    //Missing data cannot be plotted
                                    //因此通过if进行判断
                                    //Therefore, the judgment is made through if

                                    //设计为显示最多4次运动记录
                                    //Designed to display up to 4 motion records
                                    //如果不满4次则显示1次
                                    //If less than 4 times, it will be displayed once
                                    //如果无记录则显示无记录
                                    //If there is no record, no record is displayed
                                    if(list.get(0).getDistance().equals(null)){
                                        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                                                new DataPoint(0, Double.parseDouble(list.get(0).getDistance())),
                                                new DataPoint(1, Double.parseDouble(list.get(1).getDistance())),
                                                new DataPoint(3, Double.parseDouble(list.get(2).getDistance())),
                                                new DataPoint(4, Double.parseDouble(list.get(3).getDistance()))
                                        });
                                        series.setColor(Color.BLUE);
                                        graph.addSeries(series);
                                        graph.setTitle("Last 4 data of running");
                                        graph.setTitleTextSize(60);
                                        graph.setTitleColor(Color.BLUE);
                                    }else {
                                        Toast.makeText(FriendInfoActivity.this, "Please wait for friends to view the last exercise record", Toast.LENGTH_SHORT).show();
                                        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                                                new DataPoint(1, Double.parseDouble(list.get(1).getDistance())),
                                                new DataPoint(3, Double.parseDouble(list.get(2).getDistance())),
                                                new DataPoint(4, Double.parseDouble(list.get(3).getDistance()))
                                        });
                                        series.setColor(Color.BLUE);
                                        graph.addSeries(series);
                                        graph.setTitle("Last 3 data of running");
                                        graph.setTitleTextSize(60);
                                        graph.setTitleColor(Color.BLUE);
                                    }
                                }else if(list.size()>=1 && list.size()<4){
                                    if(!list.get(0).getDistance().equals(null)){
                                        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                                                new DataPoint(0, Double.parseDouble(list.get(0).getDistance())),
                                                new DataPoint(1, 0),
                                                new DataPoint(2, 0),
                                                new DataPoint(3, 0)
                                        });
                                        series.setColor(Color.BLUE);
                                        graph.addSeries(series);
                                        graph.setTitle("Data of last run");
                                        graph.setTitleTextSize(60);
                                        graph.setTitleColor(Color.BLUE);
                                    }else {
                                        graph.setTitle("Please wait for friend to confirm the exercise record");
                                        graph.setTitleTextSize(60);
                                        graph.setTitleColor(Color.BLUE);
                                        Toast.makeText(FriendInfoActivity.this, "Please wait for your friend to view the last exercise record, and then try again", Toast.LENGTH_SHORT).show();
                                    }
                                }else{
                                    graph.setTitle("Lack of running data");
                                    graph.setTitleTextSize(60);
                                    graph.setTitleColor(Color.BLUE);
                                }
                            }else {
                                Toast.makeText(FriendInfoActivity.this,"Friend query error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                }else {
                    Toast.makeText(FriendInfoActivity.this,"Query error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}