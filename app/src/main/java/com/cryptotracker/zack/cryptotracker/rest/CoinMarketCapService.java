package com.cryptotracker.zack.cryptotracker.rest;

import android.content.Context;
import android.util.Log;

import com.cryptotracker.zack.cryptotracker.currencydetails.chartandtable.GraphFragment;
import com.cryptotracker.zack.cryptotracker.models.rest.CMCChartData;
import com.cryptotracker.zack.cryptotracker.models.rest.CMCCoin;
import com.cryptotracker.zack.cryptotracker.models.rest.CMCQuickSearch;
import com.grizzly.rest.GenericRestCall;
import com.grizzly.rest.Model.afterTaskCompletion;
import com.grizzly.rest.Model.afterTaskFailure;

import org.springframework.http.HttpMethod;


public class CoinMarketCapService {

    public static final String COIN_MARKETCAP_ALL_COINS_URL = "https://api.coinmarketcap.com/v1/ticker/?limit=10"; //to switch to local server use: http://192.168.1.15:3000/crypto
    public static final String COIN_MARKETCAP_CHART_URL_ALL_DATA = "https://graphs2.coinmarketcap.com/currencies/%s/";
    public static final String COIN_MARKETCAP_CHART_URL_WINDOW = "https://graphs2.coinmarketcap.com/currencies/%s/%s/%s/";
    public static final String COIN_MARKETCAP_QUICK_SEARCH_URL = "https://s2.coinmarketcap.com/generated/search/quick_search.json";

    public static void getAllCoins(Context context, afterTaskCompletion<CMCCoin[]> taskCompletion, afterTaskFailure failure, boolean async) {
        new GenericRestCall<>(Void.class, CMCCoin[].class, String.class)
                .setUrl(COIN_MARKETCAP_ALL_COINS_URL)
                .setContext(context.getApplicationContext())
                .isCacheEnabled(true)
                .setCacheTime(300000L)
                .setMethodToCall(HttpMethod.GET)
                .setTaskCompletion(taskCompletion)
                .setTaskFailure(failure)
                .setAutomaticCacheRefresh(true).execute(async);
    }

    public static void getCMCChartData(Context context, String coinID, afterTaskCompletion<CMCChartData> taskCompletion, afterTaskFailure failure, boolean async) {
        Log.d("I", "URL: " + GraphFragment.CURRENT_CHART_URL);
        new GenericRestCall<>(Void.class, CMCChartData.class, String.class)
                .setUrl(String.format(GraphFragment.CURRENT_CHART_URL, coinID))
                .setContext(context.getApplicationContext())
                .isCacheEnabled(true)
                .setCacheTime(60000L)
                .setMethodToCall(HttpMethod.GET)
                .setTaskCompletion(taskCompletion)
                .setTaskFailure(failure)
                .setAutomaticCacheRefresh(false).execute(async);
    }

    public static void getCMCQuickSearch(Context context, afterTaskCompletion<CMCQuickSearch[]> taskCompletion, afterTaskFailure failure, boolean async) {
        Log.d("I", "Getting URL in getCMCQuickSearch: " + COIN_MARKETCAP_QUICK_SEARCH_URL);
        new GenericRestCall<>(Void.class, CMCQuickSearch[].class, String.class)
                .setUrl(COIN_MARKETCAP_QUICK_SEARCH_URL)
                .setContext(context.getApplicationContext())
                .isCacheEnabled(true)
                // cache for 6 hours
                .setCacheTime(21600000L)
                .setMethodToCall(HttpMethod.GET)
                .setTaskCompletion(taskCompletion)
                .setTaskFailure(failure)
                .setAutomaticCacheRefresh(true).execute(async);
    }

}
