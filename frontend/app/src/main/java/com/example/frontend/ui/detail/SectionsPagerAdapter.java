package com.example.frontend.ui.detail;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.frontend.R;

import org.json.JSONObject;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2,  R.string.tab_text_3};
    private final Context mContext;
    private JSONObject data;


    public SectionsPagerAdapter(Context context, FragmentManager fm, JSONObject data) {
        super(fm);
        mContext = context;
        this.data = data;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        if(position == 0)
            return PlaceholderFragment.newInstance(position + 1, data);
        else if(position == 1)
            return DetailRelatedFragment.newInstance(position + 1, data);
        else //if(position == 2)
            return QuestionFragment.newInstance(position + 1, data);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 3;
    }
}