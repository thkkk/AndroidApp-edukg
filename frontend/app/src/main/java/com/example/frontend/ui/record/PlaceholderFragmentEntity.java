package com.example.frontend.ui.record;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.fastjson.JSON;
import com.example.frontend.DetailActivity;
import com.example.frontend.SearchActivity;
import com.example.frontend.databinding.FragmentRecordBinding;
import com.example.frontend.ui.search.SearchListAdapter;
import com.example.frontend.utils.DBForEntity;
import com.example.frontend.utils.RecordToStore;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragmentEntity extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;
    private FragmentRecordBinding binding;

    private ArrayList<Map<String, Object>> lists;
    private SearchListAdapter adapter;
    private ListView listView;

    public static PlaceholderFragmentEntity newInstance(int index) {
        PlaceholderFragmentEntity fragment = new PlaceholderFragmentEntity();
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

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentRecordBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        List<RecordToStore> listRecord = DBForEntity.detailDB.getAllRecords();
        lists = new ArrayList<>();
        int len = listRecord.size();

        final TextView textView = binding.sectionLabel;
        pageViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(len == 0 ? "空空如也" : "");
            }
        });

        for (int i = 0; i < len; ++i) {
            JSONObject element;
            Map<String, Object> map = new HashMap<>();
            try {
                element = new JSONObject(listRecord.get(i).content);
                map.put("label", element.getString("label"));
                map.put("uri", element.getString("uri"));
                map.put("course", element.getString("course"));
                map.put("category", "");
                map.put("marked", element.getBoolean("marked"));
                map.put("visited", true);
                map.put("data", element);
                lists.add(map);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            adapter=new SearchListAdapter(getActivity(),lists);
            listView = binding.recordEntityList;
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // position表示点击位置位于列表中的位置
                    Intent intent=new Intent(getActivity(), DetailActivity.class);
                    intent.putExtra("course", (String) lists.get(position).get("course"));
                    intent.putExtra("label", (String) lists.get(position).get("label"));
                    intent.putExtra("uri", (String) lists.get(position).get("uri"));
                    intent.putExtra("category", (String) lists.get(position).get("category"));
                    intent.putExtra("visited", String.valueOf(lists.get(position).get("visited")));
                    intent.putExtra("marked", String.valueOf(lists.get(position).get("marked")));
                    intent.putExtra("data", String.valueOf(lists.get(position).get("data")));
                    intent.putExtra("local", true);
                    // marked在infoByInstanceName里面本来就有
                    //及时改变访问状态
                    lists.get(position).replace("visited",true);
                    adapter.notifyDataSetChanged();
                    startActivity(intent);
                }
            });
        }
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}