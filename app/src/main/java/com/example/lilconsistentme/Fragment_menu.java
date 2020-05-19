package com.example.lilconsistentme;


import android.animation.Animator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;

import static android.content.Context.MODE_PRIVATE;

public class Fragment_menu extends Fragment implements View.OnLongClickListener, View.OnTouchListener,View.OnLayoutChangeListener {
    private final int MAX_NUM_ITEMS = 6;
    public static ArrayList<TrackingItem> mTrackingItemList;
    private LinearLayout[] item = new LinearLayout[MAX_NUM_ITEMS];
    private LinearLayout[] activityLayout = new LinearLayout[MAX_NUM_ITEMS];
    private TextView[] textView = new TextView[MAX_NUM_ITEMS];
    private TextView[] text_hp = new TextView[MAX_NUM_ITEMS];
    private TextView[] text_pts = new TextView[MAX_NUM_ITEMS];
    private float x1,x2,y1,y2;
    private Vibrator vibrator;
    private LinearLayout addActivity;
    private boolean isLongPressed = false;
    private final int PIXEL_MARGIN =50;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.menu_fragment, container, false);

        //Animation
        container.addOnLayoutChangeListener(this);

        //Vibrator
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);

        //Load List
        loadList();

        //Initialize Views
        int textViewID, hpID, ptsID, itemID,activityLayoutID;
        addActivity=v.findViewById(R.id.button_addactivity);
        addActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), DefineActivity.class);
                intent.putExtra("Position", mTrackingItemList.size() + 1);
                startActivity(intent);
            }
        });
        String packageName = "com.example.lilconsistentme";

        for (int i = 0; i < MAX_NUM_ITEMS; i++) {
            //Get IDs
            textViewID = getResources().getIdentifier("textView" + (i + 1), "id", packageName);
            hpID = getResources().getIdentifier("text_hp" + (i + 1), "id", packageName);
            ptsID = getResources().getIdentifier("text_pts" + (i + 1), "id", packageName);
            itemID = getResources().getIdentifier("item" + (i + 1), "id", packageName);
            activityLayoutID = getResources().getIdentifier("itemLayout" + (i + 1), "id", packageName);

            //Find Views
            textView[i] = v.findViewById(textViewID);
            text_hp[i] = v.findViewById(hpID);
            text_pts[i] = v.findViewById(ptsID);
            item[i] = v.findViewById(itemID);
            activityLayout[i]=v.findViewById(activityLayoutID);

            //Set OnClickListeners
            item[i].setOnTouchListener(this);
            item[i].setOnLongClickListener(this);
        }
        return v;
    }
    private void saveList(){
        Gson gson = new Gson();
        String json = gson.toJson(mTrackingItemList);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("TrackingItemList",json).apply();
    }

    private void loadList(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("prefs", MODE_PRIVATE);
        String json = sharedPreferences.getString("TrackingItemList",null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<TrackingItem>>() {}.getType();
        mTrackingItemList=gson.fromJson(json,type);

        if(mTrackingItemList== null){
            mTrackingItemList= new ArrayList<>();
        }
    }

    private void updateVisibility() {
        //items
        for(int i=0;i<MAX_NUM_ITEMS; i++) {
            if (i < mTrackingItemList.size()) {
                activityLayout[i].setVisibility(View.VISIBLE);
                textView[i].setText(mTrackingItemList.get(i).getItemName());
                text_hp[i].setText(mTrackingItemList.get(i).getHp()/(60*60*1000) + "hp");
                text_pts[i].setText(mTrackingItemList.get(i).getPts() + "pts");
            }
            else
                activityLayout[i].setVisibility(View.GONE);
        }

        //add item button
        if(mTrackingItemList.size()==MAX_NUM_ITEMS)
            addActivity.setVisibility(View.GONE);
        else
            addActivity.setVisibility(View.VISIBLE);
    }

    private void updateAddedPts(){
        int hp=0;
        int pts=0;
        boolean itemSelected = false;

//      ADD POINTS
        for (int i = 0; i < mTrackingItemList.size(); i++)
            if (mTrackingItemList.get(i).isSelected()) {
                itemSelected = true;
                hp += mTrackingItemList.get(i).getHp();
                pts += mTrackingItemList.get(i).getPts();
            }
        MainActivity.timeAdded = hp;
        MainActivity.ptsAdded = pts;

//      UPDATE DIALOG
        if (itemSelected)
            ((TextView)getActivity().findViewById(R.id.text_dialogue)).setText(R.string.character_feed);
        else if(MainActivity.menuOpen)
            ((TextView)getActivity().findViewById(R.id.text_dialogue)).setText(R.string.character_selectActivity);
    }

    public void shortPress(View view) {
        vibrator.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE));
        int pos = getItemPosition(view);

        if(pos!=-1) {
            if (!mTrackingItemList.get(pos - 1).isSelected()) {
                item[pos - 1].setBackgroundResource(R.drawable.bg_item_selected);
                mTrackingItemList.get(pos - 1).setSelected(true);
                mTrackingItemList.get(pos - 1).addDateEntry();
            } else {// De-Select Item
                item[pos - 1].setBackgroundResource(R.drawable.bg_item);
                mTrackingItemList.get(pos - 1).setSelected(false);
                mTrackingItemList.get(pos - 1).removePrevDateEntry();
            }
        }
        updateAddedPts();
    }

    private int getItemPosition(View view){
        int pos=-1;
        switch (view.getId()) {
            case R.id.item1:
                pos=1;
                break;
            case R.id.item2:
                pos=2;
                break;
            case R.id.item3:
                pos=3;
                break;
            case R.id.item4:
                pos=4;
                break;
            case R.id.item5:
                pos=5;
                break;
            case R.id.item6:
                pos=6;
                break;
        }
        return pos;
    }

    @Override
    public boolean onLongClick(View view) {
        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        int pos= getItemPosition(view);

        item[pos - 1].setBackgroundResource(R.drawable.bg_item_delete);
        showDeleteDialog(pos);

        updateAddedPts();
        isLongPressed=true;
        return true;
    }

    private void showDeleteDialog(final int pos) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme);
        View view = LayoutInflater.from(getActivity()).inflate(
                R.layout.delete_dialog, (ConstraintLayout) getActivity().findViewById(R.id.deleteDialogContainer)
        );
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();
        view.findViewById(R.id.layout_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //DELETE ITEM
                mTrackingItemList.remove(pos-1);
                item[pos - 1].setBackgroundResource(R.drawable.bg_item);

                //Update HP & lvl pts
                for(int i=0;i<mTrackingItemList.size();i++)
                    mTrackingItemList.get(i).updatePoints(mTrackingItemList.size());

                saveList();

                //Update UI
                updateAddedPts();
                updateVisibility();
                alertDialog.dismiss();
            }
        });

        view.findViewById(R.id.layout_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //CANCEL
                mTrackingItemList.get(pos-1).setSelected(false);
                item[pos - 1].setBackgroundResource(R.drawable.bg_item);
                alertDialog.dismiss();
            }
        });

        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        alertDialog.show();
    }

    @Override
    public void onLayoutChange(View v, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
        circleAnim(v);
        v.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadList();
        updateVisibility();
        updateAddedPts();
    }

    private void circleAnim(View v) {
        //Parameters
        int x = v.getWidth() / 2;
        int y = v.getHeight() / 2;
        int startRadius = 0;
        int endRadius = (int) Math.hypot(x, y);

        //Animation
        Animator anim = ViewAnimationUtils.createCircularReveal(v, x, y, startRadius, endRadius);
        anim.setDuration(250).start();
    }
    public boolean onTouch(View view, MotionEvent touchEvent) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (touchEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                y1 = touchEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                y2 = touchEvent.getY();
                if(!isLongPressed) {
                    if (x1 + PIXEL_MARGIN < x2) {
                        Intent intent = new Intent(getActivity(), LeftActivity.class);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slideleft_in, R.anim.slideleft_out);
                        editor.putBoolean("todo_dashboard", false).apply();
                    } else if (x1 > x2 + PIXEL_MARGIN) {
                        //enable access to data visualization after the other to dos have been complete
                        if (!sharedPreferences.getBoolean("todo_feed", true)) {
                            Intent intent = new Intent(getActivity(), RightActivity.class);
                            startActivity(intent);
                            getActivity().overridePendingTransition(R.anim.slideright_in, R.anim.slideright_out);
                            editor.putBoolean("todo_progress", false).apply();
                        }
                    } else
                        shortPress(view);
                }
                else
                    isLongPressed=false;
                break;
        }
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        //de-Select Items
        for (int i = 0; i < mTrackingItemList.size(); i++) {
            mTrackingItemList.get(i).setSelected(false);
            item[i].setBackgroundResource(R.drawable.bg_item);
        }
        MainActivity.ptsAdded=0;
        MainActivity.timeAdded=0;
    }
}

