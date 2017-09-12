package com.example.buihai.my_youtube.THVL;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.buihai.my_youtube.R;
import com.example.buihai.my_youtube.Retrofit.APIRetrofit;
import com.example.buihai.my_youtube.Retrofit.ServerInterface;
import com.example.buihai.my_youtube.VideoModel;
import com.google.gson.JsonElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by buihai on 9/6/17.
 */

public class Fragment_THVL extends Fragment {

    private RecyclerView recyclerListVideoTHVL;
    private Adapter_THVL adapter;
    ProgressBar progressBar;
    List<VideoModel> listVideo=new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout=inflater.inflate(R.layout.thvl_fragment,container,false);
        progressBar=(ProgressBar) layout.findViewById(R.id.progress_loadconact);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false);
        recyclerListVideoTHVL=(RecyclerView)layout.findViewById(R.id.recycleList_THVL);
        recyclerListVideoTHVL.setLayoutManager(linearLayoutManager);
        DividerItemDecoration divi= new DividerItemDecoration(getActivity(),linearLayoutManager.getOrientation());
        recyclerListVideoTHVL.addItemDecoration(divi);

        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {

                ServerInterface serverInterfaceTHVL=new APIRetrofit().getjson().create(ServerInterface.class);
                Call<JsonElement> call= serverInterfaceTHVL.getVideoTHVL();

                call.enqueue(new Callback<JsonElement>() {
                    @Override
                    public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                        try {
                            final JSONObject data = new JSONObject(response.body().toString());
//                            Log.i("BLH",response.body().toString());
                            Log.i("BLH","123456");

                            final JSONArray arrVideo=data.getJSONArray("items");
                            JSONObject video1 = (JSONObject) arrVideo.get(1);
                            String number = video1.getJSONObject("id").getString("videoId");
                            Log.i("BLH",number);

                            getActivity().runOnUiThread(new Runnable() {



                                @Override
                                public void run() {


                                    for (int i = 0; i < arrVideo.length(); i++) {


                                        VideoModel model = new VideoModel();
                                        // create video model if this item is video
                                        try {
                                            JSONObject video = (JSONObject) arrVideo.get(i);
                                            if(video.getJSONObject("id").has("videoId") == true) {
                                                model.id = video.getJSONObject("id").getString("videoId");
                                                JSONObject snippet = (JSONObject) video.getJSONObject("snippet");
                                                model.name = snippet.getString("title");
                                                Log.i("BLH","hello");
                                                model.description = snippet.getString("description");
                                                model.uploadTime = snippet.getString("publishedAt");

                                                // thumbnails
                                                JSONObject thumbnails = (JSONObject) snippet.getJSONObject("thumbnails");
                                                model.imgThumbDefault = ((JSONObject) thumbnails.getJSONObject("default")).getString("url");
                                                model.imgThumbMedium = ((JSONObject) thumbnails.getJSONObject("medium")).getString("url");
                                                model.imgThumbHigh = ((JSONObject) thumbnails.getJSONObject("high")).getString("url");

                                                // add model to arraylist

                                                listVideo.add(model);

                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }


                                    }
                                    progressBar.setVisibility(View.GONE);
                                    adapter=new Adapter_THVL(listVideo,getActivity());
                                    adapter.notifyDataSetChanged();
                                    recyclerListVideoTHVL.setAdapter(adapter);


                                }
                            });



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonElement> call, Throwable t) {

                    }
                });



            }
        });
        thread.start();

//        new loadVideo().execute();

        return layout;
    }

//    private class loadVideo extends AsyncTask<List<VideoModel>, List<VideoModel>, List<VideoModel>> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected ArrayList<VideoModel> doInBackground(List<VideoModel>... arrayLists) {
//            ServerInterface serverInterfaceTHVL=new APIRetrofit().getjson().create(ServerInterface.class);
//            Call<JsonElement> call= serverInterfaceTHVL.getVideoTHVL();
//            call.enqueue(new Callback<JsonElement>() {
//                @Override
//                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
//
//                    try {
//                        final JSONObject data = new JSONObject(response.body().toString());
////                            Log.i("BLH",response.body().toString());
//                        Log.i("BLH","123456");
//
//                        final JSONArray arrVideo=data.getJSONArray("items");
//                        JSONObject video1 = (JSONObject) arrVideo.get(1);
//                        String number = video1.getJSONObject("id").getString("videoId");
//                        Log.i("BLH",number);
//
//
//
//
//
//
//
//                                for (int i = 0; i < arrVideo.length(); i++) {
//
//
//                                    VideoModel model = new VideoModel();
//                                    // create video model if this item is video
//                                    try {
//                                        JSONObject video = (JSONObject) arrVideo.get(i);
//                                        if(video.getJSONObject("id").has("videoId") == true) {
//                                            model.id = video.getJSONObject("id").getString("videoId");
//                                            JSONObject snippet = (JSONObject) video.getJSONObject("snippet");
//                                            model.name = snippet.getString("title");
//                                            Log.i("BLH","hello");
//                                            model.description = snippet.getString("description");
//                                            model.uploadTime = snippet.getString("publishedAt");
//
//                                            // thumbnails
//                                            JSONObject thumbnails = (JSONObject) snippet.getJSONObject("thumbnails");
//                                            model.imgThumbDefault = ((JSONObject) thumbnails.getJSONObject("default")).getString("url");
//                                            model.imgThumbMedium = ((JSONObject) thumbnails.getJSONObject("medium")).getString("url");
//                                            model.imgThumbHigh = ((JSONObject) thumbnails.getJSONObject("high")).getString("url");
//
//                                            // add model to arraylist
//
//                                            listVideo.add(model);
//
//                                        }
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                    }
//
//
//                                }
//
//
//
//
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<JsonElement> call, Throwable t) {
//
//                }
//            });
//            return (ArrayList<VideoModel>) listVideo;
//        }
//
//
//        @Override
//        protected void onProgressUpdate(List<VideoModel>... values) {
//            super.onProgressUpdate(values);
//        }
//
//
//
//        @Override
//        protected void onPostExecute(List<VideoModel> videoModels) {
//            super.onPostExecute(videoModels);
//            progressBar.setVisibility(View.GONE);
//            adapter=new Adapter_THVL(listVideo,getActivity());
//            adapter.notifyDataSetChanged();
//            recyclerListVideoTHVL.setAdapter(adapter);
//
//        }
//
//
//    }
}
