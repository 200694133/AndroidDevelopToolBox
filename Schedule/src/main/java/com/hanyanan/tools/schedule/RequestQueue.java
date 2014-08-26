package com.hanyanan.tools.schedule;

import android.os.Handler;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by hanyanan on 2014/7/29.
 * A request dispatch queue with a thread pool of dispatchers.
 *
 * Calling {@link #add(Request)} will enqueue the given Request for dispatch,
 * resolving from either cache or network on a worker thread, and then delivering
 * a parsed response on the main thread.
 */
public class RequestQueue {
    /** Used for generating monotonically-increasing sequence numbers for requests. */
    private AtomicInteger mSequenceGenerator = new AtomicInteger();

    /**
     * The set of all requests currently being processed by this RequestQueue. A Request
     * will be in this set if it is waiting in any queue or currently being processed by
     * any dispatcher.
     */
//    private final Set<Request<?>> mCurrentRequests = new HashSet<Request<?>>();

    /** The queue of requests that are actually going out to the network. */
    private final PriorityBlockingQueue<Request<?>> mRequestQueue = new PriorityBlockingQueue<Request<?>>();

    /** Number of network request dispatcher threads to start. */
    private static final int DEFAULT_TASK_THREAD_POOL_SIZE = 4;

    /** The request dispatchers. */
    private final RequestDispatcher[] mRequestDispatchers;

    /** Default response delivery mechanism. */
    private final ResponseDelivery mDelivery;

    /** default retry policy, it's not support retry in default mode. */
    private final RetryPolicy mDefaultRetryPolicy;

    /** default global error listener. */
    private Response.ErrorListener mDefaultErrorListener;
    /**
     * Creates the worker pool. Processing will not begin until {@link #start()} is called.
     *
     * @param requestDispatchers a thread array for running background request
     */
    public RequestQueue(RequestDispatcher[] requestDispatchers) {
        mRequestDispatchers = requestDispatchers;
        mDelivery = new DefaultResponseDelivery(new Handler());
        mDefaultRetryPolicy = new TimesRetryPolicy(0);
    }

    /**
     * Creates the worker pool. Processing will not begin until {@link #start()} is called.
     *
     * @param requestDispatchersCount the count of threads for running background request
     */
    public RequestQueue(int requestDispatchersCount) {
        mDelivery = new DefaultResponseDelivery(new Handler());
        mDefaultRetryPolicy = new TimesRetryPolicy(0);
        mRequestDispatchers = new RequestDispatcher[requestDispatchersCount];
        for(int i =0;i<requestDispatchersCount;++i){
            mRequestDispatchers[i] = new RequestDispatcher(mRequestQueue, null, mDelivery);
        }
    }
    /**
     * Starts the dispatchers in this queue.
     */
    public void start() {
        stop();  // Make sure any currently running dispatchers are stopped.
        // Create network dispatchers (and corresponding threads) up to the pool size.
        for (int i = 0; i < mRequestDispatchers.length; i++) {
            RequestDispatcher networkDispatcher = new RequestDispatcher(mRequestQueue,null,mDelivery);
            mRequestDispatchers[i] = networkDispatcher;
            networkDispatcher.start();
        }
    }

    /**
     * Stops the cache and network dispatchers.
     */
    public void stop() {
//        if (mCacheDispatcher != null) {
//            mCacheDispatcher.quit();
//        }
        for (int i = 0; i < mRequestDispatchers.length; i++) {
            if (mRequestDispatchers[i] != null) {
                mRequestDispatchers[i].quit();
            }
        }
    }

    /**
     * Get default response delivery, which used to delivery response.
     * @return global response delivery
     */
    public ResponseDelivery getDefaultResponseDelivery(){
        return mDelivery;
    }

    /**
     * Get default retry policy
     * @return default retry policy
     */
    public RetryPolicy getDefaultRetryPolicy(){
        return mDefaultRetryPolicy;
    }

    /**
     * Get default error listener.
     * @return default error listener
     */
    public Response.ErrorListener getDefaultErrorListener(){
        return mDefaultErrorListener;
    }
    /**
     * Gets a sequence number.
     */
    public int getSequenceNumber() {
        return mSequenceGenerator.incrementAndGet();
    }

    /**
     * A simple predicate or filter interface for Requests, for use by
     * {@link RequestQueue#cancelAll(RequestFilter)}.
     */
    public interface RequestFilter {
        public boolean apply(Request<?> request);
    }

    /**
     * Cancels all requests in this queue for which the given filter applies.
     * @param filter The filtering function to use
     */
    public void cancelAll(RequestFilter filter) {
        synchronized (mRequestQueue) {
            for (Request<?> request : mRequestQueue) {
                if (filter.apply(request)) {
                    request.cancel();
                }
            }
        }
    }
    /**
     * Cancels all requests in this queue with the given tag. Tag must be non-null
     * and equality is by identity.
     */
    public void cancelAll(final Object tag) {
        if (tag == null) {
            throw new IllegalArgumentException("Cannot cancelAll with a null tag");
        }
        cancelAll(new RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return request.getTag() == tag;
            }
        });
    }

    /**
     * Adds a Request to the dispatch queue.
     * @param request The request to service
     * @return The passed-in request
     */
    public Request add(Request request) {
        // Tag the request as belonging to this queue and add it to the set of current requests.
//        request.setRequestQueue(this);
        synchronized (mRequestQueue) {
            mRequestQueue.add(request);
        }

        // Process requests in the order they are added.
        request.setSequence(getSequenceNumber());
        request.addMarker("add-to-queue");

//        // If the request is uncacheable, skip the cache queue and go straight to the network.
//        if (!request.shouldCache()) {
//            mNetworkQueue.add(request);
//            return request;
//        }
        return request;
    }
    /**
     * Called from {@link Request#finish(String)}, indicating that processing of the given request
     * has finished.
     *
     * <p>Releases waiting requests for <code>request.getCacheKey()</code> if
     *      <code>request.shouldCache()</code>.</p>
     */
    void finish(Request<?> request) {
        // Remove from the set of requests currently being processed.
        synchronized (mRequestQueue) {
            mRequestQueue.remove(request);
        }
    }
}
