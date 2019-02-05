package com.cryptotracker.zack.cryptotracker.models.rest;

import java.util.HashMap;
import java.util.List;


public class CoinFavoritesStructures {
    public List<String> favoriteList;
    public HashMap<String, String> favoritesMap;

    public CoinFavoritesStructures(List<String> favoriteList, HashMap<String, String> favoritesMap) {
        this.favoriteList = favoriteList;
        this.favoritesMap = favoritesMap;
    }
}
