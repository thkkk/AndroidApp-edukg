package com.example.frontend;

import android.content.Intent;
import android.os.Bundle;

import com.example.frontend.ui.search.SearchListAdapter;
import com.example.frontend.utils.Communication;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.frontend.databinding.ActivityHistoryBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

public class HistoryActivity extends AppCompatActivity {

    private ActivityHistoryBinding binding;

    private ListView listView;
    private ArrayList<Map<String, Object>> lists;  //前端接受信息的列表
    private SearchListAdapter adapter;
    JSONArray data;
    int code;
    String msg;

    private Thread historyCommunication() {
        // 向后端发送请求
        // convert key-value pairs to a JSONObject

        JSONObject object = new JSONObject();
        JSONArray course = new JSONArray();
        for (int i = 0; i < GlobalConst.subjects.length; ++i ) {
            course.put(GlobalConst.subjects[i]);
        }

        try {
            object.put("course", course);
            object.put("maxNum", GlobalConst.maxHistoryNumber);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new Thread(() -> {
            try{
                Communication communication = new Communication(object);
                Response response = communication.sendPost("getHistoryList", true);
                JSONObject jsonObject = new JSONObject(response.body().string());
                Log.e("jsonObject", jsonObject.toString());

                try {
                    msg = jsonObject.getString("msg");
                    data = jsonObject.getJSONArray("data");
                    Log.e("detail: dataLen", String.valueOf(data.toString().length()));
                    code = jsonObject.getInt("code");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }catch (Exception e){
                e.printStackTrace();
                runOnUiThread(() -> {
                    // update UI
                    Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        Toolbar toolbar = binding.toolbar;
//        setSupportActionBar(toolbar);
//        CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;
//        toolBarLayout.setTitle(getTitle());

//        listView = (ListView) findViewById(R.id.history_entity_list);
        listView = binding.historyEntityList;

        Thread t = historyCommunication();
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (code == 0 && data != null && data.length() != 0) {
            int len = data.length();
            lists = new ArrayList<>();


            for (int i = 0; i < len; i++) {
                Map<String, Object> map = new HashMap<>();
                JSONObject element;
                try {
                    element = data.getJSONObject(i);
                    map.put("label", element.getString("name"));
                    map.put("uri", element.getString("uri"));
                    map.put("course", element.getString("course"));
                    map.put("category", "");
                    map.put("marked", element.getBoolean("marked"));
                    map.put("visited", true);
                    lists.add(map);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            Log.e("lists", lists.toString());

            adapter=new SearchListAdapter(this,lists);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // position表示点击位置位于列表中的位置
                    Intent intent=new Intent(HistoryActivity.this, DetailActivity.class);
                    intent.putExtra("course", (String) lists.get(position).get("course"));
                    intent.putExtra("label", (String) lists.get(position).get("label"));
                    intent.putExtra("uri", (String) lists.get(position).get("uri"));
                    intent.putExtra("category", (String) lists.get(position).get("category"));
                    intent.putExtra("visited", String.valueOf(lists.get(position).get("visited")));
                    intent.putExtra("marked", String.valueOf(lists.get(position).get("marked")));
                    // marked在infoByInstanceName里面本来就有
                    //及时改变访问状态
                    lists.get(position).replace("visited",true);
                    adapter.notifyDataSetChanged();
                    startActivity(intent);
                }
            });
        }
        else if(code == 0 && data != null){
            //并非网络请求失败
            Toast.makeText(this, R.string.nothingSearched, Toast.LENGTH_SHORT).show();
        }

//        FloatingActionButton fab = binding.fab;
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }
}