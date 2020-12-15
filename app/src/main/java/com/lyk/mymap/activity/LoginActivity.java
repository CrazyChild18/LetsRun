package com.lyk.mymap.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lyk.mymap.R;
import com.lyk.mymap.User.User;

import java.util.Map;
import java.util.Set;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FetchUserInfoListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * 通过密码登录，只针对已经通过密码注册的用户
 * Login with password, only for users who have already registered with password
 */
public class LoginActivity extends AppCompatActivity {

    EditText mEdtUsername;
    EditText mEdtPassword;
    TextView mTvInfo;
    Button btn_login;
    Button btn_register;
    Button btn_reset;
    SharedPreferences LoginPreferences;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Bmob.initialize(this, "0ab90242dddf2627acb58ea561e4c180");

        mEdtUsername = findViewById(R.id.edt_username);
        mEdtPassword = findViewById(R.id.edt_password);
        mTvInfo = findViewById(R.id.tv_info);
        btn_login = findViewById(R.id.btn_login);
        btn_register = findViewById(R.id.btn_register);
        btn_reset = findViewById(R.id.btn_reset);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //得到用户名
                //Get the user name
                String username = mEdtUsername.getText().toString().trim();
                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(LoginActivity.this, "Please input user name", Toast.LENGTH_SHORT).show();
                    return;
                }
                //得到密码
                //Get the password
                String password = mEdtPassword.getText().toString().trim();
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                    return;
                }
                //创建User对象
                //Create the user object
                //传入用户名和密码
                //Pass in the user name and password
                //进行登陆并验证
                //Log in and verify
                final User user = new User();
                user.setUsername(username);
                user.setPassword(password);
                user.login(new SaveListener<User>() {
                    @Override
                    public void done(User user, BmobException e) {
                        if (e == null) {
                            Toast toast = Toast.makeText(LoginActivity.this, "login successful", Toast.LENGTH_SHORT);
                            toast.show();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("user", user);
                            startActivity(intent);
                            //将登陆信息写入sp
                            //传输到整个程序，来实现自动登录功能
                            LoginPreferences = getSharedPreferences("Login_Red", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = LoginPreferences.edit();
                            editor.putString("username", mEdtUsername.getText().toString().trim());
                            editor.putString("password", mEdtPassword.getText().toString().trim());
                            editor.putString("isLogin", "true");
                            editor.putString("isExit", "false");
                            editor.commit();
                            finish();
                        } else {
                            Toast toast = Toast.makeText(LoginActivity.this, "Logon failed: "+ e.getMessage(), Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                });
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetActivity.class));
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
