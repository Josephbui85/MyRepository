package com.example.huaanhhong.chatvn.Function.Contact;

import android.support.annotation.NonNull;

/**
 * Created by huaanhhong on 23/08/2017.
 */

public class Contact implements Comparable<Contact>{

    String mName;
    String mID;
    byte[] mImage;

    public Contact() {
    }

    public Contact(String mName, String mID) {
        this.mName = mName;
        this.mID = mID;
    }

    public byte[] getImage() {
        return mImage;
    }

    public void setImage(byte[] mImage) {
        this.mImage = mImage;
    }

    public Contact(String mName, String mID, byte[] mImage) {

        this.mName = mName;
        this.mID = mID;
        this.mImage = mImage;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getID() {
        return mID;
    }

    public void setID(String mID) {
        this.mID = mID;
    }

    @Override
    public int compareTo(@NonNull Contact o) {
        return getName().toString().compareTo(o.getName().toString());
    }
}

