package com.lyk.mymap.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.lyk.mymap.Adapter.FriendAdapter;
import com.lyk.mymap.Adapter.MyDataAdapter;
import com.lyk.mymap.R;
import com.lyk.mymap.User.Friend;
import com.lyk.mymap.User.Trace;
import com.lyk.mymap.User.User;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

public class FriendActivity extends AppCompatActivity {

    private User mUser;
    private ListView listView;
    private ImageView exit;
    private EditText search_friend;
    private String name;
    private Button search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        mUser = (User) getIntent().getSerializableExtra("user");
        listView = findViewById(R.id.friend);
        exit = findViewById(R.id.iv_exit);
        search_friend = findViewById(R.id.friend_search);
        search = findViewById(R.id.friend_search_button);
        //显示好友列表
        showFriend();

        //设置搜索好友点击事件
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = search_friend.getText().toString();
                if(name == ""){
                    Toast.makeText(FriendActivity.this, "Username can't be null", Toast.LENGTH_SHORT);
                }else{
                    BmobQuery<User> query = new BmobQuery<>();
                    query.addWhereEqualTo("username", name);
                    query.findObjects(new FindListener<User>() {
                        @Override
                        public void done(List<User> list, BmobException e) {
                            if(e==null){
                                //得到查询用户的id
                                //Get the id of the query user
                                User userGet = list.get(0);
                                //加入好友
                                //Add buddy
                                Friend friendAdd = new Friend();
                                friendAdd.setName(userGet.getNickname());
                                friendAdd.setUser(mUser);
                                friendAdd.save(new SaveListener<String>() {
                                    @Override
                                    public void done(String s, BmobException e) {
                                        if (e==null){
                                            //输入框清空
                                            //Empty input field
                                            search_friend.setText("");
                                            //更新好友列表
                                            //Update your Friends list
                                            showFriend();
                                        }else{
                                            //输入框清空
                                            //Empty input field
                                            search_friend.setText("");
                                            Toast.makeText(FriendActivity.this,"Add friend failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }else {
                                Toast.makeText(FriendActivity.this,"Query error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        //设置点击好友查看好友具体信息
        //Set click on the buddy to see the buddy details
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BmobQuery<Friend> query = new BmobQuery<>();
                query.addWhereEqualTo("user", mUser);
                query.order("-createdAt");
                query.findObjects(new FindListener<Friend>() {
                    @Override
                    public void done(List<Friend> list, BmobException e) {
                        if(e==null){
                            //加入好友
                            //add friends to list
                            Friend friendQuery = list.get(position);
                            Intent intent = new Intent(FriendActivity.this, FriendInfoActivity.class);
                            intent.putExtra("queryFriend", friendQuery);
                            startActivity(intent);
                        }else {
                            Toast.makeText(FriendActivity.this,"Select error!!!" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        //设置长按好友列表来删除好友
        //Set long press the buddy list to delete a buddy
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder build = new AlertDialog.Builder(FriendActivity.this);
                build.setMessage("Are you sure you want to delete this friend???");
                build.setIcon(R.drawable.ic_warning);
                build.setPositiveButton("Cruel delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BmobQuery<Friend> query = new BmobQuery<>();
                        //按照时间降序
                        //In descending order of time
                        User userQuery = new User();
                        userQuery.setObjectId(mUser.getObjectId());
                        query.addWhereEqualTo("user", userQuery);
                        query.order("-createdAt");
                        query.findObjects(new FindListener<Friend>() {
                            @Override
                            public void done(List<Friend> list, BmobException e) {
                                if(e==null){
                                    Friend friendDelete = list.get(position);
                                    friendDelete.delete(new UpdateListener() {
                                        @Override
                                        public void done(BmobException e) {
                                            if(e==null){
                                                //更新好友列表
                                                //Update your Friends list
                                                showFriend();
                                                Toast.makeText(FriendActivity.this,"Delete successful", Toast.LENGTH_SHORT).show();
                                            }else{
                                                Toast.makeText(FriendActivity.this,"Delete friend error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }else {
                                    Toast.makeText(FriendActivity.this,"Delete friend query error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });
                build.setNegativeButton("Think again", null);
                build.show();
                return false;
            }
        });

        //界面返回按钮
        //Interface return button
        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    /*
     * 显示好友列表方法
     * 通过查询然后传入list
     * 可以用来在每一次操作后刷新好友列表
     *
     * Displays a list of friends method
     * query it and pass in the List
     * Can be used to refresh the buddy list after each operation
     */
    public void showFriend(){
        BmobQuery<Friend> query = new BmobQuery<>();
        //按照时间降序
        //In descending order of time
        User userQuery = new User();
        userQuery.setObjectId(mUser.getObjectId());
        query.addWhereEqualTo("user", userQuery);
        query.order("-createdAt");
        query.findObjects(new FindListener<Friend>() {
            @Override
            public void done(List<Friend> list, BmobException e) {
                if(e==null){
                    for(int i=0; i<list.size(); i++) {
                        listView.setAdapter(new FriendAdapter(FriendActivity.this,  list));
                    }
//                    Toast.makeText(FriendActivity.this,"Query success", Toast.LENGTH_SHORT).show();
                }else {
                    System.out.println("Query error: " + e.getMessage());
                    Toast.makeText(FriendActivity.this,"Query error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}