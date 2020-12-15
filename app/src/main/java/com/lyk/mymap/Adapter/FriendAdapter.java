package com.lyk.mymap.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lyk.mymap.R;
import com.lyk.mymap.User.Friend;
import com.lyk.mymap.User.Trace;

import java.util.List;

/**
 * 好友列表适配器
 * 直接传入list来实现frienditem的适配
 * 为了配合Bmob查询函数返回的list
 *
 * Buddy list adapter
 * Pass in the List directly to implement the friendItem adaptation
 * To match the list returned by the Bmob query function
 */

public class FriendAdapter extends BaseAdapter {
    private Context context;
    private List<Friend> list;

    public FriendAdapter(Context context, List<Friend> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            String name = list.get(position).getName();
//            ImageView head = new ImageView(D:\App\frame\app\src\main\res\drawable\ic_friend.png)
            LayoutInflater inflater = LayoutInflater.from(context);
            //实例化一个布局文件
            //Instantiate a layout file
            convertView = inflater.inflate(R.layout.friend_item, null);
            TextView friend_name = convertView.findViewById(R.id.friend_name);
            ImageView friend_head = convertView.findViewById(R.id.friend_image);
            friend_name.setText(name);
            friend_head.setImageResource(R.drawable.ic_avatar);
        }
        return convertView;
    }
}
