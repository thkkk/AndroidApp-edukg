package com.example.frontend.ui.discovery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.frontend.R;

import java.util.ArrayList;
import java.util.HashMap;

public class DiscoveryEntityAdapter extends BaseAdapter {

    private ArrayList<HashMap<String,Object>> myList;
    private Context mContext;
    private LayoutInflater layoutInflater;

    static class ViewHolder{
        public TextView entity_name; //实体名称
        public TextView entity_type; //实体类型
    }

    public DiscoveryEntityAdapter(Context context, ArrayList<HashMap<String,Object>>mlist){
        this.mContext=context;
        this.myList=mlist;
        layoutInflater=LayoutInflater.from(context);
    }

    public void removeAll(){
        this.myList.clear();
    }

    public void setMyList(ArrayList<HashMap<String,Object>> list){
        this.myList=list;
    }

    @Override
    public int getCount() {
        return this.myList.size();
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
            convertView=layoutInflater.inflate(R.layout.discovery_item,null);
            holder=new ViewHolder();
            holder.entity_name=convertView.findViewById(R.id.entity);
            holder.entity_type=convertView.findViewById(R.id.entity_type);
            convertView.setTag(holder);
        }else{
            holder=(ViewHolder)convertView.getTag();
        }
        holder.entity_name.setText(myList.get(position).get("entity").toString());
        holder.entity_type.setText(myList.get(position).get("entity_type").toString());
        return convertView;
    }
}
