package com.example.frontend.ui.mark;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.util.Log;
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
import com.example.frontend.R;
import com.example.frontend.utils.Communication;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import okhttp3.Response;

import static com.xuexiang.xui.utils.ResUtils.getColor;

public class MarkProblemListViewAdapter extends BaseAdapter {
    private ArrayList<Map<String,Object>> myList;
    private Context myContext;
    private LayoutInflater layoutInflater;

    public MarkProblemListViewAdapter(Context context,ArrayList<Map<String,Object>>myList){
        this.myContext=context;
        this.myList=myList;
        layoutInflater=LayoutInflater.from(context);
    }

    static class ViewHolder{
        public TextView question; // 问题题面
        public TextView course; //所属学科
        public TextView answerA; //A答案
        public TextView answerB; //B答案
        public TextView answerC; //C答案
        public TextView answerD; //D答案
        public ImageView mark_star; //标记是否收藏的五角星
    }

    @Override
    public int getCount() {
        if( myList == null) return 0;
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

    private void sendUnmark(int id){
        JSONObject object=new JSONObject();
        try{
            object.put("problemid",id);
        }catch (JSONException e){
            e.printStackTrace();
        }
        new Thread(()->{
            try{
                Communication communication=new Communication(object);
                Response response=communication.sendPost("unmarkProblem",true);
            }catch (Exception e){
                e.printStackTrace();
                ((Activity)myContext).runOnUiThread(() -> {
                    Toast.makeText(myContext, R.string.network_error, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }


    private void showCancelMark(int id,int position){
        AlertDialog.Builder builder=new AlertDialog.Builder(myContext);
        builder.setTitle(R.string.tips);
        builder.setIcon(R.drawable.question);
        builder.setMessage(R.string.question_unmark);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendUnmark(id);
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

//    @SuppressLint("ResourceAsColor")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder=null;
        if(convertView==null){
            convertView=layoutInflater.inflate(R.layout.mark_problem_listcell,null);
            holder=new MarkProblemListViewAdapter.ViewHolder();
            holder.question=convertView.findViewById(R.id.mark_problem_question);
            holder.mark_star=convertView.findViewById(R.id.mark_check_problem);
            holder.answerA=convertView.findViewById(R.id.mark_problem_answerA);
            holder.answerB=convertView.findViewById(R.id.mark_problem_answerB);
            holder.answerC=convertView.findViewById(R.id.mark_problem_answerC);
            holder.answerD=convertView.findViewById(R.id.mark_problem_answerD);
            convertView.setTag(holder);
        }else{
            holder=(ViewHolder)convertView.getTag();
        }
        holder.question.setText(myList.get(position).get("qBody").toString());
        holder.answerA.setText(myList.get(position).get("answerA").toString());
        holder.answerB.setText(myList.get(position).get("answerB").toString());
        holder.answerC.setText(myList.get(position).get("answerC").toString());
        holder.answerD.setText(myList.get(position).get("answerD").toString());
        holder.answerA.setTextColor(R.color.grey);
        holder.answerB.setTextColor(R.color.grey);
        holder.answerC.setTextColor(R.color.grey);
        holder.answerD.setTextColor(R.color.grey);
        String qAnswer=myList.get(position).get("qAnswer").toString();
        switch (qAnswer){   //显示正确答案
            case "A":
                holder.answerA.setTextColor(myContext.getColor(R.color.xui_btn_green_select_color));
                break;
            case "B":
                holder.answerB.setTextColor(myContext.getColor(R.color.xui_btn_green_select_color));
                break;
            case "C":
                holder.answerC.setTextColor(myContext.getColor(R.color.xui_btn_green_select_color));
                break;
            case "D":
                holder.answerD.setTextColor(myContext.getColor(R.color.xui_btn_green_select_color));
                break;
            default:
                break;
        }
        int id=(Integer) myList.get(position).get("id");
        holder.mark_star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelMark(id,position);
            }
        });
        return convertView;
    }
}
