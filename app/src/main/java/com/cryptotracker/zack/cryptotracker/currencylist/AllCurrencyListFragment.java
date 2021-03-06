package com.cryptotracker.zack.cryptotracker.currencylist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.cryptotracker.zack.cryptotracker.CustomItemClickListener;
import com.cryptotracker.zack.cryptotracker.DrawerController;
import com.cryptotracker.zack.cryptotracker.R;
import com.cryptotracker.zack.cryptotracker.currencydetails.CurrencyDetailsTabsActivity;
import com.cryptotracker.zack.cryptotracker.currencydetails.chartandtable.GraphFragment;
import com.cryptotracker.zack.cryptotracker.models.rest.CMCCoin;
import com.cryptotracker.zack.cryptotracker.models.rest.CMCQuickSearch;
import com.cryptotracker.zack.cryptotracker.rest.CoinMarketCapService;
import com.cryptotracker.zack.cryptotracker.singletons.DatabaseHelperSingleton;
import com.grizzly.rest.Model.afterTaskCompletion;
import com.grizzly.rest.Model.afterTaskFailure;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.cryptotracker.zack.cryptotracker.SortUtil;

import static android.content.Context.MODE_PRIVATE;
import static com.cryptotracker.zack.cryptotracker.currencydetails.chartandtable.GraphFragment.SHAREDPREF_SETTINGS;
import static com.cryptotracker.zack.cryptotracker.currencylist.CurrencyListTabsActivity.SORT_SETTING;


public class AllCurrencyListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        SearchView.OnQueryTextListener {

    public static String currQuery = "";
    public static boolean searchViewFocused = false;
    ArrayList<CMCCoin> searchList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView currencyRecyclerView;
    private AllCurrencyListAdapter adapter;
    private ArrayList<CMCCoin> currencyItemList;
    private ArrayList<CMCCoin> filteredList = new ArrayList<>();
    private MenuItem searchItem;
    private SearchView searchView;
    private View rootView;
    private Context mContext;
    private HashMap<String, String> searchedSymbols = new HashMap<>();
    private HashMap<String, Integer> slugToIDMap = new HashMap<>();
    private FavoritesListUpdater favsUpdateCallback;
    private SharedPreferences sharedPreferences;
    private DrawerController drawerController;

    public AllCurrencyListFragment() {
    }

    public static AllCurrencyListFragment newInstance() {
        return new AllCurrencyListFragment();
    }

    public void performAllCoinsSort() {
        int sortType = sharedPreferences.getInt(SORT_SETTING, 1);
        SortUtil.sortList(adapter.getCurrencyList(), sortType);
        adapter.notifyDataSetChanged();
    }

    public void getQuickSearch() {
        CoinMarketCapService.getCMCQuickSearch(mContext, new afterTaskCompletion<CMCQuickSearch[]>() {
            @Override
            public void onTaskCompleted(CMCQuickSearch[] quickSearchNodeList) {
                slugToIDMap = new HashMap<>();
                Parcelable recyclerViewState;
                recyclerViewState = currencyRecyclerView.getLayoutManager().onSaveInstanceState();
                for (CMCQuickSearch node : quickSearchNodeList) {
                    slugToIDMap.put(node.getSlug(), node.getId());
                }
                if (searchViewFocused) {
                    for (CMCCoin coin : searchList) {
                        if (slugToIDMap.get(coin.getId()) != null) {
                            coin.setQuickSearchID(slugToIDMap.get(coin.getId()));
                        }
                    }
                    adapter.setCurrencyList(searchList);
                } else {
                    for (CMCCoin coin : currencyItemList) {
                        if (coin.getId() != null && slugToIDMap.get(coin.getId()) != null) {
                            coin.setQuickSearchID(slugToIDMap.get(coin.getId()));
                        }
                    }
                    adapter.setCurrencyList(currencyItemList);
                }
                int sortType = sharedPreferences.getInt(SORT_SETTING, 1);
                SortUtil.sortList(adapter.getCurrencyList(), sortType);
                adapter.notifyDataSetChanged();
                favsUpdateCallback.performFavsSort();
                currencyRecyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
                swipeRefreshLayout.setRefreshing(false);
            }
        }, new afterTaskFailure() {
            @Override
            public void onTaskFailed(Object o, Exception e) {
                Log.e("ERROR", "Server Error: " + e.getMessage());
                swipeRefreshLayout.setRefreshing(false);
            }
        }, true);
    }

    @Override
    public void onRefresh() {
        getCurrencyList();
    }

    public void getCurrencyList() {
        swipeRefreshLayout.setRefreshing(true);
        CoinMarketCapService.getAllCoins(mContext, new afterTaskCompletion<CMCCoin[]>() {
            @Override
            public void onTaskCompleted(CMCCoin[] cmcCoinList) {
                try {
                    if (searchViewFocused) { // Copy some code here to make the checks faster
                        searchedSymbols.clear();
                        searchList.clear();
                        for (CMCCoin coin : filteredList) {
                            searchedSymbols.put(coin.getSymbol(), coin.getSymbol());
                        }
                        for (CMCCoin coin : cmcCoinList) {
                            if (searchedSymbols.get(coin.getSymbol()) != null) {
                                searchList.add(coin);
                            }
                        }
                    } else {
                        currencyItemList.clear();
                        currencyItemList.addAll(Arrays.asList(cmcCoinList));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                getQuickSearch();
            }
        }, new afterTaskFailure() {
            @Override
            public void onTaskFailed(Object o, Exception e) {
                Log.e("ERROR", "Server Error: " + e.getMessage());
                swipeRefreshLayout.setRefreshing(false);
            }
        }, true);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_all_currency_list, container, false);
        setHasOptionsMenu(true);
        DatabaseHelperSingleton db = DatabaseHelperSingleton.getInstance(mContext);
        sharedPreferences = getContext().getSharedPreferences(SHAREDPREF_SETTINGS, MODE_PRIVATE);
        searchList = new ArrayList<>();
        // Setup currency list
        currencyRecyclerView = rootView.findViewById(R.id.currency_list_recycler_view);
        HorizontalDividerItemDecoration divider = new HorizontalDividerItemDecoration.Builder(mContext).build();
        currencyRecyclerView.addItemDecoration(divider);
        LinearLayoutManager llm = new LinearLayoutManager(mContext);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        currencyRecyclerView.setLayoutManager(llm);
        currencyItemList = new ArrayList<>();
        adapter = new AllCurrencyListAdapter(favsUpdateCallback, currencyItemList, db, (AppCompatActivity) mContext, new CustomItemClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Intent intent = new Intent(mContext, CurrencyDetailsTabsActivity.class);
                intent.putExtra(GraphFragment.ARG_SYMBOL, adapter.getCurrencyList().get(position).getSymbol());
                intent.putExtra(GraphFragment.ARG_ID, adapter.getCurrencyList().get(position).getId());
                intent.putExtra(GraphFragment.COIN_OBJECT, adapter.getCurrencyList().get(position));
                mContext.startActivity(intent);
            }
        });
        currencyRecyclerView.setAdapter(adapter);
        // Setup swipe refresh layout
        swipeRefreshLayout = rootView.findViewById(R.id.currency_list_swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                getCurrencyList();
            }
        });
        return rootView;
    }

    // sort button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_button:
                int sortType = sharedPreferences.getInt(SORT_SETTING, 1);
                new MaterialDialog.Builder(getActivity())
                        .title(R.string.sort_by)
                        .items(R.array.sort_options)
                        .dividerColorRes(R.color.colorPrimary)
                        .widgetColorRes(R.color.colorPrimary)
                        .buttonRippleColorRes(R.color.colorPrimary)
                        .itemsCallbackSingleChoice(sortType, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                SortUtil.sortList(adapter.getCurrencyList(), which);
                                adapter.notifyDataSetChanged();
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt(SORT_SETTING, which);
                                editor.apply();
                                favsUpdateCallback.performFavsSort();
                                Toast toast = Toast.makeText(getContext(), "Sorting by: " + text, Toast.LENGTH_SHORT);
                                toast.show();
                                return true;
                            }
                        })
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        currQuery = query;
        query = query.toLowerCase();
        filteredList.clear();
        for (CMCCoin coin : currencyItemList) {
            if (coin.getSymbol().toLowerCase().contains(query) || coin.getName().toLowerCase().contains(query)) {
                filteredList.add(coin);
            }
        }
        adapter.setCurrencyList(filteredList);
        return true;
    }

    private void showInputMethod(View view) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(view, 0);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (searchView != null && searchViewFocused) {
            ((AppCompatActivity) mContext).getSupportActionBar().setTitle("");
            searchView.requestFocusFromTouch();
            searchView.setIconified(false);
            searchView.setIconified(false);
            searchView.setQuery(currQuery, false);
            showInputMethod(rootView);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        this.drawerController = (DrawerController) getActivity();
        this.favsUpdateCallback = (FavoritesListUpdater) context;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.all_currency_list_tab_menu, menu);
        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        // Detect SearchView icon clicks
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchViewFocused = true;
                setItemsVisibility(menu, searchItem, false);
                drawerController.hideHamburger();
            }
        });
        // Detect SearchView close
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                searchViewFocused = false;
                setItemsVisibility(menu, searchItem, true);
                drawerController.showHamburger();
                return false;
            }
        });
        if (searchViewFocused) ((AppCompatActivity) mContext).getSupportActionBar().setTitle("");
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setItemsVisibility(Menu menu, MenuItem exception, boolean visible) {
        for (int i = 0; i < menu.size(); ++i) {
            MenuItem item = menu.getItem(i);
            if (item != exception) item.setVisible(visible);
        }
        if (!visible) {
            ((AppCompatActivity) mContext).getSupportActionBar().setTitle("");
        } else {
            ((AppCompatActivity) mContext).getSupportActionBar().setTitle(getResources().getString(R.string.app_name));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        searchViewFocused = false;
    }

    public AllCurrencyListAdapter getAdapter() {
        return this.adapter;
    }

    public interface FavoritesListUpdater {
        void removeFavorite(CMCCoin coin);

        void addFavorite(CMCCoin coin);

        void performFavsSort();
    }
}
