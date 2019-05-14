package it.polito.mad.customer;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class myFragmentPageAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private List<SuggestedFoodInfo> suggestedFoodInfos;
    private DailyFoodFragment dailyFoodFragment;

    public myFragmentPageAdapter(Context mContext, FragmentManager fm, List<SuggestedFoodInfo> suggestedFoodInfos) {
        super(fm);
        this.mContext = mContext;
        this.suggestedFoodInfos = suggestedFoodInfos;
    }

    @Override
    public Fragment getItem(int i) {
        if (i == 0) {
            dailyFoodFragment = new DailyFoodFragment();
            dailyFoodFragment.setSuggestedFoodInfos(suggestedFoodInfos).setContext(mContext);
            return dailyFoodFragment;
        } else if (i == 1){
            return new MenuFragment();
        } else {
            return new ReviewFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        switch (position) {
            case 0:
                return mContext.getString(R.string.category_daily_menu);
            case 1:
                return mContext.getString(R.string.category_menu);
            case 2:
                return mContext.getString(R.string.category_review);
            default:
                return null;
        }
    }

    public void refreshLayout(int fragId)
    {
        if (fragId == 0) {
            dailyFoodFragment.refreshLayout();
        } else if (fragId == 1){

        } else {

        }
    }
}
