package com.example.frontend.ui.dashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.frontend.R;

import java.util.ArrayList;
import java.util.Map;

public class DashboardAdapter extends BaseAdapter {
    private ArrayList<Map<String,Object>> myList;
    private Context myContext;
    private LayoutInflater layoutInflater;

    public DashboardAdapter(Context context,ArrayList<Map<String,Object>>myList){
        this.myContext=context;
        this.myList=myList;
        layoutInflater=LayoutInflater.from(context);
    }


    static class ViewHolder{
        public TextView theme;
        public TextView content;
        public ImageView img;
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
        ViewHolder holder=null;
        if(convertView==null){
            convertView=layoutInflater.inflate(R.layout.dashboard_list_cell,null);
            holder=new ViewHolder();
            holder.theme=convertView.findViewById(R.id.dashboard_theme);
            holder.content=convertView.findViewById(R.id.dashboard_content);
            holder.img=convertView.findViewById(R.id.dashboard_image);
            convertView.setTag(holder);
        }else{
            holder=(ViewHolder)convertView.getTag();
        }
        holder.theme.setText(myList.get(position).get("theme").toString());
        holder.content.setText(myList.get(position).get("content").toString());
        holder.img.setImageResource((int)(myList.get(position).get("image")));
        return convertView;
    }
}
