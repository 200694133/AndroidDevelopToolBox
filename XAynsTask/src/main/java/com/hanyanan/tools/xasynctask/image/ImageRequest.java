package com.hanyanan.tools.xasynctask.image;

import android.graphics.Bitmap;

import com.hanyanan.tools.xasynctask.Request;
import com.hanyanan.tools.xasynctask.RequestExecutor;
import com.hanyanan.tools.xasynctask.Response;
import com.hanyanan.tools.xasynctask.ResponseDelivery;
import com.hanyanan.tools.xasynctask.RetryPolicy;

/**
 * Created by hanyanan on 2014/7/30.
 */
public class ImageRequest extends Request<Bitmap>{
    public ImageRequest(RequestExecutor requestExecutor, ResponseDelivery responseDelivery,
                        RetryPolicy retryPolicy, Response.ErrorListener listener) {
        super(requestExecutor, responseDelivery, retryPolicy, listener);
    }
}
