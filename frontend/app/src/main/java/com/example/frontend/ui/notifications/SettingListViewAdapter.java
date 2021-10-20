package com.example.frontend.ui.notifications;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.frontend.R;

import java.util.List;

public class SettingListViewAdapter extends BaseAdapter {
    //声明引用
    private Context mContext;
    private LayoutInflater mlayoutInflater;
    private TextView setting_name;
    private String[] name_list=null;

    private String getSettingName(int position){
        return name_list[position%9];
    }

    public SettingListViewAdapter(Context context,String[] list){
        this.mContext=context;
        this.name_list=list;
        mlayoutInflater=LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return 7;
    }

    @Override
    public Object getItem(int position) {
        return name_list[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView==null){
            convertView= mlayoutInflater.inflate(R.layout.setting_item,null);
            setting_name=convertView.findViewById(R.id.setting_item);
            convertView.setTag(setting_name);

        }else{
            setting_name=(TextView)convertView.getTag();
        }
        setting_name.setText(getSettingName(position));
        return convertView;
    }
}
