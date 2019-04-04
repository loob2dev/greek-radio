package com.Greek.Radios.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.Greek.Radios.Config;
import com.Greek.Radios.R;
import com.Greek.Radios.json.JsonConstant;
import com.Greek.Radios.models.Radio;
import com.Greek.Radios.services.RadioPlayerService;
import com.Greek.Radios.utilities.DatabaseHandler;
import com.Greek.Radios.utilities.GDPR;
import com.Greek.Radios.utilities.IcyStreamMeta;
import com.boswelja.lastfm.Callback;
import com.boswelja.lastfm.LastFMRequest;
import com.boswelja.lastfm.models.album.LastFMAlbum;
import com.boswelja.lastfm.models.artist.LastFMArtist;
import com.github.clans.fab.FloatingActionButton;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class ActivityRadioDetail extends AppCompatActivity implements View.OnClickListener {
    ImageView imageView1, imageView2;
    TextView textView;
    LinearLayout relativeLayout;
    SwipeRefreshLayout swipeRefreshLayout = null;
    AdView adView;
    List<Radio> itemList;
    TextView txt_radio_name;
    ImageView img_logo;
    ImageButton btn_pause, btn_play;
    DatabaseHandler databaseHandler;
    private Toolbar toolbar;
    private MainActivity mainActivity;
    private final static String COLLAPSING_TOOLBAR_FRAGMENT_TAG = "collapsing_toolbar";
    private ImageView imageView;
    TextView radio_name, radio_desc, current_play, tag, hit;
    FloatingActionButton fab_category_favorite, fab_play;
    Radio itemRadio = null;
    private InterstitialAd interstitialAd;
    Timer timer;
    int counter = 1;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int interval =  preferences.getInt("interval",0);

        if (interval==Config.ADMOB_INTERSTITIAL_ADS_INTERVAL_FOR_RADIODETAIL)
        {
            if (interstitialAd.isLoaded())
            {
                interstitialAd.show();

            }
        }
        timer.cancel();
        timer.purge();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            timer.cancel();
            timer.purge();
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_radio_detail);
        loadInterstitialAd();

        itemRadio = new Radio();
        databaseHandler = new DatabaseHandler(ActivityRadioDetail.this);
        radio_name = (TextView) findViewById(R.id.radio_name);
        current_play = (TextView) findViewById(R.id.current_play);
        radio_desc = (TextView) findViewById(R.id.radio_desc);
        tag = (TextView) findViewById(R.id.tag);
        imageView = (ImageView) findViewById(R.id.imageView);
        hit = (TextView) findViewById(R.id.hit);
        btn_pause = findViewById(R.id.main_pause);
        btn_play = findViewById(R.id.main_play);
        relativeLayout = findViewById(R.id.main_bar);
        itemRadio.setRadio_id(String.valueOf(getIntent().getStringExtra("radioID")));
        itemRadio.setRadio_image(String.valueOf(getIntent().getStringExtra("radioImg")));
        itemRadio.setRadio_name(String.valueOf(getIntent().getStringExtra("radioName")));
        itemRadio.setId(getIntent().getIntExtra("ID", 0));
        itemRadio.setRadio_url(String.valueOf(getIntent().getStringExtra("radioUrl")));
        itemRadio.setCategory_name(String.valueOf(getIntent().getStringExtra("categoryname")));
        fab_play = (FloatingActionButton) findViewById(R.id.fab_play);
        fab_category_favorite = (FloatingActionButton) findViewById(R.id.fab_category_favorite);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final SharedPreferences.Editor editor = preferences.edit();
        AdListener adListener = new AdListener(){

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                editor.putInt("interval",0);
                editor.commit();
            }
        };
        interstitialAd.setAdListener(adListener);

        int interval =  preferences.getInt("interval",0);
        if (interval <Config.ADMOB_INTERSTITIAL_ADS_INTERVAL_FOR_RADIODETAIL )
        {
            interval++;
            editor.putInt("interval",interval);
            editor.commit();
        }
        else if (interval==Config.ADMOB_INTERSTITIAL_ADS_INTERVAL_FOR_RADIODETAIL)
        {

        };
        String radioName = itemRadio.getRadio_name();
        if (RadioPlayerService.getInstance().isPlaying()) {
            Radio play = RadioPlayerService.getInstance().getPlayingRadioStation();
            String playingRadioName = play.getRadio_name();

            if (radioName.equals(playingRadioName)) {

                fab_play.setImageResource(R.drawable.ic_pause);
            }
        }
        radio_name.setText(itemRadio.getRadio_name());
        URL url = null;
        try {
            url = new URL(itemRadio.radio_url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        // new ActivityRadioDetail.MetadataTask().execute(url);
        itemList = databaseHandler.getFavRow(itemRadio.getRadio_id());
        if (itemList.size() != 0) {
            fab_category_favorite.setImageResource(R.drawable.ic_favorite);

        }
        databaseHandler.close();
        loadBannerAd();
        int delay = 500; // delay for 0 sec.
        timer = new Timer();
        final URL finalUrl = url;
        if (!itemRadio.radio_url.endsWith(".m3u8")){
            timer.scheduleAtFixedRate(new TimerTask() {

                public void run() {
                    new ActivityRadioDetail.MetadataTask().execute(finalUrl);
                }
            }, delay, Config.ALBUM_REFRESH_PERIOD);

        }
        {
            current_play.setText(getText(R.string.failed_m3u8_stream));
        }
        fab_category_favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (databaseHandler.getFavRow(itemRadio.getRadio_id()).size() == 0) {
                    databaseHandler.AddtoFavorite(new Radio(itemRadio.radio_id, itemRadio.radio_name, itemRadio.category_name, itemRadio.radio_image, itemRadio.radio_url));
                    Toast.makeText(getApplicationContext(), getString(R.string.favorite_added), Toast.LENGTH_SHORT).show();
                    fab_category_favorite.setImageResource(R.drawable.ic_favorite);

                } else {
                    databaseHandler.RemoveFav(new Radio(itemRadio.radio_id));
                    Toast.makeText(getApplicationContext(), getString(R.string.favorite_removed), Toast.LENGTH_SHORT).show();
                    fab_category_favorite.setImageResource(R.drawable.ic_favorite_outline_white);

                }
            }

        });


        Picasso
                .with(ActivityRadioDetail.this)
                .load(Config.ADMIN_PANEL_URL + "/upload/" + itemRadio.getRadio_image())
                .placeholder(R.mipmap.ic_launcher)
                .into(imageView);


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getString(R.string.option_set_detail));
        toolbar.setSubtitle(itemRadio.getRadio_name());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        fab_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String radioName = itemRadio.getRadio_name();
                if (RadioPlayerService.getInstance().isPlaying()) {
                    Radio play = RadioPlayerService.getInstance().getPlayingRadioStation();
                    String playingRadioName = play.getRadio_name();

                    if (radioName.equals(playingRadioName)) {
                        play(false);
                        JsonConstant.IS_PLAYING = "1";
                        fab_play.setImageResource(R.drawable.ic_play);

                    } else {
                        play(false);

                        RadioPlayerService.initialize(ActivityRadioDetail.this, itemRadio, 4);
                        play(true);
                        JsonConstant.IS_PLAYING = "0";
                    }
                } else {

                    RadioPlayerService.initialize(ActivityRadioDetail.this, itemRadio, 4);
                    play(true);
                    JsonConstant.IS_PLAYING = "0";
                    fab_play.setImageResource(R.drawable.ic_pause);

                }
            }
        });
        // new ActivityRadioDetail.GetTask().execute(Config.RADIO_DETAIL_URL + "/getradioinfo.php?id=" + itemRadio.getRadioId());
    }

    private void loadInterstitialAd() {
        Log.d("TAG", "showAd");
        interstitialAd = new InterstitialAd(ActivityRadioDetail.this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.admob_interstitial_unit_id));
        AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, GDPR.getBundleAd(ActivityRadioDetail.this)).addTestDevice("7DC9EBD1A35048AB8E05CCAB1EB12A26").build();
        interstitialAd.loadAd(adRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (RadioPlayerService.getInstance().isPlaying()) {
            notifyShowBar();
        }
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
                RadioPlayerService.instance(ActivityRadioDetail.this, 4);
                btn_pause.setVisibility(View.VISIBLE);
                btn_play.setVisibility(View.GONE);


                break;

            default:
                break;
        }
    }

    public void play(boolean toPlay) {
        String radioName;
        radioName = itemRadio.getRadio_name();
        Radio play = RadioPlayerService.getInstance().getPlayingRadioStation();
        String playingRadioName = play.getRadio_name();

        if (!toPlay) {
            if (radioName.equals(playingRadioName)) {

                fab_play.setImageResource(R.drawable.ic_play);
            }

            stopService(new Intent(ActivityRadioDetail.this, RadioPlayerService.class));
            btn_pause.setVisibility(View.GONE);
            btn_play.setVisibility(View.VISIBLE);

        } else {
            startService(new Intent(ActivityRadioDetail.this, RadioPlayerService.class));
            btn_pause.setVisibility(View.VISIBLE);
            btn_play.setVisibility(View.GONE);
            if (radioName.equals(playingRadioName)) {

                fab_play.setImageResource(R.drawable.ic_pause);
            }
        }

    }

    public void notifyShowBar() {

        Radio station = RadioPlayerService.getInstance().getPlayingRadioStation();

        img_logo = findViewById(R.id.main_bar_logo);
        txt_radio_name = findViewById(R.id.main_bar_station);
        btn_pause.setOnClickListener(this);
        btn_play.setOnClickListener(this);

        Picasso
                .with(this)
                .load(Config.ADMIN_PANEL_URL + "/upload/" + station.radio_image)
                .placeholder(R.mipmap.ic_launcher)
                .into(img_logo);

        txt_radio_name.setText(station.radio_name);
        relativeLayout.setVisibility(View.VISIBLE);

    }

    protected class MetadataTask extends AsyncTask<URL, Void, IcyStreamMeta> {
        protected IcyStreamMeta streamMeta;

        @Override
        protected IcyStreamMeta doInBackground(URL... urls) {
            streamMeta = new IcyStreamMeta(urls[0]);

            try {
                streamMeta.refreshMeta();
            } catch (IOException e) {
                // TODO: Handle
                Log.e(ActivityRadioDetail.MetadataTask.class.toString(), e.getMessage());
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
            return streamMeta;
        }

        @Override
        protected void onPostExecute(final IcyStreamMeta result) {
            try {
                System.out.println(result.getArtist());
                final LastFMRequest request = new LastFMRequest() // Create the request
                        .setApiKey("d488abe5913c4a5e7f4c859ca98f54dd");
                String title = result.getTitle();
                final String artist = result.getArtist();
                current_play.setText(artist + " - " + title);

                request.requestAlbum().withQuery(title, artist).setCallback(new Callback<LastFMAlbum>() {
                    @Override
                    public void onDataRetrieved(LastFMAlbum data) {
                        if (data.getAlbum() != null) {
                            if (!data.getAlbum().getImage().get(data.getAlbum().getImage().size() - 1).getText().isEmpty()) {
                                Picasso.with(ActivityRadioDetail.this).load(data.getAlbum().getImage().get(data.getAlbum().getImage().size() - 1).getText()).into(imageView);
                            } else {
                                request.requestArtist().withName(artist).setCallback(new Callback<LastFMArtist>() {
                                    @Override
                                    public void onDataRetrieved(LastFMArtist data) {
                                        if (data.getArtist() != null) {
                                            if (!data.getArtist().getImage().get(data.getArtist().getImage().size() - 1).getText().isEmpty()) {
                                                Picasso.with(ActivityRadioDetail.this).load(data.getArtist().getImage().get(data.getArtist().getImage().size() - 1).getText()).into(imageView);
                                            }

                                        }

                                    }

                                    @Override
                                    public void onFailed(Throwable t) {

                                    }

                                    @Override
                                    public void onResultEmpty() {

                                    }
                                }).build();


                            }

                        } else {
                            request.requestArtist().withName(artist).setCallback(new Callback<LastFMArtist>() {
                                @Override
                                public void onDataRetrieved(LastFMArtist data) {
                                    if (data.getArtist() != null) {
                                        if (!data.getArtist().getImage().get(data.getArtist().getImage().size() - 1).getText().isEmpty()) {
                                            Picasso.with(ActivityRadioDetail.this).load(data.getArtist().getImage().get(data.getArtist().getImage().size() - 1).getText()).into(imageView);
                                        }

                                    }

                                }

                                @Override
                                public void onFailed(Throwable t) {

                                }

                                @Override
                                public void onResultEmpty() {

                                }
                            }).build();
                        }
                    }

                    @Override
                    public void onFailed(Throwable t) {

                    }

                    @Override
                    public void onResultEmpty() {

                    }
                }).build();


            } catch (IOException e) {
                e.printStackTrace();
            } catch (NetworkOnMainThreadException ex) {
                ex.printStackTrace();

            }
        }

    }


    public void loadBannerAd() {
        if (Config.ENABLE_ADMOB_BANNER_ADS) {
            adView = findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, GDPR.getBundleAd(ActivityRadioDetail.this)).build();
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


/*
    protected class refreshMetaTask extends AsyncTask<URL, Void, IcyStreamMeta> {
        protected IcyStreamMeta streamMeta;

        @Override
        protected IcyStreamMeta doInBackground(URL... urls) {
            streamMeta = new IcyStreamMeta(urls[0]);
            try {
                streamMeta.refreshMeta();
            } catch (IOException e) {
                // TODO: Handle
                Log.e(ActivityRadioDetail.MetadataTask.class.toString(), e.getMessage());
            } catch (OutOfMemoryError error) {
                error.printStackTrace();
            }
            return streamMeta;
        }

        @Override
        protected void onPostExecute(final IcyStreamMeta result) {
            try {
                String title = result.getTitle();
                final String artist = result.getArtist();
                current_play.setText(artist + " - " + title);


            } catch (IOException e) {
                e.printStackTrace();
            } catch (NetworkOnMainThreadException ex) {
                ex.printStackTrace();

            }
        }

    }
*/

}