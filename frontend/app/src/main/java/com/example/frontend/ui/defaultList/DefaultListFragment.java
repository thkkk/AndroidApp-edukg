package com.example.frontend.ui.defaultList;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.frontend.DetailActivity;
import com.example.frontend.GlobalConst;
import com.example.frontend.R;
import com.example.frontend.SearchActivity;
import com.example.frontend.ui.search.SearchListAdapter;
import com.example.frontend.utils.Communication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

public class DefaultListFragment extends Fragment {
    private ListView listView;
    private JSONArray data;
    private ArrayList<Map<String,Object>> lists=new ArrayList<>();
    private SearchListAdapter madapter;

    private void sendCommunication(String course){ //发送请求获取默认列表
        JSONObject object=new JSONObject();
        try{
            object.put("course",course);
        }catch (JSONException e){
            e.printStackTrace();
        }
        if(data==null) {
            Thread t=new Thread(() -> {
                try {
                    Communication communication = new Communication(object);
                    Response response = communication.sendPost("defaultUriList", true);
                    JSONObject jsonObject = new JSONObject(response.body().string());
                    Log.v("DefaultList", jsonObject.toString());
                    int code = jsonObject.getInt("code");
                    if (code == 0) {
                        data=jsonObject.getJSONArray("data");
                    }else{
                        data=null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), R.string.network_error, Toast.LENGTH_SHORT).show();
                    });
                }
            });
            t.start();
            try{
                t.join();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(data!=null){ //此部分复用了搜索结果中的代码
            lists=new ArrayList<>();
            for (int i = 0; i <data.length(); i++) {
                Map<String, Object> map = new HashMap<>();
                JSONObject element;
                try {
                    element = new JSONObject(data.get(i).toString());
                    map.put("label", element.getString("name"));
                    map.put("course", element.getString("course"));
                    map.put("marked", element.getBoolean("marked"));
                    map.put("visited", element.getBoolean("visited"));
                    map.put("uri",element.getString("uri"));
                    lists.add(map);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        madapter=new SearchListAdapter(getActivity(),lists);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_defaultlist, container, false);
        int subject_id = (int)getArguments().getInt("subject");
        listView=view.findViewById(R.id.default_list_view);
        sendCommunication(GlobalConst.getNameByid(subject_id));
        listView.setAdapter(madapter);
        // 点击进入详情页
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // position表示点击位置位于列表中的位置
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("course", (String) lists.get(position).get("course"));
                intent.putExtra("label", (String) lists.get(position).get("label"));
                intent.putExtra("uri", (String) lists.get(position).get("uri"));
                intent.putExtra("visited", String.valueOf(lists.get(position).get("visited")));
                intent.putExtra("category","");
                intent.putExtra("marked", String.valueOf(lists.get(position).get("marked")));
                // marked在infoByInstanceName里面本来就有
                //及时改变访问状态
                lists.get(position).replace("visited", true);
                madapter.notifyDataSetChanged();
                startActivity(intent);
            }
        });
        return view;
    }

    public static DefaultListFragment newInstance(int position) {
        Bundle args = new Bundle();
        args.putInt("subject", position);
        DefaultListFragment fragment = new DefaultListFragment();
        fragment.setArguments(args);
        return fragment;
    }
}
