package com.example.frontend;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.alibaba.fastjson.JSON;
import com.example.frontend.utils.Communication;
import com.example.frontend.utils.DBForEntity;
import com.example.frontend.utils.RecordDBOperator;
import com.example.frontend.utils.RecordToStore;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.frontend.ui.detail.SectionsPagerAdapter;
import com.example.frontend.databinding.ActivityDetailBinding;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.auth.AuthInfo;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WbAuthListener;
import com.sina.weibo.sdk.common.UiError;
import com.sina.weibo.sdk.openapi.IWBAPI;
import com.sina.weibo.sdk.openapi.WBAPIFactory;
import com.sina.weibo.sdk.share.WbShareCallback;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Response;

public class DetailActivity extends AppCompatActivity {

    private ActivityDetailBinding binding;
    private ImageView share;
    private ImageView mark;
    private ImageView return_back;

    int code;
    String msg;
    JSONObject data;
    Boolean marked=true;
    Boolean mark_success=true;
    String label;


    private Thread mark(String uri,String course, boolean flag){
        //flag为true时收藏实体，为false时取消收藏，试题收藏另写
        JSONObject object=new JSONObject();
        try{
            object.put("uri",uri);
            object.put("course",course);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return new Thread(()->{
            try{
                Communication communication=new Communication(object);
                Response response;
                if(flag){
                    response=communication.sendPost("markUri",true);
                }else{
                    response=communication.sendPost("unmarkUri",true);
                }
                JSONObject jsonObject=new JSONObject(response.body().string());
                int code=jsonObject.getInt("code");
                mark_success=( code == 0 );
            }catch (Exception e){
                e.printStackTrace();
                mark_success=false;
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private Thread detailCommunication(String course, String label, String uri) {
        // 向后端发送请求
        // convert key-value pairs to a JSONObject
        Log.e( "detail", "label :" + label);
        JSONObject object = new JSONObject();

        try {
            object.put("course", course);
            object.put("name", label);
            object.put("uri", uri);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("instance", course + " " + label);

        return new Thread(() -> {
            try{
                Communication communication = new Communication(object);
                Response response = communication.sendPost("infoByInstanceName", true);
                JSONObject jsonObject = new JSONObject(response.body().string());
                Log.e("jsonObject", jsonObject.toString());

                try {
                    msg = jsonObject.getString("msg");
                    data = jsonObject.getJSONObject("data");
                    Log.v("Detail",data.toString());
                    marked=data.getBoolean("marked");
                    // marked 在data里面
                    Log.e("detail: dataLen", String.valueOf(data.toString().length()));
                    code = jsonObject.getInt("code");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }catch (Exception e){
                e.printStackTrace();
                runOnUiThread(() -> {
                    // update UI
                    Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private IWBAPI mWBAPI;


    public void initSDK(){
        AuthInfo authInfo=new AuthInfo(this,GlobalConst.APP_KY, GlobalConst.REDIRECT_URL,
                GlobalConst.SCOPE);
        mWBAPI= WBAPIFactory.createWBAPI(this);
        mWBAPI.registerApp(this,authInfo);
    }

    private void startAuth(){
        mWBAPI.authorize(new WbAuthListener() {
            @Override
            public void onComplete(Oauth2AccessToken oauth2AccessToken) {
                Toast.makeText(DetailActivity.this,R.string.get_token_success,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(UiError uiError) {
                Toast.makeText(DetailActivity.this,R.string.get_token_fail,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(DetailActivity.this,R.string.get_token_cancel,Toast.LENGTH_SHORT).show();
            }
        });
    }


    private class ShareCallback implements WbShareCallback{

        @Override
        public void onComplete() {
            Toast.makeText(DetailActivity.this,R.string.share_success,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(UiError uiError) {
            Toast.makeText(DetailActivity.this,R.string.share_fail,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            Toast.makeText(DetailActivity.this,R.string.share_cancel,Toast.LENGTH_SHORT).show();
        }
    }
    private ShareCallback mShareCallback=new ShareCallback();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable
            Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mWBAPI != null) {
            mWBAPI.doResultIntent(data, mShareCallback);
        }
    }

    public void doWeiboShare(String text){
        startAuth();
        WeiboMultiMessage message=new WeiboMultiMessage();
        TextObject textObject=new TextObject();
        textObject.text=text;
        message.textObject=textObject;
        boolean isClientOnly=false;
        mWBAPI.shareMessage(message,isClientOnly);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSDK();
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mark=binding.detailMark;
        share=binding.detailShare;
        //return_back=binding.returnFlag;

        Intent intent = getIntent();
        String course = intent.getStringExtra("course");
        label = intent.getStringExtra("label");
        String uri = intent.getStringExtra("uri");
//        String visited = intent.getStringExtra("visited");
//        String marked = intent.getStringExtra("marked");
//        String category = intent.getStringExtra("category");
        // 向后端发送请求，得到查找结果
        Thread t = detailCommunication(course, label, uri);


        // 进入detail界面时看数据库中存不存在uri，存在直接调过来。
        // 之后会将这个data传入QuestionFragment，QuestionFragment检测data中是否有questions
        // 字段，如果没有则再次调用网络请求获取questions，并将带有questions的data存入数据库中

        // UPDATE: 本地缓存的话，从RecordActivity会传入local的extra，detail也在data中放入一个local的字段
        if( intent.hasExtra("local") && DBForEntity.detailDB.exist(uri) ) {
            Log.e("detail from","local");
            try {
                code = 0;
                data = new JSONObject( DBForEntity.detailDB.getContentByUri(uri) );
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            Log.e("detail from","cloud");
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        binding.title.setText(label);


        // 使用data查看返回信息
        if (code == 0 && data != null && data.length() != 0) {
            try {
                data.put("course", course);
                data.put("uri", uri);
//                data.put("visited", visited);
//                data.put("category", category);
//                data.put("marked", marked);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            assert data.has("course");
            assert data.has("uri");
            assert data.has("marked");
            assert data.has("label");

            /**
             * data includes: course, uri, marked, label,
             * 存储进本地缓存，会自动判重
             */
            DBForEntity.detailDB.addRecord(new RecordToStore("detail", uri, data.toString()));
            //get record list
//            List<RecordToStore> list = detailDB.getAllRecords();
//            int len = list.size();
//            for (int i = 0; i < len; ++i) {
//                Log.e(list.get(i).type + i, list.get(i).content);
//            }

            // 把data传过去
            SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager(), data);
            ViewPager viewPager = binding.viewPager;
            viewPager.setAdapter(sectionsPagerAdapter);
            TabLayout tabs = binding.tabs;
            tabs.setupWithViewPager(viewPager);

            sectionsPagerAdapter.getItem(1);  // fragment 1
        }
        else if(code == 0 && data != null){
            //并非网络请求失败
            Toast.makeText(this, R.string.nothingSearched, Toast.LENGTH_SHORT).show();
        }

        if(marked){ //如果已经被收藏
            mark.setImageResource(R.drawable.star_favourites);
        }else{
            mark.setImageResource(R.drawable.star_empty);
        }
        mark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(marked){ //取消收藏
                    Thread t=mark(uri,course,false);
                    t.start();
                    try{
                        t.join();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if(mark_success){
                        Toast.makeText(DetailActivity.this,R.string.unmark_success,Toast.LENGTH_SHORT).show();
                        marked=false;
                        mark.setImageResource(R.drawable.star_empty);
                    }else{
                        Toast.makeText(DetailActivity.this,R.string.unmark_fail,Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Thread t=mark(uri,course,true);
                    t.start();
                    try{
                        t.join();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if(mark_success){
                        Toast.makeText(DetailActivity.this,R.string.mark_success,Toast.LENGTH_SHORT).show();
                        marked=true;
                        mark.setImageResource(R.drawable.star_favourites);
                    }else{
                        Toast.makeText(DetailActivity.this,R.string.mark_fail,Toast.LENGTH_SHORT).show();
                    }
                }


            }
        });
        share.setOnClickListener(new View.OnClickListener() { //微博分享
            @Override
            public void onClick(View v) {
                doWeiboShare(getString(R.string.default_share_text)+label);
            }
        });
        /*
        因为实体详情页面不一定是由搜索结果进入的，暂时废弃这个优化
        return_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //退回到搜索结果
                Intent intent=new Intent(DetailActivity.this,SearchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

         */

/*
        FloatingActionButton fab = binding.fab;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
*/
    }
}