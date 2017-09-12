package com.example.buihai.my_youtube;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.example.buihai.my_youtube.DIENQUAN.Fragment_DienQuan;
import com.example.buihai.my_youtube.FISHING.Fragment_Fishing;
import com.example.buihai.my_youtube.THVL.Fragment_THVL;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private TabLayout tabLayout = null;
    private ViewPagerAdapter adapter;
    public static String STR_THVL_FRAGMENT = "THVL";
    public static String STR_FISHING_FRAGMENT = "FISHING";
    public static String STR_DIENQUAN_FRAGMENT = "LEARNING_IT";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle("MyYouTube");
        }

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        initTab();


    }

    private void initTab() {
        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.colorIndivateTab));
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

    }



    private void setupViewPager(ViewPager viewPager) {

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new Fragment_THVL(), STR_THVL_FRAGMENT);
        adapter.addFrag(new Fragment_Fishing(), STR_FISHING_FRAGMENT);
        adapter.addFrag(new Fragment_DienQuan(), STR_DIENQUAN_FRAGMENT);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {

            // return null to display only the icon
            return mFragmentTitleList.get(position);
        }
    }
}
