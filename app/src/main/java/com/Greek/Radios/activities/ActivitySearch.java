package com.Greek.Radios.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.Greek.Radios.Config;
import com.Greek.Radios.R;
import com.Greek.Radios.adapters.AdapterFavorite;
import com.Greek.Radios.callbacks.CallbackRadio;
import com.Greek.Radios.models.Radio;
import com.Greek.Radios.rests.ApiInterface;
import com.Greek.Radios.rests.RestAdapter;
import com.Greek.Radios.services.RadioPlayerService;
import com.Greek.Radios.utilities.Constant;
import com.Greek.Radios.utilities.DatabaseHandler;
import com.Greek.Radios.utilities.NetworkCheck;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySearch extends AppCompatActivity implements View.OnClickListener {

    private EditText et_search;
    private RecyclerView recyclerView;
    private AdapterFavorite mAdapterSearch;
    private ImageButton bt_clear;
    private ProgressBar progressBar;
    private AdView adView;
    private View view;
    Snackbar snackbar;
    private Call<CallbackRadio> callbackCall = null;
    TextView txt_radio_name;
    ImageView img_logo;
    ImageButton btn_pause, btn_play;
    LinearLayout relativeLayout;
    private DatabaseHandler databaseHandler;
    private CharSequence charSequence = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        view = findViewById(android.R.id.content);

        if (Config.ENABLE_RTL_MODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        }

        btn_pause = findViewById(R.id.main_pause);
        btn_play = findViewById(R.id.main_play);
        relativeLayout = findViewById(R.id.main_bar);

        et_search = findViewById(R.id.et_search);
        bt_clear = findViewById(R.id.bt_clear);
        bt_clear.setVisibility(View.GONE);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        et_search.addTextChangedListener(textWatcher);

        //set data and list mAdapterSearch
        mAdapterSearch = new AdapterFavorite(this, recyclerView, new ArrayList<Radio>());
        recyclerView.setAdapter(mAdapterSearch);

        bt_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_search.setText("");
            }
        });

        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard();
                    searchAction();
                    return true;
                }
                return false;
            }
        });

        mAdapterSearch.setOnItemClickListener(new AdapterFavorite.OnItemClickListener() {
            @Override
            public void onItemClick(View v, Radio obj, int position) {
                if (RadioPlayerService.getInstance().isPlaying()) {
                    Radio play = RadioPlayerService.getInstance().getPlayingRadioStation();
                    String playingRadioName = play.radio_name;

                    if (obj.radio_name.equals(playingRadioName)) {
                        ((ActivitySearch.this)).play(false);
                        Constant.IS_PLAYING = "1";
                    } else {
                        ((ActivitySearch.this)).play(false);
                        RadioPlayerService.initialize(ActivitySearch.this, obj, 3);
                        ((ActivitySearch.this)).play(true);
                        Constant.IS_PLAYING = "0";
                    }
                } else {
                    RadioPlayerService.initialize(ActivitySearch.this, obj, 3);
                    ((ActivitySearch.this)).play(true);
                    Constant.IS_PLAYING = "0";
                }
            }
        });

        mAdapterSearch.setOnItemOverflowClickListener(new AdapterFavorite.OnItemClickListener() {
            @Override
            public void onItemClick(View v, final Radio obj, int position) {
                PopupMenu popup = new PopupMenu(ActivitySearch.this, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_popup, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_context_favorite:
                                if (charSequence.equals(getString(R.string.option_set_favorite))) {
                                    databaseHandler.AddtoFavorite(new Radio(obj.radio_id, obj.radio_name, obj.category_name, obj.radio_image, obj.radio_url));
                                    Toast.makeText(getApplicationContext(), getString(R.string.favorite_added), Toast.LENGTH_SHORT).show();

                                } else if (charSequence.equals(getString(R.string.option_unset_favorite))) {
                                    databaseHandler.RemoveFav(new Radio(obj.radio_id));
                                    Toast.makeText(getApplicationContext(), getString(R.string.favorite_removed), Toast.LENGTH_SHORT).show();
                                }
                                return true;
                            /* Ekleme Başlangıç -- @Aykut — caliskan.aa@gmail.com  */
                            case R.id.menu_context_detail:

                                Intent sendIntent2 = new Intent(ActivitySearch.this, ActivityRadioDetail.class);
                                sendIntent2.putExtra("ID", obj.getId());
                                sendIntent2.putExtra("radioUrl", obj.getRadio_url());
                                sendIntent2.putExtra("radioID", obj.getRadio_id());
                                sendIntent2.putExtra("radioImg", obj.getRadio_image());
                                sendIntent2.putExtra("radioName", obj.getRadio_name());

                                startActivity(sendIntent2);
                                return true;
                            case R.id.menu_context_report:
                                Intent email = new Intent(Intent.ACTION_SEND );
                                // prompts email clients only

                                email.putExtra(Intent.EXTRA_EMAIL,  new String[] { getString(R.string.email_address)});
                                email.putExtra(Intent.EXTRA_SUBJECT, obj.radio_name+ " " +  getString(R.string.email_subject));
                                email.putExtra(Intent.EXTRA_TEXT, obj.radio_name + " " +  getString(R.string.email_message));
                                email.setType("message/rfc822");
                                try {
                                    // the user can choose the email client
                                    startActivity(Intent.createChooser(email, getString(R.string.choose_email)));

                                } catch (android.content.ActivityNotFoundException ex) {
                                    Toast.makeText(getApplicationContext() , getString(R.string.email_problem),
                                            Toast.LENGTH_LONG).show();
                                }
                                return  true;
                            /* Ekleme Bitiş -- @Aykut — caliskan.aa@gmail.com  */
                            case R.id.menu_context_share:

                                String share_title = android.text.Html.fromHtml(obj.radio_name).toString();
                                String share_content = android.text.Html.fromHtml(getResources().getString(R.string.share_content)).toString();
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, share_title + "\n\n" + share_content + "\n\n" + "https://play.google.com/store/apps/details?id=" + getPackageName());
                                sendIntent.setType("text/plain");
                                startActivity(sendIntent);
                                return true;

                            default:
                        }
                        return false;
                    }
                });
                popup.show();

                databaseHandler = new DatabaseHandler(ActivitySearch.this);
                List<Radio> data = databaseHandler.getFavRow(obj.radio_id);
                if (data.size() == 0) {
                    popup.getMenu().findItem(R.id.menu_context_favorite).setTitle(R.string.option_set_favorite);
                    charSequence = popup.getMenu().findItem(R.id.menu_context_favorite).getTitle();
                } else {
                    if (data.get(0).getRadio_id().equals(obj.radio_id)) {
                        popup.getMenu().findItem(R.id.menu_context_favorite).setTitle(R.string.option_unset_favorite);
                        charSequence = popup.getMenu().findItem(R.id.menu_context_favorite).getTitle();
                    }
                }

            }
        });

        setupToolbar();

        loadBannerAd();

    }

    public void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence c, int i, int i1, int i2) {
            if (c.toString().trim().length() == 0) {
                bt_clear.setVisibility(View.GONE);
            } else {
                bt_clear.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    private void requestSearchApi(final String query) {
        ApiInterface apiInterface = RestAdapter.createAPI();
        callbackCall = apiInterface.getSearchPosts(query, 100);
        callbackCall.enqueue(new Callback<CallbackRadio>() {
            @Override
            public void onResponse(Call<CallbackRadio> call, Response<CallbackRadio> response) {
                CallbackRadio resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    mAdapterSearch.insertData(resp.posts);
                    if (resp.posts.size() == 0) showNotFoundView(true);
                } else {
                    onFailRequest();
                }
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<CallbackRadio> call, Throwable t) {
                onFailRequest();
                progressBar.setVisibility(View.GONE);
            }

        });
    }

    private void onFailRequest() {
        if (NetworkCheck.isConnect(this)) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.no_internet_text));
        }
    }

    private void searchAction() {
        showFailedView(false, "");
        showNotFoundView(false);
        final String query = et_search.getText().toString().trim();
        if (!query.equals("")) {
            mAdapterSearch.resetListData();
            progressBar.setVisibility(View.VISIBLE);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    requestSearchApi(query);
                }
            }, 250);
        } else {
            snackbar = Snackbar.make(view, getResources().getString(R.string.msg_search_input), Snackbar.LENGTH_SHORT);
            snackbar.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchAction();
            }
        });
    }

    private void showNotFoundView(boolean show) {
        View lyt_no_item = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.no_post_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if (et_search.length() > 0) {
            et_search.setText("");
        } else {
            super.onBackPressed();
        }
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
                RadioPlayerService.instance(ActivitySearch.this, 3);
                btn_pause.setVisibility(View.VISIBLE);
                btn_play.setVisibility(View.GONE);
                break;

            default:
                break;
        }
    }

    public void play(boolean toPlay) {

        if (!toPlay) {
            stopService(new Intent(ActivitySearch.this, RadioPlayerService.class));
            relativeLayout.setVisibility(View.VISIBLE);
            btn_pause.setVisibility(View.GONE);
            btn_play.setVisibility(View.VISIBLE);

        } else {
            startService(new Intent(ActivitySearch.this, RadioPlayerService.class));
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

    public void loadBannerAd() {
        if (Config.ENABLE_ADMOB_BANNER_ADS) {
            adView = findViewById(R.id.adView);
            adView.loadAd(new AdRequest.Builder().build());
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

}
