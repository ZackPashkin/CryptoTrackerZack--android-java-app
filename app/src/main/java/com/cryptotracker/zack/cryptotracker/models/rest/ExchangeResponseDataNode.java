package com.cryptotracker.zack.cryptotracker.models.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public class ExchangeResponseDataNode {

    @JsonProperty("Exchanges")
    private List<MarketNode> marketsList;

    public List<MarketNode> getMarketsList() {
        return marketsList;
    }

    public void setMarketsList(List<MarketNode> marketsList) {
        this.marketsList = marketsList;
    }
}
