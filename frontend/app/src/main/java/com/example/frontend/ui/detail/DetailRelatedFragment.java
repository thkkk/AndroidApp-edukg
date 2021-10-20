package com.example.frontend.ui.detail;

import static com.xuexiang.xui.utils.ResUtils.getColor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.frontend.DetailActivity;
import com.example.frontend.GlobalConst;
import com.example.frontend.R;
import com.example.frontend.databinding.FragmentDetailRelatedBinding;
import com.example.frontend.ui.review.ResultRecyclerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DetailRelatedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailRelatedFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    public int id;
    private JSONObject data;

    private List<Map<String, Object>> lists;
    private SimpleAdapter adapter;
    private ListView listView;

    private Map<String, JSONArray> predicateMap;

    private PageViewModel pageViewModel;
    private FragmentDetailRelatedBinding binding;
    View root;

    Thread questionsThread;
    JSONArray questions;
    int code = -1;
    String msg, label;

    public static DetailRelatedFragment newInstance(int index, JSONObject data) {
        DetailRelatedFragment fragment = new DetailRelatedFragment();
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
     * 关联实体，可以点击进入界面
     * TODO: 有很多不是实体！
     */
    /*private void setPage2View() {
        lists = new ArrayList<>();

        JSONArray content = null;
        try {
            content = data.getJSONArray("content");
            int len = content.length();
            for (int i = 0; i < len; ++i) {
                Map<String, Object> map = new HashMap<>();
                JSONObject object = new JSONObject(content.get(i).toString());
                if (object.has("subject_label")) {
                    map.put("predicateLabel", object.getString("predicate_label"));
                    map.put("object", object.getString("subject_label") + "\n" + object.getString("subject"));
                } else {
                    map.put("predicateLabel", object.getString("predicate_label"));
                    map.put("object", object.getString("object_label") + "\n" + object.getString("object"));
                }
                lists.add(map);
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }

        adapter = new SimpleAdapter(getActivity(), lists, R.layout.detail_related_list_cell,
                new String[]{"predicateLabel", "object"}, new int[]{R.id.list_theme, R.id.list_content});
        listView = binding.listview;
        listView.setAdapter(adapter);

        // 点击进入详情页
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // position表示点击位置位于列表中的位置
                JSONArray content = null;
                JSONObject object = null;
                String course = null;
                try {
                    content = data.getJSONArray("content");
                    object = new JSONObject(content.get(position).toString());
                    course = data.getString("course");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String label = null, uri = null;
                assert object != null;

                try {
                    if(object.has("subject_label")) {
                        label = object.getString("subject_label");
                        uri = object.getString("subject");
                    }
                    else {
                        label = object.getString("object_label");
                        uri = object.getString("object");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Intent intent=new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("course", course);  // 同一个科目
                intent.putExtra("label", label);
                intent.putExtra("uri", uri);
//                    intent.putExtra("category", (String) lists.get(position).get("category"));
//                    intent.putExtra("visited", String.valueOf(lists.get(position).get("visited")));
//                intent.putExtra("marked", String.valueOf(lists.get(position).get("marked")));
                // marked在infoByInstanceName里面本来就有
                startActivity(intent);
            }
        });
    }*/


    void showNoResult() { //提示没有找到搜索结果
        Toast.makeText(getActivity(), R.string.no_related,Toast.LENGTH_SHORT).show();
    }

    // new view

    private DetailRelatedRecyclerAdapter recyclerAdapter;
    private RecyclerView myRecycler;
    /**
     * 最后用于显示的list
     */
    private ArrayList<Map<String,Object>> result_list;

    private void setPage2ViewNew() {
        // init
        myRecycler=(RecyclerView)root.findViewById(R.id.related_list);

        lists = new ArrayList<>();

        result_list=new ArrayList<>();


        String course = null;
        try {
            course = data.getString("course");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray content = null;
        try {
            predicateMap = new HashMap<>();
            // TODO

            content = data.getJSONArray("content");
            int len = content.length();
            for (int i = 0; i < len; ++i) {
                JSONObject contentObject = new JSONObject(content.get(i).toString());

                JSONObject object = new JSONObject();
                if (contentObject.has("subject_label")) {
                    try{
                        object.put("name", contentObject.getString("subject_label"));
                        object.put("uri", contentObject.getString("subject"));
                        object.put("course", course);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
                else {
                    try{
                        object.put("name", contentObject.getString("object_label"));
                        object.put("uri", contentObject.getString("object"));
                        object.put("course", course);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }

                if(!predicateMap.containsKey(contentObject.getString("predicate_label"))){
                    predicateMap.put(contentObject.getString("predicate_label"),
                            new JSONArray());
                }
                predicateMap.get(contentObject.getString("predicate_label")).put(object);
            }
            // 现在predicateMap包括若干个谓词作为key， jsonArray作为value

            for (String predicate: predicateMap.keySet()) {
                Map<String, Object> map = new HashMap<>();
                map.put("predicate", predicate);
                map.put("object", predicateMap.get(predicate).toString());

                Log.e("related !! " + predicate, predicateMap.get(predicate).toString());
                result_list.add(map);
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }

        if(result_list.size()==0){
            // TODO
            showNoResult();
            return ;
        }
        recyclerAdapter=new DetailRelatedRecyclerAdapter(getActivity(),result_list);
        myRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        myRecycler.setAdapter(recyclerAdapter);
        myRecycler.getItemAnimator().setChangeDuration(300);
        myRecycler.getItemAnimator().setMoveDuration(300);

        // 点击进入详情页
    }


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentDetailRelatedBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        Toast.makeText(getActivity(), R.string.questionLoad,Toast.LENGTH_SHORT).show();

//        final TextView textView = binding.sectionLabel;
        pageViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            /**
             * id = 1: 基本信息，包括property
             * id = 2: 关联实体
             * id = 3: 相关试题 -> 在QuestionFragment里面
             * @param s: hello ... section x  (no use)
             */
            @Override
            public void onChanged(@Nullable String s) {
                if (id == 2) {
//                    textView.setText("属性2");
                    setPage2ViewNew();
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