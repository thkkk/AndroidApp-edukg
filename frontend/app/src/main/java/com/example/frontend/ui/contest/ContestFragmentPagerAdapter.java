package com.example.frontend.ui.contest;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.frontend.ui.contest.ContestFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ContestFragmentPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<String>title_list;
    private Context mcontext;
    public ContestFragment[] fragmentList;

    JSONArray data;
    int questionNumber;

    public ContestFragmentPagerAdapter(@NonNull FragmentManager fm, int index, ArrayList<String>list,
                                       Context context, JSONArray data) {
        super(fm,index);
        this.title_list=list;
        this.mcontext=context;
        this.data = data;
        questionNumber = this.title_list.size();
        fragmentList = new ContestFragment[questionNumber];
    }

    @Nullable
    @Override
    public ContestFragment getItem(int position){
        // 从0开始
        Log.e("ContestFragmentPagerAdapter", String.valueOf(position));
        JSONObject question = null;
        try {
            question = data.getJSONObject(position);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        fragmentList[position] = ContestFragment.newInstance(position, question);
        return fragmentList[position];
    }

    public ContestFragment getItemWithNoCreating(int position){
        // 从0开始
        return fragmentList[position];
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position){
        return title_list.get(position);
    }


    @Override
    public int getCount() {
        return this.title_list.size();
    }
}
