package com.example.frontend;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.frontend.R;
import com.example.frontend.ui.question.*;
import com.example.frontend.utils.Communication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

import okhttp3.Response;

public class QuestionAndAnswerActivity extends AppCompatActivity {

    private List<MyMessage>myList=new ArrayList<>();
    private RecyclerView msgRecycleView;
    private EditText myText;
    private Button sendButton;
    private LinearLayoutManager layoutManager;
    private MessageAdapter messageAdapter;

    private Spinner question_spinner;
    private ArrayAdapter spinnerAdapter;
    private int my_subject_id;//选中的科目顺序

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

    private final static int[] hello_message_id={
            R.string.QA_hello_1,
            R.string.QA_hello_2,
            R.string.QA_hello_3,
            R.string.QA_hello_4,
            R.string.QA_hello_5
    };
    private final static int[] fail_messgae_id={
            R.string.QA_fail_1,
            R.string.QA_fail_2,
            R.string.QA_fail_3
    };

    private String getRandomHello(){  //生成一句随机问候语
        int index=(int)(Math.random()*5);
        return this.getString(hello_message_id[index]);
    }

    private String getRandomFail(){ //随机生成一句失败提示语
        int index=(int)(Math.random()*3);
        return this.getString(fail_messgae_id[index]);
    }


    private List<MyMessage> getInitData(){
        List<MyMessage>list=new ArrayList<>();
        list.add(new MyMessage(getRandomHello(),MyMessage.TYPE_RECEIVED,null));
        return list;
    }

    private void initComponent(){ //初始化组件
        msgRecycleView=(RecyclerView)findViewById(R.id.message_list);
        myText=(EditText)findViewById(R.id.question_text);
        sendButton=(Button)findViewById(R.id.question_send);
        layoutManager=new LinearLayoutManager(this);
        messageAdapter=new MessageAdapter(myList=getInitData());
        question_spinner=(Spinner)findViewById(R.id.question_select);
        for(int i=0;i<subject_name_id.length;++i){
            subject_name[i]=getString(subject_name_id[i]);
        }

        spinnerAdapter=new ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,subject_name);
        question_spinner.setAdapter(spinnerAdapter);
        question_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                my_subject_id=position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        msgRecycleView.setLayoutManager(layoutManager);
        msgRecycleView.setAdapter(messageAdapter);

    }

    private String getCourse(){ //返回用户选中科目的约定状态码表示
        return GlobalConst.subjects_by_order[my_subject_id];
    }

    private void sendQuestion(String question){
        JSONObject object=new JSONObject();
        try{ //加入请求参数
            object.put("course",getCourse());
            object.put("inputQuestion",question);
        }catch (JSONException e){
            e.printStackTrace();
        }
        Log.v("Question","I have get object");

        // 网络通信部分
        new Thread(()->{
            try{
                Communication communication=new Communication(object);
                Response response=communication.sendPost("inputQuestion",true);

                Log.v("Question","get response");
                JSONObject jsonObject=new JSONObject(response.body().string());
                Log.v("Question","my response is"+jsonObject);
                //获得回答后更新对话界面
                runOnUiThread(()->{
                    int code=0;
                    String msg;
                    try{
                        code=jsonObject.getInt("code");
                        msg=jsonObject.getString("msg");
                        if(code==0){ //成功得到回答
                            JSONArray data=jsonObject.getJSONArray("data");
                            JSONObject answer;
                            try{
                                //暂时只取了有利用价值的信息
                                answer=data.getJSONObject(0);
                                String subject=answer.getString("subject");
                                String subjectUri=answer.getString("subjectUri");
                                String predicate=answer.getString("predicate");
                                Double score=answer.getDouble("score");
                                //boolean answerflag=answer.getBoolean("answerflag");
                                String value=answer.getString("value");
                                Log.v("Question","value:"+value);
                                if(value.equals("")){ //没有找到答案
                                    myList.add(new MyMessage(getRandomFail(),MyMessage.TYPE_RECEIVED,null));
                                }else{
                                    myList.add(new MyMessage(value,MyMessage.TYPE_RECEIVED,null));
                                }
                            }catch (JSONException e){
                                myList.add(new MyMessage(getRandomFail(),MyMessage.TYPE_RECEIVED,null));
                            }
                            messageAdapter.notifyItemInserted(myList.size()-1);
                        }else{
                            runOnUiThread(()->{
                                Toast.makeText(QuestionAndAnswerActivity.this,R.string.network_error,Toast.LENGTH_SHORT).show();
                            });
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                });

            }catch (Exception e){
                e.printStackTrace();
                runOnUiThread(()->{
                    Toast.makeText(QuestionAndAnswerActivity.this,R.string.network_error,Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_and_answer);
        initComponent();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String question=myText.getText().toString();
                if(!question.equals("")){ //问题必须非空
                    myList.add(new MyMessage(question,MyMessage.TYPE_SEND,subject_name[my_subject_id]));
                    messageAdapter.notifyItemInserted(myList.size()-1);
                    myText.setText(""); //输入框清空
                    sendQuestion(question);
                }
            }
        });

    }
}