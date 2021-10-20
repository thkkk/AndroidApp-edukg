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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button return_button;
    private Button register;

    private void initComponent(){
        usernameEditText=(EditText)findViewById(R.id.username);
        passwordEditText=(EditText)findViewById(R.id.password);
        confirmPasswordEditText=(EditText)findViewById(R.id.confirm_password);
        return_button=(Button)findViewById(R.id.return_button);
        register=(Button)findViewById(R.id.register);
    }

    private void registerCommunication(String username, String password) {
        JSONObject object = new JSONObject();
        try {
            object.put("username", username);
            object.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 网络通信
        new Thread(() -> {
            try{
                Communication communication = new Communication(object);
                Response response = communication.sendPost("register", false);
                JSONObject jsonObject = new JSONObject(response.body().string());

                // 控制UI变化的线程
                runOnUiThread(() -> {
                    int code = 0;
                    String msg;
                    try {
                        code = jsonObject.getInt("code");
                        msg = jsonObject.getString("msg");

//                        if(msg.equals("密码过长或过短"))
//                            msg = "密码长度应在8~16范围内";
                        Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
                        Log.e( "response", "response is :" + jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (code == 0) {
                        // update UI
                        //返回登录界面
                        Toast.makeText(RegisterActivity.this,
                                "Register successfully,please log in",
                                Toast.LENGTH_SHORT).show();

                        Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // update UI
                        Toast.makeText(RegisterActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initComponent();
        return_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //返回登录界面
                Intent intent=new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username=usernameEditText.getText().toString();
                String password_one=passwordEditText.getText().toString();
                String password_two=confirmPasswordEditText.getText().toString();
                if(password_one.equals(password_two)){
                    boolean username_valid=false;
                    boolean password_valid=false;
                    //to do:检验用户名和密码的正确性

                    if(username.length()>3){
                        username_valid=true;
                    }
                    if(password_one.length()>=8){
                        password_valid=true;
                    }

                    if(username_valid&&password_valid){
                        // 向后端发送注册请求
                        // convert key-value pairs to a JSONObject
                        registerCommunication(username, password_one);
                    }
                    if (!username_valid) {
                        Toast.makeText(RegisterActivity.this,
                                R.string.username_too_short,
                                Toast.LENGTH_SHORT).show();
                    }
                    if (!password_valid) {
                        Toast.makeText(RegisterActivity.this,
                                R.string.password_too_short,
                                Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(RegisterActivity.this,
                            R.string.password_not_equal,
                            Toast.LENGTH_LONG).show();
                    confirmPasswordEditText.setText("");
                }
            }
        });

    }
}