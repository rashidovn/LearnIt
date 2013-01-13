package com.learnit.LearnIt;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.util.Random;

public class HomeworkActivity extends FragmentActivity{
    int notificationId = -1;
    String queryWord = null;
    String article = null;
    final String LOG_TAG = "my_logs";
    DBHelper dbHelper;
    int[] btnIds = {R.id.left_top_button,
                    R.id.right_top_button,
                    R.id.left_bottom_button,
                    R.id.right_bottom_button};

    private void getEverythingFromIntent() {
        Intent intent = getIntent();
        article = intent.getStringExtra("article");
        queryWord = intent.getStringExtra("word");
        notificationId = intent.getIntExtra("id", -1);
        Log.d(LOG_TAG, "got intent word=" + queryWord + " id = "
                + notificationId);
        dbHelper = new DBHelper(this);
    }

    private void setBtnTexts(int correctId)
    {
        for (int i = 0; i<btnIds.length; ++i)
        {
            if (correctId!=i)
            {
                ((Button) findViewById(btnIds[i])).setText(dbHelper.getRandomTranslation(queryWord));
            }
            else
            {
                dbHelper.getTranslations(queryWord);
                ((Button) findViewById(btnIds[i])).setText(dbHelper.getTranslations(queryWord).get(0));
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getEverythingFromIntent();
        setContentView(R.layout.homework);
        MyButtonOnClick myButtonOnClick = new MyButtonOnClick();
        Random random = new Random();
        int randIdx = random.nextInt(btnIds.length);
        myButtonOnClick.correct=btnIds[randIdx];
        (findViewById(R.id.left_top_button))
                .setOnClickListener(myButtonOnClick);
        (findViewById(R.id.right_bottom_button))
                .setOnClickListener(myButtonOnClick);
        (findViewById(R.id.left_bottom_button))
                .setOnClickListener(myButtonOnClick);
        (findViewById(R.id.right_top_button))
                .setOnClickListener(myButtonOnClick);
        setBtnTexts(randIdx);
    }

    private void showDialogWrong()
    {
        MyDialogFragment frag = new MyDialogFragment();
        Bundle args = new Bundle();
        args.putInt(MyDialogFragment.ID_TAG, MyDialogFragment.DIALOG_WRONG_GUESS);
        frag.setArguments(args);
        frag.show(getSupportFragmentManager(), "wrong_guess");
    }

    protected void stopActivity()
    {
        this.finish();
    }

    private class MyButtonOnClick implements OnClickListener
    {
        public int correct = 0;
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (correct==id)
            {
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(notificationId);
                stopActivity();
            }
            else
            {
                showDialogWrong();
            }
        }
    }

    protected void onResume() {
        super.onResume();

        TextView queryWordTextView = (TextView) findViewById(R.id.word_to_ask);
        queryWordTextView.setText(article + " " + queryWord);
    }
}