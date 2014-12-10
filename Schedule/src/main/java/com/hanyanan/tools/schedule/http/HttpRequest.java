package com.hanyanan.tools.schedule.http;

import android.text.TextUtils;

import com.hanyanan.tools.schedule.Cache;
import com.hanyanan.tools.schedule.HttpCacheExecutor;
import com.hanyanan.tools.schedule.Request;
import com.hanyanan.tools.schedule.RequestExecutor;
import com.hanyanan.tools.schedule.RequestQueue;
import com.hanyanan.tools.schedule.http.cache.HttpCache;

/**
 * Created by hanyanan on 2014/7/29.
 */
public class HttpRequest extends Request<HttpRequestParam>{
    private Cache.Mode mCacheMode = Cache.Mode.Disable;
    private HttpCache mHttpCache;
    protected HttpProgressListener mHttpProgressListener;
    private String mKey;
    public HttpRequest(RequestQueue requestQueue,
                       RequestExecutor requestExecutor, HttpRequestParam param) {
        super(requestQueue,requestExecutor,param);
    }

    public HttpRequest(RequestQueue requestQueue,
                       RequestExecutor requestExecutor, HttpRequestParam param, Cache.Mode mode) {
        super(requestQueue,requestExecutor,param);
        mCacheMode = mode;
    }

    public HttpRequest setHttpProgressListener(final HttpProgressListener httpProgressListener){
        if(null == httpProgressListener) return this;
        mHttpProgressListener = new HttpProgressListener(){
            private float prevUp = -1;
            private float prevDown = -1;
            public void uploadProgress(final float progress) {
                if(Math.abs(prevUp - progress) < 0.03) return ;

                getResponseDelivery().postRunnable(HttpRequest.this, new Runnable() {
                    public void run() {
                        prevUp = progress;
                        if(null != httpProgressListener) httpProgressListener.uploadProgress(progress);
                    }
                });
            }
            public void downloadProgress(final float progress) {
                if(Math.abs(prevDown - progress) < 0.03) return ;

                getResponseDelivery().postRunnable(HttpRequest.this, new Runnable() {
                    public void run() {
                        prevDown = progress;
                        if(null != httpProgressListener) httpProgressListener.downloadProgress(progress);
                    }
                });
            }
        };
        return this;
    }

    public HttpProgressListener getHttpProgressListener(){
        return mHttpProgressListener;
    }

    public HttpRequest setHttpCache(HttpCache httpCache){
        this.mHttpCache = httpCache;
        return this;
    }

    public HttpCache getHttpCache(){
        return mHttpCache;
    }
    public HttpRequest setCacheMode(Cache.Mode mode){
        mCacheMode = mode;
        return this;
    }

    public Cache.Mode getCacheMode(){
        return mCacheMode;
    }
    public String getKey(){
        if(!TextUtils.isEmpty(mKey)) return mKey;
        return String.valueOf(Math.abs(getRequestParam().getUrl().hashCode()));
    }

    public HttpRequest setKey(String key){
        mKey = key;
        return this;
    }
    public String getUrl(){
        return getRequestParam().getUrl();
    }

    public static interface HttpProgressListener{
        public void uploadProgress(float progress);

        public void downloadProgress(float progress);
    }
}
