package com.example.lilconsistentme;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.DefaultValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.example.lilconsistentme.Fragment_Progress.position;

public class Fragment_Achievements extends Fragment implements View.OnClickListener{
    private  ArrayList<TrackingItem> mTrackingItemList;
    private HorizontalBarChart horizontalBarChart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment__achievements, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadList();

        //Setup buttons
        ImageButton leftButton = getActivity().findViewById(R.id.arrow_left);
        ImageButton rightButton = getActivity().findViewById(R.id.arrow_right);
        leftButton.setOnClickListener(this);
        rightButton.setOnClickListener(this);

        //Setup barchart
        horizontalBarChart=view.findViewById(R.id.barchart);
        horizontalBarChart.setOnTouchListener(null);
        if(!mTrackingItemList.isEmpty()) {
            setupBarChart(position);
            ((TextView) getActivity().findViewById(R.id.currentTrackingItemName)).setText(mTrackingItemList.get(position).getItemName());
            ((TextView)getActivity().findViewById(R.id.frequency_barchart)).setText("Measured in "+ mTrackingItemList.get(position).getFrequency()+"s");
        }
        else{
            leftButton.setVisibility(View.INVISIBLE);
            rightButton.setVisibility(View.INVISIBLE);
            horizontalBarChart.setVisibility(View.INVISIBLE);
            view.findViewById(R.id.addActivityMessage_Tab2).setVisibility(View.VISIBLE);
        }
    }


    private void setupBarChart(int pos){
        TrackingItem item = mTrackingItemList.get(pos);
        int numGoalReached=item.calculateDaysConsistent();

        //CURRENT CONSISTENCY
        ArrayList<BarEntry> trackingData = new ArrayList<>();
        if(numGoalReached==0)
            trackingData.add(new BarEntry(30f,0.02f));
        else
            trackingData.add(new BarEntry(30f,numGoalReached));
        //HIGHSCORE
        if(item.getCurrentRecord()==0)
            trackingData.add(new BarEntry(20f,0.02f));
        else
            trackingData.add(new BarEntry(20f,item.getCurrentRecord()));
        //GOAL
        String temp;
        if (item.getNextGoal() != 0)
            trackingData.add(new BarEntry(10f,item.getNextGoal()));

        //DataSet
        BarDataSet dataSet = new BarDataSet(trackingData,"");
        dataSet.setBarBorderWidth(0.9f);
        dataSet.setColors(new int[] { R.color.yeti_pink, R.color.yeti_yellow_light,R.color.bg_green},getContext());
        dataSet.setValueTextSize(14);
        dataSet.setValueTypeface(ResourcesCompat.getFont(getContext(), R.font.spacemono_bold));
        dataSet.setValueFormatter(new DefaultValueFormatter(0));

        //Set Data
        BarData data = new BarData(dataSet);
        data.setBarWidth(9f);
        horizontalBarChart.setData(data);

        //Bar Chart Specifications
        horizontalBarChart.setFitBars(true);
        horizontalBarChart.animateXY(500,500);
        YAxis yAxis = horizontalBarChart.getAxisLeft();
        yAxis.setAxisMinValue(0);
        yAxis.setEnabled(false);
        horizontalBarChart.getAxisRight().setEnabled(false);
        XAxis xAxis = horizontalBarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinValue(0);
        xAxis.setEnabled(false);
        horizontalBarChart.setDrawValueAboveBar(false);

        //Legend Specifications
        Legend legend= horizontalBarChart.getLegend();
        legend.setTextSize(14);
        legend.setTextColor(Color.WHITE);
        legend.setTypeface(ResourcesCompat.getFont(getContext(), R.font.spacemono_regular));
        legend.setXEntrySpace(16);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setWordWrapEnabled(true);
        horizontalBarChart.getDescription().setEnabled(false);

        //Legend Workaround
        LegendEntry legendEntryA = new LegendEntry();
        legendEntryA.label = "Current streak";
        legendEntryA.formColor = getContext().getColor(R.color.yeti_pink);
        LegendEntry legendEntryB = new LegendEntry();
        legendEntryB.label = "Longest streak";
        legendEntryB.formColor = getContext().getColor(R.color.yeti_yellow_light);
        LegendEntry legendEntryC = new LegendEntry();
        legendEntryC.label = "Your next goal";
        legendEntryC.formColor = getContext().getColor(R.color.bg_green);
        legend.setCustom(Arrays.asList(legendEntryA, legendEntryB, legendEntryC));

        ((TextView)getActivity().findViewById(R.id.currentTrackingItemName)).setText(mTrackingItemList.get(position).getItemName());
        ((TextView)getActivity().findViewById(R.id.frequency_barchart)).setText("Consistency Measured in "+ mTrackingItemList.get(position).getFrequency()+"s");
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
        setupBarChart(position);
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
