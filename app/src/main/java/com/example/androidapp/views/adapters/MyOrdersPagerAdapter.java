package com.example.androidapp.views.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.androidapp.views.fragments.MyOrderListFragment;

public class MyOrdersPagerAdapter extends FragmentStateAdapter {

    private final String[] statusValues;

    public MyOrdersPagerAdapter(@NonNull FragmentActivity fragmentActivity, String[] statusValues) {
        super(fragmentActivity);
        this.statusValues = statusValues;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return MyOrderListFragment.newInstance(statusValues[position]);
    }

    @Override
    public int getItemCount() {
        return statusValues.length;
    }
}