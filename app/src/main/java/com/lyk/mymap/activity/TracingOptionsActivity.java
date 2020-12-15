package com.lyk.mymap.activity;

/**
 * coding = utf-8
 *@Author : Li Yunkai(黎耘恺)
 *@Email : 2049721941@qq.com
 *@File : l
 *@Software : Android Studio
 */

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.baidu.trace.model.LocationMode;
import com.lyk.mymap.R;
import com.lyk.mymap.utils.Constants;

import static com.baidu.trace.model.LocationMode.High_Accuracy;

/**
 * 轨迹追踪选项
 * Trajectory tracking options
 */
public class TracingOptionsActivity extends BaseActivity {

    // 返回结果
    // return to the result
    private Intent result = null;

    private EditText gatherIntervalText = null;
    private EditText packIntervalText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.tracing_options_title);
        setOptionsButtonInVisible();

        //防止熄灭屏幕
        //Prevent off screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        init();
    }

    private void init() {
        gatherIntervalText = findViewById(R.id.gather_interval);
        packIntervalText = findViewById(R.id.pack_interval);

        gatherIntervalText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                EditText textView = (EditText) view;
                String hintStr = textView.getHint().toString();
                if (hasFocus) {
                    textView.setHint("");
                } else {
                    textView.setHint(hintStr);
                }
            }
        });

        packIntervalText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                EditText textView = (EditText) view;
                String hintStr = textView.getHint().toString();
                if (hasFocus) {
                    textView.setHint("");
                } else {
                    textView.setHint(hintStr);
                }
            }
        });

    }

    public void onCancel(View v) {
        super.onBackPressed();
    }

    public void onFinish(View v) {
        result = new Intent();

        RadioGroup locationModeGroup = findViewById(R.id.location_mode);
        RadioButton locationModeRadio = findViewById(locationModeGroup.getCheckedRadioButtonId());
        //定位模式
        //positioning mode
        LocationMode locationMode = High_Accuracy;
        switch (locationModeRadio.getId()) {
            case R.id.device_sensors:
                locationMode = LocationMode.Device_Sensors;
                break;

            case R.id.battery_saving:
                locationMode = LocationMode.Battery_Saving;
                break;

            case R.id.high_accuracy:
                locationMode = High_Accuracy;
                break;

            default:
                break;
        }
        result.putExtra("locationMode", locationMode.name());

        EditText gatherIntervalText = findViewById(R.id.gather_interval);
        EditText packIntervalText = findViewById(R.id.pack_interval);
        String gatherIntervalStr = gatherIntervalText.getText().toString();
        String packIntervalStr = packIntervalText.getText().toString();

        //采集频率
        //collection frequency
        if (!TextUtils.isEmpty(gatherIntervalStr)) {
            try {
                result.putExtra("gatherInterval", Integer.parseInt(gatherIntervalStr));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        //打包频率
        //Packaging frequency
        if (!TextUtils.isEmpty(packIntervalStr)) {
            try {
                result.putExtra("packInterval", Integer.parseInt(packIntervalStr));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        setResult(Constants.RESULT_CODE, result);
        super.onBackPressed();
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_tracing_options;
    }

}
