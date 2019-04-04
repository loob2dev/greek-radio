package com.Greek.Radios.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.Greek.Radios.BuildConfig;
import com.Greek.Radios.Config;
import com.Greek.Radios.R;
import com.Greek.Radios.fragments.FragmentAbout;
import com.Greek.Radios.fragments.FragmentSleepMode;
import com.Greek.Radios.models.Radio;
import com.Greek.Radios.services.RadioPlayerService;
import com.Greek.Radios.tab.FragmentTabFavorite;
import com.Greek.Radios.tab.FragmentTabHome;
import com.Greek.Radios.utilities.GDPR;
import com.Greek.Radios.utilities.HttpTask;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.squareup.picasso.Picasso;
import com.stephentuso.welcome.WelcomeHelper;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    WelcomeHelper welcomeScreen;
    private final static String COLLAPSING_TOOLBAR_FRAGMENT_TAG = "collapsing_toolbar";
    private final static String SELECTED_TAG = "selected_index";
    private static int selectedIndex;
    private final static int COLLAPSING_TOOLBAR = 0;
    ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout drawerLayout;
    NavigationView navigationView;
    TextView txt_radio_name;
    ImageView img_logo;
    ImageButton btn_pause, btn_play;
    LinearLayout relativeLayout;
    private AdView adView;
    private InterstitialAd interstitialAd;
    View view;
    SeekBar seekbar;
    private ImageView img_volume;
    AudioManager audioManager;
    RelativeLayout lyt_volumeBar;
    SharedPreferences preferences;
    Context context;
    public MenuItem sleepMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = findViewById(android.R.id.content);

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        /* Ekleme Başlangıç -- @Aykut — caliskan.aa@gmail.com  */
        welcomeScreen = new WelcomeHelper(MainActivity.this,WelcomeActivity.class);
        welcomeScreen.show(savedInstanceState);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        int login =  preferences.getInt("login",0);
        if (login <2 )
        {
            login++;
            editor.putInt("login",login);
            editor.commit();
        }
        else if (login==2)
        {
            editor.putInt("login",3);
            editor.commit();
            android.app.AlertDialog alert = new android.app.AlertDialog.Builder(MainActivity.this)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            final String appName = getPackageName();
                            try {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName)));
                            } catch (android.content.ActivityNotFoundException anfe) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appName)));
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert).create();
            alert.setTitle(getString(R.string.rate_title));
            alert.setMessage(getString(R.string.rate_message));
            alert.show();
        }

        /* Ekleme Bitiş -- @Aykut — caliskan.aa@gmail.com  */
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        drawerLayout = findViewById(R.id.drawer_layout);
        lyt_volumeBar = findViewById(R.id.lyt_volumeBar);

        loadInterstitialAd();
        loadBannerAd();

        btn_pause = findViewById(R.id.main_pause);
        btn_play = findViewById(R.id.main_play);
        relativeLayout = findViewById(R.id.main_bar);

        if (savedInstanceState != null) {
            navigationView.getMenu().getItem(savedInstanceState.getInt(SELECTED_TAG)).setChecked(true);
            return;
        }

        selectedIndex = COLLAPSING_TOOLBAR;
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, new FragmentTabHome(), COLLAPSING_TOOLBAR_FRAGMENT_TAG)
                .commit();

        if (Config.ENABLE_VOLUME_BAR) {
            volumeBar();
        } else {
            lyt_volumeBar.setVisibility(View.GONE);
        }

        sendRegistrationIdToBackend();

        Intent intent = getIntent();
        final String link = intent.getStringExtra("link");

        if (link != null) {
            if (!link.equals("")) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(link));
                startActivity(i);
            }
        }

        GDPR.updateConsentStatus(this);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_TAG, selectedIndex);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.search:
                Intent intent = new Intent(getApplicationContext(), ActivitySearch.class);
                startActivity(intent);
                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.drawer_recent:
                if (!menuItem.isChecked()) {
                    menuItem.setChecked(true);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentTabHome(), COLLAPSING_TOOLBAR_FRAGMENT_TAG).commit();
                    showInterstitialAd();
                }
                drawerLayout.closeDrawer(GravityCompat.START);

                return true;

            case R.id.drawer_favorite:

                if (!menuItem.isChecked()) {
                    menuItem.setChecked(true);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FragmentTabFavorite(), COLLAPSING_TOOLBAR_FRAGMENT_TAG).commit();
                    showInterstitialAd();
                }
                drawerLayout.closeDrawer(GravityCompat.START);

                return true;

            case R.id.drawer_rate:

                final String appName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appName)));
                }
                showInterstitialAd();

                return false;

/*            case R.id.drawer_more:

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_more_apps))));
                showInterstitialAd();

                return true;*/

            case R.id.drawer_about:
                if (!menuItem.isChecked()) {
                    menuItem.setChecked(true);
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new FragmentAbout(), COLLAPSING_TOOLBAR_FRAGMENT_TAG).commit();

                    showInterstitialAd();
                }
                drawerLayout.closeDrawer(GravityCompat.START);

                return true;
            /* Ekleme Başlangıç -- @Aykut - caliskan.aa@gmail.com */

            case R.id.drawer_radio_add:
                if (!menuItem.isChecked()) {
                    Intent i = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(getResources().getString(R.string.radio_add)));
                    startActivity(i);
                    showInterstitialAd();
                }
                drawerLayout.closeDrawer(GravityCompat.START);

                return false;
            case R.id.drawer_sleep_mode:
                if (!menuItem.isChecked()) {
                    menuItem.setChecked(true);
                    sleepMode=menuItem;
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new FragmentSleepMode(), COLLAPSING_TOOLBAR_FRAGMENT_TAG)
                            .commit();

                    showInterstitialAd();
                }
                drawerLayout.closeDrawer(GravityCompat.START);

                return true;


            case R.id.drawer_exit:
                exitDialog();
                return false;
            /* Ekleme Bitiş -- @Aykut caliskan.aa@gmail.com */
        }
        return false;
    }

    public void setupNavigationDrawer(Toolbar toolbar) {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    public void volumeBar() {

        seekbar = findViewById(R.id.seekBar1);

        lyt_volumeBar.setVisibility(View.VISIBLE);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Config.DEFAULT_VOLUME, 0);

        seekbar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekbar.setProgress(Config.DEFAULT_VOLUME);
        seekbar.setMax(15);

        img_volume = findViewById(R.id.ic_volume);
        img_volume.setImageResource(R.drawable.ic_volume);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {

                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);

                if (progress == 0) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.volume_off), Toast.LENGTH_SHORT).show();
                    img_volume.setImageResource(R.drawable.ic_volume_off);
                } else if (progress == 15) {
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.volume_max), Toast.LENGTH_SHORT).show();
                    img_volume.setImageResource(R.drawable.ic_volume);
                } else {
                    img_volume.setImageResource(R.drawable.ic_volume);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    public void notifyShowBar() {

        final Radio station = RadioPlayerService.getInstance().getPlayingRadioStation();


        img_logo = findViewById(R.id.main_bar_logo);
        txt_radio_name = findViewById(R.id.main_bar_station);
        btn_pause.setOnClickListener(this);
        btn_play.setOnClickListener(this);

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent2 = new Intent(getApplicationContext(), ActivityRadioDetail.class);
                sendIntent2.putExtra("ID", station.getId());
                sendIntent2.putExtra("radioUrl", station.getRadio_url());
                sendIntent2.putExtra("radioID", station.getRadio_id());
                sendIntent2.putExtra("radioImg", station.getRadio_image());
                sendIntent2.putExtra("radioName", station.getRadio_name());
                sendIntent2.putExtra("categoryname", station.getCategory_name());
                startActivity(sendIntent2);
            }
        });
        Picasso
                .with(this)
                .load(Config.ADMIN_PANEL_URL + "/upload/" + station.radio_image)
                .placeholder(R.mipmap.ic_launcher)
                .into(img_logo);

        txt_radio_name.setText(station.radio_name);
        relativeLayout.setVisibility(View.VISIBLE);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_pause:
                play(false);
                btn_pause.setVisibility(View.GONE);
                btn_play.setVisibility(View.VISIBLE);
                break;

            case R.id.main_play:
                play(true);
                RadioPlayerService.instance(MainActivity.this, 0);
                btn_pause.setVisibility(View.VISIBLE);
                btn_play.setVisibility(View.GONE);
                break;

            default:
                break;
        }
    }

    public void play(boolean toPlay) {

        if (!toPlay) {
            stopService(new Intent(MainActivity.this, RadioPlayerService.class));
            btn_pause.setVisibility(View.GONE);
            btn_play.setVisibility(View.VISIBLE);

        } else {
            startService(new Intent(MainActivity.this, RadioPlayerService.class));
            btn_pause.setVisibility(View.VISIBLE);
            btn_play.setVisibility(View.GONE);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        RadioPlayerService.getInstance().onStop();
        super.onStop();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (RadioPlayerService.getInstance().isPlaying()) {
            notifyShowBar();
        } else {
            relativeLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            exitDialog();
        }
    }

    public void exitDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setTitle(R.string.app_name);
        dialog.setMessage(getResources().getString(R.string.message));
        dialog.setPositiveButton(getResources().getString(R.string.quit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.finish();
                stopService(new Intent(MainActivity.this, RadioPlayerService.class));
            }
        });

        dialog.setNegativeButton(getResources().getString(R.string.minimize), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                minimizeApp();
            }
        });

        dialog.setNeutralButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        dialog.show();
    }

    public void minimizeApp() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void loadBannerAd() {
        if (Config.ENABLE_ADMOB_BANNER_ADS) {
            adView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, GDPR.getBundleAd(MainActivity.this)).build();
            adView.loadAd(adRequest);
            adView.setAdListener(new AdListener() {

                @Override
                public void onAdClosed() {
                }

                @Override
                public void onAdFailedToLoad(int error) {
                    adView.setVisibility(View.GONE);
                }

                @Override
                public void onAdLeftApplication() {
                }

                @Override
                public void onAdOpened() {
                }

                @Override
                public void onAdLoaded() {
                    adView.setVisibility(View.VISIBLE);
                }
            });
        } else {
            Log.d("Log", "Banner Ad is Disabled!");
        }
    }

    private void loadInterstitialAd() {
        Log.d("TAG", "showAd");
        interstitialAd = new InterstitialAd(MainActivity.this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstitial_unit_id));
        AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, GDPR.getBundleAd(MainActivity.this)).build();
        interstitialAd.loadAd(adRequest);
    }


    private void showInterstitialAd() {
        if (Config.ENABLE_ADMOB_INTERSTITIAL_ADS_ON_DRAWER_MENU) {
            if (interstitialAd.isLoaded()) {
                interstitialAd.show();
            }
        } else {
            Log.d("Log", "Interstitial Ad is Disabled!");
        }
    }

    private void sendRegistrationIdToBackend() {

        Log.d("INFO", "Start update data to server...");

        String token = preferences.getString("fcm_token", null);
        String appVersion = BuildConfig.VERSION_CODE + " (" + BuildConfig.VERSION_NAME + ")";
        String osVersion = currentVersion() + " " + Build.VERSION.RELEASE;
        String model = android.os.Build.MODEL;
        String manufacturer = android.os.Build.MANUFACTURER;

        // Register FCM Token ID to server
        if (token != null) {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("user_android_token", token));
            nameValuePairs.add(new BasicNameValuePair("user_app_version", appVersion));
            nameValuePairs.add(new BasicNameValuePair("user_os_version", osVersion));
            nameValuePairs.add(new BasicNameValuePair("user_device_model", model));
            nameValuePairs.add(new BasicNameValuePair("user_device_manufacturer", manufacturer));
            new HttpTask(null, MainActivity.this, Config.ADMIN_PANEL_URL + "/register.php", nameValuePairs, false).execute();
        }

    }

    public static String currentVersion() {
        double release = Double.parseDouble(Build.VERSION.RELEASE.replaceAll("(\\d+[.]\\d+)(.*)", "$1"));
        String codeName = "Unsupported";//below Jelly bean OR above Oreo
        if (release >= 4.1 && release < 4.4) codeName = "Jelly Bean";
        else if (release < 5) codeName = "Kit Kat";
        else if (release < 6) codeName = "Lollipop";
        else if (release < 7) codeName = "Marshmallow";
        else if (release < 8) codeName = "Nougat";
        else if (release < 9) codeName = "Oreo";
        return codeName;
    }

}
