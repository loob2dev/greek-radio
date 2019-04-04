package com.Greek.Radios.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.Greek.Radios.Config;
import com.Greek.Radios.R;
import com.Greek.Radios.activities.ActivityRadioDetail;
import com.Greek.Radios.activities.MainActivity;
import com.Greek.Radios.adapters.AdapterFavorite;
import com.Greek.Radios.json.JsonConstant;
import com.Greek.Radios.json.JsonUtils;
import com.Greek.Radios.models.Radio;
import com.Greek.Radios.services.RadioPlayerService;
import com.Greek.Radios.utilities.Constant;
import com.Greek.Radios.utilities.DatabaseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FragmentFavorite extends Fragment {

    private List<Radio> data = new ArrayList<Radio>();
    View root_view, parent_view;
    AdapterFavorite mAdapterFavorite;
    DatabaseHandler databaseHandler;
    RecyclerView recyclerView;
    LinearLayout linearLayout;
    private CharSequence charSequence = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_favorite, null);
        parent_view = getActivity().findViewById(R.id.main_content);

        linearLayout = root_view.findViewById(R.id.lyt_no_favorite);
        recyclerView = root_view.findViewById(R.id.recyclerView);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        databaseHandler = new DatabaseHandler(getActivity());
        data = databaseHandler.getAllData();
        mAdapterFavorite = new AdapterFavorite(getActivity(), recyclerView, data);
        recyclerView.setAdapter(mAdapterFavorite);

        if (data.size() == 0) {
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            linearLayout.setVisibility(View.INVISIBLE);
        }

        return root_view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    /* Ekleme Başlangıç -- @Aykut — caliskan.aa@gmail.com  */
    // add pos global..
    private class GetTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            if (null == result || result.length() == 0) {
                Toast.makeText(getActivity(), getActivity().getString(R.string.internet_disabled), Toast.LENGTH_SHORT).show();

            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                   // JSONArray jsonArray = mainJson.getJSONArray(JsonConstant.RADIO_ARRAY_NAME);
                 //   JSONObject objJson = null;


                    Radio item = new Radio();
                    item.setRadio_name(mainJson.getString(JsonConstant.RADIO_NAME));
                    item.setCategory_name(mainJson.getString(JsonConstant.RADIO_CATEGORY_NAME));
                    item.setRadio_id(mainJson.getString(JsonConstant.RADIO_CID));
                    item.setRadio_image(mainJson.getString(JsonConstant.RADIO_IMAGE));
                    item.setRadio_url(mainJson.getString(JsonConstant.RADIO_URL));

                    RadioPlayerService.initialize(getActivity(), item, 1);
                    ((MainActivity) getActivity()).play(true);
                    Constant.IS_PLAYING = "0";

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }
    /* Ekleme Bitiş -- @Aykut — caliskan.aa@gmail.com  */
    @Override
    public void onResume() {

        data = databaseHandler.getAllData();
        mAdapterFavorite = new AdapterFavorite(getActivity(), recyclerView, data);
        recyclerView.setAdapter(mAdapterFavorite);

        mAdapterFavorite.setOnItemClickListener(new AdapterFavorite.OnItemClickListener() {
            @Override
            public void onItemClick(View v, Radio obj, int position) {
                if (RadioPlayerService.getInstance().isPlaying()) {
                    Radio play = RadioPlayerService.getInstance().getPlayingRadioStation();
                    String playingRadioName = play.radio_name;

                    if (obj.radio_name.equals(playingRadioName)) {
                        ((MainActivity) getActivity()).play(false);
                        Constant.IS_PLAYING = "1";
                    } else {
                        new GetTask().execute(Config.ADMIN_PANEL_URL + "/api/get_radioDetail?radio_id=" + obj.getRadio_id()+"&api_key="+Config.API_KEY);

                  /*      ((MainActivity) getActivity()).play(false);
                        RadioPlayerService.initialize(getActivity(), obj, 1);
                        ((MainActivity) getActivity()).play(true);
                        Constant.IS_PLAYING = "0";*/
                    }

                } else {
                    new GetTask().execute(Config.ADMIN_PANEL_URL + "/api/get_radioDetail?radio_id=" + obj.getRadio_id()+"&api_key="+Config.API_KEY);

/*                    RadioPlayerService.initialize(getActivity(), obj, 1);
                    ((MainActivity) getActivity()).play(true);
                    Constant.IS_PLAYING = "0";*/
                }
            }
        });

        mAdapterFavorite.setOnItemOverflowClickListener(new AdapterFavorite.OnItemClickListener() {
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
                                    refreshFragment();
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

        if (data.size() == 0) {
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            linearLayout.setVisibility(View.INVISIBLE);
        }

        super.onResume();

    }

    public void refreshFragment() {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.detach(this).attach(this).commit();
    }

}
