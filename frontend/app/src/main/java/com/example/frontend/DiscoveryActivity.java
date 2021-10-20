package com.example.frontend;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.frontend.utils.Communication;
import com.example.frontend.ui.discovery.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import okhttp3.Response;

import static com.xuexiang.xui.utils.ResUtils.getColor;

public class DiscoveryActivity extends AppCompatActivity {

    private EditText discovery_text;
    private Spinner discovery_select;
    private Button discovery_button;
    private Button discovery_cancel_button;
    private ListView discovery_answer;
    private ArrayAdapter adapter;
    private int my_subject_id;//选中的科目编号

    private ArrayList<HashMap<String,Object>>entity_list=new ArrayList<>();
    private DiscoveryEntityAdapter entityAdapter;

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

    private void initComponent(){
        discovery_text=(EditText)findViewById(R.id.discovery_text);
        discovery_select=(Spinner)findViewById(R.id.discovery_select);
        discovery_button=(Button)findViewById(R.id.discovery_button);
        discovery_cancel_button=(Button)findViewById(R.id.discovery_cancel);
        discovery_answer=(ListView)findViewById(R.id.discovery_answer);
        for(int i=0;i<subject_name_id.length;++i){
            subject_name[i]=getString(subject_name_id[i]);
        }
        discovery_cancel_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discovery_text.setText("");
            }
        });
        adapter=new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item,subject_name);
        entityAdapter=new DiscoveryEntityAdapter(this,entity_list);
        discovery_answer.setAdapter(entityAdapter);
    }


    private String getCourse(){
        return GlobalConst.subjects_by_order[my_subject_id];
    }

    private void showFailDialog(){  //发现知识点失败的对话窗口
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(R.string.tips);
        builder.setIcon(R.drawable.warn);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setMessage(R.string.Discovery_fail);
        AlertDialog dialog=builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        Button button=dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        button.setTextColor(getColor(R.color.xui_config_color_50_blue));
    }

    private HashSet<String> getEntity=new HashSet<String>(); //记录收到的实体名称，用于去重

    private void highLight(int begin,int end){ //高亮文本中的对应字段
        SpannableString sp=new SpannableString(discovery_text.getText());
        sp.setSpan(new ForegroundColorSpan(getColor(R.color.golden_test)),begin,end+1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        discovery_text.setText(sp);
    }

    private void addResult(JSONObject result){ //处理得到的识别结果
        try{
            String entity_type=result.getString("entity_type"); //实体所属概念
            String entity_url=result.getString("entity_url"); //实体uri
            int start_index=result.getInt("start_index"); //实体名称在文中出现的位置起点
            int end_index=result.getInt("end_index"); //实体名称在文中出现的位置终点
            String entity=result.getString("entity"); //实体名称
            if(!getEntity.contains(entity)){ //如果尚未显示过这个实体
                getEntity.add(entity);
                HashMap<String,Object> map=new HashMap<String,Object>();
                map.put("entity",entity);
                map.put("entity_type",entity_type);
                map.put("entity_uri",entity_url);
                entity_list.add(map);
            }
            highLight(start_index,end_index);

        }catch (Exception e){
            e.printStackTrace();
        }

    }



    private void sendDiscovery() { //向后端发送查询请求
        JSONObject object=new JSONObject();
        try{ //加入请求参数
            object.put("course",getCourse());
            object.put("context",discovery_text.getText().toString());
        }catch (JSONException e){
            e.printStackTrace();
        }
        Log.v("Discovery","I get object");

        //网络通信部分
        new Thread(()->{
            try{
                Communication communication=new Communication(object);
                Response response=communication.sendPost("linkInstance",true);

                JSONObject jsonObject=new JSONObject(response.body().string());
                Log.v("Discovery",jsonObject.toString());
                //获得文本中包含的实体列表后更新答案界面
                runOnUiThread(()->{
                    int code=0;
                    String msg;
                    try{
                        code=jsonObject.getInt("code");
                        msg= jsonObject.getString("msg");
                        if(code==0){
                            JSONArray results=jsonObject.getJSONObject("data").getJSONArray("results");
                            int results_length=results.length();
                            if(results_length==0){  //没有结果
                                showFailDialog();
                            }else{
                                for(int i=0;i<results_length;++i){ //处理识别出的每一个实体
                                    JSONObject result=results.getJSONObject(i);
                                    addResult(result);
                                }
                                entityAdapter.setMyList(entity_list);
                                entityAdapter.notifyDataSetChanged();
                            }
                        }else{
                            showFailDialog();
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
                runOnUiThread(()->{
                    Toast.makeText(DiscoveryActivity.this,R.string.network_error,Toast.LENGTH_SHORT).show();
                });
            }
        }).start();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_discovery);
        initComponent();//初始化界面组件
        discovery_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) { //发送搜索信息
                getEntity.clear(); //清空采集到的实体名
                entityAdapter.removeAll(); //清空上一次显示的实体列表
                entity_list.clear();
                sendDiscovery();
            }
        });

        discovery_select.setAdapter(adapter);
        discovery_select.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                my_subject_id=position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                my_subject_id=0;
            }
        });
        discovery_answer.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //知识点链接到实体详情
                Intent intent=new Intent(DiscoveryActivity.this,DetailActivity.class);
                intent.putExtra("label",(String)entity_list.get(position).get("entity"));
                intent.putExtra("uri",(String)entity_list.get(position).get("entity_uri"));
                intent.putExtra("course",getCourse());
                startActivity(intent);
            }
        });
    }
}