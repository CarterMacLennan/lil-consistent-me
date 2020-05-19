package com.example.lilconsistentme;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements View.OnTouchListener {
    //Variables
    private TextView dialogue, score, textView_lvl;
    private float x1, x2, y1, y2;
    private Vibrator vibrator;
    private int curScore,lvl=0;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private View fragmentView;
    ArrayList<TrackingItem> mTrackingItemList;

    //Static Variables
    public static long ptsAdded, timeAdded = 0;
    public static boolean menuOpen = false;

    //Timer
    private CountDownTimer mCountDownTimer;
    private TextView mTextViewCountDown;
    private final long WEEK_IN_MILLIS = 7*24*60*60*1000;
    private long mTimeLeftInMillis, mEndTime;
    private ProgressBar progressBar_hp,progressBar_lvl;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Vibrator
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //Settings
        sharedPreferences = getApplicationContext().getSharedPreferences("prefs",MODE_PRIVATE);
        editor = sharedPreferences.edit();

        //Initialize Views
        textView_lvl=findViewById(R.id.textview_lvl);
        progressBar_hp = findViewById(R.id.progressBar_hp);
        progressBar_lvl=findViewById(R.id.progressBar_lvl);
        fragmentView = findViewById(R.id.menu_frag);
        dialogue = findViewById(R.id.text_dialogue);
        ImageButton button = findViewById(R.id.button_yeti);
        mTextViewCountDown = findViewById(R.id.text_countdown);
        button.setOnTouchListener(this);
        findViewById(R.id.layout_main).setOnTouchListener(this);

        //Level
        lvl= sharedPreferences.getInt("lvl",0);
        progressBar_lvl.setMax(sharedPreferences.getInt("max_pts",100));
        textView_lvl.setText("lvl "+lvl);

        //Score
        score = findViewById(R.id.text_score);
        curScore = sharedPreferences.getInt("total_pts", MODE_PRIVATE);
        score.setText(curScore + "pts");
        progressBar_lvl.setProgress(curScore);

        //If a new Activity was added, refresh the menu
        if (getIntent().getBooleanExtra("LoadMenu", false)) {
            findViewById(R.id.fragmentContainer_menu).setVisibility(View.VISIBLE);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragmentContainer_menu, new Fragment_menu(), "tag_menu").commit();
            menuOpen = true;
        }
    }


    public void button_yeti(View view) {
        //Vibrate
        vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE));

        //CLOSE MENU
        if (menuOpen) {
            menuOpen = false;
            fragmentView = findViewById(R.id.menu_frag);
            closeMenu(fragmentView);
            updateUI();

            //Update to do list if pts were added
            if(progressBar_lvl.getProgress()>0)
                editor.putBoolean("todo_feed", false).apply();
            checklistVisibility();

            //De-select items & save the list
            mTrackingItemList= Fragment_menu.mTrackingItemList;
            for(int i=0; i<mTrackingItemList.size() ;i++)
                mTrackingItemList.get(i).setSelected(false);
            saveList();
        }
        //OPEN MENU if previous to do has been complete
        else if(!sharedPreferences.getBoolean("todo_dashboard",true)){//Open Menu
            menuOpen = true;
            findViewById(R.id.fragmentContainer_menu).setVisibility(View.VISIBLE);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragmentContainer_menu, new Fragment_menu(), "tag_menu").commit();
        }
    }
    private void saveList(){
        Gson gson = new Gson();
        editor.putString("TrackingItemList",gson.toJson(mTrackingItemList)).apply();
    }

    private void loadList(){
        SharedPreferences sharedPreferences = getSharedPreferences("prefs", MODE_PRIVATE);
        String json = sharedPreferences.getString("TrackingItemList",null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<TrackingItem>>() {}.getType();
        mTrackingItemList = gson.fromJson(json, type);
        if (mTrackingItemList == null)
                mTrackingItemList = new ArrayList<TrackingItem>();
    }

    private void checklistClear(){
        findViewById(R.id.title_todo).setVisibility(View.GONE);
        findViewById(R.id.todo_dashboard).setVisibility(View.GONE);
        findViewById(R.id.todo_progress).setVisibility(View.GONE);
        findViewById(R.id.todo_addActivity).setVisibility(View.GONE);
        findViewById(R.id.todo_feed).setVisibility(View.GONE);
    }

    private void checklistVisibility(){
        checklistClear();

        if(sharedPreferences.getBoolean("todo_dashboard",true)) {
            findViewById(R.id.todo_dashboard).setVisibility(View.VISIBLE);
            findViewById(R.id.title_todo).setVisibility(View.VISIBLE);
        }
        else if(sharedPreferences.getBoolean("todo_addActivity",true)){
            findViewById(R.id.todo_addActivity).setVisibility(View.VISIBLE);
            findViewById(R.id.title_todo).setVisibility(View.VISIBLE);
        }
        else if(sharedPreferences.getBoolean("todo_feed",true)){
            findViewById(R.id.todo_feed).setVisibility(View.VISIBLE);
            findViewById(R.id.title_todo).setVisibility(View.VISIBLE);
        }
        else if(sharedPreferences.getBoolean("todo_progress",true)){
            findViewById(R.id.todo_progress).setVisibility(View.VISIBLE);
            findViewById(R.id.title_todo).setVisibility(View.VISIBLE);
        }
    }

    public void startTimer() {
        mEndTime = System.currentTimeMillis() + mTimeLeftInMillis;
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis=millisUntilFinished;
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                Intent intent = new Intent(getApplicationContext(),DiedActivity.class);
                startActivity(intent);
                finish();
            }
        }.start();
    }

    public void updateCountDownText() {
        //Format Time
        long second = (mTimeLeftInMillis / 1000) % 60;
        long minute = (mTimeLeftInMillis / (1000 * 60)) % 60;
        long hour = ((mTimeLeftInMillis / (1000 * 60 * 60)) % 24);
        mTextViewCountDown.setText(String.format("%02d:%02d:%02d", hour, minute, second));
        int progress = (int) mTimeLeftInMillis;
        progressBar_hp.setProgress(progress);
    }

    private void updateUI() {

        curScore += ptsAdded;
        ptsAdded = 0;

        //Update Lvl
        while((curScore) >= progressBar_lvl.getMax()) {
            curScore = (int) curScore - progressBar_lvl.getMax();
            progressBar_lvl.setMax(progressBar_lvl.getMax() + progressBar_lvl.getMax() / 4);
            lvl++;
            Log.i("TAG", "curScore: " +curScore +"  progressBarMax: "+ progressBar_lvl.getMax());
        }

        //Add Points
        textView_lvl.setText("lvl "+lvl);
        progressBar_lvl.setProgress(curScore);
        score.setText(curScore + "pts");

        //Update Clock
        if (timeAdded != 0) {
            mCountDownTimer.cancel();
            if ((mTimeLeftInMillis + timeAdded) >= WEEK_IN_MILLIS)
                mTimeLeftInMillis = WEEK_IN_MILLIS;
            else
                mTimeLeftInMillis += timeAdded;
            startTimer();
            timeAdded = 0;
        }

        //Update Dialogue
        if(menuOpen)
            dialogue.setText(R.string.character_selectActivity);
        else if(!sharedPreferences.getBoolean("todo_dashboard",true))
            dialogue.setText(R.string.character_hungry);
    }

    private void closeMenu(View v) {
        //Animation
        int x = v.getWidth() / 2;
        int y = v.getHeight() / 2;
        Animator anim = ViewAnimationUtils.createCircularReveal(v, x, y, (int) Math.hypot(x, y), 0);
        anim.setDuration(250);

        //Close Fragment OnFinish
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                fragmentView.setVisibility(View.INVISIBLE);
                getSupportFragmentManager().popBackStack();
                findViewById(R.id.fragmentContainer_menu).setVisibility(View.GONE);
            }
        });
        anim.start();
    }

    public boolean onTouch(View v, MotionEvent touchEvent) {
        switch (touchEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                y1 = touchEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                y2 = touchEvent.getY();
                if (x1+100 < x2) {
                    Intent intent = new Intent(MainActivity.this, LeftActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slideleft_in, R.anim.slideleft_out);
                    editor.putBoolean("todo_dashboard",false).apply();
                } else if (x1 > x2+100){
                    //enable access to data visualization after the other to dos have been complete
                    if(!sharedPreferences.getBoolean("todo_feed",true)) {
                        Intent intent = new Intent(MainActivity.this, RightActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slideright_in, R.anim.slideright_out);
                        editor.putBoolean("todo_progress", false).apply();
                    }
                }
                else
                    button_yeti(v);
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();

        //Load Lists
        loadList();
        checklistVisibility();

        //Timer
        mEndTime = sharedPreferences.getLong("endTime", System.currentTimeMillis() + WEEK_IN_MILLIS);
        mTimeLeftInMillis = mEndTime - System.currentTimeMillis();
        startTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Points
        ptsAdded=0;
        timeAdded=0;

        //Score
        editor.putInt("total_pts", curScore);
        editor.putInt("lvl",lvl);
        editor.putInt("max_pts",progressBar_lvl.getMax());
        //Timer
        editor.putLong("endTime", mEndTime).apply();
        mCountDownTimer.cancel();
    }
}