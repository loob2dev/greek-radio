package com.Greek.Radios.fragments;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import com.Greek.Radios.R;
import com.Greek.Radios.activities.MainActivity;
import com.Greek.Radios.services.AlarmReceiver;
import com.Greek.Radios.tab.FragmentTabLayout;

import java.util.Calendar;

import static android.content.Context.MODE_PRIVATE;


public class FragmentSleepMode extends Fragment {

    WebView webview;
    SwipeRefreshLayout mSwipeRefreshLayout = null;
    CoordinatorLayout coordinatorLayout;
    TextView txt_Minute,alarmInfo;
    Button yes,no,cancel;
    AlarmManager alarmMgr;
    PendingIntent alarmIntent;
    int minute,minuteOfDay;
    int hour,mhourOfDay;
    LinearLayout LinearLayot_Cancel;
    SharedPreferences preferenceManager;
    SharedPreferences.Editor editor;
    TimePicker timePicker;
    private Toolbar toolbar;
    private MainActivity mainActivity;
    private final static String COLLAPSING_TOOLBAR_FRAGMENT_TAG = "collapsing_toolbar";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_sleep_mode, container, false);


        setHasOptionsMenu(true);

        toolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        setupToolbar();

        timePicker = (TimePicker)rootView.findViewById(R.id.timePicker);
        alarmMgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        alarmInfo= (TextView) rootView.findViewById(R.id.alarmInfo);
       // txt_Minute = (TextView)rootView.findViewById(R.id.activity_sleep_minute);
        yes = (Button)rootView.findViewById(R.id.activity_sleep_yes);
        no = (Button)rootView.findViewById(R.id.activity_sleep_no);
        cancel= (Button)rootView.findViewById(R.id.activity_sleep_cancel);
        LinearLayot_Cancel = (LinearLayout) rootView.findViewById(R.id.activity_sleep_linear_cancel);
        timePicker.setIs24HourView(true);

        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hourOfDay, int min) {
                Calendar datetime = Calendar.getInstance();
                Calendar c = Calendar.getInstance();
                datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                datetime.set(Calendar.MINUTE, min);
                mhourOfDay = hourOfDay;
                minuteOfDay= min;
                minute = min - c.get(Calendar.MINUTE);
                hour = hourOfDay - c.get(Calendar.HOUR_OF_DAY);
                if (hour<0)
                {
                    hour = hour +24;
                }

                if (hour==0 && minute<0)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        timePicker.setMinute(c.get(Calendar.MINUTE)+1);
                    }
                    else
                    {
                        timePicker.setCurrentMinute(c.get(Calendar.MINUTE)+1);
                    }
                }

                if (hour!=0 && minute<0)
                {
                    hour--;
                    minute = minute+60;
                }
                Log.i("SleepMode", "Hour:"+ String.valueOf(hour));
                Log.i("SleepMode", "Minute:"+ String.valueOf(minute));

        //         txt_Minute.setText( getString(R.string.sleep_mode_text_close_time) + " " + hour + " " + getString(R.string.sleep_mode_hour) + " " + minute + " " + getString(R.string.sleep_mode_minute));


            }
        });
/*
        minute=mPicker.getValue();
*/
     //   txt_Minute.setText( getString(R.string.sleep_mode_text_close_time) + " " + hour + " " + getString(R.string.sleep_mode_hour) + " " + minute + " " + getString(R.string.sleep_mode_minute));

        preferenceManager = getActivity().getSharedPreferences("Alarm",MODE_PRIVATE);
        editor = preferenceManager.edit();

        if(preferenceManager.getInt("minute",0) == 0)
        {
            LinearLayot_Cancel.setVisibility(View.GONE);
        }
        else
        {
            int min = preferenceManager.getInt("minute",0);
            int hour = preferenceManager.getInt("hour",0);

             Calendar c = Calendar.getInstance();


            min = min - c.get(Calendar.MINUTE)  ;
            hour =hour - c.get(Calendar.HOUR_OF_DAY)   ;
            if (hour<0)
            {
                hour = hour +24;
            }

            if (hour==0 && min<0)
            {

            }

            if (hour!=0 && min<0)
            {
                hour--;
                min = min+60;
            }
            alarmInfo.setText(getString(R.string.sleep_mode_remaning) + " " + getString(R.string.sleep_mode_hour) + ": "+hour
                    + " " + getString(R.string.sleep_mode_min)+ " " +min);
        }

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getApplicationContext(), AlarmReceiver.class);
                alarmIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                Calendar c = Calendar.getInstance();
                c.set(Calendar.SECOND,0);

                alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + +
                                60 * 1000* (minute + (hour*60)), alarmIntent);
                editor.putInt("minute",minuteOfDay);
                editor.putInt("hour",mhourOfDay);
                editor.commit();
                Toast.makeText(getActivity(),getString(R.string.sleep_mode_text_close_time) + " " + hour + " " + getString(R.string.sleep_mode_hour) + " " + minute + " " + getString(R.string.sleep_mode_minute), Toast.LENGTH_SHORT).show();

            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mainActivity.sleepMode !=null)
                {
                    mainActivity.sleepMode.setChecked(false);
                }
               mainActivity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new FragmentTabLayout(), COLLAPSING_TOOLBAR_FRAGMENT_TAG).commit();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity().getApplicationContext(), AlarmReceiver.class);

                alarmIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmMgr.cancel(alarmIntent);
                editor.putInt("minute",0);
                editor.commit();
                LinearLayot_Cancel.setVisibility(View.GONE);

            }
        });


        return rootView;
    }
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mainActivity = (MainActivity) activity;
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mainActivity.setupNavigationDrawer(toolbar);
    }
    private void setupToolbar() {
        toolbar.setTitle(getString(R.string.app_name));
        toolbar.setSubtitle(getString(R.string.drawer_sleep));
        mainActivity.setSupportActionBar(toolbar);
    }


}