package com.example.frontend.ui.contest;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.frontend.DetailActivity;
import com.example.frontend.GlobalConst;
import com.example.frontend.R;
import com.example.frontend.ui.search.SearchListAdapter;
import com.example.frontend.utils.Communication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

public class ContestFragment extends Fragment {
    private ListView listView;
    private JSONArray data;
    private ArrayList<Map<String,Object>> lists=new ArrayList<>();
    private SearchListAdapter madapter;

    JSONObject question;
    int position;  //从0开始

    private TextView mScoreView;
    private TextView mQuestionView;
    private Button[] mButtonChoice;

    private ImageView shareImage;
    private ImageView markImage;

    public int chosen = -1;  // 0: A, 1: B
    public int answer = -1;
    int tot, passed;

    /**
     * onCreateView会多次调用
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_contest, container, false);


        Button nextQuestion = (Button) root.findViewById(R.id.nextQuestion);
        nextQuestion.setVisibility(View.INVISIBLE);
        Button previousQuestion = (Button) root.findViewById(R.id.previousQuestion);
        previousQuestion.setVisibility(View.INVISIBLE);

        markImage=(ImageView)root.findViewById(R.id.score_mark);
        markImage.setVisibility(View.INVISIBLE);
        shareImage=(ImageView)root.findViewById(R.id.score_share);
        shareImage.setVisibility(View.INVISIBLE);


        mScoreView = (TextView)root.findViewById(R.id.score);
        mQuestionView = (TextView)root.findViewById(R.id.question);
        mButtonChoice = new Button[4];
        mButtonChoice[0] = (Button)root.findViewById(R.id.choice1);
        mButtonChoice[1] = (Button)root.findViewById(R.id.choice2);
        mButtonChoice[2] = (Button)root.findViewById(R.id.choice3);
        mButtonChoice[3] = (Button)root.findViewById(R.id.choice4);

        for(int i = 0; i < 4; ++i) {
            if( chosen == i)
                mButtonChoice[i].setBackground(getResources().getDrawable(R.drawable.btn_pressed));
            else
                mButtonChoice[i].setBackground(getResources().getDrawable(R.drawable.btn_normal));
        }

        for(int i = 0; i < 4; ++i) {
            int finalI = i;
            mButtonChoice[i].setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    if( answer == -1) {
                        // 还没对答案
                        if(chosen == -1){
                            // 未选择选项
                            mButtonChoice[finalI].setBackground(getResources().getDrawable(R.drawable.btn_pressed));
                            chosen = finalI;
                        }
                        else {
                            // 选项切换
                            mButtonChoice[chosen].setBackground(getResources().getDrawable(R.drawable.btn_normal));
                            mButtonChoice[finalI].setBackground(getResources().getDrawable(R.drawable.btn_pressed));
                            chosen = finalI;
                        }
//                        else if(chosen == me) {
//                            mButtonChoice[finalI].setBackground(getResources().getDrawable(R.drawable.btn_normal));
//                            chosen = -1;
//                        }
                    }
                }
            });
        }

        if( answer != -1) {
            receiveResult(tot , passed, answer);
        }
        else {
            mScoreView.setText(R.string.notSubmitted);
        }

        try {
            mQuestionView.setText((position+1) + ". " + question.getString("qBody"));
            mButtonChoice[0].setText(question.getString("answerA"));
            mButtonChoice[1].setText(question.getString("answerB"));
            mButtonChoice[2].setText(question.getString("answerC"));
            mButtonChoice[3].setText(question.getString("answerD"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return root;
    }

    public void receiveResult(int tot, int passed, int answer) {
        Log.e("fragment position", String.valueOf(position));
        this.answer = answer;
        this.tot = tot;
        this.passed = passed;
        if( chosen == answer) {
            mButtonChoice[chosen].setBackground(getResources().getDrawable(R.drawable.btn_correct));
        }
        else {
            mButtonChoice[chosen].setBackground(getResources().getDrawable(R.drawable.btn_wrong));
            mButtonChoice[answer].setBackground(getResources().getDrawable(R.drawable.btn_correct));
        }
        mScoreView.setText("" + passed + "/" + tot);
    }

    public static ContestFragment newInstance(int position, JSONObject question) {
        Bundle args = new Bundle();
        args.putInt("No.", position);
        ContestFragment fragment = new ContestFragment();
        fragment.question = question;
        fragment.position = position;
        fragment.setArguments(args);
        return fragment;
    }

}
