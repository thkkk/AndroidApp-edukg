package com.example.frontend.ui.search;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;

import com.example.frontend.R;
import com.example.frontend.GlobalConst;
import com.example.frontend.utils.Communication;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import okhttp3.Response;

public class SearchListAdapter extends BaseAdapter {
    private ArrayList<Map<String,Object>> myList;
    private Context myContext;
    private LayoutInflater layoutInflater;
    private Boolean mark_success=true;

    public SearchListAdapter(Context context,ArrayList<Map<String,Object>>myList){
        this.myContext=context;
        this.myList=myList;
        layoutInflater=LayoutInflater.from(context);
    }

    static class ViewHolder{
        public TextView theme; //实体名
        public TextView content; //实体介绍
        public TextView course;  //所属学科
        public CardView item_card; //背景卡片
        public ImageView mark_star; //标记是否收藏的五角星
    }

    private Thread mark(String uri,String course,boolean flag){ //flag为true时收藏，flag为false时取消收藏
        JSONObject object=new JSONObject();
        try{
            object.put("uri",uri);
            object.put("course",course);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return new Thread(()->{
            try{
                Communication communication=new Communication(object);
                Response response;
                if(flag){
                    response=communication.sendPost("markUri",true);
                }else{
                    response=communication.sendPost("unmarkUri",true);
                }
                JSONObject jsonObject=new JSONObject(response.body().string());
                int code=jsonObject.getInt("code");
                mark_success=( code == 0 );
            }catch (Exception e){
                e.printStackTrace();
                mark_success=false;
                ((Activity)myContext).runOnUiThread(() -> {
                    Toast.makeText(myContext, R.string.network_error, Toast.LENGTH_SHORT).show();
                });
            }
        });
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
        SearchListAdapter.ViewHolder holder=null;
        if(convertView==null){
            convertView=layoutInflater.inflate(R.layout.list_cell,null);
            holder=new ViewHolder();
            holder.theme=convertView.findViewById(R.id.list_theme);
            holder.content=convertView.findViewById(R.id.list_content);
            holder.item_card=convertView.findViewById(R.id.list_all);
            holder.course=convertView.findViewById(R.id.list_tag);
            holder.mark_star=convertView.findViewById(R.id.mark_check);
            convertView.setTag(holder);
        }else{
            holder=(ViewHolder)convertView.getTag();
        }
        holder.theme.setText(myList.get(position).get("label").toString());
        try{    //适应default_list中的参数
            holder.content.setText(myList.get(position).get("category").toString());
        }catch (NullPointerException e){
            holder.content.setText("");
        }
        String raw_subject=myList.get(position).get("course").toString();
        String uri=myList.get(position).get("uri").toString();
        holder.course.setText(myContext.getString(GlobalConst.getSubjectByString(raw_subject)));
        if((boolean)myList.get(position).get("marked")){ //如果被标记为收藏
            holder.mark_star.setImageResource(R.drawable.star_favourites);
            holder.mark_star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Thread t=mark(uri,raw_subject,false);
                    t.start();
                    try{
                        t.join();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if(mark_success){
                        Toast.makeText(myContext,R.string.unmark_success,Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(myContext,R.string.unmark_fail,Toast.LENGTH_SHORT).show();
                        return;
                    }
                    myList.get(position).replace("marked",false);
                    notifyDataSetChanged();
                }
            });
        }else{ //没有标记为收藏
            holder.mark_star.setImageResource(R.drawable.star_empty);
            holder.mark_star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Thread t=mark(uri,raw_subject,true);
                    t.start();
                    try{
                        t.join();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if(mark_success){
                        Toast.makeText(myContext,R.string.mark_success,Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(myContext,R.string.mark_fail,Toast.LENGTH_SHORT).show();
                        return;
                    }
                    myList.get(position).replace("marked",true);
                    notifyDataSetChanged();
                }
            });
        }
        Log.v("filter",myList.get(position).get("visited").toString());
        if((boolean)myList.get(position).get("visited")){ //如果已经访问过,背景颜色切换为灰色
            holder.item_card.setCardBackgroundColor(myContext.getColor(R.color.grey_blue_test));
        }else{
            holder.item_card.setCardBackgroundColor(myContext.getColor(R.color.white));
        }

        return convertView;
    }
}
