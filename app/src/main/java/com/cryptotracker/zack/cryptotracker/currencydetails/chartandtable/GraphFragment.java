package com.cryptotracker.zack.cryptotracker.currencydetails.chartandtable;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.cryptotracker.zack.cryptotracker.R;
import com.cryptotracker.zack.cryptotracker.currencydetails.CustomViewPager;
import com.cryptotracker.zack.cryptotracker.formatters.MonthSlashDayDateFormatter;
import com.cryptotracker.zack.cryptotracker.formatters.MonthSlashYearFormatter;
import com.cryptotracker.zack.cryptotracker.formatters.TimeDateFormatter;
import com.cryptotracker.zack.cryptotracker.models.rest.CMCChartData;
import com.cryptotracker.zack.cryptotracker.models.rest.CMCCoin;
import com.cryptotracker.zack.cryptotracker.rest.CoinMarketCapService;
import com.cryptotracker.zack.cryptotracker.singletons.CurrencyFormatterSingleton;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.grizzly.rest.Model.afterTaskCompletion;
import com.grizzly.rest.Model.afterTaskFailure;
import com.nex3z.togglebuttongroup.SingleSelectToggleGroup;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;
import static com.cryptotracker.zack.cryptotracker.rest.CoinMarketCapService.COIN_MARKETCAP_CHART_URL_ALL_DATA;
import static com.cryptotracker.zack.cryptotracker.rest.CoinMarketCapService.COIN_MARKETCAP_CHART_URL_WINDOW;

/**
 * A placeholder fragment containing a simple view.
 */
public class GraphFragment extends Fragment implements OnChartValueSelectedListener {

    public final static String BASE_CMC_SOURCE_URL = "https://coinmarketcap.com/currencies/";
    public final static DecimalFormat rawNumberFormat = new DecimalFormat("#,###.##");
    public static final String SHAREDPREF_SETTINGS = "cryptobuddy_settings";
    public static final String CHART_SPINNER_SETTING = "chart_spinner_setting";
    public static final String ARG_SYMBOL = "symbol";
    public static final String ARG_ID = "ID";
    public static final String COIN_OBJECT = "COIN_OBJECT";
    private final static String CHART_URL_WEEK = "https://min-api.cryptocompare.com/data/histohour?fsym=%s&tsym=USD&limit=168&aggregate=1";
    private final static String CHART_URL_ALL_DATA = "https://min-api.cryptocompare.com/data/histoday?fsym=%s&tsym=USD&allData=true";
    private final static String CHART_URL_YEAR = "https://min-api.cryptocompare.com/data/histoday?fsym=%s&tsym=USD&limit=183&aggregate=2";
    private final static String CHART_URL_MONTH = "https://min-api.cryptocompare.com/data/histohour?fsym=%s&tsym=USD&limit=240&aggregate=3";
    private final static String CHART_URL_3_MONTH = "https://min-api.cryptocompare.com/data/histohour?fsym=%s&tsym=USD&limit=240&aggregate=14";
    private final static String CHART_URL_1_DAY = "https://min-api.cryptocompare.com/data/histominute?fsym=%s&tsym=USD&limit=144&aggregate=10";
    private final static String TICKER_URL = "https://min-api.cryptocompare.com/data/pricemultifull?fsyms=%s&tsyms=USD";
    public static String CURRENT_CHART_URL;
    public final IAxisValueFormatter monthSlashDayXAxisFormatter = new MonthSlashDayDateFormatter();
    public final TimeDateFormatter dayCommaTimeDateFormatter = new TimeDateFormatter();
    public final MonthSlashYearFormatter monthSlashYearFormatter = new MonthSlashYearFormatter();
    NumberFormat chartUSDPriceFormat = NumberFormat.getInstance();
    SimpleDateFormat fullDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH);
    private int chartFillColor;
    private int chartBorderColor;
    private String cryptoID;
    private int percentageColor;
    private LineChart lineChart;
    private View rootView;
    private CustomViewPager viewPager;
    private IAxisValueFormatter XAxisFormatter;
    private String currentTimeWindow = "";
    private SingleSelectToggleGroup buttonGroup;
    private LockableNestedScrollView nestedScrollView;
    private int displayWidth;
    private ProgressBar chartProgressBar;
    private String tsymbol;
    private CurrencyFormatterSingleton currencyFormatter;
    private SharedPreferences sharedPreferences;

    public GraphFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static GraphFragment newInstance(String symbol, String id) {
        GraphFragment fragment = new GraphFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SYMBOL, symbol);
        args.putString(ARG_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    public void setColors(float percentChange) {
        if (percentChange >= 0) {
            chartFillColor = ResourcesCompat.getColor(getActivity().getResources(), R.color.materialLightGreen, null);
            chartBorderColor = ResourcesCompat.getColor(getActivity().getResources(), R.color.darkGreen, null);
            percentageColor = ResourcesCompat.getColor(getActivity().getResources(), R.color.percentPositiveGreen, null);
        } else {
            chartFillColor = ResourcesCompat.getColor(getActivity().getResources(), R.color.materialLightRed, null);
            chartBorderColor = ResourcesCompat.getColor(getActivity().getResources(), R.color.darkRed, null);
            percentageColor = ResourcesCompat.getColor(getActivity().getResources(), R.color.percentNegativeRed, null);
        }
    }

    public void setUpChart() {
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawAxisLine(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        xAxis.setAvoidFirstLastClipping(true);
        lineChart.getAxisLeft().setEnabled(true);
        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getXAxis().setDrawGridLines(false);
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setScaleEnabled(false);
        lineChart.getDescription().setEnabled(false);
        lineChart.setContentDescription("");
        lineChart.setNoDataText(getString(R.string.noChartDataString));
        lineChart.setNoDataTextColor(R.color.darkRed);
        lineChart.setOnChartValueSelectedListener(this);
        lineChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                YAxis yAxis = lineChart.getAxisLeft();
                // Allow scrolling in the right and left margins
                if (me.getX() > yAxis.getLongestLabel().length() * yAxis.getTextSize() &&
                        me.getX() < displayWidth - lineChart.getViewPortHandler().offsetRight()) {
                    viewPager.setPagingEnabled(false);
                    nestedScrollView.setScrollingEnabled(false);
                }
            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                viewPager.setPagingEnabled(true);
                nestedScrollView.setScrollingEnabled(true);
            }

            @Override
            public void onChartLongPressed(MotionEvent me) {

            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {

            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {

            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {

            }
        });
    }

    public LineDataSet setUpLineDataSet(List<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, "Price");
        dataSet.setColor(chartBorderColor);
        dataSet.setFillColor(chartFillColor);
        dataSet.setDrawHighlightIndicators(true);
        dataSet.setDrawFilled(true);
        dataSet.setDrawCircles(true);
        dataSet.setCircleColor(chartBorderColor);
        dataSet.setDrawCircleHole(false);
        dataSet.setDrawValues(false);
        dataSet.setCircleRadius(1);
        dataSet.setHighlightLineWidth(2);
        dataSet.setHighlightEnabled(true);
        dataSet.setDrawHighlightIndicators(true);
        dataSet.setHighLightColor(chartBorderColor); // color for highlight indicator
        return dataSet;
    }

    public void getCMCChart() {
        final TextView percentChangeText = rootView.findViewById(R.id.percent_change);
        final TextView currPriceText = rootView.findViewById(R.id.current_price);
        lineChart.setEnabled(true);
        lineChart.clear();
        chartProgressBar.setVisibility(View.VISIBLE);
        CoinMarketCapService.getCMCChartData(getActivity(), cryptoID, new afterTaskCompletion<CMCChartData>() {
            @Override
            public void onTaskCompleted(CMCChartData cmcChartData) {
                List<Entry> closePrices = new ArrayList<>();
                if (tsymbol.equals("USD")) {
                    for (List<Float> priceTimeUnit : cmcChartData.getPriceUSD()) {
                        closePrices.add(new Entry(priceTimeUnit.get(0), priceTimeUnit.get(1)));
                    }
                } else {
                    for (List<Float> priceTimeUnit : cmcChartData.getPriceBTC()) {
                        closePrices.add(new Entry(priceTimeUnit.get(0), priceTimeUnit.get(1)));
                    }
                }
                if (closePrices.size() == 0) {
                    lineChart.setData(null);
                    lineChart.setEnabled(false);
                    lineChart.invalidate();
                    percentChangeText.setText("");
                    currPriceText.setText("");
                    lineChart.setNoDataText(getString(R.string.noChartDataString));
                    chartProgressBar.setVisibility(View.GONE);
                    return;
                }
                XAxis xAxis = lineChart.getXAxis();
                xAxis.setValueFormatter(XAxisFormatter);
                TextView currentPriceTextView = rootView.findViewById(R.id.current_price);
                float currPrice = closePrices.get(closePrices.size() - 1).getY();
                TextView chartDateTextView = rootView.findViewById(R.id.graphFragmentDateTextView);
                chartDateTextView.setText(getFormattedFullDate(closePrices.get(closePrices.size() - 1).getX()));
                if (tsymbol.equals("USD")) {
                    currentPriceTextView.setText(String.format(getString(R.string.unrounded_usd_chart_price_format), String.valueOf(currPrice)));
                } else {
                    currentPriceTextView.setText(currencyFormatter.format(currPrice, "BTC"));
                }
                currentPriceTextView.setTextColor(Color.BLACK);
                float firstPrice = closePrices.get(0).getY();
                // Handle edge case where we dont have data for the interval on the chart. E.g. user selects
                // 3 month window, but we only have data for last month
                for (Entry e : closePrices) {
                    if (firstPrice != 0) {
                        break;
                    } else {
                        firstPrice = e.getY();
                    }
                }
                float difference = (currPrice - firstPrice);
                float percentChange = (difference / firstPrice) * 100;
                if (percentChange < 0) {
                    if (tsymbol.equals("USD")) {
                        percentChangeText.setText(String.format(getString(R.string.negative_variable_pct_change_with_dollars_format), currentTimeWindow, percentChange, Math.abs(difference)));
                    } else {
                        percentChangeText.setText(String.format(getString(R.string.negative_variable_pct_change_without_dollars_format), currentTimeWindow, percentChange));
                    }
                } else {
                    if (tsymbol.equals("USD")) {
                        percentChangeText.setText(String.format(getString(R.string.positive_variable_pct_change_with_dollars_format), currentTimeWindow, percentChange, Math.abs(difference)));
                    } else {
                        percentChangeText.setText(String.format(getString(R.string.positive_variable_pct_change_without_dollars_format), currentTimeWindow, percentChange));
                    }
                }
                setColors(percentChange);
                percentChangeText.setTextColor(percentageColor);
                LineDataSet dataSet = setUpLineDataSet(closePrices);
                LineData lineData = new LineData(dataSet);
                lineChart.setData(lineData);
                lineChart.animateX(800);
                chartProgressBar.setVisibility(View.GONE);
            }
        }, new afterTaskFailure() {
            @Override
            public void onTaskFailed(Object o, Exception e) {
                Log.e("ERROR", "Server Error: " + e.getMessage());
                lineChart.setNoDataText(getString(R.string.noChartDataString));
                chartProgressBar.setVisibility(View.GONE);
            }
        }, true);
    }

    public void setDayChecked(Calendar cal) {
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();
        cal.clear();
        CURRENT_CHART_URL = String.format(COIN_MARKETCAP_CHART_URL_WINDOW, cryptoID, startTime, endTime);
        currentTimeWindow = getString(R.string.oneDay);
        XAxisFormatter = dayCommaTimeDateFormatter;
    }

    public void setWeekChecked(Calendar cal) {
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -7);
        long startTime = cal.getTimeInMillis();
        cal.clear();
        CURRENT_CHART_URL = String.format(COIN_MARKETCAP_CHART_URL_WINDOW, cryptoID, startTime, endTime);
        currentTimeWindow = getString(R.string.Week);
        XAxisFormatter = monthSlashDayXAxisFormatter;
    }

    public void setMonthChecked(Calendar cal) {
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, -1);
        long startTime = cal.getTimeInMillis();
        cal.clear();
        CURRENT_CHART_URL = String.format(COIN_MARKETCAP_CHART_URL_WINDOW, cryptoID, startTime, endTime);
        currentTimeWindow = getString(R.string.Month);
        XAxisFormatter = monthSlashDayXAxisFormatter;
    }

    public void setThreeMonthChecked(Calendar cal) {
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.MONTH, -3);
        long startTime = cal.getTimeInMillis();
        cal.clear();
        CURRENT_CHART_URL = String.format(COIN_MARKETCAP_CHART_URL_WINDOW, cryptoID, startTime, endTime);
        currentTimeWindow = getString(R.string.threeMonth);
        XAxisFormatter = monthSlashDayXAxisFormatter;
    }

    public void setYearChecked(Calendar cal) {
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.YEAR, -1);
        long startTime = cal.getTimeInMillis();
        cal.clear();
        CURRENT_CHART_URL = String.format(COIN_MARKETCAP_CHART_URL_WINDOW, cryptoID, startTime, endTime);
        currentTimeWindow = getString(R.string.Year);
        XAxisFormatter = monthSlashYearFormatter;
    }

    public void setAllTimeChecked() {
        currentTimeWindow = getString(R.string.AllTime);
        CURRENT_CHART_URL = String.format(COIN_MARKETCAP_CHART_URL_ALL_DATA, cryptoID);
        XAxisFormatter = monthSlashYearFormatter;
    }

    public void setTable(CMCCoin coinObject, View rootVeiw) {
        String usdFormat = getString(R.string.usd_format);
        String negativePctFormat = getString(R.string.negative_pct_format);
        String positivePctFormat = getString(R.string.positive_pct_format);
        int negativeRedColor = getResources().getColor(R.color.percentNegativeRed);
        int positiveGreenColor = getResources().getColor(R.color.percentPositiveGreen);
        TextView nameTextView = rootVeiw.findViewById(R.id.tableNameDataTextView);
        if (coinObject.getName() == null) {
            nameTextView.setText("N/A");
        } else {
            nameTextView.setText(coinObject.getName());
        }

        TextView priceUSDTextView = rootVeiw.findViewById(R.id.tablePriceUSDDataTextView);
        if (coinObject.getPrice_usd() == null) {
            priceUSDTextView.setText("N/A");
        } else {
            priceUSDTextView.setText(String.format(usdFormat, Double.parseDouble(coinObject.getPrice_usd())));
        }

        TextView priceBTCTextView = rootVeiw.findViewById(R.id.tablePriceBTCDataTextView);
        if (coinObject.getPrice_btc() == null) {
            priceBTCTextView.setText("N/A");
        } else {
            priceBTCTextView.setText(String.format(getString(R.string.btc_format), coinObject.getPrice_btc()));
        }

        TextView volumeTextView = rootVeiw.findViewById(R.id.tableVolUSDDataTextView);
        if (coinObject.getVolume_usd_24h() == null) {
            volumeTextView.setText("N/A");
        } else {
            volumeTextView.setText(String.format(usdFormat, Double.parseDouble(coinObject.getVolume_usd_24h())));
        }

        TextView mktCapTextView = rootVeiw.findViewById(R.id.tableMktCapDataTextView);
        if (coinObject.getMarket_cap_usd() == null) {
            mktCapTextView.setText("N/A");
        } else {
            mktCapTextView.setText(String.format(usdFormat, Double.parseDouble(coinObject.getMarket_cap_usd())));
        }

        TextView availSupplyTextView = rootVeiw.findViewById(R.id.tableAvailableSupplyDataTextView);
        if (coinObject.getAvailable_supply() == null) {
            availSupplyTextView.setText("N/A");
        } else {
            availSupplyTextView.setText(rawNumberFormat.format(Double.parseDouble(coinObject.getAvailable_supply())));
        }

        TextView totalSupplyTextView = rootVeiw.findViewById(R.id.tableTotalSupplyDataTextView);
        if (coinObject.getTotal_supply() == null) {
            totalSupplyTextView.setText("N/A");
        } else {
            totalSupplyTextView.setText(rawNumberFormat.format(Double.parseDouble(coinObject.getTotal_supply())));
        }

        TextView maxSupplyTextView = rootVeiw.findViewById(R.id.tableMaxSupplyDataTextView);
        if (coinObject.getMax_supply() == null) {
            maxSupplyTextView.setText("N/A");
        } else {
            maxSupplyTextView.setText(rawNumberFormat.format(Double.parseDouble(coinObject.getMax_supply())));
        }

        TextView oneHrChangeTextView = rootVeiw.findViewById(R.id.table1hrChangeDataTextView);
        if (coinObject.getPercent_change_1h() == null) {
            oneHrChangeTextView.setText("N/A");
        } else {
            double amount = Double.parseDouble(coinObject.getPercent_change_1h());
            if (amount >= 0) {
                oneHrChangeTextView.setText(String.format(positivePctFormat, amount));
                oneHrChangeTextView.setTextColor(positiveGreenColor);
            } else {
                oneHrChangeTextView.setText(String.format(negativePctFormat, amount));
                oneHrChangeTextView.setTextColor(negativeRedColor);
            }
        }

        TextView dayChangeTextView = rootVeiw.findViewById(R.id.table24hrChangeDataTextView);
        if (coinObject.getPercent_change_24h() == null) {
            dayChangeTextView.setText("N/A");
        } else {
            double amount = Double.parseDouble(coinObject.getPercent_change_24h());
            if (amount >= 0) {
                dayChangeTextView.setText(String.format(positivePctFormat, amount));
                dayChangeTextView.setTextColor(positiveGreenColor);
            } else {
                dayChangeTextView.setText(String.format(negativePctFormat, amount));
                dayChangeTextView.setTextColor(negativeRedColor);
            }
        }

        TextView weekChangeTextView = rootVeiw.findViewById(R.id.tableWeekChangeDataTextView);
        if (coinObject.getPercent_change_7d() == null) {
            weekChangeTextView.setText("N/A");
        } else {
            double amount = Double.parseDouble(coinObject.getPercent_change_7d());
            if (amount >= 0) {
                weekChangeTextView.setText(String.format(positivePctFormat, amount));
                weekChangeTextView.setTextColor(positiveGreenColor);
            } else {
                weekChangeTextView.setText(String.format(negativePctFormat, amount));
                weekChangeTextView.setTextColor(negativeRedColor);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_graph, container, false);
        lineChart = rootView.findViewById(R.id.chart);
        chartUSDPriceFormat = NumberFormat.getInstance();
        chartUSDPriceFormat.setMaximumFractionDigits(10);
        setUpChart();
        currencyFormatter = CurrencyFormatterSingleton.getInstance(getContext());
        WindowManager mWinMgr = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        displayWidth = mWinMgr.getDefaultDisplay().getWidth();
        chartProgressBar = rootView.findViewById(R.id.chartProgressSpinner);
        Button sourceButton = rootView.findViewById(R.id.sourceButton);
        sourceButton.setPaintFlags(sourceButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        sharedPreferences = getContext().getSharedPreferences(SHAREDPREF_SETTINGS, MODE_PRIVATE);
        Spinner chartCurrencySelector = rootView.findViewById(R.id.chartCurrencySelectSpinnr);
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, getResources().getStringArray(R.array.chart_spinner_options));
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        tsymbol = sharedPreferences.getString(CHART_SPINNER_SETTING, "USD");
        chartCurrencySelector.setAdapter(spinnerArrayAdapter);
        if (tsymbol.equals("USD")) {
            chartCurrencySelector.setSelection(0);
        } else {
            chartCurrencySelector.setSelection(1);
        }
        chartCurrencySelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tsymbol = spinnerArrayAdapter.getItem(position);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(CHART_SPINNER_SETTING, tsymbol);
                editor.apply();
                getCMCChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        viewPager = (CustomViewPager) container;
        nestedScrollView = rootView.findViewById(R.id.graphFragmentNestedScrollView);
        buttonGroup = rootView.findViewById(R.id.chart_interval_button_grp);
        cryptoID = getArguments().getString(ARG_ID);
        setDayChecked(Calendar.getInstance());
        buttonGroup.check(R.id.dayButton);
        currentTimeWindow = getString(R.string.oneDay);
        sourceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(getActivity(), Uri.parse(BASE_CMC_SOURCE_URL + cryptoID));
            }
        });
        buttonGroup.setOnCheckedChangeListener(new SingleSelectToggleGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SingleSelectToggleGroup group, int checkedId) {
                Calendar.getInstance();
                switch (checkedId) {
                    case R.id.dayButton:
                        setDayChecked(Calendar.getInstance());
                        getCMCChart();
                        break;
                    case R.id.weekButton:
                        setWeekChecked(Calendar.getInstance());
                        getCMCChart();
                        break;
                    case R.id.monthButton:
                        setMonthChecked(Calendar.getInstance());
                        getCMCChart();
                        break;
                    case R.id.threeMonthButton:
                        setThreeMonthChecked(Calendar.getInstance());
                        getCMCChart();
                        break;
                    case R.id.yearButton:
                        setYearChecked(Calendar.getInstance());
                        getCMCChart();
                        break;
                    case R.id.allTimeButton:
                        setAllTimeChecked();
                        getCMCChart();
                        break;
                }
            }
        });
        CMCCoin coinObject = getActivity().getIntent().getParcelableExtra(GraphFragment.COIN_OBJECT);
        setTable(coinObject, rootView);
        return rootView;
    }

    // test commit

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        TextView currentPrice = rootView.findViewById(R.id.current_price);
        TextView dateTextView = rootView.findViewById(R.id.graphFragmentDateTextView);
        if (tsymbol.equals("USD")) {
            currentPrice.setText(String.format(getString(R.string.unrounded_usd_chart_price_format), String.valueOf(e.getY())));
        } else {
            currentPrice.setText(currencyFormatter.format(e.getY(), "BTC"));
        }
        dateTextView.setText(getFormattedFullDate(e.getX()));
    }

    @Override
    public void onNothingSelected() {

    }

    public String getFormattedFullDate(float unixSeconds) {
        Date date = new Date((long) unixSeconds);
        return fullDateFormat.format(date);
    }
}
