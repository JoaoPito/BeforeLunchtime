package com.joaopito.beforelunchtime;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TimerHelper {

    private static final String TAG = "MainActivity";
    private static final int defaultHour = 12;
    private static final int defaultMin = 30;

    Calendar lunchTime;
    int remainingTime;

    String lunchTimePrefKey = "LunchTime";

    public TimerHelper(){
        StartTimer();
    }

    void StartTimer(){
        Log.d(TAG,"Start");
        lunchTime = LoadLunchTime();

        Update();
    }

    public void Update(){
        int remainingHours = lunchTime.get(Calendar.HOUR) - Calendar.getInstance().get(Calendar.HOUR);
        remainingTime = (lunchTime.get(Calendar.MINUTE) - Calendar.getInstance().get(Calendar.MINUTE)) + (remainingHours*60);
    }

    public void ChangeLunchTime(int hour, int minute){
        Calendar newTime = Calendar.getInstance();
        newTime.set(Calendar.HOUR, hour);
        newTime.set(Calendar.MINUTE, minute);

        ChangeLunchTime(newTime);
    }

    public void ChangeLunchTime(Calendar newValue){

        /*int hours = newValue.get();

        Context context = getActivity();
        SharedPreferences prefs = context.getPreferences(Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(lunchTimePrefKey, newValue);
        editor.commit();*/

        lunchTime = newValue;
    }

    public Calendar LoadLunchTime(){
        //Placeholder
        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR, defaultHour);
        time.set(Calendar.MINUTE, defaultMin);
        time.set(Calendar.SECOND, 0);

        return time;
    }

    public boolean PastLunchTime(){
        return (remainingTime<=0);
    }

    public int GetRemainingHours(){
        return Math.abs(Math.round(remainingTime/60));
    }

    public int GetRemainingMin(){
        return Math.abs(Math.round(remainingTime%60));
    }

    public String GetRemainingString(){
        return String.format(Locale.getDefault(),"%02d:%02d", GetRemainingHours(), GetRemainingMin());
    }
}
