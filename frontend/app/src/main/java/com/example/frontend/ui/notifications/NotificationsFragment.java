package com.example.frontend.ui.notifications;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.frontend.GlobalConst;
import com.example.frontend.HistoryActivity;
import com.example.frontend.LoginActivity;
import com.example.frontend.MainActivity;
import com.example.frontend.MarkActivity;
import com.example.frontend.R;
import com.example.frontend.RecordActivity;
import com.example.frontend.databinding.FragmentNotificationsBinding;
import com.example.frontend.utils.Communication;
import com.example.frontend.utils.RecordDBOperator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import okhttp3.Response;

import static com.xuexiang.xui.utils.ResUtils.getColor;

//public class NotificationsFragment extends Fragment {
//
//    private NotificationsViewModel notificationsViewModel;
//
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//        notificationsViewModel =
//                new ViewModelProvider(this).get(NotificationsViewModel.class);
//        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
//        final TextView textView = root.findViewById(R.id.text_notifications);
//        notificationsViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
//        return root;
//    }
//}

public class NotificationsFragment extends Fragment {

    private NotificationsViewModel notificationsViewModel;
    private FragmentNotificationsBinding binding;

    private String[] name_list=new String[7];
    private final int[] name_list_id={
            R.string.change_password,
            R.string.favorites,
            R.string.history_records,
            R.string.local_cache,
            R.string.language,
            R.string.message_notification,
            R.string.log_out
    };

    private void initNameList(){
        for(int i=0;i<name_list_id.length;++i){
            name_list[i]=getString(name_list_id[i]);
        }
    }

    private TextView mUsername; //用户名
    private ImageView photo;//用户头像
    private ListView mList; //列表
    private SettingListViewAdapter mAdapter; //适配器

    private void showLogoutDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.tips);
        builder.setIcon(R.drawable.question);
        builder.setMessage(R.string.ask_log_out);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent=new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //pass
            }
        });
        builder.setCancelable(true);
        AlertDialog dialog=builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        Button button=dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        button.setTextColor(getColor(R.color.black));
        button=dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        button.setTextColor(getColor(R.color.xui_config_color_50_blue));
    }

    private void showInfoDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.tips);
        builder.setIcon(R.drawable.warn);
        builder.setMessage(R.string.no_info);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setCancelable(true);
        AlertDialog dialog=builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
        Button button=dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        button.setTextColor(getColor(R.color.black));
    }


    private void changePassword(){ //修改密码
        LayoutInflater layoutInflater=LayoutInflater.from(getActivity());
        final View changeView=layoutInflater.inflate(R.layout.change_password,null);
        final EditText oldPasswordView=(EditText)changeView.findViewById(R.id.old_password);
        final EditText newPasswordView=(EditText)changeView.findViewById(R.id.new_password);
        final EditText confirmPasswordView=(EditText)changeView.findViewById(R.id.confirm_password_change);
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.change_password).setIcon(R.drawable.question);
        builder.setView(changeView);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        Button button=dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword=oldPasswordView.getText().toString();
                String newPassword=newPasswordView.getText().toString();
                String confirmPassword=confirmPasswordView.getText().toString();
                if(!newPassword.equals(confirmPassword)){ //密码不一致
                    Toast.makeText(getActivity(),R.string.password_not_equal,Toast.LENGTH_LONG).show();
                }else{
                    JSONObject object=new JSONObject();
                    try{ //加入请求参数
                        object.put("oldpassword",oldPassword);
                        object.put("newpassword",newPassword);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                    //网络通信部分
                    new Thread(()->{
                        try{
                            Communication communication=new Communication(object);
                            Response response=communication.sendPost("changePassword",true);

                            JSONObject jsonObject=new JSONObject(response.body().string());
                            Log.v("Change Password",jsonObject.toString());
                            int code=jsonObject.getInt("code");
                            if(code==0){
                                getActivity().runOnUiThread(()->{
                                    Toast.makeText(getActivity(),R.string.change_password_success,Toast.LENGTH_SHORT).show();
                                });
                                dialog.dismiss();
                            }else{
                                getActivity().runOnUiThread(()-> {
                                    Toast.makeText(getActivity(), R.string.change_password_fail, Toast.LENGTH_SHORT).show();
                                });
                            }
                        }catch (Exception e){
                            getActivity().runOnUiThread(()-> {
                                Toast.makeText(getActivity(),R.string.network_error,Toast.LENGTH_SHORT).show();
                            });
                        }
                    }).start();
                }
            }
        });
        button.setTextColor(getColor(R.color.black));
        button=dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        button.setTextColor(getColor(R.color.xui_config_color_50_blue));
    }

    public void refreshSelf(){ //刷新
        Intent intent=new Intent(getActivity(),MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * 注意这是表示本地缓存
     * 不是后端的历史记录
     */
    public void showRecord() {
        Intent intent=new Intent(getActivity(), RecordActivity.class);
        startActivity(intent);
    }

    private void selectLanguage(){//修改语言
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.select_language);
        builder.setIcon(R.drawable.question);
        builder.setCancelable(true);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        String []languages={"简体中文","English"};
        builder.setItems(languages, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Resources resources=getResources();
                Configuration config=resources.getConfiguration();
                DisplayMetrics dm=resources.getDisplayMetrics();
                if(which==1){//英文
                    config.setLocale(Locale.ENGLISH);
                    Toast.makeText(getActivity(), "The system language has been switched to English", Toast.LENGTH_SHORT).show();
                }else{ //中文
                    config.setLocale(Locale.CHINESE);
                    Toast.makeText(getActivity(), "系统语言已切换至中文", Toast.LENGTH_SHORT).show();
                }
                resources.updateConfiguration(config,dm);   //TODO 找到此API的替代
                refreshSelf(); //刷新界面
            }
        });
        AlertDialog dialog=builder.create();
        dialog.show();
        Button button=dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        button.setTextColor(getColor(R.color.black));
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);
        initNameList();
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        photo=binding.userPhoto;
        //TODO 用户头像设置

        mUsername=binding.settingUsername;
        mUsername.setText(GlobalConst.username);
        mList=binding.settingList;
        mAdapter=new SettingListViewAdapter(getActivity(),name_list);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    // TODO 单一条目的点击事件与界面跳转
                    case 0: //修改密码
                        changePassword();
                        break;
                    case 1: //收藏
                        Intent intent=new Intent(getActivity(), MarkActivity.class);
                        startActivity(intent);
                        break;
                    case 2: // 历史记录
                        Intent intent3=new Intent(getActivity(), HistoryActivity.class);
                        startActivity(intent3);
                        break;
                    case 3: // 本地缓存
                        showRecord();
                        break;
                    case 4: //语言
                        selectLanguage();
                        break;
                    case 5: //通知
                        showInfoDialog();
                        break;
                    case 6: //登出
                        showLogoutDialog();
                        break;
                    default:
                        break;
                }
            }
        });
        mList.setAdapter(mAdapter);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}