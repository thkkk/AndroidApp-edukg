package com.example.frontend.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.frontend.DefaultListActivity;
import com.example.frontend.GlobalConst;
import com.example.frontend.R;
import com.example.frontend.SearchActivity;
import com.example.frontend.databinding.FragmentHomeBinding;
import com.example.frontend.ui.search.ICallBack;
import com.example.frontend.ui.search.SearchView;
import com.example.frontend.ui.search.bCallBack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import static com.xuexiang.xui.utils.ResUtils.getColor;


public class HomeFragment extends Fragment {
    private SearchView searchView;

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;

    private SharedPreferences pref;//简单存储当前界面选择的科目
    private SharedPreferences.Editor editor;
    private final int[] subject_image={
            R.drawable.new_chinese,
            R.drawable.new_math,
            R.drawable.new_english,
            R.drawable.new_physics,
            R.drawable.new_chemistry,
            R.drawable.new_biology,
            R.drawable.new_politics,
            R.drawable.new_history,
            R.drawable.new_geography
    };
    private ArrayList<HashMap<String,Object>> myList;
    private SubjectGridViewAdapter mAdapter;
    private DragGridView subject_groups;


    private ArrayList<HashMap<String,Object>> getMenuList(){//获取菜单表格
        ArrayList<HashMap<String,Object>> data=new ArrayList<HashMap<String,Object>>();
        boolean choose=pref.getBoolean("remember_subjects",false);//此界面之前是否被点击过
        if(choose){//若点击过则检查是否选择选中这一科目
            int[] subject_order =new int[9];
            for(int i=0;i<GlobalConst.subject_name_id.length;++i){
                int tmp=pref.getInt(GlobalConst.subject_name_id[i].toString(),-1);
                subject_order[i]=tmp;
                if(tmp>=0){//如果显示了这个图标
                    HashMap<String,Object> map=new HashMap<String,Object>();
                    map.put("id",GlobalConst.subject_name_id[i]);
                    map.put("image",subject_image[i]);
                    map.put("init_order",i); //初始顺序，其实也是科目代码
                    data.add(map);
                }
            }
            data.sort(new Comparator<HashMap<String, Object>>() {
                @Override
                public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {
                    int num1=(int)o1.get("init_order");
                    int num2=(int)o2.get("init_order");
                    return subject_order[num1]-subject_order[num2];
                }
            });
        }else{//否则初始化所有科目
            editor=pref.edit();
            editor.putBoolean("remember_subjects",true);
            for(int i=0;i<GlobalConst.subject_name_id.length;++i){
                HashMap<String,Object> map=new HashMap<String,Object>();
                map.put("id",GlobalConst.subject_name_id[i]);
                map.put("image",subject_image[i]);
                map.put("init_order",i);
                editor.putInt(GlobalConst.subject_name_id[i].toString(),i);
                data.add(map);
            }
            editor.apply();
        }
        return data;
    }


    private void showAddItems(){//显示添加学科的选择框
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.add_a_subject);
        builder.setIcon(R.drawable.chinese);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ArrayList<String>can_choose_subjects=new ArrayList<String>();
        for(Integer name_id:GlobalConst.subject_name_id){
            String name=getString(name_id);
            if(pref.getInt(name_id.toString(),-1)<0){ //如果没有出现
                can_choose_subjects.add(name);
            }
        }
        builder.setItems((String[])can_choose_subjects.toArray(new String[0]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {//点击添加对应学科
                String name=can_choose_subjects.get(which);
                int id=0;
                for(;id<GlobalConst.subject_name_id.length;++id){
                    if(name.equals(getString(GlobalConst.subject_name_id[id]))){
                        break;
                    }
                }
                HashMap<String,Object> map=new HashMap<String,Object>();
                map.put("id",GlobalConst.subject_name_id[id]);
                map.put("image",subject_image[id]);
                map.put("init_order",id);
                editor=pref.edit();
                editor.putInt(GlobalConst.subject_name_id[id].toString(),myList.size());
                editor.apply();
                myList.add(map);
                mAdapter.notifyDataSetChanged();
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
        Button button=dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        button.setTextColor(getColor(R.color.black));
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 搜索栏
        // 1. 绑定组件
//        searchView = (SearchView) findViewById(R.id.search_view);
        searchView = binding.mySearchView;

        // 2. 设置点击搜索按键后的操作（通过回调接口）
        // 参数 = 搜索框输入的内容
        searchView.setOnClickSearch(new ICallBack() {
            @Override
            public void SearchAction(String string) {
                System.out.println("我收到了" + string);

                Intent intent=new Intent(getActivity(), SearchActivity.class);
                intent.putExtra("key", string);
                startActivity(intent);
            }
        });

        // 3. 设置点击返回按键后的操作（通过回调接口）
        // 隐藏搜索记录
        searchView.setOnClickBack(new bCallBack() {
            @Override
            public void BackAction() {
                searchView.hideRecord();
//                finish();
//                SearchListView listView = (SearchListView) searchView.findViewById(R.id.listView);
//                listView.setVisibility(View.INVISIBLE);
            }
        });
        pref=getActivity().getSharedPreferences("subjects_by_order", Context.MODE_PRIVATE);
        //pref= PreferenceManager.getDefaultSharedPreferences(getActivity());
        myList=getMenuList();
        mAdapter=new SubjectGridViewAdapter(getActivity(),myList);
        mAdapter.setPref(pref);
        subject_groups=binding.subjectGrid;//获取grid布局并注册监听器
        subject_groups.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mAdapter.getIsShow()){
                    mAdapter.setIsShow(false);
                    mAdapter.notifyDataSetChanged();
                }else{
                    if(position==parent.getChildCount()-1&&myList.size()<9){    //添加操作
                        showAddItems();
                        mAdapter.setIsShow(false);
                        mAdapter.notifyDataSetChanged();
                    }else{
                        // 学科详情
                        Intent intent=new Intent(getActivity(), DefaultListActivity.class);
                        intent.putExtra("subjectName",(int)myList.get(position).get("id"));
                        intent.putExtra("beginNum",position);
                        startActivity(intent);
                    }
                }
            }
        });
        subject_groups.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //如果已经显示了删除图标则取消
                //否则显示删除图标
                //为删除图标注册OnClickListener
                mAdapter.setIsShow(!mAdapter.getIsShow());
                mAdapter.notifyDataSetChanged();
                return true;
            }
        });
        subject_groups.setOnChangeListener(new DragGridView.OnChanageListener() {
            @Override
            public void onChange(int from, int to) {
                if(from==myList.size()){
                    return;
                }
                if(to==myList.size()){
                    to-=1;
                }
                HashMap<String,Object>tmp=myList.get(from);
                if(from<to){
                    editor = pref.edit();
                    for(int i=from+1;i<=to;++i){
                        editor.putInt(myList.get(i).get("id").toString(), i-1);
                    }
                    for(int i=from;i<to;++i){
                        Collections.swap(myList,i,i+1);
                    }
                }else if(from>to){
                    editor = pref.edit();
                    for(int i=from-1;i>=to;--i){
                        editor.putInt(myList.get(i).get("id").toString(), i+1);
                    }
                    for(int i=from;i>to;--i){
                        Collections.swap(myList,i,i-1);
                    }
                }
                myList.set(to,tmp);
                editor.putInt(tmp.get("id").toString(),to);
                editor.apply();
                mAdapter.notifyDataSetChanged();
            }
        });
        subject_groups.setAdapter(mAdapter);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}