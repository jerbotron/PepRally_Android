package com.peprally.jeremy.peprally.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.List;

public class ProfileViewPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    public ProfileViewPagerAdapter(FragmentManager manager) {
        super(manager);
        this.fragmentManager = manager;
        fragmentTransaction = fragmentManager.beginTransaction();
    }
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }
    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }
    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFrag(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    public void removeFrag(int position) {
        mFragmentList.remove(position);
        mFragmentTitleList.remove(position);
    }

    public void detachFrag(int position) {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.detach(mFragmentList.get(position));
        fragmentTransaction.commit();
    }

    public void attachFrag(int position) {
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.attach(mFragmentList.get(position));
        fragmentTransaction.commit();
    }

    public void disableLeftSwipe() {

    }
}
