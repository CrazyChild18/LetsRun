package com.lyk.mymap.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.lyk.mymap.R;
import com.lyk.mymap.User.User;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;

/**
 * 通过短信重置密码，只针对已经通过密码注册并且通过短信验证绑定的用户
 * Reset password via SMS, only for users who have already registered with password and authenticated by SMS
 */
public class ResetActivity extends AppCompatActivity {

    @BindView(R.id.edt_new_password)
    EditText mEdtNewPassword;
    @BindView(R.id.edt_code)
    EditText mEdtCode;
    @BindView(R.id.tv_info)
    TextView mTvInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_send, R.id.btn_reset})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_send:
                User user = BmobUser.getCurrentUser(User.class);
                String phone = user.getMobilePhoneNumber();
                Boolean verify = user.getMobilePhoneNumberVerified();
                if (TextUtils.isEmpty(phone)) {
                    Toast.makeText(this, "Please bind the phone number first", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (verify == null || !verify) {
                    Toast.makeText(this, "Please bind the phone number first", Toast.LENGTH_SHORT).show();
                    return;
                }
                BmobSMS.requestSMSCode(phone, "sendM", new QueryListener<Integer>() {
                    @Override
                    public void done(Integer smsId, BmobException e) {
                        if (e == null) {
                            mTvInfo.append("Verification code sent successfully" + "\n");
                        } else {
                            mTvInfo.append("Failed to send verification code：" + e.getMessage() + "\n");
                        }
                    }
                });
                break;
            case R.id.btn_reset:
                String newPassword = mEdtNewPassword.getText().toString().trim();
                if (TextUtils.isEmpty(newPassword)) {
                    Toast.makeText(this, "Please enter a new password", Toast.LENGTH_SHORT).show();
                    return;
                }
                String code = mEdtCode.getText().toString().trim();
                if (TextUtils.isEmpty(code)) {
                    Toast.makeText(this, "Please enter the verification code", Toast.LENGTH_SHORT).show();
                    return;
                }
                BmobUser.resetPasswordBySMSCode(code, newPassword, new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e == null) {
                            mTvInfo.append("Reset the success");
                            startActivity(new Intent(ResetActivity.this, LoginActivity.class));
                        } else {
                            mTvInfo.append("Reset failed：" + e.getErrorCode() + "-" + e.getMessage());
                        }
                    }
                });
                break;

            default:
                break;
        }
    }
}
