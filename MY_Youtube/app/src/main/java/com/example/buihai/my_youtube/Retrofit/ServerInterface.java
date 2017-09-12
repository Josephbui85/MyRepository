package com.example.buihai.my_youtube.Retrofit;

import com.google.gson.JsonElement;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by anhhong on 20/06/2017.
 */

public interface ServerInterface {

    @GET("search?key=AIzaSyAJfHk5yn7Cp3gbz6mEHfoTlrLLaq7VNHE&channelId=UCruaM4824Rr_ry7fsD5Jwag&part=snippet,id&order=date&maxResults=50")
    Call<JsonElement> getVideoTHVL();
    @GET("search?key=AIzaSyAJfHk5yn7Cp3gbz6mEHfoTlrLLaq7VNHE&channelId=UC3nIPINcfdWkStHTamt3SMA&part=snippet,id&order=date&maxResults=50")
    Call<JsonElement> getVideoFISHING();
    @GET("search?key=AIzaSyAJfHk5yn7Cp3gbz6mEHfoTlrLLaq7VNHE&channelId=UCh4yfIsO_D_7mYN8P-cOnuA&part=snippet,id&order=date&maxResults=50")
    Call<JsonElement> getVideoDIENQUAN();
}
