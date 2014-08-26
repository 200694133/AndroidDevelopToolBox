package com.hanyanan.tools.schedule;

/**
 * Retry policy for a request.
 */
public interface RetryPolicy {
    public static final int DEFAULT_TIME_OUT = 5000;
    public static final int DEFAULT_MAX_COUNT = 3;
    /**
     * Returns the current timeout (used for logging).
     */
    public int getCurrentTimeout();

    /**
     * Returns the current retry count (used for logging).
     */
    public int getCurrentRetryCount();

    /**
     * Prepares for the next retry by applying a backoff to the timeout.
     * @param error The error code of the last attempt.
     * @throws XError In the event that the retry could not be performed (for example if we
     * ran out of attempts), the passed in error is thrown.
     */
    public void retry(XError error) throws XError;
}
