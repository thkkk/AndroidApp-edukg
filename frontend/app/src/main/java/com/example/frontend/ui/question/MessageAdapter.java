package com.example.frontend.ui.question;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend.R;

import org.w3c.dom.Text;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<MyMessage> list;
    public MessageAdapter(List<MyMessage>list){
        this.list=list;
    }


    static class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMessage;
        TextView rightMessage;
        TextView rightTips;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            leftLayout=itemView.findViewById(R.id.left_layout);
            leftMessage=itemView.findViewById(R.id.answer_text);

            rightLayout=itemView.findViewById(R.id.right_layout);
            rightMessage=itemView.findViewById(R.id.ask_text);

            rightTips=itemView.findViewById(R.id.ask_subject);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.messgae_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyMessage msg=list.get(position);
        if(msg.getType()==MyMessage.TYPE_RECEIVED){ //接受到的消息置于屏幕左侧
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.leftMessage.setText(msg.getContent());
            holder.rightLayout.setVisibility(View.GONE);
        }else{
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.rightMessage.setText(msg.getContent());
            holder.rightTips.setText(msg.getSubject());
            holder.leftLayout.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}
