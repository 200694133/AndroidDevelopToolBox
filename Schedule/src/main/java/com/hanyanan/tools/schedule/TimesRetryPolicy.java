package com.hanyanan.tools.schedule;

/**
 * Created by hanyanan on 2014/8/26.
 */
public class TimesRetryPolicy implements RetryPolicy {
    private int mMaxTimes = DEFAULT_MAX_COUNT;
    private int mCurrentTimes = 0;
    public TimesRetryPolicy(int times){
        mMaxTimes = times;
    }
    @Override
    public int getCurrentTimeout() {
        return DEFAULT_TIME_OUT;
    }

    @Override
    public int getCurrentRetryCount() {
        return mCurrentTimes;
    }

    @Override
    public void retry(XError error) throws XError {
        mCurrentTimes++;
        if(mCurrentTimes > mMaxTimes){
            throw new XError("It's more than max retry count.");
        }
    }
}
