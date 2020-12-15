package com.lyk.mymap.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.lyk.mymap.R;
import com.lyk.mymap.TrackApplication;
import com.lyk.mymap.User.User;
import com.lyk.mymap.utils.BitmapUtil;
import com.lyk.mymap.utils.ScreenInfoUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.FetchUserInfoListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * @author Eric Li
 *
 * 系统主界面
 * 初始化创建地图和定位，并创建左侧侧滑框来进行功能跳转
 * 同时在主界面读取sp来检测是否需要自动登录和头像是否更改
 * 如需要更新头像则进行查询和下载
 * 定义onRestart生命周期来实现Info界面和主界面的同步
 *
 * System main interface
 * Initializes the creation of the map and location, and creates the left side slider for the function jump
 * At the same time, read SP on the main interface to detect whether automatic login and avatar changes are needed
 * If you need to update your avatar, you can query and download it
 * Define the onRestart life cycle to synchronize the Info interface with the main interface
 */

public class MainActivity extends AppCompatActivity {

    public User mUser;

    public LocationClient mLocationClient;
    private MapView mapView;
    private BaiduMap baiduMap;
    private boolean isFirstLocate = true;
    private LinearLayout mName;
    private TrackApplication trackApp;
    private Button location;

    ImageView img_user_avatar;

    private BmobFile bmobFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScreenInfoUtils.fullScreen(this);
        //注册Bmob数据库
        Bmob.initialize(this, "0ab90242dddf2627acb58ea561e4c180");

        // 创建LocationClient实例，参数为Context
        // Create a LocationClient instance with Context
        mLocationClient = new LocationClient(getApplicationContext());

        // 调用 registerLocationListener() 方法注册一个定位监听器
        // Calling the registerLocationListener() method registers a location listener
        // 当获取到位置信息的时候，会回调这个定位监听器
        // The location listener is called back when location information is obtained
        mLocationClient.registerLocationListener(new MyLocationListener());

        // 调用SDKInitializer进行初始化
        // SDKInitializer is called for initialization
        SDKInitializer.initialize(getApplicationContext());

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 声明 map 页面
        // Declare the Map page
        setContentView(R.layout.activity_main);
        //apikey的授权需要一定的时间，apikey授权成功后会发送广播通知
        //An ApiKey authorization takes some time, and a broadcast notification is sent when the apiKey authorization is successful
        //这里注册 SDK 广播监听者
        //Register the SDK broadcast listener here
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
//        mReceiver = new SDKReceiver();
//        registerReceiver(mReceiver, iFilter);

        // 找到 MapView 的实例
        // Find an instance of the MapView
        mapView = findViewById(R.id.bmapView);

        // 得到 BaiduMap 的实例（百度LBS SDK提供），地图的总控制器
        // Get the example of BaiduMap (provided by Baidu LBS SDK), the master controller of the map
        baiduMap = mapView.getMap();

        // 显示位置的功能打开
        // Display location function turns on
        baiduMap.setMyLocationEnabled(true);

        // 防止屏幕熄屏，同时可以关闭屏幕使用activity
        // Prevent the screen from going off, and turn off the screen to use activity
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        /*
        *创建一个空的List，判断这3个权限是否被授权，如果没有被授权就添加到集合中
        *然后判断List里是否有对象，有则代表需要动态申请权限
        *调用toArray()方法将List转换成数组
        *然后传入到requestPermissions()全部申请
        *
        *Create an empty List, determine if the three permissions are granted
        * add them to the collection if not
        *Then determine if there is an object in the List, and if there is, you need to dynamically apply for permission
        *Call the toArray() method to convert the List to an array
        *Then pass in to the requestPermissions() to complete the application
        */
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        // list不为空则代表有需要申请的权限
        // If the List is not empty, then there are permissions that need to be applied
        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {
            // 开始定位
            // To locate
            requestLocation();
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        NavigationView navigationview = findViewById(R.id.navigation_view);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        location = findViewById(R.id.location);
        BitmapUtil.init();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, 0, 0);
        //初始化状态
        //Initialized State
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        /*---------------------------添加头布局和尾布局-----------------------------*/
        //获取xml头布局view
        //Gets the XML header layout View
        View headerView = navigationview.getHeaderView(0);

        //寻找头部里面的控件
        //Look for the control inside the header
        img_user_avatar = headerView.findViewById(R.id.img_user_avatar);
        TextView tx_login_username = findViewById(R.id.tx_login_username);

        //用来传输对象user
        //Used to transfer the object User
        mUser = (User)getIntent().getSerializableExtra("user");

        //设置侧边栏头像
        //当mUser不为空时，表明用户已经登陆（从login activity）
        if(mUser != null) {
            BmobQuery<User> query = new BmobQuery<User>();
            query.addWhereEqualTo("username", mUser.getUsername()).findObjects(new FindListener<User>() {
                @Override
                public void done(List<User> users, BmobException e) {
                    if (e == null) {
                        User userQuery = users.get(0);
                        //头像
                        if (userQuery.getAvatar() == null) {

                        } else {
                            bmobFile = userQuery.getAvatar();
                            File file = new File(Environment.getExternalStorageDirectory()+ "/" +bmobFile.getFilename());
                            if(file.exists()){
                                img_user_avatar.setImageURI(Uri.fromFile(new File(Environment.getExternalStorageDirectory()+"/"+bmobFile.getFilename())));
                            }else{
                                File saveFile = new File(Environment.getExternalStorageDirectory(), bmobFile.getFilename());
                                bmobFile.download(saveFile, new DownloadFileListener() {
                                    @Override
                                    public void done(String s, BmobException e) {
                                        if (e == null) {
                                            img_user_avatar.setImageURI(Uri.parse(s));
                                        } else {
                                            Toast.makeText(MainActivity.this, "download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    @Override
                                    public void onProgress(Integer integer, long l) {
                                    }
                                });
                            }
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "query fails：" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }else{
            //用户未登陆但标记为未退出，需要系统自动登录
            //自动登录
            //The user is not logged in but marked as not logged out, the system is required to automatically log in
            //automatic login
            SharedPreferences LoginPreferences = getSharedPreferences("Login_Red", Context.MODE_PRIVATE);
            if(LoginPreferences.getString("isLogin", "false").equals("true")){
                if(LoginPreferences.getString("isExit", "true").equals("false")){
                    User user = new User();
                    mUser = user;
                    user.setUsername(LoginPreferences.getString("username", null));
                    user.setPassword(LoginPreferences.getString("password", null));
                    user.login(new SaveListener<User>() {
                        @Override
                        public void done(User user, BmobException e) {
                            if (e == null) {
                            } else {
                                Toast toast = Toast.makeText(MainActivity.this, "Logon failed: "+ e.getMessage(), Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    });
                    BmobQuery<User> query = new BmobQuery<User>();
                    query.addWhereEqualTo("username", mUser.getUsername()).findObjects(new FindListener<User>() {
                        @Override
                        public void done(List<User> users, BmobException e) {
                            if (e == null) {
                                User userQuery = users.get(0);
                                //头像
                                //avatar
                                if (userQuery.getAvatar() == null) {

                                } else {
                                    bmobFile = userQuery.getAvatar();
                                    File file = new File(Environment.getExternalStorageDirectory()+ "/" +bmobFile.getFilename());
                                    if(file.exists()){
                                        img_user_avatar.setImageURI(Uri.fromFile(new File(Environment.getExternalStorageDirectory()+"/"+bmobFile.getFilename())));
                                    }else{
                                        File saveFile = new File(Environment.getExternalStorageDirectory(), bmobFile.getFilename());
                                        bmobFile.download(saveFile, new DownloadFileListener() {
                                            @Override
                                            public void done(String s, BmobException e) {
                                                if (e == null) {
                                                    img_user_avatar.setImageURI(Uri.parse(s));
                                                } else {
                                                    Toast.makeText(MainActivity.this, "download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            @Override
                                            public void onProgress(Integer integer, long l) {
                                            }
                                        });
                                    }
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "query fails：" + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        }

        //设置退出按钮
        //将sp中islogin设置为false
        //将sp中isExit设置为true
        //标记为用户退出，需要重新登陆
        //Set exit button
        //Set isLogin in SP to false
        //set isExit in Sp to true
        //Mark as user exit, need to login again
        Button footer_item_out = findViewById(R.id.footer_item_out);
        footer_item_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mUser != null){
                    BmobUser.logOut();
                    SharedPreferences LoginPreferences = getSharedPreferences("Login_Red", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = LoginPreferences.edit();
                    editor.putString("isLogin", "false");
                    editor.putString("isExit", "true");
                    editor.commit();
                    finish();
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(intent);
                }else{
                }
            }
        });

        //头像点击事件
        //Avatar click event
        img_user_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mUser != null){
                    Intent intent = new Intent(MainActivity.this, InforActivity.class);
                    //System.out.println("Go into Information");
                    intent.putExtra("user", mUser);
                    startActivity(intent);
                }else{
                    //System.out.println("Go back");
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        });

        navigationview.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        });
        ColorStateList csl = getResources().getColorStateList(R.color.nav_menu_text_color);
        //设置item的条目颜色
        //Set the item's item color
        navigationview.setItemTextColor(csl);
        //去掉默认颜色显示原来颜色  设置为null显示本来图片的颜色
        //Remove the default color display and set the original color to null to display the original color of the image
        navigationview.setItemIconTintList(csl);

        //设置条目点击监听
        //Set the entry to click listen
        navigationview.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                //点击哪个按钮
                //Which button to click
                if(menuItem.getTitle().equals("Start Run")){
                    if (mUser != null) {
                        Intent intent = new Intent(MainActivity.this, TracingActivity.class);
                        intent.putExtra("user", mUser);
                        startActivity(intent);
                    }else{
                        Toast.makeText(MainActivity.this, "You haven't logged in yet, please log in", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    }
                }
                //查看好友列表
                if(menuItem.getTitle().equals("Friends")){
                    if (mUser != null) {
                        Intent intent = new Intent(MainActivity.this, FriendActivity.class);
                        intent.putExtra("user", mUser);
                        startActivity(intent);
                    }else{
                        Toast.makeText(MainActivity.this, "You haven't logged in yet, please log in", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    }
                }
                //查看我的数据
                if(menuItem.getTitle().equals("My Data")){
                    if (mUser != null) {
                        Intent intent = new Intent(MainActivity.this, MyDataActivity.class);
                        intent.putExtra("user", mUser);
                        startActivity(intent);
                    }else{
                        Toast.makeText(MainActivity.this, "You haven't logged in yet, please log in", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    }
                }
                //查看关于
                if(menuItem.getTitle().equals("About")){
                    if (mUser != null) {
                        Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                        intent.putExtra("user", mUser);
                        startActivity(intent);
                    }else{
                        Toast.makeText(MainActivity.this, "You haven't logged in yet, please log in", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    }
                }
                return false;
            }
        });

        //设置定位按钮进行定位
        //Set the positioning button for positioning
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFirstLocate = true;
                requestLocation();
            }
        });

    }

    /*
     *通过一个循环将申请的每个权限都进行了判断
     *如果有任何一个权限被拒绝，则调用finish()销毁当前界面
     *只有当全部权限被授权，才会调用 requestLocation() 开始位置定位
     *
     *Each permission is evaluated in a loop
     *If any permissions are denied, call Finish () to destroy the current interface
     *RequestLocation () is called to start the location only when all permissions have been granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "Permission to use the program must be granted", Toast.LENGTH_SHORT).show();
                        }
                        finish();
                        return;
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "Unknown error occurred", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }

    /*
     *调用 LocationClient 的 start() 即可开始定位
     *定位的结果会回调到前面注册的监听器 MyLocationListener 当中
     *在默认情况下，start() 只会定位一次
     *因此在 requestLocation() 方法前新增一个 initLocation() 方法
     *
     *The location can be started by calling Start () of the LocationClient
     *The result of the location is called back to the previously registered listener MyLocationListener
     *By default, Start () is positioned only once
     *So an initLocation() method is added before the requestLocation() method
     */
    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    //当位置移动的时候，调用 onReceiveLocation() 方法
    //OnReceiveLocation () method is called when the position is moved
    //把 BDLocation 对象传给 navigateTo()方法
    //Pass the BDLocation object to the navigateTo() method
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if(location.getLocType() == BDLocation.TypeGpsLocation || location.getLocType() == BDLocation.TypeNetWorkLocation){
                navigateTo(location);
            }
        }
    }

    /*
     * 创建了一个 LocationClientOption 对象
     * setScanSpan()设置每次更新位置间隔时间
     * 然后调用 LocationClient 的 setLocOption() 方法，并传入 LocationClientOption 对象
     * 在活动被销毁的时候，调用 LocationClient 的 stop() 来停止定位
     * Hight_Accuracy ：表示高精度模式，优先使用GPS，在无法接收GPS的时候使用网络定位
     * Battery_Saving ：表示节电模式，只会使用网络定位
     * Device_Sensors ：表示传感器模式，只会使用GPS定位
     * 加入setIsNeedAddress()，传入 true 代表需要获取当前位置的详细信息
     *
     * Create a LocationClientOption object
     * SetScanSpan () sets the interval between each update position
     * Call the LocationClient's setLocOption() method and passes in the LocationClientOption object
     * When the activity is destroyed, stop() of the LocationClient is called to stop the location
     * Hight_Accuracy: Indicates high-precision mode, gives preference to GPS, and USES network positioning when GPS cannot be received
     * Battery_Saving: means power saving mode, only using Internet location
     * Device_Sensors: Means sensor mode, USES GPS only
     * Add setIsNeedAddress() and pass true to indicate that you need to get the details of your current location
     */
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(1000);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setIsNeedAddress(true);
        // 调整坐标系
        // Coordinate system adjustment
        // gcj02：中国坐标偏移标准，Google、高德、腾讯使用 （默认）
        // WGS84：国际标准GPS坐标
        // BD09：百度坐标偏移
        option.setCoorType("bd09ll");
        mLocationClient.setLocOption(option);
    }

    /*
     * 对mapView进行管理，以便资源及时得到释放
     * Manage the mapView so that resources are released on time
     */


    @Override
    protected void onRestart() {
        super.onRestart();
        //设置侧边栏头像
        //用户未登陆，设置为默认头像
        //Set sidebar heads
        //User not logged in, set as default avatar
        SharedPreferences LoginPreferences = getSharedPreferences("Login_Red", Context.MODE_PRIVATE);
        System.out.println("isLogin: " + LoginPreferences.getString("isLogin", "false") + " isExit: "  + LoginPreferences.getString("isExit", "true"));
        if(LoginPreferences.getString("isLogin", "false").equals("true")){
            if(LoginPreferences.getString("isExit", "true").equals("false")){
                User user = new User();
                mUser = user;
                user.setUsername(LoginPreferences.getString("username", null));
                user.setPassword(LoginPreferences.getString("password", null));
                user.login(new SaveListener<User>() {
                    @Override
                    public void done(User user, BmobException e) {
                        if (e == null) {
                        } else {
//                            Toast.makeText(MainActivity.this, LoginPreferences.getString("isLogin", "false"), Toast.LENGTH_SHORT).show();
                            Toast toast = Toast.makeText(MainActivity.this, "Logon failed: "+ e.getMessage(), Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                });
                BmobQuery<User> query = new BmobQuery<User>();
                query.addWhereEqualTo("username", mUser.getUsername()).findObjects(new FindListener<User>() {
                    @Override
                    public void done(List<User> users, BmobException e) {
                        if (e == null) {
                            User userQuery = users.get(0);
                            //头像
                            //avatar
                            if (userQuery.getAvatar() == null) {

                            } else {
                                bmobFile = userQuery.getAvatar();
                                File file = new File(Environment.getExternalStorageDirectory()+ "/" +bmobFile.getFilename());
                                if(file.exists()){
                                    img_user_avatar.setImageURI(Uri.fromFile(new File(Environment.getExternalStorageDirectory()+"/"+bmobFile.getFilename())));
                                }else{
                                    File saveFile = new File(Environment.getExternalStorageDirectory(), bmobFile.getFilename());
                                    bmobFile.download(saveFile, new DownloadFileListener() {
                                        @Override
                                        public void done(String s, BmobException e) {
                                            if (e == null) {
                                                img_user_avatar.setImageURI(Uri.parse(s));
                                            } else {
                                                Toast.makeText(MainActivity.this, "download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        @Override
                                        public void onProgress(Integer integer, long l) {
                                        }
                                    });
                                }
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "query fails：" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        //把显示位置的功能关闭
        //Turn off the location display
        baiduMap.setMyLocationEnabled(false);
//        BitmapUtil.clear();

//        SharedPreferences LoginPreferences = getSharedPreferences("Login_Red", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = LoginPreferences.edit();
//        editor.putString("isLogin", "false");
//        editor.commit();

    }

    /*
     * 借助 LatLng 类，创建一个实例，并传入纬度、经度作为参数
     * 接着调用 MapStatusUpdateFactory.newLatLng()方法，并将LatLng实例传入，得到一个 MapStatusUpdate 对象
     * 然后把这个对象传入到 BaiduMap 的 animateMapStatus()方法中
     * 可以将地图移动到当前经纬度位置
     *
     * With the LatLng class, create an instance and pass in latitude and longitude as parameters
     * Then call MapStatusUpdateFactory. NewLatLng () method, and LatLng instance into, get a MapStatusUpdate object
     * This object is then passed into the method animateMapStatus() of BaiduMap
     * Can move the map to the current latitude and longitude position
     */
    private void navigateTo(BDLocation location){
        if(isFirstLocate){
            LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
            baiduMap.animateMapStatus(update);
            //传入一个float常量作为缩放级别
            //Pass in a float constant as the zoom level
            //得到一个MapStatusUpdate 对象
            //Get a MapStatusUpdate object
            update = MapStatusUpdateFactory.zoomTo(16f);
            //将上面得到的 MapStatusUpdate 对象传入 BaiduMap 的 animateMapStatus()方法中
            //Pass the MapStatusUpdate object obtained above into the method animateMapStatus() of BaiduMap
            //完成地图缩放级别的设置
            //Complete the map zoom level Settings
            baiduMap.animateMapStatus(update);
            //防止多次加载 animateMapStatus()方法
            //Prevent multiple loading of the animateMapStatus() method
            isFirstLocate = false;
        }
        /*
         * 增加地图中的位置光标显示功能
         * 使用百度 LBS SDK中提供了一个 MyLocationData.Builder 类，只需传入经纬度
         * 调用 MyLocationData.Builder 的 build() 方法得到一个 MyLocationData 类实例
         * 最后再将这个实例传入到 BaiduMap 的 setMyLocationData()方法中
         * 这样就可以让位置显示在地图上了
         *
         * ！！！注意：这部分写在 isFirstLocate 这个if条件语句外
         * ！！！是因为只需要在第一次定位的时候执行让地图移动到当前位置就可以了
         * ！！！不需要跟随设备移动而实时改变
         */
        MyLocationData.Builder builder = new MyLocationData.Builder();
        builder.latitude(location.getLatitude());
        builder.longitude(location.getLongitude());
        MyLocationData locationData = builder.build();
        baiduMap.setMyLocationData(locationData);
    }

}
