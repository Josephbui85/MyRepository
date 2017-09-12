package com.example.buihai.my_youtube.DIENQUAN;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.buihai.my_youtube.R;
import com.example.buihai.my_youtube.VideoModel;

import java.util.List;

/**
 * Created by buihai on 9/7/17.
 */

public class AdapterDienQuan extends RecyclerView.Adapter<AdapterDienQuan.ViewHolder> {
    List<VideoModel> listVideo;
    Context context;

    public AdapterDienQuan(List<VideoModel> listVideo, Context context) {
        this.listVideo = listVideo;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_dienquan,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        holder.txtTitle.setText(listVideo.get(position).name);
        holder.txtTime.setText(listVideo.get(position).uploadTime.substring(0,10));

        Glide.with(context)
                .load(listVideo.get(position).imgThumbDefault)
                .into(holder.imageView);

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v="+listVideo.get(position).id)));
                Log.i("Video", "Video Playing....");

            }
        });

    }

    @Override
    public int getItemCount() {
        return listVideo.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtTitle, txtTime;
        public ImageView imageView;
        public LinearLayout linearLayout;
        public ViewHolder(View itemView) {
            super(itemView);

            txtTitle=(TextView) itemView.findViewById(R.id.tv_TitleDienQuan);
            txtTime=(TextView) itemView.findViewById(R.id.tv_TimeDienQuan);
            imageView=(ImageView) itemView.findViewById(R.id.img_viewDienQuan);
            linearLayout=(LinearLayout) itemView.findViewById(R.id.linearlayout);
        }
    }
}
