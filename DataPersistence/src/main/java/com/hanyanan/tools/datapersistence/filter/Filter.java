package com.hanyanan.tools.datapersistence.filter;

import com.hanyanan.tools.datapersistence.storage.QueryResultEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hanyanan on 2014/7/25.
 */
public class Filter {
    private static final List<IFilter> sFilterChains = new ArrayList<IFilter>();
    static{
        sFilterChains.add(new ExpireFilter());
    }
    public static boolean isIllegal(QueryResultEntry entry){
        for(IFilter filter : sFilterChains){
            if(!filter.isIllegal(entry)){
                return false;
            }
        }
        return true;
    }
}
