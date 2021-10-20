package com.example.frontend.ui.review;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.example.frontend.DetailActivity;
import com.example.frontend.GlobalConst;
import com.example.frontend.R;
import com.example.frontend.ReviewActivity;
import com.example.frontend.ui.discovery.DiscoveryEntityAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ResultListViewAdapter extends BaseAdapter {
    private ArrayList<Map<String,Object>> myList;
    private Context mContext;
    private LayoutInflater layoutInflater;

    static class ViewHolder{
        public TextView name; //实体名称
        public TextView course; //实体所属学科
        public Button look_for_detail; //查看详情
        public Button continue_review; //继续梳理
        public String uri; //实体uri
    }

    public ResultListViewAdapter(Context context, ArrayList<Map<String,Object>>mlist){
        this.mContext=context;
        this.myList=mlist;
        layoutInflater=LayoutInflater.from(context);
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
            convertView=layoutInflater.inflate(R.layout.review_detail_item,null);
            holder=new ViewHolder();
            holder.name=convertView.findViewById(R.id.review_detail_name);
            holder.course=convertView.findViewById(R.id.review_detail_course);
            holder.look_for_detail=convertView.findViewById(R.id.review_detail_look);
            holder.continue_review=convertView.findViewById(R.id.review_detail_continue);
            convertView.setTag(holder);
        }else{
            holder=(ViewHolder)convertView.getTag();
        }
        holder.name.setText(myList.get(position).get("name").toString());
        holder.course.setText(mContext.getString(GlobalConst.getSubjectByString(myList.get(position).get("course").toString())));
        holder.uri=myList.get(position).get("uri").toString();
        if(holder.uri.equals("")){
            holder.continue_review.setVisibility(View.GONE);
            holder.look_for_detail.setVisibility(View.GONE);
            return convertView;
        }
        holder.name.setMaxWidth(260);
        holder.continue_review.setOnClickListener(new View.OnClickListener() { //继续知识梳理
            @Override
            public void onClick(View v) {
                ((ReviewActivity)mContext).setNameAndCourse(myList.get(position).get("name").toString(),myList.get(position).get("course").toString());
                ((ReviewActivity)mContext).setUriAndSendCommunication((String)myList.get(position).get("uri"));
            }
        });
        holder.look_for_detail.setOnClickListener(new View.OnClickListener() { //查看实体详情
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext, DetailActivity.class);
                intent.putExtra("course", (String) myList.get(position).get("course"));
                intent.putExtra("label", (String) myList.get(position).get("name"));
                intent.putExtra("uri", (String)myList.get(position).get("uri"));
                mContext.startActivity(intent);
            }
        });
        return convertView;
    }
}
