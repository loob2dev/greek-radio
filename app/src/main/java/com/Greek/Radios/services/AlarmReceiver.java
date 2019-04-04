package com.Greek.Radios.services;

/**
 * Created by Aykut on 18.08.2016.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;


public class AlarmReceiver extends BroadcastReceiver
{
    SharedPreferences preferenceManager;
    SharedPreferences.Editor editor;
    @Override
    public void onReceive(Context context, Intent intent)
    {

        preferenceManager = context.getSharedPreferences("Alarm",MODE_PRIVATE);
        editor = preferenceManager.edit();
        editor.putInt("minute",0);
        editor.commit();

        context.stopService(new Intent(context,RadioPlayerService.class));



    }
}