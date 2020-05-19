package com.example.lilconsistentme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class DiedActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    ArrayList<TrackingItem> mTrackingItemList;
    final long WEEK_IN_MILLIS=7*24*60*60*1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_died);
        MainActivity.menuOpen = false;

        sharedPreferences =getSharedPreferences("prefs",MODE_PRIVATE);
        editor = sharedPreferences.edit();

        //Rescue Button
        findViewById(R.id.layout_rescue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putLong("endTime", WEEK_IN_MILLIS+System.currentTimeMillis()).apply();
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //Replay Button
        findViewById(R.id.text_replay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Delete List
                loadList();
                mTrackingItemList.clear();
                saveList();

                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                editor.putLong("endTime", WEEK_IN_MILLIS+System.currentTimeMillis()).apply();
                startActivity(intent);
                finish();
            }
        });
    }

    private void saveList(){
        Gson gson = new Gson();
        String json = gson.toJson(mTrackingItemList);
        editor.putString("TrackingItemList",json).apply();
    }

    private void loadList(){
        SharedPreferences sharedPreferences = getSharedPreferences("prefs", MODE_PRIVATE);
        String json = sharedPreferences.getString("TrackingItemList",null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<TrackingItem>>() {}.getType();
        mTrackingItemList=gson.fromJson(json,type);

        if(mTrackingItemList== null){
            mTrackingItemList= new ArrayList<>();
        }
    }
}
