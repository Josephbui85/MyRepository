package com.example.huaanhhong.chatvn.Function.Contact;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.huaanhhong.chatvn.R;

import java.util.List;

/**
 * Created by huaanhhong on 23/08/2017.
 */

public class ContactRecycleAdapter extends RecyclerView.Adapter<ContactRecycleAdapter.ViewHolder>{

    List<Contact> listcontact;
    Context context;


    public ContactRecycleAdapter(List<Contact> listcontact, Context context) {
        this.listcontact = listcontact;
        this.context = context;
    }




    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutin= LayoutInflater.from(parent.getContext());
        View itemView= layoutin.inflate(R.layout.item_recycler_contact,parent,false);

        return new ViewHolder(itemView);


    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mTxtMobile.setText( listcontact.get(position).getID());
        holder.mTxtnameontact.setText(listcontact.get(position).getName());

        final String mobile=listcontact.get(position).getID();
        byte[]image=listcontact.get(position).getImage();
        if ( image != null) {
            holder.mImage.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
//
//            Glide.with(context)
//                    .load(image)
//                    .into(holder.mImage);
        }
        holder.mLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mobile!=null){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + mobile));
                    intent.putExtra("sms_body", "Minh dang su dung ChatVn de nhan tin, hay cung dung voi minh de chung ta tro chuyen voi nhau nhe!Vui lam!");
                    context.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return listcontact.size();

    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout mLinear;
        TextView mTxtnameontact,mTxtMobile;
        ImageView mImage;
        public ViewHolder(View itemView) {
            super(itemView);
            mLinear=(LinearLayout)itemView.findViewById(R.id.linear);
            mImage=(ImageView) itemView.findViewById(R.id.image_contact);
            mTxtMobile=(TextView) itemView.findViewById(R.id.txt_mobilecontact);
            mTxtnameontact=(TextView) itemView.findViewById(R.id.txt_namecontact);
        }
    }
}
