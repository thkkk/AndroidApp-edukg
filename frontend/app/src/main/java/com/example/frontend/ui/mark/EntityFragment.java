package com.example.frontend.ui.mark;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.frontend.DetailActivity;
import com.example.frontend.GlobalConst;
import com.example.frontend.R;
import com.example.frontend.utils.Communication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

public class EntityFragment extends Fragment {
    private ListView listView;
    private JSONArray data;
    private ArrayList<Map<String,Object>> lists; //接受到的所有收藏实体
    private ArrayList<Map<String,Object>> show_lists; //需要显示的收藏实体
    private MarkEntityListViewAdapter madapter;
    private Spinner spinner; //用于收藏学科的筛选
    private Button button; //用于确认筛选

    private int my_subject_id=0;

    private String[] subject_name=new String[10];
    private final int[] subject_name_id={R.string.Chinese,
            R.string.Math,
            R.string.English,
            R.string.Physics,
            R.string.Chemistry,
            R.string.Biology,
            R.string.Politics,
            R.string.History,
            R.string.Geometry};
    private void setSpinner(){
        subject_name[0]=getString(R.string.all_subjects); //注意第0选项是不进行筛选
        for(int i=0;i<subject_name_id.length;++i){
            subject_name[i+1]=getString(subject_name_id[i]);
        }
        ArrayAdapter adapter=new ArrayAdapter(getActivity(),R.layout.support_simple_spinner_dropdown_item,subject_name);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                my_subject_id=position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                my_subject_id=0;
            }
        });
    }

    private void setButton(){
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(my_subject_id>0){ //为0时不触发筛选
                    show_lists.clear();
                    int index=my_subject_id-1;
                    for(Map<String,Object>item:lists){ //筛选显示科目
                        if(GlobalConst.getSubjectByString(item.get("course").toString())==subject_name_id[index]){
                            show_lists.add(item);
                        }
                    }
                }else{
                    show_lists=lists;
                }
                madapter.notifyDataSetChanged();
            }
        });
    }


    private void sendCommunication(){ //与后端通信
        Thread t=new Thread(()->{
            JSONObject object=new JSONObject();
            try{
                object.put("test","test");
            }catch (JSONException e){
                e.printStackTrace();
            }
            try{
                Communication communication=new Communication(object);
                Response response=communication.sendPost("getMarkList",true);
                JSONObject jsonObject=new JSONObject(response.body().string());
                Log.v("Mark",jsonObject.toString());
                int code=jsonObject.getInt("code");
                if(code==0){
                    data=jsonObject.getJSONArray("data");
                }else{
                    data=null;
                }
            }catch (Exception e){
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), R.string.network_error, Toast.LENGTH_SHORT).show();
                });
            }
        });
        t.start();
        try{
            t.join();
        }catch (Exception e){
            e.printStackTrace();
        }
        if(data!=null){
            lists=new ArrayList<>();
            show_lists=new ArrayList<>();
            for (int i = 0; i <data.length(); i++) {
                Map<String, Object> map = new HashMap<>();
                JSONObject element;
                try {
                    element = new JSONObject(data.get(i).toString());
                    map.put("name", element.getString("name"));
                    map.put("course", element.getString("course"));
                    map.put("uri",element.getString("uri"));
                    lists.add(map);
                    show_lists.add(map);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        madapter=new MarkEntityListViewAdapter(getActivity(),show_lists);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mark_entity, container, false);
        listView=view.findViewById(R.id.mark_entity_list_view);
        spinner=view.findViewById(R.id.mark_select_entity);
        button=view.findViewById(R.id.mark_button_entity);
        sendCommunication();    //后端通信获取收藏列表
        listView.setAdapter(madapter);
        setSpinner();
        setButton();
        // 点击进入详情页
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // position表示点击位置位于列表中的位置
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("course", (String) show_lists.get(position).get("course"));
                intent.putExtra("label", (String) show_lists.get(position).get("name"));
                intent.putExtra("uri", (String) show_lists.get(position).get("uri"));
                //intent.putExtra("visited", "true");
                //intent.putExtra("category","");
                //intent.putExtra("marked", String.valueOf(lists.get(position).get("marked")));
                // marked在infoByInstanceName里面本来就有
                startActivity(intent);
            }
        });
        return view;
    }

    public EntityFragment(){
    }
}
