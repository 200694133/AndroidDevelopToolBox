package com.hanyanan.tools.magicbox.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.hanyanan.tools.schedule.RequestQueue;

/**
 * Created by hanyanan on 2014/8/12.
 */
public class NetworkImageView extends ImageView{
    private static final RequestQueue RequestQueue =  new  RequestQueue(4);
    public static int sDefaultImageId = -1;
    public static int sDefaultFaultId = -1;
    private int mDefaultImageId = sDefaultImageId;
    private int mDefaultFaultId = sDefaultFaultId;
    private String mUrl;
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
        mUrl = url;
        loadImageIfNecessary();
    }
    void loadImageIfNecessary(){

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        loadImageIfNecessary();
    }
    @Override
    protected void onDetachedFromWindow() {
        if (mImageContainer != null) {
            // If the view was bound to an image request, cancel it and clear
            // out the image from the view.
            mImageContainer.cancelRequest();
            setImageBitmap(null);
            // also clear out the container so we can reload the image if necessary.
            mImageContainer = null;
        }
        super.onDetachedFromWindow();
    }
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        invalidate();
    }
}
