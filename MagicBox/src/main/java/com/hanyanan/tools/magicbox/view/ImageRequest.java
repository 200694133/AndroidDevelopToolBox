package com.hanyanan.tools.magicbox.view;

import android.graphics.Bitmap;

import com.hanyanan.tools.magicbox.MagicApplication;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.ResponseDelivery;
import com.hanyanan.tools.schedule.http.NetworkRequest;
import com.hanyanan.tools.storage.disk.FixSizeDiskStorage;

import java.util.HashMap;

/**
 * Created by hanyanan on 2014/8/13.
 */
class ImageRequest extends NetworkRequest<Bitmap> {
    private static final BitmapExecutor sBitmapExecutor = new BitmapExecutor();
    private int mMaxWidth, mMaxHeight;

    public void setMaxWidth(int width){
        mMaxWidth = width;
    }

    public void setMaxHeight(int height){
        mMaxHeight = height;
    }

    public FixSizeDiskStorage getFixSizeDiskStorage(){
        MagicApplication mApp = MagicApplication.getInstance();
        if(null == mApp) return null;
        return mApp.getFixSizeDiskStorage();
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

    public ImageRequest(String url,ResponseDelivery delivery, Response.Listener<Bitmap> listener, Response.ErrorListener listener1) {
        super(url, new HashMap<String, String>(), sBitmapExecutor, delivery, listener1);
        setListener(listener);
    }
}
