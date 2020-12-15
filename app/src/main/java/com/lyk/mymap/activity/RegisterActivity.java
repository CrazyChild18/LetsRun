package com.lyk.mymap.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lyk.mymap.R;
import com.lyk.mymap.User.User;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * 用户注册界面
 * 通过手机号注册
 * 需要用户自己输入用户名
 * 用户名作为登陆使用
 *
 * User registration interface
 * Register by phone number
 * The user is required to enter the user name
 * The user name is used as a login
 */

public class RegisterActivity extends AppCompatActivity {

    EditText mEdtUsername;
    EditText mEdtPassword;
    EditText mEdtNickname;
    EditText mEdtPhone;
    EditText mEdtCode;
    TextView mTvInfo;
    Button btn_send;
    Button btn_signup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initBomb();
        mEdtUsername = findViewById(R.id.edt_username);
        mEdtPassword = findViewById(R.id.edt_password);
        mEdtNickname = findViewById(R.id.edt_nickname);
        mEdtPhone = findViewById(R.id.edt_phone);
        mEdtCode = findViewById(R.id.edt_code);
        mTvInfo = findViewById(R.id.tv_info);
        btn_send = findViewById(R.id.btn_send);
        btn_signup = findViewById(R.id.btn_signup);
        initEvent();
    }

    private void initEvent() {
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = mEdtPhone.getText().toString();
                String code = mEdtCode.getText().toString();
                String username = mEdtUsername.getText().toString();
                String password = mEdtPassword.getText().toString();
                String nickname = mEdtNickname.getText().toString();
                User user = new User();
                //Set mobile phone number (required)
                user.setMobilePhoneNumber(phone);
                if (TextUtils.isEmpty(phone)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your mobile phone number", Toast.LENGTH_SHORT).show();
                    return;
                }
                //Set user name
                user.setUsername(username);
                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(RegisterActivity.this, "Please input user name", Toast.LENGTH_SHORT).show();
                    return;
                }
                //Set User Password
                user.setPassword(password);
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(RegisterActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                    return;
                }
                //Set additional information: nickname here
                user.setNickname(nickname);
                if (TextUtils.isEmpty(nickname)) {
                    Toast.makeText(RegisterActivity.this, "Please enter nickname", Toast.LENGTH_SHORT).show();
                    return;
                }
                user.signOrLogin(code, new SaveListener<BmobUser>() {
                    @Override
                    public void done(BmobUser bmobUser, BmobException e) {
                        if (e == null) {
                            mTvInfo.append("SMS registration successful：" + bmobUser.getUsername());
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        } else {
                            mTvInfo.append("SMS registration failed：" + e.getErrorCode() + "-" + e.getMessage() + "\n");
                        }
                    }
                });

            }
        });

        //发送验证码
        //Send verification code
        btn_send.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String phone_num = mEdtPhone.getText().toString();
                if (phone_num.length() != 11) {
                    Toast.makeText(RegisterActivity.this, "Please enter 11 valid mobile phone numbers", Toast.LENGTH_SHORT).show();
                }
                else {
                    //进行获取验证码操作和倒计时1分钟操作
                    //Carry out the operation of obtaining captcha and counting down 1 minute
                    BmobSMS.requestSMSCode(phone_num, "sendM", new QueryListener<Integer>() {
                        @Override
                        public void done(Integer smsId, BmobException e) {
                            if (e == null) {
                                //发送成功时，让获取验证码按钮不可点击，且为灰色
                                //When the send is successful, make the get captcha button unclickable and gray
                                btn_send.setClickable(false);
                                btn_send.setBackgroundColor(Color.GRAY);
                                Toast.makeText(RegisterActivity.this, "Verification code has been sent successfully. Please use it as soon as possible", Toast.LENGTH_SHORT).show();
                                /**
                                 * 倒计时1分钟操作
                                 * Countdown to 1 minute operation
                                 *
                                 * 说明：
                                 * new CountDownTimer(60000, 1000),第一个参数为倒计时总时间，第二个参数为倒计时的间隔时间
                                 * 单位都为ms，其中必须要实现onTick()和onFinish()两个方法，onTick()方法为当倒计时在进行中时，
                                 * 所做的操作，它的参数millisUntilFinished为距离倒计时结束时的时间，以此题为例，总倒计时长
                                 * 为60000ms,倒计时的间隔时间为1000ms，然后59000ms、58000ms、57000ms...该方法的参数
                                 * millisUntilFinished就等于这些每秒变化的数，然后除以1000，把单位变成秒，显示在textView
                                 * 或Button上，则实现倒计时的效果，onFinish()方法为倒计时结束时要做的操作，此题可以很好的
                                 * 说明该方法的用法，最后要注意的是当new CountDownTimer(60000, 1000)之后，一定要调用start()
                                 * 方法把该倒计时操作启动起来，不调用start()方法的话，是不会进行倒计时操作的
                                 */
                                new CountDownTimer(120000, 1000) {
                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        btn_send.setBackgroundResource(R.drawable.button_shape02);
                                        btn_send.setText(millisUntilFinished / 1000 + "seconds");
                                    }

                                    @Override
                                    public void onFinish() {
                                        btn_send.setClickable(true);
                                        btn_send.setBackgroundResource(R.drawable.button_shape);
                                        btn_send.setText("Resend");
                                    }
                                }.start();
                            }
                            else {
                                Toast.makeText(RegisterActivity.this, "Verification code failed to send, please check network connection" + e.getErrorCode() + "-" + e.getMessage() + "\n", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });
    }

    private void initBomb() {
        Bmob.initialize(this, "0ab90242dddf2627acb58ea561e4c180");
    }


}