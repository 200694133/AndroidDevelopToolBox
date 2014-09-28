package com.hanyanan.tools.magicbox;

import android.app.Application;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.util.LruCache;

import com.hanyanan.tools.schedule.RequestQueue;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.http.GsonObjectRequestExecutor;
import com.hanyanan.tools.schedule.http.HttpConnectionImpl;
import com.hanyanan.tools.schedule.http.HttpRequestParam;
import com.hanyanan.tools.schedule.http.NetworkRequest;
import com.hanyanan.tools.schedule.http.RawRequestExecutor;
import com.hanyanan.tools.schedule.http.StringRequestExecutor;
import com.hanyanan.tools.storage.disk.DiskStorage;
import com.hanyanan.tools.storage.disk.LimitedSizeDiskStorage;

import org.apache.http.HttpRequest;
import org.apache.http.protocol.HttpRequestExecutor;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * Created by hanyanan on 2014/8/15.
 */
public class MagicApplication extends Application {
    protected static WeakReference<MagicApplication> sInstance = new WeakReference<MagicApplication>(null);
    protected static final int M = 1024 * 1024;
    protected final RequestQueue mRequestQueue;
    protected final LruCache<String, Bitmap> mBitmapLruCache;
    protected DiskStorage mTemporaryDiskCache;
    protected final HashMap<String, String> mGlobalHttpProperty = new HashMap<String, String>();

    public MagicApplication() {
        super();
        sInstance = new WeakReference<MagicApplication>(this);
        mRequestQueue = new RequestQueue(Runtime.getRuntime().availableProcessors());
        long max = Runtime.getRuntime().maxMemory();
        int cacheSize = (int)max/8;
        cacheSize = cacheSize<40*M?40*M:cacheSize;
        mBitmapLruCache = new LruCache<String, Bitmap>(cacheSize){
            protected int sizeOf(String key, Bitmap value){
                if(null == value) return 0;
                return value.getByteCount();
            }
        };
    }

    public static MagicApplication getInstance() {
        return sInstance.get();
    }

    public LruCache<String, Bitmap> getBitmapLruCache(){
        return mBitmapLruCache;
    }

    /**
     * Lazy init the disk cache.
     * @return disk cache
     */
    public synchronized DiskStorage getFixSizeDiskStorage(){
        if(null == mTemporaryDiskCache){
            String packageName = getPackageName();
            File cacheDir = new File(Environment.getExternalStorageDirectory(),"data/"+packageName+"/cache");
            mTemporaryDiskCache = LimitedSizeDiskStorage.open(cacheDir, 200 * M);
        }
        return mTemporaryDiskCache;
    }
    public RequestQueue getRequestQueue(){
        return mRequestQueue;
    }

    public void onCreate() {
        mRequestQueue.start();
        super.onCreate();
    }


    public MagicApplication serGlobalHttpProperty(String name, String value){
        if(TextUtils.isEmpty(name) || TextUtils.isEmpty(value)) return this;
        mGlobalHttpProperty.put(name, value);
        return this;
    }

    /**
     * Create a http and parse to object
     * @param url
     * @param listener
     * @return
     */
    public <T> NetworkRequest newGsonHttpRequest(String url, Response.Listener<T> listener, Class<T>  t){
        HttpRequestParam param = new HttpRequestParam(url);
        param.setHeaderProperty(mGlobalHttpProperty);
        GsonObjectRequestExecutor<T> executor = new GsonObjectRequestExecutor<T>(new HttpConnectionImpl(),t);
        NetworkRequest networkRequest = new NetworkRequest(mRequestQueue,executor, param);
        networkRequest.setListener(listener);
        return networkRequest;
    }

    public NetworkRequest newStringHttpRequest(String url, Response.Listener<String> listener){
        HttpRequestParam param = new HttpRequestParam(url);
        param.setHeaderProperty(mGlobalHttpProperty);
        StringRequestExecutor executor = new StringRequestExecutor(new HttpConnectionImpl());
        NetworkRequest networkRequest = new NetworkRequest(mRequestQueue,executor, param);
        networkRequest.setListener(listener);
        return networkRequest;
    }

    public NetworkRequest newHttpRequest(String url, Response.Listener<byte[]> listener){
        HttpRequestParam param = new HttpRequestParam(url);
        param.setHeaderProperty(mGlobalHttpProperty);
        RawRequestExecutor executor = new RawRequestExecutor(new HttpConnectionImpl());
        NetworkRequest networkRequest = new NetworkRequest(mRequestQueue,executor, param);
        networkRequest.setListener(listener);
        return networkRequest;
    }

    public void onTerminate() {
        mRequestQueue.stop();
        super.onTerminate();
    }
}
