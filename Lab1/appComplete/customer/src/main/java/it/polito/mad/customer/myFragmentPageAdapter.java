package it.polito.mad.customer;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class myFragmentPageAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private List<SuggestedFoodInfo> suggestedFoodInfo;
    private List<ReviewInfo> reviewInfo;

    public myFragmentPageAdapter(Context mContext, FragmentManager fm, List<SuggestedFoodInfo> suggestedFoodInfo, List<ReviewInfo> reviewInfo) {
        super(fm);
        this.mContext = mContext;
        this.suggestedFoodInfo = suggestedFoodInfo;
        this.reviewInfo = reviewInfo;
    }

    @Override
    public Fragment getItem(int i) {
        if (i == 0) {
            DailyFoodFragment dailyFoodFragment = new DailyFoodFragment();
            dailyFoodFragment.setSuggestedFoodInfos(suggestedFoodInfo).setContext(mContext);
            return dailyFoodFragment;
        } else {
            ReviewFragment reviewFragment = new ReviewFragment();
            reviewFragment.setReviewInfos(reviewInfo).setContext(mContext);
            return reviewFragment;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

}
