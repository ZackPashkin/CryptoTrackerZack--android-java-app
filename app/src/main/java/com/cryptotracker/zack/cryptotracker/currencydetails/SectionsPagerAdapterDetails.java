package com.cryptotracker.zack.cryptotracker.currencydetails;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.cryptotracker.zack.cryptotracker.currencydetails.chartandtable.GraphFragment;
import com.cryptotracker.zack.cryptotracker.currencydetails.markets.MarketsFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapterDetails extends FragmentPagerAdapter {

    private String symbol;
    private String id;

    public SectionsPagerAdapterDetails(FragmentManager fm) {
        super(fm);
    }

    protected SectionsPagerAdapterDetails(FragmentManager fm, String symbol, String id) {
        super(fm);
        this.symbol = symbol;
        this.id = id;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a GraphFragment (defined as a static inner class below).
        switch (position) {
            case 0:
                return GraphFragment.newInstance(this.symbol, this.id);
            case 1:
                return MarketsFragment.newInstance(this.symbol);
        }
        return null;
    }

    @Override
    public int getCount() {
        // Total pages to show
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Chart";
            case 1:
                return "Markets";
            default:
                return null;
        }
    }
}
