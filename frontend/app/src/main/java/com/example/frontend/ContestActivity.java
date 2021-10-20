package com.example.frontend;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.TableLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.example.frontend.ui.contest.*;
import com.example.frontend.databinding.ActivityContestBinding;
import com.example.frontend.utils.Communication;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import okhttp3.Response;

public class ContestActivity extends AppCompatActivity {

    private ActivityContestBinding binding;
    private TabLayout mTablayout;
    private ViewPager mViewpager;
    private int begin_subject_id; //初始学科id
    private int subject_num;
    private ContestFragmentPagerAdapter adapter;

    JSONArray data;  // 只是存放试题数据
    JSONArray result;  // 存放返回的结果
    int questionNumber = 0;
    Map<Integer ,Integer>map;  // question id -> 在data中的顺序

    int code, tot, passed;
    String msg;

    /**
     * 得到：result, tot, passed
     * @param userAnswer
     * @return
     */
    private Thread checkCommunication(JSONArray userAnswer) {
        // 向后端发送请求
        // convert key-value pairs to a JSONObject
        Log.e( "userAnswer ", userAnswer.toString());
        JSONObject object = new JSONObject();

        try {
            object.put("data", userAnswer);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new Thread(() -> {
            try{
                Communication communication = new Communication(object);
                Response response = communication.sendPost("checkProblems", true);
                JSONObject jsonObject = new JSONObject(response.body().string());
                Log.e("jsonObject", jsonObject.toString());

                try {
                    msg = jsonObject.getString("msg");
                    result = jsonObject.getJSONArray("data");
                    Log.e("detail: dataLen", String.valueOf(data.toString().length()));
                    code = jsonObject.getInt("code");
                    tot = jsonObject.getInt("tot");
                    passed = jsonObject.getInt("passed");
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


    // TODO: 如何更新 tab 的状态？

    /**
     *
     * @return
     *  0: 正常
     *  1: 题目没做完
     *  2: 网络连接问题
     */
    int checkQuestions() {
        Log.e("contest", "checking");
        // TODO: 通过 adapter 查看每个fragment的 chosen， 然后发送请求，得到结果更新每个页面以及score
        JSONArray userAnswer = new JSONArray();

        boolean notFinished = false;
        for (int i = 0; i < questionNumber; ++i) {
            int problemId = 0;
            try {
                problemId = data.getJSONObject(i).getInt("id");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (adapter.fragmentList[i] == null || adapter.fragmentList[i].chosen == -1){
                notFinished = true;
                mTablayout.getTabAt(i).setIcon(R.mipmap.ic_warning);
                continue;
            }
            mTablayout.getTabAt(i).setIcon(null);
            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("problemid", problemId);
                jsonObject.put("answer", GlobalConst.options[adapter.fragmentList[i].chosen]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            userAnswer.put(jsonObject);
        }
        if (notFinished) return 1;

        Thread t = checkCommunication(userAnswer);
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(code != 0) return 2;
        int resultLen = result.length();

        for (int i = 0; i < resultLen; ++i) {
            try {
                JSONObject jsonObject = result.getJSONObject(i);
                int questionId = jsonObject.getInt("id");
                int position = map.get(questionId);
                int answer = GlobalConst.options_anti.get(jsonObject.getString("stdanswer"));

                Log.e("check", "id=" + questionId + " position=" + position + " "
                        + GlobalConst.options[adapter.fragmentList[position].chosen] + " "
                        + jsonObject.getString("youranswer")
                );
//                assert GlobalConst.options[adapter.fragmentList[position].chosen] == jsonObject.getString("youranswer");

                adapter.fragmentList[position].receiveResult(tot, passed, answer);

                if(jsonObject.getString("stdanswer").equals(jsonObject.getString("youranswer")))
                    mTablayout.getTabAt(position).setIcon(R.mipmap.ic_right);
                else
                    mTablayout.getTabAt(position).setIcon(R.mipmap.ic_wrong);

            } catch (JSONException e) {
                // 后端的问题
                e.printStackTrace();
            }
        }
        return 0;
    }


    private void initComponent(){ //初始化组件
        mViewpager=binding.defaultViewPager;
        mTablayout=binding.defaultTabs;
        ArrayList<String>mylist=new ArrayList<>();
        subject_num=0;

        begin_subject_id = 1;

        int begin_item=0;  //设置最初显示的fragment

        map = new TreeMap<Integer, Integer>();

        for(int i = 0; i < questionNumber; ++i) {  //加入需要显示的学科目录
            mylist.add(String.valueOf(i+1));
            mTablayout.addTab(mTablayout.newTab().setText(String.valueOf(i+1)));

            JSONObject question = null;
            try {
                question = data.getJSONObject(i);
                map.put(question.getInt("id"), i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        adapter=new ContestFragmentPagerAdapter(getSupportFragmentManager(),mTablayout.getTabCount(),mylist,this, data);
        mTablayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        mViewpager.setAdapter(adapter);
        mViewpager.setCurrentItem(begin_item);
        mTablayout.setupWithViewPager(mViewpager);

        FloatingActionButton fab = binding.fab;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "checking...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                int ret = checkQuestions();
                if(ret == 1) {
                    Snackbar.make(view, R.string.notFinished, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                else if(ret == 2){
                    Snackbar.make(view, R.string.network_error, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                // 否则直接在checkQuestions里面做处理
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityContestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 需要intent: data, 来自专项测试和推荐习题
        Intent intent = getIntent();
        try {
            data = new JSONArray(intent.getStringExtra("data"));
            questionNumber = data.length();
            Log.e("questionNumber", String.valueOf(questionNumber));
            Log.e("contest", data.toString());

            // TODO：保存每个题目id对应的顺序
            initComponent();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (questionNumber == 0)
            Toast.makeText(ContestActivity.this, R.string.no_question, Toast.LENGTH_SHORT).show();


        mTablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });



    }
}