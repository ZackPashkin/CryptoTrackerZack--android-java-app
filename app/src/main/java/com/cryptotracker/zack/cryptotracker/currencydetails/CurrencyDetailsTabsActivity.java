package com.cryptotracker.zack.cryptotracker.currencydetails;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.cryptotracker.zack.cryptotracker.R;
import com.cryptotracker.zack.cryptotracker.currencydetails.chartandtable.GraphFragment;


/**
 * Created by Zack
 */

public class CurrencyDetailsTabsActivity extends AppCompatActivity {
    public CustomViewPager mViewPager;
    private SectionsPagerAdapterDetails mSectionsPagerAdapter;
    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_details_tabs);
        mToolbar = findViewById(R.id.toolbar_currency_details);
        setSupportActionBar(mToolbar);
        String symbol = getIntent().getStringExtra(GraphFragment.ARG_SYMBOL);
        String id = getIntent().getStringExtra(GraphFragment.ARG_ID);
        mViewPager = findViewById(R.id.currencyTabsViewPager);
        mSectionsPagerAdapter = new SectionsPagerAdapterDetails(getSupportFragmentManager(), symbol, id);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setSelectedTabIndicatorColor(Color.WHITE);
        getSupportActionBar().setTitle(symbol);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.currency_tabs_menu, menu);
        return true;
    }

    

}
