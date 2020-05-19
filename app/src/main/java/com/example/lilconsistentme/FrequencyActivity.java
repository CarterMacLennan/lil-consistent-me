package com.example.lilconsistentme;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class FrequencyActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    ArrayList<TrackingItem> mTrackingItemList;
    String title;
    ImageButton buttonSave;
    TextView textSave;
    SharedPreferences.Editor editor;
    Vibrator vibrator;
    NumberPicker numberPicker;
    Spinner spinner;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frequency);
         sharedPreferences= getApplicationContext().getSharedPreferences("prefs", MODE_PRIVATE);

        //Set UI
        title = getIntent().getStringExtra("ActivityName");
        ((TextView) findViewById(R.id.text_FrequencyTitle)).setText("How often do you want to \n" + title + "?");
        buttonSave = findViewById(R.id.button_save);
        textSave = findViewById(R.id.text_save);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //Number Picker
        numberPicker = findViewById(R.id.numberpicker_frequency);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(20);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int num1, int num2) {
                if (num2 != 0) {
                    buttonSave.setBackgroundResource(R.drawable.bg_save_updated);
                    textSave.setTextColor(Color.parseColor("#ffffff"));
                    buttonSave.setEnabled(true);
                } else {
                    buttonSave.setBackgroundResource(R.drawable.bg_save);
                    textSave.setTextColor(Color.parseColor("#40000000"));
                    buttonSave.setEnabled(false);
                }
            }
        });

        //Spinner
        spinner = findViewById(R.id.spinner_frequency);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinner_options, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        //Cancel Button
        findViewById(R.id.text_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE));
                finish();
                overridePendingTransition(R.anim.slidedown_in, R.anim.slidedown_out);
            }
        });

        //Save Button
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE));

                loadList();
                createTrackingItem();
                saveList();

                //START MAIN ACTIVITY
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                //Load Menu Or Not
                if(!sharedPreferences.getBoolean("todo_feed",true))
                    intent.putExtra("LoadMenu", true);
                else
                    MainActivity.menuOpen=false;

                //Transition
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slidedown_in, R.anim.slidedown_out);
            }
        });
    }

    private void saveList() {
        Gson gson = new Gson();
        String json = gson.toJson(mTrackingItemList);
        editor = sharedPreferences.edit();
        editor.putString("TrackingItemList", json);
        editor.putBoolean("todo_addActivity",false).apply();
    }

    private void loadList() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString("TrackingItemList", null);
        Type type = new TypeToken<ArrayList<TrackingItem>>() {}.getType();
        mTrackingItemList = gson.fromJson(json, type);

        if (mTrackingItemList == null) {
            mTrackingItemList = new ArrayList<>();
        }
    }

    /************************************************************
     * Purpose: Derive the magnitude of the hp and pts received as a result of performing the activity
     * Current:
     *      - Takes into account how frequently you wish to perform this activity
     *      - Take into account the current number of activities
     ***********************************************************  */
    private void createTrackingItem() {
        int numTimes = numberPicker.getValue();
        String frequency = spinner.getSelectedItem().toString();
        int numMenuItems = mTrackingItemList.size()+1;

        //Make Tracking Item Object
        TrackingItem newItem = new TrackingItem(title, frequency, numTimes);
        mTrackingItemList.add(newItem);

        //Update HP & lvl pts
        for(int i=0;i<mTrackingItemList.size();i++)
            mTrackingItemList.get(i).updatePoints(numMenuItems);
    }

    //Spinner Workaround for Text Format
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        ((TextView) parent.getChildAt(0)).setTextColor(getColor(R.color.bg_white));
        ((TextView) parent.getChildAt(0)).setTextSize(24);
        Typeface typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.spacemono_regular);
        ((TextView) parent.getChildAt(0)).setTypeface(typeface);
    }
    public void onNothingSelected(AdapterView<?> parent) {};
}
