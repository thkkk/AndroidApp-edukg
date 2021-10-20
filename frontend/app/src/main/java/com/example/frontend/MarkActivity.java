package com.example.frontend;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.example.frontend.databinding.ActivityMarkBinding;
import com.example.frontend.ui.mark.EntityFragment;
import com.example.frontend.ui.mark.ProblemFragment;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class MarkActivity extends AppCompatActivity {

    private TabLayout mTablayout;
    private ViewPager mViewpager;
    private int[] tab_id={
            R.string.mark_knowledge,
            R.string.mark_problem
    };
    private ArrayList<Fragment> fragments=new ArrayList<Fragment>();
    private ArrayList<String> titles=new ArrayList<>();

    private void initComponent(){ //初始化组件
        mViewpager=(ViewPager)findViewById(R.id.mark_view_pager);
        mTablayout=(TabLayout)findViewById(R.id.mark_tabs);
        mTablayout.addTab(mTablayout.newTab().setText(getString(tab_id[0])));
        mTablayout.addTab(mTablayout.newTab().setText(getString(tab_id[1])));
        fragments.add(new EntityFragment());
        fragments.add(new ProblemFragment());
        titles.add(getString(tab_id[0]));
        titles.add(getString(tab_id[1]));
        mViewpager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @NonNull
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }

            @Nullable
            @Override
            public CharSequence getPageTitle(int position) {
                return titles.get(position);
            }
        });
        mTablayout.setupWithViewPager(mViewpager);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark);
        initComponent();
    }
}