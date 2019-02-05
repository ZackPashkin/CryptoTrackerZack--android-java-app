package com.cryptotracker.zack.cryptotracker.currencylist;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.view.View;
import android.widget.Toast;


import com.cryptotracker.zack.cryptotracker.BuildConfig;
import com.cryptotracker.zack.cryptotracker.DrawerController;
import com.cryptotracker.zack.cryptotracker.R;
import com.cryptotracker.zack.cryptotracker.TextDrawable;
import com.cryptotracker.zack.cryptotracker.models.rest.CMCCoin;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;


public class CurrencyListTabsActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener,
        FavoriteCurrencyListFragment.AllCoinsListUpdater, AllCurrencyListFragment.FavoritesListUpdater, DrawerController {

    public final static String DAY = "24h";
    public final static String WEEK = "7d";
    public final static String HOUR = "1h";
    public final static String SORT_SETTING = "sort_setting";
    public static String baseImageURL = "";
    public static String SYMBOL = "SYMBOL";
    public static String IMAGE_URL_FORMAT = "https://s2.coinmarketcap.com/static/img/coins/32x32/%s.png";
    public ViewPager mViewPager;
    public AppCompatActivity context;
    boolean doubleBackToExitPressedOnce = false;
    private SectionsPagerAdapterCurrencyList mSectionsPagerAdapter;
    private Toolbar mToolbar;
    private LibsBuilder libsBuilder;
    private Drawer drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_list_tabs);
        context = this;
        mToolbar = findViewById(R.id.toolbar_currency_list);
        setSupportActionBar(mToolbar);
        TabLayout tabLayout = findViewById(R.id.currency_list_tabs);
        mViewPager = findViewById(R.id.currency_list_tabs_container);
        libsBuilder = new LibsBuilder()
                //provide a style (optional) (LIGHT, DARK, LIGHT_DARK_TOOLBAR)
                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                .withAboutIconShown(true)
                .withLicenseShown(true)
                .withVersionShown(true)
                .withAboutVersionShownName(true)
                .withAboutVersionShownCode(true)
                .withAboutVersionString("Version: " + BuildConfig.VERSION_NAME)
                .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                .withActivityTitle("cryptoTrackerZack ")
                .withLibraries("easyrest", "materialabout", "androiddevicenames", "customtabs", "togglebuttongroup", "materialfavoritebutton");

        TextDrawable t = new TextDrawable(this);
        t.setText("");
        t.setTextAlign(Layout.Alignment.ALIGN_CENTER);
        t.setTextColor(Color.BLACK);
        t.setTextSize(10);
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(t).build();
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mToolbar)
                .withSelectedItem(1)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withIdentifier(1).withName(R.string.Home).withIcon(FontAwesome.Icon.faw_home),
                        new PrimaryDrawerItem().withIdentifier(2).withName(R.string.About).withIcon(FontAwesome.Icon.faw_question_circle)

                )
                .withTranslucentStatusBar(false)
                .build();
        drawer.setOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                switch (position) {
                    case 1:
                        drawer.closeDrawer();
                        return true;
                    case 2:
                        drawer.closeDrawer();
                        drawer.setSelection(1);
                        libsBuilder.start(context);
                    default:
                        return true;
                }
            }
        });

        mSectionsPagerAdapter = new SectionsPagerAdapterCurrencyList(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.addOnPageChangeListener(this);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setSelectedTabIndicatorColor(Color.WHITE);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

        Fragment fragment = mSectionsPagerAdapter.getFragment(position);
        if (fragment != null) {
            fragment.onResume();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Tap back again to exit.", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    public void hideHamburger() {
        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);
    }

    public void showHamburger() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);
    }

    public void removeFavorite(CMCCoin coin) {
        FavoriteCurrencyListFragment frag = (FavoriteCurrencyListFragment) mSectionsPagerAdapter.getFragment(1);
        if (frag != null) {
            frag.removeFavorite(coin);
        }
    }

    public void addFavorite(CMCCoin coin) {
        FavoriteCurrencyListFragment frag = (FavoriteCurrencyListFragment) mSectionsPagerAdapter.getFragment(1);
        if (frag != null) {
            frag.addFavorite(coin);
        }
    }

    public void allCoinsModifyFavorites(CMCCoin coin) {
        AllCurrencyListFragment frag = (AllCurrencyListFragment) mSectionsPagerAdapter.getFragment(0);
        if (frag != null) {
            frag.getAdapter().notifyDataSetChanged();
        }
    }

    public void performFavsSort() {
        FavoriteCurrencyListFragment frag = (FavoriteCurrencyListFragment) mSectionsPagerAdapter.getFragment(1);
        if (frag != null) {
            frag.performFavsSort();
        }
    }

    public void performAllCoinsSort() {
        AllCurrencyListFragment frag = (AllCurrencyListFragment) mSectionsPagerAdapter.getFragment(0);
        if (frag != null) {
            frag.performAllCoinsSort();
        }
    }

}
