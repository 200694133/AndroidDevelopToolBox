package com.hanyanan.tools.schedule;

import android.os.Handler;

/**
 * Created by hanyanan on 2014/7/29.
 */
public abstract class Request<T> implements Comparable<Request<T>>{
    /** Listener interface for errors. */
    private Response.ErrorListener mErrorListener;
    private Response.Listener<T> mResultListener;
    private static int sSequence = 0;
    /** Sequence number of this request, used to enforce FIFO ordering. */
    private Integer mSequence = new Integer(sSequence++);

    private volatile boolean isCanceled = false;
    /**
     * Request status
     */
    static enum Status{
        /**
         * It's idle status, it forbid to running.
         */
        IDLE,
        /**
         * Current Request is in waiting queue, it's still waiting for running
         */
        Pending,
        /**
         * Current Request is running.
         */
        Running,
        /**
         * Current request has finished, it may be successful/cancelled or error occurred.
         */
        Finish
    }
    /** Whether or not a response has been delivered for this request yet. */
    private boolean mResponseDelivered = false;
    // A cheap variant of request tracing used to dump slow requests.
    private long mRequestBirthTime = 0;
    private Status mStatus = Status.IDLE;
    /** Current request executor. #{@see RequestExecutor.performRequest}. */
    private final RequestExecutor mRequestExecutor;
    /** retry policy used to retry current request when request failed occurred. */
    private final RetryPolicy mRetryPolicy;
    /** used to delivery response. */
    private final ResponseDelivery mResponseDelivery;
    /** An opaque token tagging this request; used for bulk cancellation. */
    private Object mTag;

    /** The priority of this request. */
    private Priority mPriority = Priority.NORMAL;

    /** The cache policy of this request to indicate if need cache the terminal response. */
    private CachePolicy mCachePolicy;

    /**
     * Creates a new request with the given RequestExecutor, RetryPolicy, ResponseDelivery and
     * error listener.  Note that the normal response listener is not provided here as
     * delivery of responses is provided by subclasses, who have a better idea of how to deliver
     * an already-parsed response.
     */
    public Request(RequestExecutor requestExecutor, ResponseDelivery responseDelivery,
                   RetryPolicy retryPolicy, Response.ErrorListener listener) {
        mErrorListener = listener;
        mResponseDelivery = responseDelivery;
        mRequestExecutor = requestExecutor;
        mRetryPolicy = retryPolicy;
        mRequestBirthTime = System.currentTimeMillis();
        setStatus(Status.Pending);
//        mDefaultTrafficStatsTag = findDefaultTrafficStatsTag(url);
    }

    public Request(RequestExecutor requestExecutor, Response.ErrorListener listener) {
        mErrorListener = listener;
        mResponseDelivery = new DefaultResponseDelivery(new Handler());
        mRequestExecutor = requestExecutor;
        mRetryPolicy = new DefaultRetryPolicy();
        mRequestBirthTime = System.currentTimeMillis();
        setStatus(Status.Pending);
    }
    public RequestExecutor getRequestExecutor(){
        return mRequestExecutor;
    }
    public ResponseDelivery getResponseDelivery(){
        return mResponseDelivery;
    }
    public void setStatus(Status status){
        mStatus = status;
    }
    public Status getStatus(){
        return mStatus;
    }
    public void setListener(Response.Listener<T> l){
        mResultListener = l;
    }
    public void finish(final String info){
        //TODO
    }

    public boolean isCanceled(){
        return false;
    }

    /**
     * Mark this request as canceled.  No callback will be delivered.
     */
    public void cancel(){
        isCanceled = true;
    }

    /**
     * Setup this request to reuse this request.
     */
    public void reset(){
        //TODO
    }

    public void setCachePolicy(CachePolicy cachePolicy){
        mCachePolicy = cachePolicy;
    }

    public CachePolicy getCachePolicy(){
        return mCachePolicy;
    }
    /**
     * Set a tag on this request. Can be used to cancel all requests with this
     * tag by {@link RequestQueue#cancelAll(Object)}.
     *
     * @return This Request object to allow for chaining.
     */
    public Request<?> setTag(Object tag) {
        mTag = tag;
        return this;
    }

    /**
     * Returns this request's tag.
     * @see Request#setTag(Object)
     */
    public Object getTag() {
        return mTag;
    }

    /**
     * Sets the sequence number of this request.  Used by {@link RequestQueue}.
     *
     * @return This Request object to allow for chaining.
     */
    public final Request<?> setSequence(int sequence) {
        mSequence = sequence;
        return this;
    }


    /**
     * Returns the sequence number of this request.
     */
    public final int getSequence() {
        if (mSequence == null) {
            throw new IllegalStateException("getSequence called before setSequence");
        }
        return mSequence;
    }

    /**
     * Returns the {@link Priority} of this request; {@link Priority#NORMAL} by default.
     */
    public Priority getPriority() {
        return mPriority;
    }
    /**
     * Returns the {@link Priority} of this request; {@link Priority#NORMAL} by default.
     */
    public void setPriority(Priority priority) {
        mPriority = priority;
    }
    /**
     * Returns the retry policy that should be used  for  this request.
     */
    public RetryPolicy getRetryPolicy() {
        return mRetryPolicy;
    }
    /**
     * Mark this request as having a response delivered on it.  This can be used
     * later in the request's lifetime for suppressing identical responses.
     */
    public void markDelivered() {
        mResponseDelivered = true;
    }
    /**
     * Returns true if this request has had a response delivered for it.
     */
    public boolean hasHadResponseDelivered() {
        return mResponseDelivered;
    }
    /**
     * Subclasses must implement this to perform delivery of the parsed
     * response to their listeners.  The given response is guaranteed to
     * be non-null; responses that fail to parse are not delivered.
     * @param response The parsed response returned by
     */
    public void deliverResponse(T response){
        if(null != mResultListener){
            mResultListener.onResponse(response);
        }
        //TODO
    }
    /**
     * Delivers error message to the ErrorListener that the Request was
     * initialized with.
     *
     * @param error Error details
     */
    public void deliverError(XError error) {
        if (mErrorListener != null) {
            mErrorListener.onErrorResponse(error);
        }
        //TODO
    }

    /**
     * When exception occurred, retry to run this request
     * @param exception the exception occurred.
     * @return true means that is can retry again, false means it cannot retry.
     */
    protected boolean attemptRetryOnError(XError exception){
        RetryPolicy policy = getRetryPolicy();
        try {
            policy.retry(exception);
            return true;
        }catch (XError error1){
            addMarker("out of retry, finish failed!");
            return false;
        }
    }
    /**
     * Our comparator sorts from high to low priority, and secondarily by
     * sequence number to provide FIFO ordering.
     */
    @Override
    public int compareTo(Request other) {
        if(null == other) return 1;
        Priority left = this.getPriority();
        Priority right = other.getPriority();

        // High-priority requests are "lesser" so they are sorted to the front.
        // Equal priorities are sorted by sequence number to provide FIFO ordering.
        return left == right ?
                this.mSequence - other.mSequence :
                right.ordinal() - left.ordinal();
    }

    public void addMarker(String marker){
        //TODO
    }
}
