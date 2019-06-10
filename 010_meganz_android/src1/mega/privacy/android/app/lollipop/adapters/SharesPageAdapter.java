package mega.privacy.android.app.lollipop.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import mega.privacy.android.app.R;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.managerSections.IncomingSharesFragmentLollipop;
import mega.privacy.android.app.lollipop.managerSections.OutgoingSharesFragmentLollipop;
import mega.privacy.android.app.utils.Util;

public class SharesPageAdapter extends FragmentStatePagerAdapter {

    final int PAGE_COUNT = 2;
    private Context context;

    public SharesPageAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        log("getItem: "+position);
        switch (position){
            case 0: {
                IncomingSharesFragmentLollipop isF = (IncomingSharesFragmentLollipop) ((ManagerActivityLollipop) context).getSupportFragmentManager().findFragmentByTag(ManagerActivityLollipop.FragmentTag.INCOMING_SHARES.getTag());
                if (isF != null) {
                    return isF;
                }
                else {
                    return IncomingSharesFragmentLollipop.newInstance();
                }
            }
            case 1:{
                OutgoingSharesFragmentLollipop osF = (OutgoingSharesFragmentLollipop) ((ManagerActivityLollipop) context).getSupportFragmentManager().findFragmentByTag(ManagerActivityLollipop.FragmentTag.OUTGOING_SHARES.getTag());
                if (osF != null)  {
                    return osF;
                }
                else {
                    return OutgoingSharesFragmentLollipop.newInstance();
                }
            }
        }
        return null;
    }

    @Override
    public int getItemPosition(Object obj) {
        return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        switch (position){
            case 0: {
                return context.getString(R.string.tab_incoming_shares).toLowerCase();
            }
            case 1:{
                return context.getString(R.string.tab_outgoing_shares).toLowerCase();
            }
        }
        return null;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    private static void log(String log) {
        Util.log("SharesPageAdapter", log);
    }
}
