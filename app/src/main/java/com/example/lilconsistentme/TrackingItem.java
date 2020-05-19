package com.example.lilconsistentme;

import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class TrackingItem {

    //Attributes
    final long WEEK_IN_MILLIS = 7 * 24 * 60 * 60 * 1000;
    private String itemName;
    private String frequency;
    private int numTimes;
    private ArrayList<Date> history;
    private long hp;
    private long pts;
    private boolean selected = false;
    private int numGoalReached = 0;
    private int currentRecord = 0;
    private ArrayList<Integer> nextGoal;

    //Constructor
    public TrackingItem(String itemName, String frequency, int numTimes) {
        this.itemName = itemName;
        this.frequency = frequency;
        this.numTimes = numTimes;
        this.history = new ArrayList<>();
        deriveGoals();
    }

    public void updatePoints(int numMenuItems) {
        double howOften =(frequency.equals("Day")? 1f : 7f)/numTimes;

        //Derive hp
        this.hp = (long)((WEEK_IN_MILLIS / numMenuItems) * howOften);
        //Derive lvl pts
        this.pts = (long)((300 / numMenuItems) * howOften);
    }

    private void deriveGoals(){
        if(this.frequency.equals("Day"))
            this.nextGoal= new ArrayList<>(Arrays.asList(1,3,7,14,30,60,90,180,270,365));
        else //Week
            this.nextGoal= new ArrayList<>(Arrays.asList(1,2,4,8,12,16,20,35,52));
    }

    public int calculateCurrentProgress(){
        //Current Date Information
        Calendar currentCalendar = Calendar.getInstance();
        int currentYear = currentCalendar.get(Calendar.YEAR);
        int currentWeek = currentCalendar.get(Calendar.WEEK_OF_YEAR);
        int currentDay = currentCalendar.get(Calendar.DAY_OF_YEAR);
        int counter=0;

        for (int i=0; i<history.size(); i++){
            //Past Date Information
            Calendar historyCalendar = Calendar.getInstance();
            historyCalendar.setTime(history.get(i));
            int historyYear = historyCalendar.get(Calendar.YEAR);
            int historyWeek = historyCalendar.get(Calendar.WEEK_OF_YEAR);
            int historyDay = historyCalendar.get(Calendar.DAY_OF_YEAR);
            Log.i(TAG, "day of year: " +historyDay);

            if(frequency.equals("Day")){
                if(currentDay == historyDay && currentWeek == historyWeek && currentYear == historyYear)
                    counter++;
            }
            else{
                if(currentWeek == historyWeek && currentYear == historyYear)
                    counter++;
            }
        }
        return counter;
    }

    public int calculateDaysConsistent() {
        //Local Variables
        Date startingDate = null;
        boolean goalReached = false;
        int counter = 0,timesReached = 0;
        int currentPeriod=0,startingPeriod=0;
        Calendar historyCalendar = Calendar.getInstance();

        //Calculate the number of times the goal was reached
        for(int i=0; i < history.size();i++){
            //If first loop
            if (startingDate == null) {
                startingDate = history.get(i);
                counter++;
            }
            //Get Starting Data
            historyCalendar.setTime(startingDate);
            startingPeriod = ((frequency.equals("Day"))? historyCalendar.get(Calendar.DAY_OF_YEAR): historyCalendar.get(Calendar.WEEK_OF_YEAR));

            //Get Current Iteration Data
            historyCalendar.setTime(history.get(i));
            currentPeriod = ((frequency.equals("Day"))? historyCalendar.get(Calendar.DAY_OF_YEAR): historyCalendar.get(Calendar.WEEK_OF_YEAR));

            if(currentPeriod> startingPeriod+1) {
                timesReached = 0;
            }
            if (currentPeriod > startingPeriod) {
                startingDate = history.get(i);
                counter = 1;
                goalReached = false;
            }
            if (counter == numTimes && !goalReached) {
                timesReached++;
                goalReached = true;
            }
            else
                counter++;
        }
        //Get Today's Data
        historyCalendar.setTime(new Date());
        int thisPeriod = ((frequency.equals("Day"))? historyCalendar.get(Calendar.DAY_OF_YEAR): historyCalendar.get(Calendar.WEEK_OF_YEAR));

        //See if the current time has passed the next interval
        if(!history.isEmpty() && thisPeriod <=startingPeriod+1)
            numGoalReached = timesReached;
        else
            numGoalReached=0;

        //Update Current Record
        if(numGoalReached > getCurrentRecord())
            setCurrentRecord(numGoalReached);

        //Update NextGoal
        if(!nextGoal.isEmpty() && numGoalReached==nextGoal.get(0))
            nextGoal.remove(0);

        return numGoalReached;
    }

    public void addDateEntry() {
        this.history.add(new Date());
    }

    public void removePrevDateEntry() {
        history.remove(history.size() - 1);
    }

    //Getters & Setters
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public int getNumTimes() {
        return numTimes;
    }

    public void setNumTimes(int numTimes) {
        this.numTimes = numTimes;
    }

    public ArrayList<Date> getHistory() {
        return history;
    }

    public void setHistory(ArrayList<Date> history) {
        this.history = history;
    }

    public long getHp() {
        return hp;
    }

    public void setHp(long hp) {
        this.hp = hp;
    }

    public long getPts() {
        return pts;
    }

    public void setPts(long pts) {
        this.pts = pts;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getNumGoalReached() {
        return numGoalReached;
    }

    public void setNumGoalReached(int numGoalReached) {
        this.numGoalReached = numGoalReached;
    }

    public int getCurrentRecord() {
        return currentRecord;
    }

    public void setCurrentRecord(int currentRecord) {
        this.currentRecord = currentRecord;
    }

    public Integer getNextGoal() {
        if(nextGoal.isEmpty())
            return 0;
        else
            return nextGoal.get(0);
    }
}
