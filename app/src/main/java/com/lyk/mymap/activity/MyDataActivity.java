package com.lyk.mymap.activity;

import android.content.Intent;
import android.graphics.Color;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.lyk.mymap.Adapter.MyDataAdapter;
import com.lyk.mymap.R;
import com.lyk.mymap.User.Trace;
import com.lyk.mymap.User.User;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * 我的运动数据页面
 * 包含一个近四次运动数据的图表和每一次运动的list
 * 点击每一个list项目可以查看具体的运动数据
 *
 * My running data page
 * A chart containing data from nearly four movements and a list of each movement
 * Click on each list item to view the specific running data
 */

public class MyDataActivity extends AppCompatActivity {

    private User mUser;
    private ListView listView;
    private ImageView exit;
    private GraphView graph;
    private TextView total_distance;
    private String total;
    private double d = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_data_avtivity);
        mUser = (User) getIntent().getSerializableExtra("user");
        listView = findViewById(R.id.myData);
        exit = findViewById(R.id.iv_exit);
        graph = findViewById(R.id.graph);
        total_distance = findViewById(R.id.total_distance_edit);

        //通过函数得到总运动距离
        //get the total distance of running
        total = getTotalDistance();
        total_distance.setText(total);

        //执行画图函数
        drawGraph();

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //通过传入用户来查询用户运动记录
        //Query the user motion record by passing in the user
        BmobQuery<Trace> query = new BmobQuery<>();
        //按照时间降序
        //In descending order of time
        User userQuery = new User();
        userQuery.setObjectId(mUser.getObjectId());
        query.addWhereEqualTo("user", userQuery);
        query.include("user");
        query.order("-createdAt");
        query.findObjects(new FindListener<Trace>() {
            @Override
            public void done(List<Trace> list, BmobException e) {
            if(e==null){
                for(int i=0; i<list.size(); i++) {
                    listView.setAdapter(new MyDataAdapter(MyDataActivity.this, list));
                }
            }else {
                System.out.println("Query error: " + e.getMessage());
                Toast.makeText(MyDataActivity.this,"Query error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            }
        });

        //设置每个list的点击跳转
        //Set the click jump for each List
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BmobQuery<Trace> query = new BmobQuery<>();
                //按照时间降序
                //In descending order of time
                User userQuery = new User();
                userQuery.setObjectId(mUser.getObjectId());
                query.addWhereEqualTo("user", userQuery);
                query.order("-createdAt");
                query.findObjects(new FindListener<Trace>() {
                    @Override
                    public void done(List<Trace> list, BmobException e) {
                        if(e==null){
                            Trace traceQuery = list.get(position);
                            Intent intent = new Intent(MyDataActivity.this, DataInfoActivity.class);
                            intent.putExtra("trace", traceQuery);
                            startActivity(intent);
                        }else {
                            System.out.println("Query error: " + e.getMessage());
                            Toast.makeText(MyDataActivity.this,"Query error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    /**
     * 通过Bmob查询函数得到运动距离总和
     * 添加if判断最近一次的运动距离是否同步，未同步则数据未null
     *
     * Query the function by Bmob to get the sum of the motion distances
     * Add if to determine whether the last motion distance is synchronized. If not, the data is not null
     *
     * @return 字符串类型的运动距离
     */
    public String getTotalDistance(){
        BmobQuery<Trace> query = new BmobQuery<>();
        User userQuery = new User();
        userQuery.setObjectId(mUser.getObjectId());
        query.addWhereEqualTo("user", userQuery);
        query.include("user");
        query.order("-createdAt");
        query.findObjects(new FindListener<Trace>() {
            @Override
            public void done(List<Trace> list, BmobException e) {
                if(list.size()>1){
                    if(!(list.get(0).getDistance().equals(null))){
//                        System.out.println("w222222222222222222222222222222222222222222222222222222");
                        if (e == null) {
                            for (int i = 0; i < list.size(); i++) {
                                d = d + Double.parseDouble(list.get(i).getDistance());
                            }
                            total_distance.setText(String.valueOf(d));
                        } else {
                            Toast.makeText(MyDataActivity.this, "Query1 error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        if (e == null) {
//                            System.out.println("11111111111111111111111111111111111111111111111111111111111111111111111111");
                            for (int i = 1; i < list.size(); i++) {
                                d = d + Double.parseDouble(list.get(i).getDistance());
                            }
                            total_distance.setText(String.valueOf(d));
                        } else {
                            Toast.makeText(MyDataActivity.this, "Query error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }else if(list.size()==1){
                    if(!list.get(0).getDistance().isEmpty()){
                        if (e == null) {
                            for (int i = 0; i < list.size(); i++) {
                                d = d + Double.parseDouble(list.get(i).getDistance());
                            }
                            total_distance.setText(String.valueOf(d));
                        } else {
                            Toast.makeText(MyDataActivity.this, "Query error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        if (e == null) {
                            total_distance.setText("Wait for check");
                        } else {
                            Toast.makeText(MyDataActivity.this, "Query error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }else{
                    if (e == null) {
                        total_distance.setText("No sport record");
                    } else {
                        Toast.makeText(MyDataActivity.this, "Query error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        return String.valueOf(total_distance);
    }

    public void drawGraph(){
        BmobQuery<Trace> query = new BmobQuery<>();
        //按照时间降序
        //In descending order of time
        User userQuery = new User();
        userQuery.setObjectId(mUser.getObjectId());
        query.addWhereEqualTo("user", userQuery);
        query.order("-createdAt");
        query.findObjects(new FindListener<Trace>() {
            @Override
            public void done(List<Trace> list, BmobException e) {
            if(e==null){
                if(list.size()>=4){
                    //查看是否同步最近一次
                    //Check to see if you synchronized the last time
                    if(!list.get(0).getDistance().equals(null)){
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
                        Toast.makeText(MyDataActivity.this, "Please check your most recent exercise record and try again", Toast.LENGTH_SHORT).show();
                        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                                new DataPoint(1, Double.parseDouble(list.get(1).getDistance())),
                                new DataPoint(3, Double.parseDouble(list.get(2).getDistance())),
                                new DataPoint(4, Double.parseDouble(list.get(3).getDistance()))
                        });
                        series.setColor(Color.BLUE);
                        graph.addSeries(series);
                        graph.setTitle("Last 3 data of runs");
                        graph.setTitleTextSize(60);
                        graph.setTitleColor(Color.BLUE);
                    }
                }else if(list.size()>=1 && list.size()<4){
                    if(!list.get(0).getDistance().isEmpty()){
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
                        Toast.makeText(MyDataActivity.this, "Please check your most recent exercise record and try again", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    graph.setTitle("Lack of running data");
                    graph.setTitleTextSize(60);
                    graph.setTitleColor(Color.BLUE);
                }
            }else {
                Toast.makeText(MyDataActivity.this,"Query error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
            }
        });
    }

}