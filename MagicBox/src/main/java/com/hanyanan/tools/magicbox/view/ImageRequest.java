package com.hanyanan.tools.magicbox.view;

import android.graphics.Bitmap;

import com.hanyanan.tools.schedule.Request;
import com.hanyanan.tools.schedule.RequestExecutor;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.ResponseDelivery;
import com.hanyanan.tools.schedule.RetryPolicy;
import com.hanyanan.tools.schedule.network.NetworkRequest;
import com.hanyanan.tools.storage.disk.FixSizeDiskStorage;

import java.util.HashMap;

/**
 * Created by hanyanan on 2014/8/13.
 */
class ImageRequest extends NetworkRequest<Bitmap> {
    private static final BitmapExecutor sBitmapExecutor = new BitmapExecutor();
    private final FixSizeDiskStorage mFixSizeDiskStorage;
    private int mMaxWidth, mMaxHeight;

    public void setMaxWidth(int width){
        mMaxWidth = width;
    }

    public void setMaxHeight(int height){
        mMaxHeight = height;
    }

    public FixSizeDiskStorage getFixSizeDiskStorage(){
        return mFixSizeDiskStorage;
    }
    public String getUrl(){
        return mUrl;
    }

    public int getMaxWidth(){
        return mMaxWidth;
    }

    public int getMaxHeight(){
        return mMaxHeight;
    }

    public ImageRequest(String url,FixSizeDiskStorage cache, ResponseDelivery delivery, Response.Listener<Bitmap> listener, Response.ErrorListener listener1) {
        super(url, new HashMap<String, String>(), sBitmapExecutor,delivery, listener1);
        setListener(listener);
        mFixSizeDiskStorage = cache;
    }
}
