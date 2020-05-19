package com.example.lilconsistentme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;

public class LeftActivity extends Activity implements View.OnTouchListener {
    float x1, x2, y1, y2;
    Vibrator vibrator;
    private final int PIXEL_MARGIN =50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_left);
        //Vibrator
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        findViewById(R.id.guide).setOnTouchListener(this);
        findViewById(R.id.layout_leftActivity).setOnTouchListener(this);
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
                if (x1 > x2+PIXEL_MARGIN) {
                    onBackPressed();
                    overridePendingTransition(R.anim.slideright_in, R.anim.slideright_out);
                }
                else{
                    switch (v.getId()){
                        case R.id.guide:
                            Intent intent = new Intent(getApplicationContext(),GuideActivity.class);
                            startActivity(intent);
                            break;
                    }
                }
                break;
        }
        return true;
    }
}
