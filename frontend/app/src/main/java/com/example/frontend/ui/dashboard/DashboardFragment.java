package com.example.frontend.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.frontend.ContestInputActivity;
import com.example.frontend.DiscoveryActivity;
import com.example.frontend.MainActivity;
import com.example.frontend.QuestionAndAnswerActivity;
import com.example.frontend.R;
import com.example.frontend.RecommendInputActivity;
import com.example.frontend.ReviewActivity;
import com.example.frontend.databinding.FragmentDashboardBinding;
import com.example.frontend.ui.notifications.SettingListViewAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//public class DashboardFragment extends Fragment {
//
//    private DashboardViewModel dashboardViewModel;
//
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//        dashboardViewModel =
//                new ViewModelProvider(this).get(DashboardViewModel.class);
//        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
//        final TextView textView = root.findViewById(R.id.text_dashboard);
//        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
//        return root;
//    }
//}

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private FragmentDashboardBinding binding;

    // for test

    private ArrayList<Map<String, Object>> lists;
    private DashboardAdapter adapter;
    private ListView listView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textDashboard;
//        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });

        final String[] theme = {getString(R.string.func_discovery),getString(R.string.func_QA), getString(R.string.func_test),
                getString(R.string.func_outline), getString(R.string.func_recommend)};
        final String[] content = {getString(R.string.func_discovery_detail),getString(R.string.func_QA_detail), getString(R.string.func_test_detail),
                getString(R.string.func_outline_detail), getString(R.string.func_recommend_detail)};
        final int[] image={
                R.drawable.magnifer,
                R.drawable.answer_man,
                R.drawable.exam,
                R.drawable.review,
                R.drawable.document
        };


        lists = new ArrayList<>();
        for (int i = 0; i < theme.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("theme", theme[i]);
            map.put("content", content[i]);
            map.put("image",image[i]);
            lists.add(map);
        }
        //TODO 重写listView的适配器
//        adapter = new SimpleAdapter(getActivity(), lists, R.layout.dashboard_list_cell, new String[]{"theme", "content"}, new int[]{R.id.dashboard_theme, R.id.dashboard_content});
//        adapter = new SettingListViewAdapter(getActivity());
        adapter=new DashboardAdapter(getActivity(),lists);
        listView = binding.dashboardListView;
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    // TODO 点击事件跳转
                    case 0: //知识点发现
                        Log.v("Dashboard","discovery");
                        Intent intent=new Intent(getActivity(), DiscoveryActivity.class);
                        startActivity(intent);
                        break;
                    case 1: //知识问答
                        Log.v("Dashboard","QA");
                        Intent intent2=new Intent(getActivity(), QuestionAndAnswerActivity.class);
                        startActivity(intent2);
                        break;
                    case 2: //专项测试
                        Log.v("Dashboard","Contest");
                        Intent intent3=new Intent(getActivity(), ContestInputActivity.class);
                        startActivity(intent3);
                        break;
                    case 3: //知识梳理
                        Intent intent4=new Intent(getActivity(), ReviewActivity.class);
                        startActivity(intent4);
                        break;
                    case 4://试题推荐
                        Intent intent5=new Intent(getActivity(), RecommendInputActivity.class);
                        startActivity(intent5);
                        break;
                    default:
                        break;
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}