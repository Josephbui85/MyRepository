package com.example.huaanhhong.chatvn.Function.Adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.huaanhhong.chatvn.Function.CallLinphone.CallFragment;
import com.example.huaanhhong.chatvn.Function.ChatFirebase.ChatFragment;
import com.example.huaanhhong.chatvn.Function.ChatFirebase.ChatGroupFragment;
import com.example.huaanhhong.chatvn.Function.Contact.ContactFragment;
import com.example.huaanhhong.chatvn.Function.SettingFragment;

/**
 * Created by Asus on 8/15/2017.
 */

public class PagerAdapter extends FragmentPagerAdapter {
    int mNumOfTabs;


    public PagerAdapter(FragmentManager fm, int mNumOfTabs) {
        super(fm);
        this.mNumOfTabs = mNumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                ChatFragment tab1 = new ChatFragment();
                return tab1;
            case 1:
                ContactFragment tab2 = new ContactFragment();
                return tab2;
            case 2:
                CallFragment tab3 = new CallFragment();
                return tab3;
            case 3:
                ChatGroupFragment tab4= new ChatGroupFragment();
                return tab4;
            case 4:
                SettingFragment tab5= new SettingFragment();
                return tab5;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
