package com.lyk.mymap.activity;

/**
 * coding = utf-8
 *@Author : Li Yunkai(黎耘恺)
 *@Email : 2049721941@qq.com
 *@File : l
 *@Software : Android Studio
 */


import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lyk.mymap.R;

public abstract class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewId());

        //Prevent off screen
        //防止熄灭屏幕
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    //获取布局文件ID
    //Gets the layout file ID
    protected abstract int getContentViewId();

    //设置上标题栏
    //Set the title bar
    public void setTitle(int resId) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.layout_top);
        TextView textView = (TextView) layout.findViewById(R.id.tv_activity_title);
        textView.setText(resId);
    }

    //设置点击监听器
    //Set the click listener
    public void setOnClickListener(View.OnClickListener listener) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.layout_top);
        LinearLayout optionsButton = (LinearLayout) layout.findViewById(R.id.btn_activity_options);
        optionsButton.setOnClickListener(listener);
    }

    //不显示设置按钮
    //The Settings button is not displayed
    public void setOptionsButtonInVisible() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.layout_top);
        LinearLayout optionsButton = (LinearLayout) layout.findViewById(R.id.btn_activity_options);
        optionsButton.setVisibility(View.INVISIBLE);
    }

    //回退事件
    //The fallback event
    public void onBack(View v) {
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
