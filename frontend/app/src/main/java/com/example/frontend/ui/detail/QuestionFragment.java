package com.example.frontend.ui.detail;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.fastjson.JSON;
import com.example.frontend.DetailActivity;
import com.example.frontend.R;
import com.example.frontend.databinding.FragmentQuestionBinding;
import com.example.frontend.utils.Communication;
import com.example.frontend.utils.DBForEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import okhttp3.Response;


/**
 * 查看相关试题
 */
public class QuestionFragment extends Fragment {
    View root;

    private static final String ARG_SECTION_NUMBER = "section_number";
    public int id;
    private JSONObject data;

    private List<Map<String, Object>> lists;
    private SimpleAdapter adapter;
    private LinearLayout linearLayout;

    private PageViewModel pageViewModel;
    private FragmentQuestionBinding binding;

    Thread questionsThread;
    JSONArray questions;
    int code = -1;
    String msg, label;

    private TextView mScoreView;
    private TextView mQuestionView;
    private Button mButtonChoice1;
    private Button mButtonChoice2;
    private Button mButtonChoice3;
    private Button mButtonChoice4;

    private ImageView shareImage;
    private ImageView markImage;

    private String mAnswer;
    private int mScore = 0;

    boolean chosen = false;


    public static QuestionFragment newInstance(int index, JSONObject data) {
        QuestionFragment fragment = new QuestionFragment();
        fragment.id = index;
        fragment.data = data;
        try {
            fragment.label = data.getString("label");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(index == 3) {
            fragment.questionsThread = fragment.questionCommunication(fragment.label);
            if( !data.has("questions") )
                fragment.questionsThread.start();
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

    private Thread questionCommunication(String label) {
        // 向后端发送请求
        // convert key-value pairs to a JSONObject
        Log.e( "detail", "label :" + label);
        JSONObject object = new JSONObject();

        try {
            object.put("uriName", label);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new Thread(() -> {
            try{
                Communication communication = new Communication(object);
                Response response = communication.sendPost("questionListByUriName", true);
                JSONObject jsonObject = new JSONObject(response.body().string());
                Log.v("question",jsonObject.toString());
                try {
                    msg = jsonObject.getString("msg");
                    questions = jsonObject.getJSONArray("data");
                    Log.e("detail: questionsDataLen", String.valueOf(questions.toString().length()));
                    code = jsonObject.getInt("code");
                    // 还可获取marked
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }catch (Exception e){
                e.printStackTrace();
                code = -1;
//                runOnUiThread(() -> {
//                    // update UI
//                    Toast.makeText(this, "网络连接失败", Toast.LENGTH_SHORT).show();
//                });
            }
        });
    }

    private int questionsPointer = 0; // 接下来会显示的题目
    private int questionsNumber = 0;

    private int questionId=0;//当前显示题目的id
    private Boolean[] questionMark=new Boolean[30]; //当前题目是否收藏
    private Boolean[] questionInit=new Boolean[30]; //当前题目是否已经初始化过，防止重复读取marked
    private String[] questionChosen = new String[30];
    /**
     * 显示当前questionsPointer指向的题目
     */
    void updateQuestion() {
        if(questionsPointer >= questionsNumber) {
            questionsPointer = questionsNumber - 1;
            Toast.makeText(getActivity(), R.string.noNext, Toast.LENGTH_SHORT).show();
            return ;
        }
        if(questionsPointer <= -1) {
            questionsPointer = 0;
            Toast.makeText(getActivity(), R.string.noPrevious, Toast.LENGTH_SHORT).show();
            return ;
        }

        chosen = false;
        String qbody,answerA,answerB,answerC,answerD;
        try {
            JSONObject content = questions.getJSONObject(questionsPointer);
            Log.e("content", content.toString());
            qbody=(questionsPointer+1) + ". " + content.getString("qBody");
            mQuestionView.setText(qbody);
            answerA=content.getString("answerA");
            answerB=content.getString("answerB");
            answerC=content.getString("answerC");
            answerD=content.getString("answerD");
            mButtonChoice1.setText(answerA);
            mButtonChoice2.setText(answerB);
            mButtonChoice3.setText(answerC);
            mButtonChoice4.setText(answerD);

            mButtonChoice1.setBackground(getResources().getDrawable(R.drawable.btn_normal));
            mButtonChoice2.setBackground(getResources().getDrawable(R.drawable.btn_normal));
            mButtonChoice3.setBackground(getResources().getDrawable(R.drawable.btn_normal));
            mButtonChoice4.setBackground(getResources().getDrawable(R.drawable.btn_normal));

            mAnswer = content.getString("qAnswer");

            questionId=content.getInt("id");

            if(questionChosen[questionsPointer] != null)
                updateButton(questionChosen[questionsPointer]);

            if(!questionInit[questionsPointer]){
                questionInit[questionsPointer]=true;
                questionMark[questionsPointer]=content.getBoolean("marked");
            }
            if(questionMark[questionsPointer]){
                markImage.setImageResource(R.drawable.star_favourites);
            }else{
                markImage.setImageResource(R.drawable.star_empty);
            }
            shareImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((DetailActivity)getActivity()).doWeiboShare(getString(R.string.default_share_question_text)+
                            qbody+":"+answerA+answerB+answerC+answerD);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        updateMark();
    }

    private Boolean mark_success=true; //标记收藏是否成功

    private Thread markProblem(boolean flag){ //flag为true时收藏习题
        JSONObject object=new JSONObject();
        try{
            object.put("problemid",questionId);
        }catch (JSONException e){
            e.printStackTrace();
        }
        return new Thread(()->{
            try{
                Communication communication=new Communication(object);
                Response response;
                if(flag){
                    response=communication.sendPost("markProblem",true);
                }else{
                    response=communication.sendPost("unmarkProblem",true);
                }
                JSONObject jsonObject=new JSONObject(response.body().string());
                int code=jsonObject.getInt("code");
                mark_success=( code == 0 );
            }catch (Exception e){
                e.printStackTrace();
                mark_success=false;
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), R.string.network_error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateMark(){ //更新收藏标记
        markImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(questionId==0){ //没有初始化的情况
                    return;
                }
                if(questionMark[questionsPointer]){
                    //取消标记
                    Thread t=markProblem(false);
                    t.start();
                    try{
                        t.join();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if(mark_success){
                        Toast.makeText(getActivity(),R.string.unmark_success,Toast.LENGTH_SHORT).show();
                        questionMark[questionsPointer]=false;
                        markImage.setImageResource(R.drawable.star_empty);
                    }else{
                        Toast.makeText(getActivity(),R.string.unmark_fail,Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Thread t=markProblem(true);
                    t.start();
                    try{
                        t.join();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if(mark_success){
                        Toast.makeText(getActivity(),R.string.mark_success,Toast.LENGTH_SHORT).show();
                        questionMark[questionsPointer]=true;
                        markImage.setImageResource(R.drawable.star_favourites);
                    }else{
                        Toast.makeText(getActivity(),R.string.mark_fail,Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    void changeButtonColor (String button, Drawable drawable) {
        switch (button) {
            case "A":
                mButtonChoice1.setBackground(drawable);
                break;
            case "B":
                mButtonChoice2.setBackground(drawable);
                break;
            case "C":
                mButtonChoice3.setBackground(drawable);
                break;
            case "D":
                mButtonChoice4.setBackground(drawable);
                break;
        }
    }

    /**
     * press clk
     * @param clk
     */
    void updateButton(String clk) {
        questionChosen[questionsPointer] = clk;
        if(mAnswer.equals(clk)) {
            changeButtonColor(clk, getResources().getDrawable(R.drawable.btn_correct));
        }
        else {
            changeButtonColor(clk, getResources().getDrawable(R.drawable.btn_wrong));
            changeButtonColor(mAnswer, getResources().getDrawable(R.drawable.btn_correct));
        }
    }

    /**
     * init，等待试题的网络请求结束
     */
    private void initView(){
        // 检测data中是否有questions字段，如果没有则再次调用网络请求获取questions，
        // 并将带有questions的data存入数据库中

        // UPDATE: 判断local
        if( getActivity().getIntent().hasExtra("local") && data.has("questions")) {
            Log.e("detail from","local");
            try {
                questions = data.getJSONArray("questions");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        else {
            Log.e("detail from","cloud");
            try {
                questionsThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if( code == -1 ) {
                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity(), R.string.connection_failed, Toast.LENGTH_SHORT).show();
                return ;
            }
            // 现在我有questions了
            String uri = null;
            try {
                uri = data.getString("uri");
                data.put("questions", questions);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            DBForEntity.detailDB.updateContentByUri(uri, data.toString());
        }
        markImage=(ImageView)root.findViewById(R.id.score_mark);
        shareImage=(ImageView)root.findViewById(R.id.score_share);

        questionsNumber = questions.length();
        for(int i=0;i<30;++i){
            questionInit[i]=false;
            questionMark[i]=false;
        }

        mScoreView = (TextView)root.findViewById(R.id.score);
        mQuestionView = (TextView)root.findViewById(R.id.question);
        mButtonChoice1 = (Button)root.findViewById(R.id.choice1);
        mButtonChoice2 = (Button)root.findViewById(R.id.choice2);
        mButtonChoice3 = (Button)root.findViewById(R.id.choice3);
        mButtonChoice4 = (Button)root.findViewById(R.id.choice4);

        updateQuestion();
        updateScore(0);

        //Start of Button Listener for Button1
        mButtonChoice1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //My logic for Button goes in here
                if(questionsNumber == 0 ) return ;

                String clk = "A";
                chosen = (questionChosen[questionsPointer] != null);
                if( chosen ) return ;
                chosen = true;
                if (mAnswer.equals(clk)){
                    updateButton(clk);
                    mScore = mScore + 1;
                    updateScore(mScore);

//                    questionsPointer ++;
//                    updateQuestion();
                    //This line of code is optional
                    Toast.makeText(getActivity(), "correct", Toast.LENGTH_SHORT).show();
                }else {
                    updateButton(clk);
                    Toast.makeText(getActivity(), "wrong", Toast.LENGTH_SHORT).show();
//                    updateQuestion();
                }
            }
        });
        //End of Button Listener for Button1

        //Start of Button Listener for Button2
        mButtonChoice2.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //My logic for Button goes in here
                if(questionsNumber == 0 ) return ;

                String clk = "B";
                chosen = (questionChosen[questionsPointer] != null);
                if( chosen ) return ;
                chosen = true;
                if (mAnswer.equals(clk)){
                    updateButton(clk);
                    mScore = mScore + 1;
                    updateScore(mScore);

//                    questionsPointer ++;
//                    updateQuestion();
                    //This line of code is optional
                    Toast.makeText(getActivity(), "correct", Toast.LENGTH_SHORT).show();
                }else {
                    updateButton(clk);
                    Toast.makeText(getActivity(), "wrong", Toast.LENGTH_SHORT).show();
//                    updateQuestion();
                }
            }
        });
        //End of Button Listener for Button2

        //Start of Button Listener for Button3
        mButtonChoice3.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //My logic for Button goes in here
                if(questionsNumber == 0 ) return ;

                String clk = "C";
                chosen = (questionChosen[questionsPointer] != null);
                if( chosen ) return ;
                chosen = true;
                if (mAnswer.equals(clk)){
                    updateButton(clk);
                    mScore = mScore + 1;
                    updateScore(mScore);

//                    questionsPointer ++;
//                    updateQuestion();
                    //This line of code is optional
                    Toast.makeText(getActivity(), "correct", Toast.LENGTH_SHORT).show();
                }else {
                    updateButton(clk);
                    Toast.makeText(getActivity(), "wrong", Toast.LENGTH_SHORT).show();
//                    updateQuestion();
                }
            }
        });
        //End of Button Listener for Button3

        //Start of Button Listener for Button4
        mButtonChoice4.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //My logic for Button goes in here
                if(questionsNumber == 0 ) return ;

                String clk = "D";
                chosen = (questionChosen[questionsPointer] != null);
                if( chosen ) return ;
                chosen = true;
                if (mAnswer.equals(clk)){
                    updateButton(clk);
                    mScore = mScore + 1;
                    updateScore(mScore);

//                    questionsPointer ++;
//                    updateQuestion();
                    //This line of code is optional
                    Toast.makeText(getActivity(), "correct", Toast.LENGTH_SHORT).show();
                }else {
                    updateButton(clk);
                    Toast.makeText(getActivity(), "wrong", Toast.LENGTH_SHORT).show();
//                    updateQuestion();
                }
            }
        });
        //End of Button Listener for Button4

        // binding 有的时候会莫名其妙变成空的
        Button nextQuestion = (Button) root.findViewById(R.id.nextQuestion);
        nextQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                questionsPointer ++;
                updateQuestion();
            }
        });
        Button previousQuestion = (Button) root.findViewById(R.id.previousQuestion);
        previousQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                questionsPointer --;
                updateQuestion();
            }
        });
    }

    // 注意会多次onCreateView
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentQuestionBinding.inflate(inflater, container, false);
        if( binding == null)
            Log.e("null", "null");
        root = binding.getRoot();

        Log.e("onCreateView", "onCreateView");

        // 显示当前的fragment
//        if (null != root) {
//
//        } else {
//            root = binding.getRoot();
//            // 控件初始化
//            initView();
//        }

        initView();
//        Log.e("local", String.valueOf(getActivity().getIntent().hasExtra("local")));
        return root;
    }

    // 删除： 经常出现root/binding 为null的情况
//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        // 为了异步加载试题，滑到那一个fragment时才加载
//        super.setUserVisibleHint(isVisibleToUser);
//        if (isVisibleToUser) {
//            Log.e("show", "show");
//            // 显示当前的fragment
//            // 控件初始化。要多次初始化
//            initView();
//        } else {
//            Log.e("hidden", "hidden");
//            // 隐藏当前的fragment
//        }
//    }

    private void updateScore(int point) {
        mScoreView.setText("" + mScore + "/" + questionsNumber);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}