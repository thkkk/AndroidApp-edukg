package com.example.frontend;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;

import android.widget.Toast;

import com.example.frontend.databinding.ActivitySearchBinding;
import com.example.frontend.ui.search.ICallBack;
import com.example.frontend.ui.search.SearchListAdapter;
import com.example.frontend.ui.search.SearchView;
import com.example.frontend.ui.search.bCallBack;
import com.example.frontend.utils.Communication;
//import com.example.frontend;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import okhttp3.Response;

import static com.xuexiang.xui.utils.ResUtils.getColor;


public class SearchActivity extends AppCompatActivity {
    private SearchView searchView;

    private ActivitySearchBinding binding;

    int code;
    String msg;
    JSONArray data;

    private ArrayList<Map<String, Object>> lists;  //前端接受信息的列表
    private ArrayList<Map<String, Object>> show_lists; //用于展示信息的列表
    private SearchListAdapter adapter;
    private ListView listView;

    private Button filter_button; //筛选按钮
    private Button order_button; //排序按钮
    private ImageButton refresh_button; //刷新按钮

    private Boolean only_visited=false; //只显示访问过的实体
    private Boolean only_not_visited=false; //只显示未访问过的实体
    private Boolean both_visited=true; //无论是否访问过都显示

    private Boolean all_subjects=true; //是否显示所有学科的实体
    private Map<String,Boolean> subject_check=new HashMap<>();

    private Boolean no_order=true; //不排序
    private Boolean order_by_length=false;//按照知识点名称长度排序
    private Boolean order_by_dictionary=false; //按照知识点名称字典序排序
    private Boolean positive_order=true; //正序排序

    private void initMap(){
        subject_check.put("chinese",true);
        subject_check.put("math",true);
        subject_check.put("mathe",true);
        subject_check.put("english",true);
        subject_check.put("physics",true);
        subject_check.put("chemistry",true);
        subject_check.put("biology",true);
        subject_check.put("politics",true);
        subject_check.put("history",true);
        subject_check.put("geo",true);
    };


    private Thread searchCommunication(String key) {

        // 向后端发送请求
        // convert key-value pairs to a JSONObject
        Log.e( "search", "searchKey :" + key);
        JSONObject object = new JSONObject();
        JSONArray course = new JSONArray();
        for (int i = 0; i < GlobalConst.subjects.length; ++i ) {
            course.put(GlobalConst.subjects[i]);
        }

        try {
            object.put("course", course);
            object.put("searchKey", key);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new Thread(() -> {
            try{
                Communication communication = new Communication(object);
                Response response = communication.sendPost("searchInstanceList", true);
                // string太长时勿输出
//                Log.e("success", "Got response" + response.body().string());
                Log.e("search response", "response is :" + response);

                JSONObject jsonObject = new JSONObject(response.body().string());

                // 控制UI变化的线程
//                runOnUiThread(() -> {
                    try {
                        Log.e( "search response", "response is :" + jsonObject.toString());
                        // getJSONArray("data").get(0)

                        msg = jsonObject.getString("msg");
                        data = jsonObject.getJSONArray("data");
                        Log.e("data", data.toString());
                        code = jsonObject.getInt("code");

                    } catch (JSONException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            // update UI
                            Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
                        });
                    }
//                });
            }catch (Exception e){
                e.printStackTrace();
                runOnUiThread(() -> {
                    // update UI
                    Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showFilterDialog(){ //展示筛选设置对话框
        LayoutInflater layoutInflater=LayoutInflater.from(this);
        final View filterView=layoutInflater.inflate(R.layout.filter_dialog,null);
        final RadioGroup visited_filter=(RadioGroup)filterView.findViewById(R.id.visited_filter);
        final RadioGroup subject_filter=(RadioGroup)filterView.findViewById(R.id.subject_filter);
        final RadioButton onlyVisited=(RadioButton)filterView.findViewById(R.id.only_visited);
        final RadioButton onlyNotVisited=(RadioButton)filterView.findViewById(R.id.only_not_visited);
        final RadioButton bothVisited=(RadioButton)filterView.findViewById(R.id.visited_both);
        final RadioButton allSubjects=(RadioButton)filterView.findViewById(R.id.all_subjects);
        final RadioButton onlySelectedSubjects=(RadioButton)filterView.findViewById(R.id.only_selected_subjects);
        final CheckBox chineseCheck=(CheckBox)filterView.findViewById(R.id.chinese_filter);
        final CheckBox mathCheck=(CheckBox)filterView.findViewById(R.id.math_filter);
        final CheckBox englishCheck=(CheckBox)filterView.findViewById(R.id.english_filter);
        final CheckBox physicsCheck=(CheckBox)filterView.findViewById(R.id.physics_filter);
        final CheckBox chemistryCheck=(CheckBox)filterView.findViewById(R.id.chemistry_filter);
        final CheckBox biologyCheck=(CheckBox)filterView.findViewById(R.id.biology_filter);
        final CheckBox politicsCheck=(CheckBox)filterView.findViewById(R.id.politics_filter);
        final CheckBox historyCheck=(CheckBox)filterView.findViewById(R.id.history_filter);
        final CheckBox geographyCheck=(CheckBox)filterView.findViewById(R.id.geography_filter);

        if(both_visited){ //还原设置
            bothVisited.setChecked(true);
        }else if(only_visited){
            onlyVisited.setChecked(true);
        }else{
            onlyNotVisited.setChecked(true);
        }

        if(all_subjects){
            allSubjects.setChecked(true);
        }else{
            onlySelectedSubjects.setChecked(true);
        }
        chineseCheck.setChecked(subject_check.get("chinese"));
        mathCheck.setChecked(subject_check.get("mathe"));
        englishCheck.setChecked(subject_check.get("english"));
        physicsCheck.setChecked(subject_check.get("physics"));
        chemistryCheck.setChecked(subject_check.get("chemistry"));
        biologyCheck.setChecked(subject_check.get("biology"));
        politicsCheck.setChecked(subject_check.get("politics"));
        historyCheck.setChecked(subject_check.get("history"));
        geographyCheck.setChecked(subject_check.get("geo"));

        visited_filter.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                only_visited=false;
                only_not_visited=false;
                both_visited=false;
                switch (checkedId){
                    case(R.id.only_visited):
                        only_visited=true;
                        break;
                    case(R.id.only_not_visited):
                        only_not_visited=true;
                        break;
                    default:
                        both_visited=true;
                }
            }
        });

        subject_filter.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                all_subjects= (checkedId != R.id.only_selected_subjects);
            }
        });

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_setting).setIcon(R.drawable.question);
        builder.setView(filterView);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                subject_check.put("chinese",chineseCheck.isChecked());
                subject_check.put("mathe",mathCheck.isChecked());
                subject_check.put("english",englishCheck.isChecked());
                subject_check.put("physics",physicsCheck.isChecked());
                subject_check.put("chemistry",chemistryCheck.isChecked());
                subject_check.put("biology",biologyCheck.isChecked());
                subject_check.put("politics",politicsCheck.isChecked());
                subject_check.put("history",historyCheck.isChecked());
                subject_check.put("geo",geographyCheck.isChecked());
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
        Button button=dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        button.setTextColor(getColor(R.color.black));
    }

    private void showSortDialog(){
        LayoutInflater layoutInflater=LayoutInflater.from(this);
        final View sortView=layoutInflater.inflate(R.layout.order_dialog,null);
        final RadioGroup sort_filter=(RadioGroup)sortView.findViewById(R.id.order_choice);
        final RadioGroup sort_order=(RadioGroup)sortView.findViewById(R.id.order_method);
        final RadioButton sortByDic=(RadioButton) sortView.findViewById(R.id.order_by_dictionary);
        final RadioButton sortByLength=(RadioButton)sortView.findViewById(R.id.order_by_length);
        final RadioButton noSort=(RadioButton)sortView.findViewById(R.id.not_order);
        final RadioButton positiveSort=(RadioButton)sortView.findViewById(R.id.positive_order);
        final RadioButton negativeSort=(RadioButton)sortView.findViewById(R.id.negative_order);
        if(order_by_dictionary){ //恢复排序设置
            sortByDic.setChecked(true);
        }else if(order_by_length){
            sortByLength.setChecked(true);
        }else{
            noSort.setChecked(true);
        }
        if(positive_order){
            positiveSort.setChecked(true);
        }else{
            negativeSort.setChecked(true);
        }
        sort_filter.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                order_by_dictionary=false;
                order_by_length=false;
                no_order=false;
                switch (checkedId){
                    case(R.id.order_by_dictionary):
                        order_by_dictionary=true;
                        break;
                    case(R.id.order_by_length):
                        order_by_length=true;
                        break;
                    default:
                        no_order=true;
                }
            }
        });
        sort_order.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                positive_order=(checkedId==R.id.positive_order);
            }
        });
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(R.string.order_setting).setIcon(R.drawable.question);
        builder.setCancelable(true);
        builder.setView(sortView);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
        Button button=dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        button.setTextColor(getColor(R.color.black));
    }

    private void refresh(){ //刷新显示界面
        //首先进行筛选,根据两种标准进行二级筛选
        ArrayList<Map<String, Object>> tmp_lists=new ArrayList<>(); //二级筛选之间的过渡层
        if(only_visited){
            for(Map<String,Object>item:lists){
                if((boolean)item.get("visited")){ //仅选择访问过的
                    tmp_lists.add(item);
                }
            }
        }else if(only_not_visited){
            for(Map<String,Object>item:lists){
                if(!(boolean)item.get("visited")){ //仅选择未访问过的
                    tmp_lists.add(item);
                }
            }
        }else{ //此步骤不筛选
            tmp_lists=lists;
        }
        show_lists.clear();
        if(all_subjects){ //所有学科均显示
            show_lists.addAll(tmp_lists);
        }else{ //仅显示特定学科
            for(Map<String,Object>item:tmp_lists){
                if(subject_check.get(item.get("course").toString())){
                    show_lists.add(item);
                }
            }
        }
        //筛选完成后进行排序
        if(order_by_length){ //按长度排序
            show_lists.sort(new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                    if (positive_order) {
                        return o1.get("label").toString().length() - o2.get("label").toString().length();
                    } else {
                        return o2.get("label").toString().length() - o1.get("label").toString().length();
                    }
                }
            });
        }else if(order_by_dictionary){ //按字典序排序
            show_lists.sort(new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                    if(positive_order){
                        return o1.get("label").toString().compareTo(o2.get("label").toString());
                    }else{
                        return o2.get("label").toString().compareTo(o1.get("label").toString());
                    }
                }
            });
        }
        adapter.notifyDataSetChanged();
    }

    private void initButton(){
        filter_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog(); //展示筛选对话框
            }
        });
        order_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortDialog(); //展示排序对话框
            }
        });
        refresh_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh(); //刷新界面
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO: 显示学科、收藏、访问信息
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initMap();
        Intent intent = getIntent();
        String key = intent.getStringExtra("key");

        // TODO: 上面的搜索框
        // 搜索栏
        // 1. 绑定组件
//        searchView = (SearchView) findViewById(R.id.search_view);
        searchView = binding.searchView;
        EditText et_search = (EditText) findViewById(R.id.et_search);
        et_search.setText(key);
        // 2. 设置点击搜索按键后的操作（通过回调接口）
        // 参数 = 搜索框输入的内容
        searchView.setOnClickSearch(new ICallBack() {
            @Override
            public void SearchAction(String string) {
                System.out.println("我收到了" + string);

                Intent intent=new Intent(SearchActivity.this, SearchActivity.class);
                intent.putExtra("key", string);
                startActivity(intent);
            }
        });

        // 3. 设置点击返回按键后的操作（通过回调接口）
        // TODO
        searchView.setOnClickBack(new bCallBack() {
            @Override
            public void BackAction() {
                if (searchView.ifRecordVisible())
                    searchView.hideRecord();
                else
                    finish();
            }
        });

        // 向后端发送请求，得到查找结果
        Thread t = searchCommunication(key);

        // 计时
        long start = System.currentTimeMillis(), end = start;
        t.start();
        // TODO 等待界面 or just join?
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        end = System.currentTimeMillis();
        Log.e("wait", ": " + (end - start) + " ms");
        // TODO： 显示搜索用时
//        Toast.makeText(SearchActivity.this, getString(R.string.searchTime) + (end - start) + " ms", Toast.LENGTH_SHORT).show();

        // 使用data来更新界面
        Log.e("Search", key);
        if (code == 0 && data != null && data.length() != 0) {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

            int len = data.length();
            // 每个搜索结果的信息都在list中
            lists = new ArrayList<>();
            show_lists=new ArrayList<>();
            for (int i = 0; i < len; i++) {
                Map<String, Object> map = new HashMap<>();
                JSONObject element;
                try {
                    element = new JSONObject(data.get(i).toString());
                    map.put("label", element.getString("label"));
                    map.put("uri", element.getString("uri"));
                    map.put("course", element.getString("course"));
                    map.put("category", element.getString("category"));
                    map.put("marked", element.getBoolean("marked"));
                    map.put("visited", element.getBoolean("visited"));
                    lists.add(map);
                    show_lists.add(map);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            adapter=new SearchListAdapter(this,show_lists);
                listView = binding.searchListView;
            listView.setAdapter(adapter);

            // 点击进入详情页
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // position表示点击位置位于列表中的位置
                    Intent intent=new Intent(SearchActivity.this, DetailActivity.class);
                    intent.putExtra("course", (String) show_lists.get(position).get("course"));
                    intent.putExtra("label", (String) show_lists.get(position).get("label"));
                    intent.putExtra("uri", (String) show_lists.get(position).get("uri"));
                    intent.putExtra("category", (String) show_lists.get(position).get("category"));
                    intent.putExtra("visited", String.valueOf(show_lists.get(position).get("visited")));
                    intent.putExtra("marked", String.valueOf(show_lists.get(position).get("marked")));
                    // marked在infoByInstanceName里面本来就有
                    //及时改变访问状态
                    show_lists.get(position).replace("visited",true);
                    adapter.notifyDataSetChanged();
                    startActivity(intent);
                }
            });
        }
        else if(code == 0 && data != null){
            //并非网络请求失败
            Toast.makeText(this, R.string.nothingSearched, Toast.LENGTH_SHORT).show();
        }
        filter_button=(Button)findViewById(R.id.search_select_button);
        order_button=(Button)findViewById(R.id.search_order_button);
        refresh_button=(ImageButton)findViewById(R.id.search_refresh_button);
        initButton(); //设置按钮的点击事件
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}