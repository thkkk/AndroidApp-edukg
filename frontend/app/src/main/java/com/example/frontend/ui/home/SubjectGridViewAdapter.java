package com.example.frontend.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.frontend.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SubjectGridViewAdapter extends BaseAdapter {
    //声明引用
    private Context mContext;//用于加载图标
    private LayoutInflater mlayoutInflater;
    private boolean isShow;   //判断是否显示
    private ArrayList<HashMap<String,Object>> myList;

    //将GridItem的增删同步到数据存储中
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    static class ViewHolder{
        public ImageView subject_icon;//科目图标
        public TextView subject_text;//科目文本提示
        public ImageView subject_delete;//删除图标提示
    }

    public void setIsShow(boolean flag){
        this.isShow=flag;
        notifyDataSetChanged();
    }

    public void setPref(SharedPreferences _pref){
        this.pref=_pref;
    }

    public boolean getIsShow(){
        return this.isShow;
    }

    public SubjectGridViewAdapter(Context context){
        this.mContext=context;
        mlayoutInflater=LayoutInflater.from(context);
    }
    public SubjectGridViewAdapter(Context context,ArrayList<HashMap<String,Object>> list){
        this(context);
        this.myList=list;
    }

    @Override
    public int getCount() {
        if(this.myList.size()<9){
            return this.myList.size()+1;
        }else{
            return 9;
        }
    }

    @Override
    public Object getItem(int position) {
        return this.myList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder=null;
        if(convertView==null){
            convertView=mlayoutInflater.inflate(R.layout.subject_grid_item,null);
            holder=new ViewHolder();
            holder.subject_icon=convertView.findViewById(R.id.subject_image);
            holder.subject_text=convertView.findViewById(R.id.subject_text);
            holder.subject_delete=convertView.findViewById(R.id.grid_item_delete);
            convertView.setTag(holder);
        }else{
            holder=(ViewHolder)convertView.getTag();
        }
        if(position==myList.size()&&(myList.size()<9)){    //添加按钮与添加操作
            holder.subject_text.setVisibility(View.INVISIBLE);
            holder.subject_icon.setImageResource(R.drawable.add);
            holder.subject_delete.setVisibility(View.GONE);
        }else {//正常的学科图标
            String item_name = mContext.getString((int)myList.get(position).get("id"));
            holder.subject_text.setVisibility(View.VISIBLE);
            holder.subject_text.setText(item_name);
            holder.subject_icon.setImageResource(myList.get(position).get("image").hashCode());
            if (isShow) {
                holder.subject_delete.setVisibility(View.VISIBLE);
                holder.subject_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editor = pref.edit();
                        for(int i=position+1;i<myList.size();++i){
                            editor.putInt(myList.get(i).get("id").toString(), i-1);
                        }
                        editor.putInt(myList.get(position).get("id").toString(), -1);
                        editor.apply();
                        myList.remove(position);
                        notifyDataSetChanged();
                    }
                });
            } else {
                holder.subject_delete.setVisibility(View.GONE);
            }
        }
        return convertView;
    }
}
