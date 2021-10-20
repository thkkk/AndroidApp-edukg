package com.example.frontend;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Response;

import com.example.frontend.utils.Communication;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    private CheckBox rememberPass;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button login;
    private Button register;

    private SharedPreferences pref;//用户选择记住密码时的简单存储模式
    private SharedPreferences.Editor editor;

    private void initComponent(){   //初始化组件
        usernameEditText=(EditText)findViewById(R.id.username);
        passwordEditText=(EditText)findViewById(R.id.password);
        login=(Button)findViewById(R.id.login);
        rememberPass=(CheckBox)findViewById(R.id.remember_password);
        register=(Button)findViewById(R.id.register);
        pref= PreferenceManager.getDefaultSharedPreferences(this);

    }

    private void loginSuccess(){    //登录成功的界面跳转
        Intent intent=new Intent(LoginActivity.this,MainActivity.class);
        startActivity(intent);
        finish();//结束本activity
    }


    private void loginCommunication(String username, String password) {
        Log.e( "login", "username is :" + username);
        Log.e( "login", "password is :" + password);

        // 向后端发送登录请求
        // convert key-value pairs to a JSONObject
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
                Response response = communication.sendPost("login", false);
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

                        Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                        Log.e( "response", "response is :" + jsonObject);

                        if (code == 0) {
                            // TODO: token
                            token = jsonObject.getString("token");
                            Communication.setToken(token);
                            GlobalConst.username=username;
                            loginSuccess();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
                runOnUiThread(() -> {
                    // update UI
                    Toast.makeText(LoginActivity.this, R.string.network_error, Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.v("LoginActivity","enter create");

        initComponent();

        boolean isRemember=pref.getBoolean("remember_password",false);
        if(isRemember){
            //将账号密码设置到文本框中
            String username=pref.getString("username","");
            String password=pref.getString("password","");
            usernameEditText.setText(username);
            passwordEditText.setText(password);
            rememberPass.setChecked(true);
        }
        login.setOnClickListener(new View.OnClickListener() {//登录按钮点击事件

            @Override
            public void onClick(View v) {
                String username=usernameEditText.getText().toString();
                String password=passwordEditText.getText().toString();
                // TODO 修改验证逻辑
                boolean user_valid=true;
                //loginSuccess();//调试使用，跳过验证过程直接登录


                if(user_valid){//验证成功可以登录
                    editor=pref.edit();
                    if(rememberPass.isChecked()){//如果用户选择了记住密码
                        editor.putBoolean("remember_password",true);
                        editor.putString("username",username);
                        editor.putString("password",password);
                    }else{
                        editor.clear();
                    }
                    editor.apply();

                    loginCommunication(username, password);

                }else{
                    Toast.makeText(LoginActivity.this,
                            "Login Failed,please try it again",Toast.LENGTH_SHORT).show();;
                    usernameEditText.setText("");
                    passwordEditText.setText("");
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //召唤注册界面
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
                finish();//结束本activity
            }
        });
    }
}