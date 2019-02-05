package com.cryptotracker.zack.cryptotracker.models.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public class TradingPair {

    @JsonProperty("Data")
    private List<TradingPairNode> data;
    @JsonProperty("Response")
    private String response;

    public List<TradingPairNode> getData() {
        return data;
    }

    public void setData(List<TradingPairNode> data) {
        this.data = data;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
