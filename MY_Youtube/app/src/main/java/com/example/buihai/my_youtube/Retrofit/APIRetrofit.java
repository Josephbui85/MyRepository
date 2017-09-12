package com.example.buihai.my_youtube.Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by anhhong on 20/06/2017.
 */

public class APIRetrofit {

    public Retrofit getjson() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://www.googleapis.com/youtube/v3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit;
    }
}
