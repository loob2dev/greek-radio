package com.Greek.Radios.rests;

import com.Greek.Radios.Config;
import com.Greek.Radios.callbacks.CallbackCategory;
import com.Greek.Radios.callbacks.CallbackCategoryDetails;
import com.Greek.Radios.callbacks.CallbackRadio;
import com.Greek.Radios.models.Settings;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface ApiInterface {

    String CACHE = "Cache-Control: max-age=0";
    String AGENT = "Data-Agent: Your italiane App";

    @Headers({CACHE, AGENT})
    @GET("api/get_recent_radio?api_key=" + Config.API_KEY)
    Call<CallbackRadio> getRecentRadio(
            @Query("page") int page,
            @Query("count") int count
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_category_index?api_key=" + Config.API_KEY)
    Call<CallbackCategory> getAllCategories();

    @Headers({CACHE, AGENT})
    @GET("api/get_category_detail")
    Call<CallbackCategoryDetails> getCategoryDetailsByPage(
            @Query("id") int id,
            @Query("page") int page,
            @Query("count") int count
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_search_results?api_key=" + Config.API_KEY)
    Call<CallbackRadio> getSearchPosts(
            @Query("search") String search,
            @Query("count") int count
    );

    @Headers({CACHE, AGENT})
    @GET("api/get_privacy_policy")
    Call<Settings> getPrivacyPolicy();

}
