package com.hanyanan.tools.magicbox.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.LruCache;
import android.widget.ImageView;

import com.hanyanan.tools.magicbox.R;
import com.hanyanan.tools.schedule.DefaultResponseDelivery;
import com.hanyanan.tools.schedule.RequestQueue;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.XError;
import com.hanyanan.tools.*;

/**
 * Created by hanyanan on 2014/8/12.
 */
public class NetworkImageView extends ImageView implements Response.ErrorListener,Response.Listener<Bitmap>{
    public static final int MAXSIZE = 4 * 1024 * 1024;
    private static final RequestQueue sRequestQueue =  new  RequestQueue(4);
    static{
        sRequestQueue.start();
    }
    private static final LruCache<String, Bitmap> sLruCache = new LruCache<String, Bitmap>(MAXSIZE){
         protected int sizeOf(String key, Bitmap value) {
         return value.getByteCount();
        }
    };
    public static int sDefaultImageId = R.drawable.ic_launcher;
    public static int sDefaultFaultId = R.drawable.ic_launcher;
    private int mDefaultImageId = sDefaultImageId;
    private int mDefaultFaultId = sDefaultFaultId;
    private String mUrl;
    private ImageRequest mImageRequest = null;
    public NetworkImageView(Context context) {
        super(context);
    }
    public NetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public NetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Sets the default image resource ID to be used for this view until the attempt to load it
     * completes.
     */
    public void setDefaultImageId(int id){
        mDefaultImageId = id;
    }
    /**
     * Sets the error image resource ID to be used for this view in the event that the image
     * requested fails to load.
     */
    public void setDefaultFaultId(int id){
        mDefaultFaultId = id;
    }
    public void setUrl(String url){
        if(mUrl == url) return ;
        if(!TextUtils.isEmpty(mUrl) && mUrl.equals(url)) return ;

        if (mImageRequest != null) {
            mImageRequest.cancel();
        }
        mImageRequest = null;
        setImageBitmap(null);
        mUrl = url;
        loadImageIfNecessary();
    }
    void loadImageIfNecessary(){
        Bitmap bitmap = sLruCache.get(mUrl);
        if(null != bitmap) {
            this.setImageBitmap(bitmap);
        }
        mImageRequest = new ImageRequest(mUrl,new DefaultResponseDelivery(new Handler(Looper.getMainLooper())),this,this );
        mImageRequest.setMaxWidth(getMaxWidth());
        mImageRequest.setMaxHeight(getMaxHeight());
        sRequestQueue.add(mImageRequest);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }
    @Override
    protected void onDetachedFromWindow() {
        if (mImageRequest != null) {
            mImageRequest.cancel();
            setImageBitmap(null);
        }
        mImageRequest = null;
        super.onDetachedFromWindow();
    }
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }

    @Override
    public void onErrorResponse(XError error) {
        this.setImageResource(mDefaultFaultId);
    }

    @Override
    public void onResponse(Bitmap response) {
        if(null == mImageRequest) return ;
        if (null != response) {
            sLruCache.put(mUrl, response);
        }
        this.setImageBitmap(response);
    }
}
