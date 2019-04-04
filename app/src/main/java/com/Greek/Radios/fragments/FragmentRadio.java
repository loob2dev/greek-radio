package com.Greek.Radios.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.Greek.Radios.Config;
import com.Greek.Radios.R;
import com.Greek.Radios.activities.ActivityRadioDetail;
import com.Greek.Radios.activities.MainActivity;
import com.Greek.Radios.adapters.AdapterRadio;
import com.Greek.Radios.callbacks.CallbackRadio;
import com.Greek.Radios.models.Radio;
import com.Greek.Radios.rests.ApiInterface;
import com.Greek.Radios.rests.RestAdapter;
import com.Greek.Radios.services.RadioPlayerService;
import com.Greek.Radios.utilities.Constant;
import com.Greek.Radios.utilities.DatabaseHandler;
import com.Greek.Radios.utilities.NetworkCheck;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentRadio extends Fragment {

    View root_view, parent_view;
    private RecyclerView recyclerView;
    private AdapterRadio adapterRecent;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Call<CallbackRadio> callbackCall = null;
    private int post_total = 0;
    private int failed_page = 0;
    private DatabaseHandler databaseHandler;
    private CharSequence charSequence = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_radio, null);
        parent_view = getActivity().findViewById(R.id.main_content);

        setHasOptionsMenu(true);

        swipeRefreshLayout = root_view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.orange, R.color.green, R.color.blue);

        recyclerView = root_view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        //set data and list adapter
        adapterRecent = new AdapterRadio(getActivity(), recyclerView, new ArrayList<Radio>());
        recyclerView.setAdapter(adapterRecent);

        if (Config.ENABLE_RTL_MODE) {
            recyclerView.setRotationY(180);
        }

        // on item list clicked
        adapterRecent.setOnItemClickListener(new AdapterRadio.OnItemClickListener() {
            @Override
            public void onItemClick(View v, Radio obj, int position) {
                if (RadioPlayerService.getInstance().isPlaying()) {
                    Radio play = RadioPlayerService.getInstance().getPlayingRadioStation();
                    String playingRadioName = play.radio_name;

                    if (obj.radio_name.equals(playingRadioName)) {
                        ((MainActivity) getActivity()).play(false);
                        Constant.IS_PLAYING = "1";
                    } else {
                        ((MainActivity) getActivity()).play(false);
                        RadioPlayerService.initialize(getActivity(), obj, 1);
                        ((MainActivity) getActivity()).play(true);
                        Constant.IS_PLAYING = "0";
                    }

                } else {
                    RadioPlayerService.initialize(getActivity(), obj, 1);
                    ((MainActivity) getActivity()).play(true);
                    Constant.IS_PLAYING = "0";
                }
            }
        });

        adapterRecent.setOnItemOverflowClickListener(new AdapterRadio.OnItemClickListener() {
            @Override
            public void onItemClick(View v, final Radio obj, int position) {
                PopupMenu popup = new PopupMenu(getActivity(), v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_popup, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.menu_context_favorite:
                                if (charSequence.equals(getString(R.string.option_set_favorite))) {
                                    databaseHandler.AddtoFavorite(new Radio(obj.radio_id, obj.radio_name, obj.category_name, obj.radio_image, obj.radio_url));
                                    Toast.makeText(getActivity(), getString(R.string.favorite_added), Toast.LENGTH_SHORT).show();

                                } else if (charSequence.equals(getString(R.string.option_unset_favorite))) {
                                    databaseHandler.RemoveFav(new Radio(obj.radio_id));
                                    Toast.makeText(getActivity(), getString(R.string.favorite_removed), Toast.LENGTH_SHORT).show();
                                }
                                return true;
                            /* Ekleme Başlangıç -- @Aykut — caliskan.aa@gmail.com  */
                            case R.id.menu_context_detail:

                                Intent sendIntent2 = new Intent(getContext(), ActivityRadioDetail.class);
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

                                email.putExtra(Intent.EXTRA_EMAIL,  new String[] { getActivity().getString(R.string.email_address)});
                                email.putExtra(Intent.EXTRA_SUBJECT, obj.radio_name+ " " + getActivity().getString(R.string.email_subject));
                                email.putExtra(Intent.EXTRA_TEXT, obj.radio_name + " " + getActivity().getString(R.string.email_message));
                                email.setType("message/rfc822");
                                try {
                                    // the user can choose the email client
                                    getActivity().startActivity(Intent.createChooser(email, getActivity().getString(R.string.choose_email)));

                                } catch (android.content.ActivityNotFoundException ex) {
                                    Toast.makeText(getActivity(),getActivity().getString(R.string.email_problem),
                                            Toast.LENGTH_LONG).show();
                                }
                                return  true;
                            /* Ekleme Bitiş -- @Aykut — caliskan.aa@gmail.com  */
                            case R.id.menu_context_share:

                                String share_title = android.text.Html.fromHtml(obj.radio_name).toString();
                                String share_content = android.text.Html.fromHtml(getResources().getString(R.string.share_content)).toString();
                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT, share_title + "\n\n" + share_content + "\n\n" + "https://play.google.com/store/apps/details?id=" + getActivity().getPackageName());
                                sendIntent.setType("text/plain");
                                startActivity(sendIntent);
                                return true;

                            default:
                        }
                        return false;
                    }
                });
                popup.show();

                databaseHandler = new DatabaseHandler(getActivity());
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

        // detect when scroll reach bottom
        adapterRecent.setOnLoadMoreListener(new AdapterRadio.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int current_page) {
                if (post_total > adapterRecent.getItemCount() && current_page != 0) {
                    int next_page = current_page + 1;
                    requestAction(next_page);
                } else {
                    adapterRecent.setLoaded();
                }
            }
        });

        // on swipe list
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
                adapterRecent.resetListData();
                requestAction(1);
            }
        });

        requestAction(1);

        return root_view;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    private void displayApiResult(final List<Radio> videos) {
        adapterRecent.insertData(videos);
        swipeProgress(false);
        if (videos.size() == 0) {
            showNoItemView(true);
        }
    }

    private void requestListPostApi(final int page_no) {
        ApiInterface apiInterface = RestAdapter.createAPI();
        callbackCall = apiInterface.getRecentRadio(page_no, Config.LOAD_MORE);
        callbackCall.enqueue(new Callback<CallbackRadio>() {
            @Override
            public void onResponse(Call<CallbackRadio> call, Response<CallbackRadio> response) {
                CallbackRadio resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    post_total = resp.count_total;
                    displayApiResult(resp.posts);
                } else {
                    onFailRequest(page_no);
                }
            }

            @Override
            public void onFailure(Call<CallbackRadio> call, Throwable t) {
                if (!call.isCanceled()) onFailRequest(page_no);
            }

        });
    }

    private void onFailRequest(int page_no) {
        failed_page = page_no;
        adapterRecent.setLoaded();
        swipeProgress(false);
        if (NetworkCheck.isConnect(getActivity())) {
            showFailedView(true, getString(R.string.failed_text));
        } else {
            showFailedView(true, getString(R.string.failed_text));
        }
    }

    private void requestAction(final int page_no) {
        showFailedView(false, "");
        showNoItemView(false);
        if (page_no == 1) {
            swipeProgress(true);
        } else {
            adapterRecent.setLoading();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestListPostApi(page_no);
            }
        }, 250);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgress(false);
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = root_view.findViewById(R.id.lyt_failed_home);
        ((TextView) root_view.findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        root_view.findViewById(R.id.failed_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestAction(failed_page);
            }
        });
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = root_view.findViewById(R.id.lyt_no_item_home);
        ((TextView) root_view.findViewById(R.id.no_item_message)).setText(R.string.no_post_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            return;
        }
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(show);
            }
        });
    }

}
