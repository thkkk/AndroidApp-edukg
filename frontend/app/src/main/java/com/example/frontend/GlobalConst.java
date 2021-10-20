package com.example.frontend;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.HashMap;
import java.util.Map;


public class GlobalConst {
    public static final String DBForEntity = "record2.db";

    public static final String[] subjects = {
            "chinese", "english", "mathe", "physics", "chemistry",
            "biology", "history", "geo", "politics"
    };  // 数学接口不能用，因此改成mathe。本应为math

    public static final String[] subjects_by_order = { //调整为wzy代码中的科目顺序
            "chinese", "math", "english", "physics", "chemistry",
            "biology", "politics", "history", "geo"
    };
    public static final Map<String,Integer> subjects_index;
    public static final Integer[] subject_name_id={R.string.Chinese, //学科代码列表
            R.string.Math,
            R.string.English,
            R.string.Physics,
            R.string.Chemistry,
            R.string.Biology,
            R.string.Politics,
            R.string.History,
            R.string.Geometry};
    public static Map<Integer,Integer> subject_name_id_map;
    public static String username;
    public static Map<String,Integer> subject_map;
    public static Map<Integer,String> StringIdtoName;
    public static int getSubjectByString(String x){
        return subject_map.get(x);
    };
    public static String getNameByid(int x){
        return StringIdtoName.get(x);
    }
    public static int maxQuestionNumber = 25;
    public static int maxHistoryNumber = 25;
    public static String[] options = {"A", "B", "C", "D"};
    public static Map<String,Integer> options_anti;
    public static final String APP_KY = "3171566369";
    public static final String REDIRECT_URL = "https://api.weibo.com/oauth2/default.html";
    public static final String SCOPE = "email,direct_messages_read,direct_messages_write,"
+"friendships_groups_read,friendships_groups_write,statuses_to_me_read," +"follow_app_official_microblog," +"invitation_write";

    static {
        subjects_index = new HashMap<String, Integer>();
        subjects_index.put("chinese",0);
        subjects_index.put("math",1);
        subjects_index.put("mathe",1);
        subjects_index.put("english",2);
        subjects_index.put("physics",3);
        subjects_index.put("chemistry",4);
        subjects_index.put("biology",5);
        subjects_index.put("politics",6);
        subjects_index.put("history",7);
        subjects_index.put("geo",8);

        subject_name_id_map = new HashMap<Integer,Integer> ();
        subject_name_id_map.put(R.string.Chinese,0);
        subject_name_id_map.put(R.string.Math,1);
        subject_name_id_map.put(R.string.English,2);
        subject_name_id_map.put(R.string.Physics,3);
        subject_name_id_map.put(R.string.Chemistry,4);
        subject_name_id_map.put(R.string.Biology,5);
        subject_name_id_map.put(R.string.Politics,6);
        subject_name_id_map.put(R.string.History,7);
        subject_name_id_map.put(R.string.Geometry,8);

        subject_map = new HashMap<String,Integer> ();
        subject_map.put("chinese",R.string.Chinese);
        subject_map.put("math",R.string.Math);
        subject_map.put("mathe",R.string.Math);
        subject_map.put("english",R.string.English);
        subject_map.put("physics",R.string.Physics);
        subject_map.put("chemistry",R.string.Chemistry);
        subject_map.put("biology",R.string.Biology);
        subject_map.put("politics",R.string.Politics);
        subject_map.put("history",R.string.History);
        subject_map.put("geo",R.string.Geometry);

        StringIdtoName = new HashMap<Integer, String> ();
        StringIdtoName.put(R.string.Chinese,"chinese");
        StringIdtoName.put(R.string.Math,"mathe");
        StringIdtoName.put(R.string.English,"english");
        StringIdtoName.put(R.string.Physics,"physics");
        StringIdtoName.put(R.string.Chemistry,"chemistry");
        StringIdtoName.put(R.string.Biology,"biology");
        StringIdtoName.put(R.string.Politics,"politics");
        StringIdtoName.put(R.string.History,"history");
        StringIdtoName.put(R.string.Geometry,"geo");

        options_anti = new HashMap<String,Integer>();
        options_anti.put("A",0);
        options_anti.put("B",1);
        options_anti.put("C",2);
        options_anti.put("D",3);
    }
}

