package com.cryptotracker.zack.cryptotracker.currencydetails.markets;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cryptotracker.zack.cryptotracker.CustomItemClickListener;
import com.cryptotracker.zack.cryptotracker.R;
import com.cryptotracker.zack.cryptotracker.models.rest.MarketNode;
import com.cryptotracker.zack.cryptotracker.singletons.CurrencyFormatterSingleton;

import java.lang.ref.WeakReference;
import java.util.List;


public class MarketsListAdapter extends RecyclerView.Adapter<MarketsListAdapter.ViewHolder> {

    String negativePctFormat;
    String positivPctFormat;
    private CustomItemClickListener listener;
    private MarketsListAdapter.ViewHolder viewHolder;
    private List<MarketNode> markets;
    private WeakReference<AppCompatActivity> contextRef;
    private int positiveGreenColor;
    private int negativeRedColor;
    private CurrencyFormatterSingleton currencyFormatter;

    public MarketsListAdapter(List<MarketNode> markets, AppCompatActivity context, CustomItemClickListener listener) {
        this.markets = markets;
        this.currencyFormatter = CurrencyFormatterSingleton.getInstance(context);
        this.contextRef = new WeakReference<>(context);
        this.listener = listener;
        this.negativePctFormat = context.getString(R.string.negative_pct_format);
        this.positivPctFormat = context.getString(R.string.positive_pct_format);
        this.negativeRedColor = this.contextRef.get().getResources().getColor(R.color.percentNegativeRed);
        this.positiveGreenColor = this.contextRef.get().getResources().getColor(R.color.percentPositiveGreen);
    }

    @Override
    public void onBindViewHolder(final MarketsListAdapter.ViewHolder holder, final int position) {
        MarketNode node = markets.get(position);
        holder.exchangeNameTextView.setText(node.getMarket());
        String formattedVolume = this.currencyFormatter.format(node.getVolume24h(), node.getToSymbol());
        holder.volDataTextView.setText(formattedVolume);
        if (node.getChangePct24h() >= 0) {
            holder.changeTextView.setTextColor(positiveGreenColor);
            holder.changeTextView.setText(String.format(positivPctFormat, node.getChangePct24h()));
        } else {
            holder.changeTextView.setTextColor(negativeRedColor);
            holder.changeTextView.setText(String.format(negativePctFormat, node.getChangePct24h()));
        }
        String formattedPrice = this.currencyFormatter.format(node.getPrice(), node.getToSymbol());
        holder.priceTextView.setText(formattedPrice);
    }

    @Override
    public MarketsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_market_list, parent, false);
        viewHolder = new MarketsListAdapter.ViewHolder(itemLayoutView, listener);
        return viewHolder;
    }

    public int getItemCount() {
        return markets.size();
    }

    public void setMarketsList(List<MarketNode> newMarketsList) {
        this.markets.clear();
        this.markets.addAll(newMarketsList);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CustomItemClickListener listener;
        private TextView exchangeNameTextView;
        private TextView volDataTextView;
        private TextView changeTextView;
        private TextView priceTextView;

        private ViewHolder(View itemLayoutView, CustomItemClickListener listener) {
            super(itemLayoutView);
            itemLayoutView.setOnClickListener(this);
            this.exchangeNameTextView = itemLayoutView.findViewById(R.id.exchangeNameTextView);
            this.volDataTextView = itemLayoutView.findViewById(R.id.volDataTextView);
            this.changeTextView = itemLayoutView.findViewById(R.id.changeTextView);
            this.priceTextView = itemLayoutView.findViewById(R.id.priceTextView);
            this.listener = listener;
        }

        @Override
        public void onClick(View v) {
            this.listener.onItemClick(getAdapterPosition(), v);
        }
    }
}
