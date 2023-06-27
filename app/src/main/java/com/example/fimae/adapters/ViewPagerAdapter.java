package com.example.fimae.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.fimae.fragments.ChatFragment;
import com.example.fimae.fragments.FeedFragment;
import com.example.fimae.fragments.HomeFragment;
import com.example.fimae.fragments.ProfileFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new HomeFragment();
            case 1:
                return new FeedFragment();
            case 2:
                return new ChatFragment();
            case 3:
                return new ProfileFragment();
            default:
                return new HomeFragment();

        }
    }

    @Override
    public int getCount() {
        return 4;
    }
}