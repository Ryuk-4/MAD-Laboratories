package it.polito.mad.deliverman;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class SectionsPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> myFragmentList = new ArrayList<>();
    private final List<String> myFragmentsTitleList = new ArrayList<>();

    public SectionsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragments(Fragment fragment, String title){
        myFragmentList.add(fragment);
        myFragmentsTitleList.add(title);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return myFragmentsTitleList.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        return myFragmentList.get(position);
    }

    @Override
    public int getCount() {
        // Show 3 total pages.
        return myFragmentList.size();
    }
}