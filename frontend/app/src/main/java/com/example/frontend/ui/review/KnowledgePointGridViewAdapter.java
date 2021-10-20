package com.example.frontend.ui.review;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.frontend.R;

import java.util.ArrayList;

public class KnowledgePointGridViewAdapter extends BaseAdapter {
    private Context mContext;//用于加载图标
    private LayoutInflater mlayoutInflater;
    private ArrayList<String> myList;

    public KnowledgePointGridViewAdapter(Context context,ArrayList<String> lists){
        this.mContext=context;
        this.myList=lists;
        mlayoutInflater=LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return myList.size();
    }

    @Override
    public Object getItem(int position) {
        return myList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView holder=null;
        if(convertView==null){
            convertView=mlayoutInflater.inflate(R.layout.simple_tips,null);
            holder=(TextView)convertView.findViewById(R.id.tips_text);
            convertView.setTag(holder);
        }else{
            holder=(TextView)convertView.getTag();
        }
        holder.setText(myList.get(position));
        return convertView;
    }
}
