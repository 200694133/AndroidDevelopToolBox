package com.hanyanan.tools.schedule;

/**
 * Created by hanyanan on 2014/7/29.
 */
public class DefaultRetryPolicy implements RetryPolicy{
    private static final int DEFAULT_RETRY_TIME = 1;
    private int mCount = 0;
    @Override
    public int getCurrentTimeout() {
        return 5000;
    }

    @Override
    public int getCurrentRetryCount() {
        return mCount;
    }

    @Override
    public void retry(XError error) throws XError {
        ++mCount;
        if(DEFAULT_RETRY_TIME < mCount){
            throw new XError(new FaultError());
        }
    }
}
