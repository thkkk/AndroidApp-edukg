package com.example.frontend.ui.detail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.example.frontend.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DetailRelatedRecyclerAdapter extends RecyclerView.Adapter<DetailRelatedRecyclerAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Map<String,Object>> mylist;
    private int opened=-1; //标记展开的item

    public DetailRelatedRecyclerAdapter(Context context, ArrayList<Map<String,Object>>list){
        this.context=context;
        this.mylist=list;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.review_detail,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindView(position,mylist.get(position));
    }

    @Override
    public int getItemCount() {
        return mylist.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private CardView mCard;
        private TextView predicates;
        private LinearLayout detail_item;
        private ListView listview;
        private DetailRelatedListViewAdapter madapter;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            predicates=(TextView)itemView.findViewById(R.id.review_detail_predicate);
            detail_item=(LinearLayout)itemView.findViewById(R.id.review_detail_linear_layout);
            listview=(ListView)itemView.findViewById(R.id.review_detail_listview);
            mCard=(CardView)itemView.findViewById(R.id.review_detail_card);
            mCard.setOnClickListener(this);
        }

        void bindView(int pos, Map<String,Object> elements){
            predicates.setText(elements.get("predicate").toString());
            JSONArray jsonArray=JSONArray.parseArray(elements.get("object").toString());
            ArrayList<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
            int len=jsonArray.size();
            for(int i=0;i<len;++i){
                Map<String, Object> map = new HashMap<>();
                JSONObject element;
                try {
                    element = new JSONObject(jsonArray.get(i).toString());
                    map.put("name", element.getString("name"));
                    map.put("uri", element.getString("uri"));
                    map.put("course", element.getString("course"));
                    list.add(map);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            madapter=new DetailRelatedListViewAdapter(context,list);
            listview.setAdapter(madapter);
            if(pos==opened){
                detail_item.setVisibility(View.VISIBLE);
            }else{
                detail_item.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) { //实现列表项的折叠与展开
            if(opened==getAbsoluteAdapterPosition()){
                opened=-1;
                notifyItemChanged(getAbsoluteAdapterPosition());
            }else{
                int oldOpend=opened;
                opened=getAbsoluteAdapterPosition();
                notifyItemChanged(oldOpend);
                notifyItemChanged(opened);
            }

        }
    }
}
