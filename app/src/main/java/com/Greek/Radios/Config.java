package com.Greek.Radios;

public class Config {

    //put your admin panel url
    public static final String ADMIN_PANEL_URL = "http://app.berdan.net/gr";

    //put your api key which obtained from admin panel
    public static final String API_KEY = "cda11lHY0ZafN2nrti4U5QAKMDhTw7Czm1xoSsyVLduvRegkqE";

    //Ads Configuration
    //set true to enable or set false to disable
    public static final boolean ENABLE_ADMOB_BANNER_ADS = true;
    public static final boolean ENABLE_ADMOB_INTERSTITIAL_ADS_ON_DRAWER_MENU = false;
    public static final boolean ENABLE_ADMOB_INTERSTITIAL_ADS_ON_CATEGORY = true;
    public static final int ADMOB_INTERSTITIAL_ADS_INTERVAL = 2;
    public static final int ADMOB_INTERSTITIAL_ADS_INTERVAL_FOR_RADIODETAIL = 5;


    public static final int ALBUM_REFRESH_PERIOD = 20000;
    //number of columns in a row category
    public static final int CATEGORY_COLUMN_COUNT = 2;

    //if you use RTL Language e.g : Arabic Language or other, set true
    public static final boolean ENABLE_RTL_MODE = false;

    //volume bar
    public static final boolean ENABLE_VOLUME_BAR = false;
    //set default volume to listening radio streaming, volume range 0 - 15
    public static final int DEFAULT_VOLUME = 10;

    //load more for the next radio list
    public static final int LOAD_MORE = 20;

    //social link configuration
    public static final boolean ENABLE_MENU_WEBSITE = true;
    public static final boolean ENABLE_MENU_FACEBOOK = true;
    public static final boolean ENABLE_MENU_TWITTER = true;
    public static final boolean ENABLE_MENU_INSTAGRAM = true;
    public static final boolean ENABLE_MENU_EMAIL = true;

}