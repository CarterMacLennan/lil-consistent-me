package com.example.lilconsistentme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;

public class DefineActivity extends Activity {
    ConstraintLayout contentView;
    ImageButton saveButton;
    TextView save;
    EditText editText;
    boolean isFirstTimeGetFocused = true;
    int pos;
    float x1,x2,y1,y2;
    boolean updated = false,isKeyboardShowing;
    Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_define);

        //Override Entrance Transition
        overridePendingTransition(R.anim.slideup_in, R.anim.slideup_out);

        //Vibrator
        vibrator= (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //Get Extras
        pos = getIntent().getIntExtra("Position", 0);

        //Initialize Views
        contentView = findViewById(R.id.constraintLayout);
        saveButton = findViewById(R.id.button_save);
        save = findViewById(R.id.text_save);
        editText = findViewById(R.id.editText);

        //Update EditText
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && isFirstTimeGetFocused) {
                    editText.setText("");
                    editText.setTextColor(Color.parseColor("#ffffff"));
                    isFirstTimeGetFocused = false;
                }
            }
        });

        //Monitor Keyboard
        contentView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        Rect r = new Rect();
                        contentView.getWindowVisibleDisplayFrame(r);
                        int screenHeight = contentView.getRootView().getHeight();
                        int keypadHeight = screenHeight - r.bottom;

                        if (keypadHeight > screenHeight * 0.15) {
                            if (!isKeyboardShowing) {// keyboard is open
                                isKeyboardShowing = true;
                            }
                        }
                        else {// keyboard is closed
                            if (isKeyboardShowing) {
                                isKeyboardShowing = false;
                                if(!editText.getText().toString().equals("")){
                                    updated = true;
                                    saveButton.setBackgroundResource(R.drawable.bg_save_updated);
                                    save.setTextColor(Color.parseColor("#ffffff"));
                                }
                                else{
                                    updated = false;
                                    saveButton.setBackgroundResource(R.drawable.bg_save);
                                    save.setTextColor(Color.parseColor("#40000000"));
                                }
                            }
                        }
                    }

                });

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
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Save Activity Name
                if (updated) {
                    vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE));

                    //Start Frequency Activity
                    Intent intent = new Intent(getApplicationContext(), FrequencyActivity.class);
                    intent.putExtra("ActivityName",editText.getText().toString());
                    intent.putExtra("pos",pos);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slideup_in, R.anim.slideup_out);
                }

            }
        });
    }
}
