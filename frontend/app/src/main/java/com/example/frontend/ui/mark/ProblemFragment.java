package com.example.frontend.ui.mark;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.frontend.DetailActivity;
import com.example.frontend.R;
import com.example.frontend.utils.Communication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

public class ProblemFragment extends Fragment {
    private ListView listView;
    private JSONArray data;
    private ArrayList<Map<String,Object>> lists; //接受到的所有收藏题目
    private MarkProblemListViewAdapter madapter;

    private void sendCommunication() { //与后端通信
        Thread t = new Thread(() -> {
            JSONObject object = new JSONObject();
            try {
                object.put("test", "test");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                Communication communication = new Communication(object);
                Response response = communication.sendPost("getMarkedProblems", true);
                JSONObject jsonObject = new JSONObject(response.body().string());
                Log.v("Mark", jsonObject.toString());
                int code = jsonObject.getInt("code");
                if (code == 0) {
                    data = jsonObject.getJSONArray("data");
                } else {
                    data = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), R.string.network_error, Toast.LENGTH_SHORT).show();
                });
            }
        });
        t.start();
        try {
            t.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (data != null) {
            lists = new ArrayList<>();
            for (int i = 0; i < data.length(); i++) {
                Map<String, Object> map = new HashMap<>();
                JSONObject element;
                try {
                    element = new JSONObject(data.get(i).toString());
                    map.put("qAnswer", element.getString("qAnswer"));
                    map.put("id", element.getInt("id"));
                    map.put("qBody", element.getString("qBody"));
                    map.put("answerA", element.getString("answerA"));
                    map.put("answerB", element.getString("answerB"));
                    map.put("answerC", element.getString("answerC"));
                    map.put("answerD", element.getString("answerD"));
                    lists.add(map);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        madapter = new MarkProblemListViewAdapter(getActivity(), lists);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mark_problem, container, false);
        listView=view.findViewById(R.id.mark_problem_list_view);
        sendCommunication();    //后端通信获取收藏列表
        listView.setAdapter(madapter);
        // 点击进入详情页
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //试题页面暂时不设置点击事件
            }
        });
        return view;
    }

    public ProblemFragment(){
    }
}
