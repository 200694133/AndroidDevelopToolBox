package com.hanyanan.tools.datapersistence.storage.direct;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by hanyanan on 2014/7/25.
 */
class SharedPreferencesDriver {
    private static final String NAME = "storage";
    private final SharedPreferences sharedPreferences ;


    SharedPreferencesDriver(Context context){
        sharedPreferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }




    public void close(){
        //TODO
    }
}
