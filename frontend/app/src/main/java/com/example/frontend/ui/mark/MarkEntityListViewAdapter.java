package com.example.frontend.ui.mark;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.frontend.GlobalConst;
import com.example.frontend.LoginActivity;
import com.example.frontend.R;
import com.example.frontend.utils.Communication;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import okhttp3.Response;

import static com.xuexiang.xui.utils.ResUtils.getColor;

public class MarkEntityListViewAdapter extends BaseAdapter {
    private ArrayList<Map<String,Object>> myList;
    private Context myContext;
    private LayoutInflater layoutInflater;

    public MarkEntityListViewAdapter(Context context,ArrayList<Map<String,Object>>myList){
        this.myContext=context;
        this.myList=myList;
        layoutInflater=LayoutInflater.from(context);
    }

    static class ViewHolder{
        public TextView name; // 实体名
        public TextView course; //所属学科
        public ImageView mark_star; //标记是否收藏的五角星
    }

    private void sendUnmark(String course,String uri){ //向后端发送取消收藏的信息
        JSONObject object=new JSONObject();
        try{
            object.put("uri",uri);
            object.put("course",course);
        }catch (JSONException e){
            e.printStackTrace();
        }
        new Thread(()->{
            try{
                Communication communication=new Communication(object);
                Response response=communication.sendPost("unmarkUri",true);
                // 暂时默认此请求一定成功
            }catch (Exception e){
                e.printStackTrace();
                ((Activity)myContext).runOnUiThread(() -> {
                    Toast.makeText(myContext, R.string.network_error, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();

    }


    private void showCancelMark(String course,String uri,int position){ //显示取消收藏的提示框
        AlertDialog.Builder builder=new AlertDialog.Builder(myContext);
        builder.setTitle(R.string.tips);
        builder.setIcon(R.drawable.question);
        builder.setMessage(R.string.question_unmark);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendUnmark(course,uri);
                myList.remove(position);
                notifyDataSetChanged();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //pass
            }
        });
        builder.setCancelable(true);
        AlertDialog dialog=builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        Button button=dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        button.setTextColor(getColor(R.color.black));
        button=dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        button.setTextColor(getColor(R.color.xui_config_color_50_blue));

    }


    @Override
    public int getCount() {
        if(myList == null) return 0;
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
            convertView=layoutInflater.inflate(R.layout.mark_entity_listcell,null);
            holder=new ViewHolder();
            holder.name=convertView.findViewById(R.id.mark_list_theme_entity);
            holder.course=convertView.findViewById(R.id.mark_list_tag_entity);
            holder.mark_star=convertView.findViewById(R.id.mark_check_entity);
            convertView.setTag(holder);
        }else{
            holder=(ViewHolder)convertView.getTag();
        }
        holder.name.setText(myList.get(position).get("name").toString());
        String raw_subject=myList.get(position).get("course").toString();
        String uri=myList.get(position).get("uri").toString();
        holder.course.setText(myContext.getString(GlobalConst.getSubjectByString(raw_subject)));
        holder.mark_star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelMark(raw_subject,uri,position);
            }
        });
        return convertView;
    }
}
