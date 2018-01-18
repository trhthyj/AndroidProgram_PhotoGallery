package com.mi.www.photogallary;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by wm on 2018/1/18.
 */

public class QueryPreferences {
    public static final String PREF_SEARCH_QUERY = "searchQuery";

    public static String getStoreQuery(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_SEARCH_QUERY, null);
    }

    public static void setStoredQuery(Context context, String query){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SEARCH_QUERY, query)
                .apply();
    }
}