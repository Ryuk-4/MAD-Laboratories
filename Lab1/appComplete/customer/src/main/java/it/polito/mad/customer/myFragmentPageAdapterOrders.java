package it.polito.mad.customer;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class myFragmentPageAdapterOrders extends FragmentPagerAdapter {

    private Context mContext;
    private List<OrdersInfo> ordersInfoPending, ordersInfoCompleted;

    public myFragmentPageAdapterOrders(Context mContext, FragmentManager fm, List<OrdersInfo> ordersInfoPending, List<OrdersInfo> ordersInfoCompleted) {
        super(fm);
        this.mContext = mContext;
        this.ordersInfoPending = ordersInfoPending;
        this.ordersInfoCompleted = ordersInfoCompleted;
    }

    @Override
    public Fragment getItem(int i) {
        if (i == 0) {
            OrdersPendingFragment ordersPendingFragment = new OrdersPendingFragment();
            ordersPendingFragment.setOrderInfos(ordersInfoPending).setContext(mContext);
            return ordersPendingFragment;
        } else {
            OrdersCompletedFragment ordersCompletedFragment = new OrdersCompletedFragment();
            ordersCompletedFragment.setOrderInfo(ordersInfoCompleted).setContext(mContext);
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
}
