package com.example.frontend;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.frontend.ui.review.KnowledgePointGridViewAdapter;
import com.example.frontend.ui.review.ResultRecyclerAdapter;
import com.example.frontend.utils.Communication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

public class ReviewActivity extends AppCompatActivity {

    private EditText inputKnowledge;
    private Button send;
    private GridView myGrid;
    private RecyclerView myRecycler;
    private KnowledgePointGridViewAdapter gridViewAdapter;
    private ResultRecyclerAdapter recyclerAdapter;
    private Spinner spinner; //用于选择学科
    private int my_subject_id=0;

    private JSONArray history_data;
    private ArrayList<String>data_name_lists;
    private ArrayList<Integer>data_subject_lists;
    private ArrayList<Map<String,Object>> result_list;

    private  JSONArray related;

    private String review_uri;
    private String review_name;

    private String[] subject_name=new String[9];
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
        for(int i=0;i<subject_name_id.length;++i){
            subject_name[i]=getString(subject_name_id[i]);
        }
        ArrayAdapter adapter=new ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,subject_name);
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

    private void testinfo(){ //调试用
        review_uri="http://edukb.org/knowledge/0.1/instance/chinese#-06c39375f7db1d4851c6b6ea695b9835";
    }


    public void setUriAndSendCommunication(String uri){
        review_uri=uri;
        sendCommunitcation(false);
    }


    public void setNameAndCourse(String name, String course){
        inputKnowledge.setText(name);
        spinner.setSelection(GlobalConst.subjects_index.get(course));
    }


    private void showNoResult(){ //提示没有找到搜索结果
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle(R.string.tips);
            builder.setIcon(R.drawable.warn);
            builder.setMessage(R.string.no_related);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.setCancelable(true);
            AlertDialog dialog=builder.create();
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
            Button button=dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            button.setTextColor(getColor(R.color.black));
    }


    private void sendCommunitcation(Boolean flag){ //发送知识梳理请求
        String text=inputKnowledge.getText().toString();
        JSONObject object=new JSONObject();
        if(flag){
            try{
                object.put("name",text);
                object.put("course",GlobalConst.subjects_by_order[my_subject_id]);
            }catch (JSONException e){
                e.printStackTrace();
            }
        }else{
            try{
                object.put("uri",review_uri);
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        Thread t=new Thread(()->{
            try{
                Communication communication=new Communication(object);
                Response response=communication.sendPost("relatedValue",true);
                JSONObject jsonObject=new JSONObject(response.body().string());
                related=jsonObject.getJSONObject("data").getJSONArray("related");
            }catch (Exception e){
                e.printStackTrace();
                runOnUiThread(()->{
                    Toast.makeText(this,R.string.network_error,Toast.LENGTH_SHORT).show();
                });
            }
        });
        t.start();
        try{
            t.join();
        }catch (Exception e){
            e.printStackTrace();
        }
        result_list=new ArrayList<>();
        if(related==null||related.length()==0){
            showNoResult();
            return;
        }
        for(int i=0;i<related.length();++i){
            Map<String, Object> map = new HashMap<>();
            JSONObject element;
            try {
                element = new JSONObject(related.get(i).toString());
                map.put("predicate", element.getString("predicate"));
                map.put("object", element.getString("object"));
                result_list.add(map);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.v("History",result_list.toString());
        recyclerAdapter=new ResultRecyclerAdapter(this,result_list);
        myRecycler.setLayoutManager(new LinearLayoutManager(this));
        myRecycler.setAdapter(recyclerAdapter);
        myRecycler.getItemAnimator().setChangeDuration(300);
        myRecycler.getItemAnimator().setMoveDuration(300);
    }

    private void initComponent(){ //组件初始化
        inputKnowledge=(EditText)findViewById(R.id.input);
        send=(Button)findViewById(R.id.review_button);
        myGrid=(GridView)findViewById(R.id.review_grid);
        myRecycler=(RecyclerView)findViewById(R.id.review_list);
        spinner=(Spinner)findViewById(R.id.review_subject);
        setSpinner();
        send.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                sendCommunitcation(true);
            }
        });
    }


    private void getDefaultKnowledge(){ //获取默认知识点
        JSONArray courses=new JSONArray();
        for(int i=0;i<GlobalConst.subjects_by_order.length;++i){
            courses.put(GlobalConst.subjects_by_order[i]);
        }
        JSONObject object=new JSONObject();
        try{
            object.put("course",courses);
            object.put("maxNum",9);
        }catch (JSONException e){
            e.printStackTrace();
        }
        Thread t=new Thread(() -> {
            try {
                Communication communication = new Communication(object);
                Response response = communication.sendPost("getHistoryList", true);
                JSONObject jsonObject = new JSONObject(response.body().string());
                Log.v("HistoryList", jsonObject.toString());
                int code = jsonObject.getInt("code");
                history_data=jsonObject.getJSONArray("data");
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
                });
            }
        });
        t.start();
        try{
            t.join();
        }catch (Exception e){
            e.printStackTrace();
        }
        data_name_lists=new ArrayList<>();
        data_subject_lists=new ArrayList<>();
        for(int i=0;i<history_data.length();++i){
            JSONObject element;
            try{
                element=new JSONObject(history_data.get(i).toString());
                data_name_lists.add(element.getString("name"));
                data_subject_lists.add(GlobalConst.subjects_index.get(element.getString("course")));
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        gridViewAdapter=new KnowledgePointGridViewAdapter(this,data_name_lists);
        myGrid.setAdapter(gridViewAdapter);
        myGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                inputKnowledge.setText(data_name_lists.get(position));
                spinner.setSelection(data_subject_lists.get(position));
                my_subject_id=data_subject_lists.get(position);
            }
        });
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        initComponent();
        getDefaultKnowledge();//获取默认知识点
    }
}