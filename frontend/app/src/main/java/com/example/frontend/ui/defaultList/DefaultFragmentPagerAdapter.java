package com.example.frontend.ui.defaultList;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class DefaultFragmentPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<Integer>title_list;
    private Context mcontext;

    public DefaultFragmentPagerAdapter(@NonNull FragmentManager fm, int index,ArrayList<Integer>list,
                                       Context context) {
        super(fm,index);
        this.title_list=list;
        this.mcontext=context;
    }

    @Nullable
    @Override
    public Fragment getItem(int position){
        return DefaultListFragment.newInstance(title_list.get(position));
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position){
        return mcontext.getString(title_list.get(position));
    }


    @Override
    public int getCount() {
        return this.title_list.size();
    }
}
