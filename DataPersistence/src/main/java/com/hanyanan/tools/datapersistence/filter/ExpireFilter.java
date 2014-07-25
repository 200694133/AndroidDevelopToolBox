package com.hanyanan.tools.datapersistence.filter;

import com.hanyanan.tools.datapersistence.storage.QueryResultEntry;

/**
 * Created by hanyanan on 2014/7/25.
 */
public class ExpireFilter implements IFilter {
    @Override
    public boolean isIllegal(QueryResultEntry entry) {
        return entry.getExpireTime() <= System.currentTimeMillis();
    }
}
