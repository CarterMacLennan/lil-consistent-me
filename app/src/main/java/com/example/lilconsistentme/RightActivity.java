package com.example.lilconsistentme;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTabHost;

public class RightActivity extends FragmentActivity implements View.OnTouchListener {
    Float x1,x2;
    private final int PIXEL_MARGIN =50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_right);

        //Setup TabHost & specify the view the fragment will occupy
        FragmentTabHost fragmentTabHost = findViewById(android.R.id.tabhost);
        fragmentTabHost.setup(this, getSupportFragmentManager(), android.R.id.tabcontent);

        //Add Tab1: Progress
        TabHost.TabSpec tabSpec1= fragmentTabHost.newTabSpec("Tab1").setIndicator("Current");
        fragmentTabHost.addTab(tabSpec1, Fragment_Progress.class, null);

        //Add Tab2: Achievements
        TabHost.TabSpec tabSpec2= fragmentTabHost.newTabSpec("Tab2").setIndicator("Goals");
        fragmentTabHost.addTab(tabSpec2, Fragment_Achievements.class, null);

        //TabSpec Font Workaround
        setTabsFont(fragmentTabHost);

        //OnTouchListeners
        findViewById(android.R.id.tabhost).setOnTouchListener(this);
    }

    // Workaround for altering tabSpecs font found here: https://stackoverflow.com/questions/22858381/change-text-color-and-selector-in-tabwidget
    private void setTabsFont(FragmentTabHost fragmentTabHost){
        final TabWidget tw = fragmentTabHost.findViewById(android.R.id.tabs);
        Typeface typeface = ResourcesCompat.getFont(getApplicationContext(), R.font.spacemono_regular);

        for (int i = 0; i < tw.getChildCount(); ++i){
            final View tabView = tw.getChildTabViewAt(i);
            final TextView tabText = tabView.findViewById(android.R.id.title);
            tabText.setTypeface(typeface);
            tabText.setTextSize(18);
            tabText.setTextColor(getColor(R.color.yeti_white));
            fragmentTabHost.getTabWidget().getChildTabViewAt(i).getBackground().setColorFilter(getColor(R.color.selected_peach), PorterDuff.Mode.MULTIPLY);
        }
    }

    public boolean onTouch(View view,MotionEvent touchEvent) {
        switch (touchEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                if (x1+PIXEL_MARGIN < x2) {
                    finish();
                    overridePendingTransition(R.anim.slideleft_in, R.anim.slideleft_out);
                }
                break;
        }
        return true;
    }

}