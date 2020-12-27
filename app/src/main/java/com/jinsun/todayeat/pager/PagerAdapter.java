package com.jinsun.todayeat.pager;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.jinsun.todayeat.exclusionlist.ExclusionListFragment;
import com.jinsun.todayeat.search.SearchFragment;

import static com.jinsun.todayeat.Constants.PAGER_EXCLUSION_LIST;
import static com.jinsun.todayeat.Constants.PAGER_SEARCH;

public class PagerAdapter extends FragmentStatePagerAdapter {
    private static final int NUM_ITEMS_MAIN_PAGER = 2;

    public PagerAdapter(FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case PAGER_SEARCH:
                return new SearchFragment();
            case PAGER_EXCLUSION_LIST:
                return new ExclusionListFragment();
        }
        return new SearchFragment();
    }

    @Override
    public int getCount() {
        return NUM_ITEMS_MAIN_PAGER;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.destroyItem(container, position, object);
    }
}
