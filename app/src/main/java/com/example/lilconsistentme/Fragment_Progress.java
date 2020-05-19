package com.example.lilconsistentme;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import static android.content.Context.MODE_PRIVATE;

public class Fragment_Progress extends Fragment implements View.OnClickListener {
    private PieChart pieChart;
    private ArrayList<TrackingItem> mTrackingItemList;
    private int numToBePerformed=0;
    private int [] colors;
    static int position=0;
    Float x1,x2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment__progress, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Setup Views
        ImageButton leftButton = getActivity().findViewById(R.id.arrow_left);
        ImageButton rightButton = getActivity().findViewById(R.id.arrow_right);
        leftButton.setOnClickListener(this);
        rightButton.setOnClickListener(this);
        loadList();

        //Setup Piechart
        pieChart= getActivity().findViewById(R.id.piechart);
        if(!mTrackingItemList.isEmpty()) {
            setupPieChart(getTrackingData(position));
            ((TextView) getActivity().findViewById(R.id.currentTrackingItemName)).setText(mTrackingItemList.get(position).getItemName());
            ((TextView)getActivity().findViewById(R.id.currentTrackingItemName)).setText(mTrackingItemList.get(position).getItemName());
        }
        else{
            leftButton.setVisibility(View.INVISIBLE);
            rightButton.setVisibility(View.INVISIBLE);
            pieChart.setVisibility(View.INVISIBLE);
            view.findViewById(R.id.addActivityMessage_Tab1).setVisibility(View.VISIBLE);
        }
        pieChart.setOnTouchListener(null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.arrow_left:
                if(position==0)
                    position=mTrackingItemList.size()-1;
                else
                    position--;
                break;
            case R.id.arrow_right:
                if(position==mTrackingItemList.size()-1)
                    position=0;
                else
                    position++;
                break;
        }

        //Setup PieChart
        setupPieChart(getTrackingData(position));
    }

    private ArrayList<PieEntry> getTrackingData(int itemNum){
        //Get List Data
        int numPerformed = mTrackingItemList.get(itemNum).calculateCurrentProgress();
        int numTotal = mTrackingItemList.get(itemNum).getNumTimes();
        numToBePerformed = (Math.max(numTotal - numPerformed, 0));

        //Add Data
        ArrayList<PieEntry> trackingData = new ArrayList<>();
        if(numPerformed != 0) {
            trackingData.add(new PieEntry(numPerformed, "Times performed"));
            colors = new int[]{ R.color.yeti_blue, R.color.yeti_white};
        }
        else
            colors = new int []{ R.color.yeti_white};
        if(numToBePerformed != 0) {
            trackingData.add(new PieEntry(numToBePerformed, "To be peformed"));
        }
        return trackingData;
    }

    private void setupPieChart(ArrayList<PieEntry> trackingData){
        //Legend
        pieChart.getLegend().setTextSize(14);
        pieChart.getLegend().setTextColor(Color.WHITE);
        pieChart.getLegend().setTypeface(ResourcesCompat.getFont(getContext(), R.font.spacemono_regular));
        pieChart.getLegend().setXEntrySpace(8);
        pieChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        pieChart.getLegend().setWordWrapEnabled(true);
        pieChart.getDescription().setEnabled(false);
        ((TextView) getActivity().findViewById(R.id.currentTrackingItemName)).setText(mTrackingItemList.get(position).getItemName());
        ((TextView)getActivity().findViewById(R.id.frequency_piechart)).setText("Measured in "+ mTrackingItemList.get(position).getFrequency()+"s");


        //Center
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(R.color.bg_purple);
        pieChart.setTransparentCircleRadius(60);

        //Animations
        pieChart.animateY(1000, Easing.EaseInOutCirc);
        pieChart.setDragDecelerationFrictionCoef(0.9f);
        //Chart Specifications
        PieDataSet dataSet = new PieDataSet(trackingData,"");
        dataSet.setSliceSpace(3);
        dataSet.setSelectionShift(5);
        if(numToBePerformed==0)
            dataSet.setColors(new int[] { R.color.bg_green},getContext());
        else
            dataSet.setColors(colors,getContext());
        pieChart.setDrawSliceText(false);

        //Text Specifications
        PieData data = new PieData((dataSet));
        data.setValueTextSize(16);
        data.setValueTextColor(Color.BLACK);
        data.setValueFormatter(new DefaultValueFormatter(0));
        data.setValueTypeface(ResourcesCompat.getFont(getContext(), R.font.spacemono_bold));
        pieChart.setData(data);

        //Add Description
        if (mTrackingItemList.get(position).getFrequency().equals("Day"))
            ((TextView)getActivity().findViewById(R.id.frequency_piechart)).setText("Today");
        else
            ((TextView)getActivity().findViewById(R.id.frequency_piechart)).setText("This Week");
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
}
