package com.lyk.mymap.activity;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.lyk.mymap.R;
import com.lyk.mymap.User.User;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

/*
 * 个人信息页面
 * 填写个人基本信息和上传头像
 *
 * Personal Information page
 * Fill in your basic information and upload your avatar
 */

public class InforActivity extends AppCompatActivity {

    ImageView img_user_avatar, exit;
    TextView nickname, age, gender, tx_login_username, birthday;
    User mUser;
    BmobFile bmobFile;

    private TextView mDisplayDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    private File mFile;
    private Bitmap mBitmap;
    String path = "";
    public static final int CHOOSE_PHOTO = 2;
    public static final int CUT_PHOTO = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infor);
        nickname = findViewById(R.id.nickname);
        age = findViewById(R.id.age);
        gender = findViewById(R.id.gender);
        mDisplayDate = findViewById(R.id.birthday);
        tx_login_username = findViewById(R.id.tx_login_username);
        mUser = (User) getIntent().getSerializableExtra("user");
        tx_login_username.setText(mUser.getUsername());
        exit = findViewById(R.id.iv_exit);

        //设置返回按钮
        //Set the back button
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //查询用户资料信息
        //Query user profile information
        BmobQuery<User> query = new BmobQuery<User>();
        query.addWhereEqualTo("username", mUser.getUsername()).findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> users, BmobException e) {
                if (e == null) {
                    User userQuery = users.get(0);
                    //nickname
                    if (userQuery.getNickname() != null) {
                        nickname.setText(userQuery.getNickname());
                    } else {
                        nickname.setText("");
                    }
                    //age
                    if (userQuery.getAge() != null) {
                        age.setText(userQuery.getAge().toString());
                    } else {
                        age.setText("");
                    }
                    //gender
                    if (userQuery.getGender() != null) {
                        gender.setText(userQuery.getGender());
                    } else {
                        gender.setText("");
                    }
                    //birthday
                    if (userQuery.getBirthday() != null) {
                        mDisplayDate.setText(userQuery.getBirthday());
                    } else {
                        mDisplayDate.setText("");
                    }
                    //avatar
                    if (userQuery.getAvatar() == null) {
                        //未上传头像
//                        Toast.makeText(InforActivity.this, "Please upload your avatar", Toast.LENGTH_SHORT).show();
                    } else {
                        bmobFile = userQuery.getAvatar();
                        File file = new File(Environment.getExternalStorageDirectory()+ "/" +bmobFile.getFilename());
//                        Toast.makeText(InforActivity.this, "本地检测路径: " + Environment.getExternalStorageDirectory()+ "/"+bmobFile.getFilename(), Toast.LENGTH_SHORT).show();
                        if(file.exists()){
                            img_user_avatar.setImageURI(Uri.fromFile(new File(Environment.getExternalStorageDirectory()+"/"+bmobFile.getFilename())));
//                            Toast.makeText(InforActivity.this, "本地读取!!", Toast.LENGTH_SHORT).show();
                        }else{
                            File saveFile = new File(Environment.getExternalStorageDirectory(), bmobFile.getFilename());
                            bmobFile.download(saveFile, new DownloadFileListener() {
                                @Override
                                public void done(String s, BmobException e) {
                                    if (e == null) {
                                        img_user_avatar.setImageURI(Uri.parse(s));
//                                        System.out.println("保存路径:" + s);
//                                        Toast.makeText(InforActivity.this, "GetFilename:" + bmobFile.getFilename(), Toast.LENGTH_SHORT).show();
//                                        Toast.makeText(InforActivity.this, "下载成功,保存路径:" + s, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(InforActivity.this, "download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                                @Override
                                public void onProgress(Integer integer, long l) {
//                                    Toast.makeText(InforActivity.this, "下载进度: " + l, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                } else {
                    Toast.makeText(InforActivity.this, "query fails：" + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        //点击设置nickname
        //Click to setup Nickname
        nickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder build = new AlertDialog.Builder(InforActivity.this);
                EditText edit = new EditText(InforActivity.this);
                build.setTitle("Enter your nickname here");
                build.setIcon(R.drawable.ic_nickname);
                build.setView(edit);
                build.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        nickname.setText(edit.getText());
                        User userUpdate = new User();
                        userUpdate.setNickname(edit.getText().toString());
                        userUpdate.update(mUser.getObjectId(), new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                } else {
                                    Toast toast = Toast.makeText(InforActivity.this, "UPDATE FAILED", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            }
                        });
                    }
                });
                build.setNegativeButton("Return", null);
                build.show();
            }
        });

        //设置age
        //set age count
        age.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder build = new AlertDialog.Builder(InforActivity.this);
                EditText edit = new EditText(InforActivity.this);
                DigitsKeyListener numericOnlyListener = new DigitsKeyListener(false,true);
                edit.setKeyListener(numericOnlyListener);
                build.setTitle("Enter your age here");
                build.setIcon(R.drawable.ic_age);
                build.setView(edit);
                build.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        age.setText(edit.getText());
                        User userUpdate = new User();
                        userUpdate.setAge(Integer.parseInt(edit.getText().toString()));
                        userUpdate.update(mUser.getObjectId(), new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                } else {
                                    Toast toast = Toast.makeText(InforActivity.this, "UPDATE FAILED", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            }
                        });
                    }
                });
                build.setNegativeButton("Return", null);
                build.show();
            }
        });

        //设置gender
        //set data of gender
        gender.setOnClickListener(new View.OnClickListener() {
            String[] single_list = {"Male", "Female"};
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(InforActivity.this);
                builder.setTitle("Choose your gender");
                builder.setIcon(R.drawable.ic_gender);
                builder.setSingleChoiceItems(single_list, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String str = single_list[which];
                        gender.setText(str);
                        User userUpdate = new User();
                        userUpdate.setGender(str);
                        userUpdate.update(mUser.getObjectId(), new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                } else {
                                    Toast toast = Toast.makeText(InforActivity.this, "UPDATE FAILED", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            }
                        });
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        //生日设置
        //set birthday
        mDisplayDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //利用Calendar记录时间类型
                //Use Calendar to record time types
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);
                //设置生日日期选择弹窗
                //Set the birthday selection popup
                //得到年月日
                //get the year, the month, the day
                //调用mDateSetListener来接受数据并设置为规定格式
                //The mDateSetListener is called to accept the data and set to the specified format
                DatePickerDialog dialog = new DatePickerDialog(
                        InforActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        //将日期转化为固定格式
        //Convert the date to a fixed format
        //month需要+1，因为从0开始计算
        //Month requires +1 because you start at 0
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                String date = year + "/" + month + "/" + day;
                mDisplayDate.setText(date);
                User userUpdate = new User();
                userUpdate.setBirthday(date);
                userUpdate.update(mUser.getObjectId(), new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e == null) {
                        } else {
                            Toast toast = Toast.makeText(InforActivity.this, "UPDATE FAILED", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                });
            }
        };


        /*
         * 设置头像
         * avatar setting
         *
         * Reference:
         * https://blog.csdn.net/zhangjiuding/article/details/77772725
         */
        img_user_avatar = findViewById(R.id.img_user_avatar);
        img_user_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //选择相册
                //Choose an album
                pickImageFromAlbum();
            }
        });
    }

    //从相册获取图片
    //Get an image from an album
    public void pickImageFromAlbum(){
        Intent picIntent = new Intent(Intent.ACTION_PICK, null);
        picIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(picIntent, CHOOSE_PHOTO);
    }

    //得到选择照片结果
    //Get the result of selecting photos
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CHOOSE_PHOTO:
                    if (data == null || data.getData() == null) {
                        return;
                    }
                    try {
                        Bitmap bm = null;
                        //获得图片的uri
                        //Get the URI of the image
                        Uri originalUri = data.getData();
                        bm = MediaStore.Images.Media.getBitmap(getContentResolver(), originalUri); //得到bitmap图片
                        //获取图片的路径
                        //Gets the path to the image
                        String[] proj = {MediaStore.Images.Media.DATA};
                        //android多媒体数据库的封装接口，具体的看Android文档
                        //The encapsulation interface of Android multimedia database. See the Android documentation for details
                        Cursor cursor = managedQuery(originalUri, proj, null, null, null);
                        //获得用户选择的图片的索引值
                        //Gets the index value of the image selected by the user
                        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        //将光标移至开头，不小心很容易引起越界
                        //Moving the cursor to the top of the cursor can easily lead to overstepping
                        cursor.moveToFirst();
                        //最后根据索引值获取图片路径
                        //Finally, the image path is obtained according to the index value
                        path = cursor.getString(column_index);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    startPhotoZoom(data.getData());
                    break;
                case CUT_PHOTO:
                    if (data != null) {
                        setPicToView(data);
                    }
                    break;


            }
        }

    }

    /**
     * 打开系统图片裁剪功能
     * Open the system image clipping function
     *
     * @param uri  uri
     */
    private void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("scale", true); //black edging
        intent.putExtra("scaleUpIfNeeded", true); //black edging
        intent.putExtra("return-data", true);
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, CUT_PHOTO);

    }

    private void setPicToView(Intent data) {
        Bundle bundle = data.getExtras();
        if (bundle != null) {
            //文件上传
            //files upload
            mBitmap = bundle.getParcelable("data");
            img_user_avatar.setImageBitmap(mBitmap);
            if(mFile != null){
                path = mFile.getPath();
            }
            //展示图片path
            //Show the picture path
            Toast.makeText(InforActivity.this, "path:"+ path, Toast.LENGTH_SHORT).show();
            bmobFile = new BmobFile(new File(path));
            //Bmob上传文件
            //Bmob uploads files
            bmobFile.uploadblock(new UploadFileListener() {
                @Override
                public void done(BmobException e) {
                    if (e == null) {
//                        Toast.makeText(InforActivity.this, "上传成功: " + bmobFile.getFileUrl(), Toast.LENGTH_SHORT).show();
                        User userUpdate = new User();
                        userUpdate.setAvatar(bmobFile);
                        userUpdate.update(mUser.getObjectId(), new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                    Toast.makeText(InforActivity.this, "update successful", Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(InforActivity.this, "DB update failed, wait for newt version", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }else{
//                        System.out.println("Uploadwwwwwwwwwwwww: " + e.getErrorCode()+ " " +e.getMessage());
                        Toast.makeText(InforActivity.this, "file update failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

}