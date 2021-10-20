package com.example.frontend;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.frontend.utils.Communication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Response;

public class RecommendInputActivity extends AppCompatActivity {
    private EditText numEditText;
    private Button start;
    JSONArray data;

    private void initComponent(){   //初始化组件
        numEditText =(EditText)findViewById(R.id.num_input);
        start =(Button)findViewById(R.id.start_contest);
    }

    private void InputSuccess(){    //界面跳转
        Intent intent=new Intent(RecommendInputActivity.this, ContestActivity.class);
        intent.putExtra("data", data.toString());
        startActivity(intent);
//        finish();//结束本activity
    }

    private void contestInputCommunication(int num) {
        Log.e( "contestInput", "num is :" + num);

        // 向后端发送登录请求
        // convert key-value pairs to a JSONObject
        JSONObject object = new JSONObject();
        try {
            object.put("maxNum", num);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 网络通信
        new Thread(() -> {
            try{
                Communication communication = new Communication(object);
                Response response = communication.sendPost("getRecommendProblem", true);
                Log.e("success", "Got response");
                JSONObject jsonObject = new JSONObject(response.body().string());

                // 控制UI变化的线程
                runOnUiThread(() -> {
                    // 登录成功之后，UI界面的变化放这里面

                    int code = 0;
                    String msg, token;
                    try {
                        code = jsonObject.getInt("code");
                        msg = jsonObject.getString("msg");
                        data = jsonObject.getJSONArray("data");

                        Toast.makeText(RecommendInputActivity.this, msg, Toast.LENGTH_SHORT).show();
                        Log.e( "response", "response is :" + jsonObject);

                        if (code == 0) {
                            InputSuccess();
                        }
                        else {
                            Toast.makeText(RecommendInputActivity.this, R.string.illegalFormat, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
                runOnUiThread(() -> {
                    // update UI
                    Toast.makeText(RecommendInputActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend_input);

        initComponent();

        start.setOnClickListener(new View.OnClickListener() {//登录按钮点击事件
            @Override
            public void onClick(View v) {
                String numString= numEditText.getText().toString();

                int num = 0;
                try {
                    num = Integer.parseInt(numString);

                    if(num <= GlobalConst.maxQuestionNumber)
                        contestInputCommunication(num);
                    else
                        Toast.makeText(RecommendInputActivity.this,
                                R.string.numberLessThan, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(RecommendInputActivity.this, R.string.numberIllegal, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}