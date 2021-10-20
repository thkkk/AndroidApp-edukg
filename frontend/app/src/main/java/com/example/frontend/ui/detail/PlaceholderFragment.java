package com.example.frontend.ui.detail;

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

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.frontend.DetailActivity;
import com.example.frontend.R;
import com.example.frontend.databinding.FragmentDetailBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 * For
 */
public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    public int id;
    private JSONObject data;

    private List<Map<String, Object>> lists;
    private SimpleAdapter adapter;
    private ListView listView;

    private PageViewModel pageViewModel;
    private FragmentDetailBinding binding;

    Thread questionsThread;
    JSONArray questions;
    int code = -1;
    String msg, label;

    public static PlaceholderFragment newInstance(int index, JSONObject data) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        fragment.id = index;
        fragment.data = data;
        try {
            fragment.label = data.getString("label");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    /**
     * 实体的基本信息，包括property
     */
    private void setPage1View() {
        Log.e("data", data.toString());
//                    textView.setText("属性");
        // used in adapter
        lists = new ArrayList<>();

        JSONArray property = null;
        try {
            property = data.getJSONArray("property");

            // 添加course, category,
            Map<String, Object> map = new HashMap<>();
            map.put("predicateLabel", getString(R.string.course));
            map.put("object", data.getString("course"));
            lists.add(map);

//            map = new HashMap<>();
//            map.put("predicateLabel", "类别");
//            map.put("object", data.getString("category"));
            //  由实体链接点进另外一个实体链接，无法传入category

        } catch (JSONException e) {
            e.printStackTrace();
        }

        assert property != null;

        int len = property.length();
        for (int i = 0; i < len; i++) {
            Map<String, Object> map = new HashMap<>();
            JSONObject element;
            try {
                element = new JSONObject(property.get(i).toString());
                map.put("predicateLabel", element.getString("predicateLabel"));
                map.put("object", element.getString("object"));
                lists.add(map);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        adapter = new SimpleAdapter(getActivity(), lists, R.layout.detail_list_cell,
                new String[]{"predicateLabel", "object"}, new int[]{R.id.list_theme, R.id.list_content});
        listView = binding.listview;
        listView.setAdapter(adapter);
    }


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentDetailBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.sectionLabel;
        pageViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            /**
             * id = 1: 基本信息，包括property
             * id = 2: 关联实体
             * id = 3: 相关试题 -> 在QuestionFragment里面
             * @param s: hello ... section x  (no use)
             */
            @Override
            public void onChanged(@Nullable String s) {
                if(id == 1) {
                    setPage1View();
                }
//                textView.setText(s);
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