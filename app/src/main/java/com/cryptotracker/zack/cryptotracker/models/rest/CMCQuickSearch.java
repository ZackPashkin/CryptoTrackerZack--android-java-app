package com.cryptotracker.zack.cryptotracker.models.rest;

import com.fasterxml.jackson.annotation.JsonProperty;


public class CMCQuickSearch {
    @JsonProperty("slug")
    private String slug;
    @JsonProperty("id")
    private int id = -1;

    public String getSlug() {
        return slug;
    }

    public int getId() {
        return id;
    }
}
