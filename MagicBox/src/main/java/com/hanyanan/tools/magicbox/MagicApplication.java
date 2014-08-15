package com.hanyanan.tools.magicbox;

import android.app.Application;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.LruCache;

import com.hanyanan.tools.schedule.RequestQueue;
import com.hanyanan.tools.storage.disk.FixSizeDiskStorage;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by hanyanan on 2014/8/15.
 */
public class MagicApplication extends Application {
    private static WeakReference<MagicApplication> sInstance = new WeakReference<MagicApplication>(null);
    private static final int M = 1024 * 1024;
    private RequestQueue mRequestQueue = null;
    private LruCache<String, Bitmap> mBitmapLruCache = null;
    private FixSizeDiskStorage mTemporaryDiskCache;
    public static MagicApplication getInstance(){
        return sInstance.get();
    }
    public MagicApplication() {
        super();
        sInstance = new WeakReference<MagicApplication>(this);
        mRequestQueue = new RequestQueue(Runtime.getRuntime().availableProcessors());
        long max = Runtime.getRuntime().maxMemory();
        int cacheSize = (int)max/8;
        cacheSize = cacheSize<4*M?4*M:cacheSize;
        mBitmapLruCache = new LruCache<String, Bitmap>(cacheSize){
            protected int sizeOf(String key, Bitmap value){
                if(null == value) return 0;
                return value.getByteCount();
            }
        };
    }

    public LruCache<String, Bitmap> getBitmapLruCache(){
        return mBitmapLruCache;
    }

    /**
     * Lazy init the disk cache.
     * @return disk cache
     */
    public synchronized FixSizeDiskStorage getFixSizeDiskStorage(){
        if(null == mTemporaryDiskCache){
            String packageName = getPackageName();
            File cacheDir = new File(Environment.getExternalStorageDirectory(),"data/"+packageName+"/cache");
            try {
                mTemporaryDiskCache = FixSizeDiskStorage.open(cacheDir, 1, 200 * M);
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    public void onTerminate() {
        mRequestQueue.stop();
        super.onTerminate();
    }
}
