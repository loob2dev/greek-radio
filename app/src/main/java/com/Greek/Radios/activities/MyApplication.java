package com.Greek.Radios.activities;

import android.app.Application;

import com.google.firebase.analytics.FirebaseAnalytics;


public class MyApplication extends Application {

    private static FirebaseAnalytics mFirebaseAnalytics;
    @Override
    public void onCreate() {
        super.onCreate();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    public static FirebaseAnalytics getFirebaseAnalytics() {
        return mFirebaseAnalytics;
    }
}