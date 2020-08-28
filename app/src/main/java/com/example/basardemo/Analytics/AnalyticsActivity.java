package com.example.basardemo.Analytics;

import androidx.appcompat.app.AppCompatActivity;
import com.example.basardemo.R;
import com.huawei.hms.analytics.HiAnalytics;
import com.huawei.hms.analytics.HiAnalyticsInstance;
import com.huawei.hms.analytics.HiAnalyticsTools;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.huawei.hms.analytics.type.HAEventType.*;
import static com.huawei.hms.analytics.type.HAParamType.*;

public class AnalyticsActivity extends AppCompatActivity {

    private Button btnSetting;

    private int questions [] = {R.string.q1,R.string.q2,R.string.q3,R.string.q4,R.string.q5};
    private boolean answers [] = {true,true,false,false,true};

    private int curQuestionIdx = 0;
    private TextView txtQuestion;
    private Button btnNext;
    private Button btnTrue;
    private Button btnFalse;
    private Button postScore;
    private int score = 0;

    HiAnalyticsInstance instance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        // Initiate Analytics Kit
        // Enable Analytics Kit Log
        HiAnalyticsTools.enableLog();

        // Generate the Analytics Instance
        instance = HiAnalytics.getInstance(this);

        // You can also use Context initialization
        txtQuestion = (TextView) findViewById(R.id.question_text_view);
        txtQuestion.setText(questions[curQuestionIdx]);

        btnSetting = (Button) findViewById(R.id.setting_button);
        btnSetting.setOnClickListener(view -> {
            Intent i = new Intent(AnalyticsActivity.this, SettingsActivity.class);
            startActivityForResult(i, 0);
        });
        btnNext = (Button) findViewById(R.id.next_button);
        btnNext.setOnClickListener(view -> {
            curQuestionIdx = (curQuestionIdx + 1) % questions.length;
            nextQuestion();
        });

        btnTrue = (Button) findViewById(R.id.true_button);
        btnTrue.setOnClickListener(view -> {
            checkAnswer(true);
            reportAnswerEvt("true");
        });


        btnFalse = (Button) findViewById(R.id.false_button);
        btnFalse.setOnClickListener(view -> {
            checkAnswer(false);
            reportAnswerEvt("false");
        });

        postScore = (Button) findViewById(R.id.post_score_button);
        postScore.setOnClickListener(view -> postScore());
    }
    private void nextQuestion() {
        txtQuestion.setText(questions[curQuestionIdx]);
    }

    private boolean checkAnswer(boolean answer) {
        String q = txtQuestion.getText().toString().trim();

        if (answer == answers[curQuestionIdx]) {
            score = score + 20;
            Toast.makeText(this, R.string.correct_answer, Toast.LENGTH_SHORT).show();
            // Report a customized Event

        } else {
            Toast.makeText(this, R.string.wrong_answer, Toast.LENGTH_SHORT).show();
            // Report a customized Event

        }
        return answers[curQuestionIdx];
    }

    private void reportAnswerEvt(String answer) {
        // Report a customzied Event
        // Event Name: Answer
        // Event Parameters:
        //  -- question: String
        //  -- answer:String
        //  -- answerTime: String

        // Initiate Parameters
        Bundle bundle = new Bundle();
        bundle.putString("question", txtQuestion.getText().toString().trim());
        bundle.putString("answer", answer);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        bundle.putString("answerTime", sdf.format(new Date()));

        // Report a customzied Event
        instance.onEvent("Answer", bundle);
    }

    private void postScore() {
        // Report score by using SUBMITSCORE Event
        // Initiate Parameters
        Bundle bundle = new Bundle();
        bundle.putLong(SCORE, score);

        // Report a preddefined Event
        instance.onEvent(SUBMITSCORE, bundle);
    }
}