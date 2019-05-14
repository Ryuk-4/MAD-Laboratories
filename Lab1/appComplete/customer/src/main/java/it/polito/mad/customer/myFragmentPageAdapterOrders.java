package it.polito.mad.customer;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class myFragmentPageAdapterOrders extends FragmentPagerAdapter {

    private Context mContext;
    private List<OrdersInfo> ordersInfosPending, ordersInfosCompleted;
    private OrdersPendingFragment ordersPendingFragment;
    private OrdersCompletedFragment ordersCompletedFragment;

    public myFragmentPageAdapterOrders(Context mContext, FragmentManager fm, List<OrdersInfo> ordersInfosPending, List<OrdersInfo> ordersInfosCompleted) {
        super(fm);
        this.mContext = mContext;
        this.ordersInfosPending = ordersInfosPending;
        this.ordersInfosCompleted = ordersInfosCompleted;
    }

    @Override
    public Fragment getItem(int i) {
        if (i == 0) {
            ordersPendingFragment = new OrdersPendingFragment();
            ordersPendingFragment.setOrderInfos(ordersInfosPending).setContext(mContext);
            return ordersPendingFragment;
        } else {
            ordersCompletedFragment = new OrdersCompletedFragment();
            ordersCompletedFragment.setOrderInfos(ordersInfosCompleted).setContext(mContext);
            return ordersCompletedFragment;
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        switch (position) {
            case 0:
                return mContext.getString(R.string.category_pending_order);
            case 1:
                return mContext.getString(R.string.category_completed_order);
            default:
                return null;
        }
    }

    public void refreshLayout(int fragId)
    {
        if (fragId == 0) {
            ordersPendingFragment.refreshLayout();
        } else if (fragId == 1){
            ordersCompletedFragment.refreshLayout();
        }
    }

}
