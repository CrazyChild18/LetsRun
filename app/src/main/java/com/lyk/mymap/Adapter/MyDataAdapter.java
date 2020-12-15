package com.lyk.mymap.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lyk.mymap.R;
import com.lyk.mymap.User.Trace;

import java.util.List;

/**
 * 个人运动数据列表适配器
 * 直接传入list来实现
 * 为了配合Bmob查询函数返回的list
 *
 * Personal Exercise data list adapter
 * Implement this by passing in the List directly
 * To match the list returned by the Bmob query function
 */

public class MyDataAdapter extends BaseAdapter {
    private Context context;
    private List<Trace> list;

    public MyDataAdapter(Context context, List<Trace> list) {
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
            String start = list.get(position).getStartDate() + " " + list.get(position).getStartTime();
            String end = list.get(position).getEndDate() + " " + list.get(position).getEndTime();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.mydata_item, null);//实例化一个布局文件
            TextView s = convertView.findViewById(R.id.start);
            TextView e = convertView.findViewById(R.id.end);
            s.setText(start);
            e.setText(end);
        }
        return convertView;
    }
}
