package com.example.buihai.my_youtube.THVL;

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
 * Created by buihai on 9/6/17.
 */

public class Adapter_THVL extends RecyclerView.Adapter<Adapter_THVL.ViewHolder> {

    List<VideoModel> listVieo;
    Context context;

    public Adapter_THVL(List<VideoModel> listVieo, Context context) {
        this.listVieo = listVieo;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_thvl,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.txtTitle.setText(listVieo.get(position).name);
        holder.txtTime.setText(listVieo.get(position).uploadTime.substring(0,10));

        Glide.with(context)
                .load(listVieo.get(position).imgThumbDefault)
                .into(holder.imgView);

        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v="+listVieo.get(position).id)));
                Log.i("Video", "Video Playing....");

            }
        });

    }

    @Override
    public int getItemCount() {
        return listVieo.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txtTitle, txtTime;
        public ImageView imgView;
        public LinearLayout linearLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            imgView = (ImageView) itemView.findViewById(R.id.img_viewTHVL);
            txtTitle = (TextView) itemView.findViewById(R.id.tv_TitleTHVL);
            txtTime = (TextView) itemView.findViewById(R.id.tv_TimeTHVL);
            linearLayout=(LinearLayout) itemView.findViewById(R.id.linearlayout);
        }
    }
}
