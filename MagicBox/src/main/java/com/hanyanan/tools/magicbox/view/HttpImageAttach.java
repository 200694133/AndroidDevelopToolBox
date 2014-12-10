package com.hanyanan.tools.magicbox.view;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hanyanan on 2014/12/9.
 */
public interface HttpImageAttach {
    /**
     * Set the url of current attached image view.
     * @param url
     */
    public void setUrl(String url,HashMap<String,String> header);

    /**
     * Refresh the image view, if there is no cache in disk, then request from server. other wise, if
     * has cache in disk, still send a request to server, if server ask 304 then use disk cache, if
     * the status code is 200-299, then down load to disk cache and display the new content.
     * @param url
     */
    public void refresh(String url,HashMap<String,String> header);

    /**
     * Get current attached image view.
     * @return
     */
    public ImageView getAttachedView();

    /**
     * Get the max width of current image view.
     * @return
     */
    public int getMaxWidth();

    /**
     * Get max height of current  image view.
     * @return
     */
    public int getMaxHeight();

    /**
     * Get default response id
     * @return
     */
    public int getDefaultResourceId();

    /**
     * Get fault resource id, when get image failed from server, then display this image.
     * @return
     */
    public int getFaultResourceId();

    /**
     * Cancel the request, then display the default resource.
     */
    public void cancel();

    /**
     * generate a string as the key of disk cache.
     * @return key of cache.
     */
    public String generateKey();
}
