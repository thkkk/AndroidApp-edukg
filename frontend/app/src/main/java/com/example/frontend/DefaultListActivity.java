package com.example.frontend;

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
import android.widget.Adapter;
import android.widget.TableLayout;

import com.example.frontend.ui.defaultList.*;
import com.example.frontend.databinding.ActivityDefaultListBinding;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class DefaultListActivity extends AppCompatActivity {

    private ActivityDefaultListBinding binding;
    private TabLayout mTablayout;
    private ViewPager mViewpager;
    private SharedPreferences pref;
    private int begin_subject_id; //初始学科id
    private int subject_num;
    private DefaultFragmentPagerAdapter adapter;


    private void initComponent(){ //初始化组件
        mViewpager=binding.defaultViewPager;
        mTablayout=binding.defaultTabs;
        pref=getSharedPreferences("subjects_by_order", Context.MODE_PRIVATE);
        ArrayList<Integer>mylist=new ArrayList<>();
        subject_num=0;
        Intent intent=getIntent();
        begin_subject_id=intent.getIntExtra("subjectName",R.string.Chinese);
        int begin_item=intent.getIntExtra("beginNum",0);  //设置最初显示的fragment
        int[] subject_order =new int[9];
        for(int i=0;i<GlobalConst.subject_name_id.length;++i){  //加入需要显示的学科目录
            int tmp=pref.getInt(GlobalConst.subject_name_id[i].toString(),-1);
            subject_order[i]=tmp;
            if(tmp>=0){
                mylist.add(GlobalConst.subject_name_id[i]);
                //mTablayout.addTab(mTablayout.newTab().setText(GlobalConst.subject_name_id[i]));
            }
        }
        mylist.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                int num1=GlobalConst.subject_name_id_map.get(o1);
                int num2=GlobalConst.subject_name_id_map.get(o2);
                return subject_order[num1]-subject_order[num2];
            }
        });
        adapter=new DefaultFragmentPagerAdapter(getSupportFragmentManager(),mTablayout.getTabCount(),mylist,this);
        mTablayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        mViewpager.setAdapter(adapter);
        mViewpager.setCurrentItem(begin_item);
        mTablayout.setupWithViewPager(mViewpager);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityDefaultListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initComponent();

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