package com.hanyanan.tools.magicbox.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.hanyanan.tools.schedule.RequestExecutor;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.XError;
import com.hanyanan.tools.schedule.http.HttpConnectionExecutor;
import com.hanyanan.tools.schedule.http.HttpExecutor;
import com.hanyanan.tools.schedule.http.NetworkError;
import com.hanyanan.tools.schedule.http.NetworkRequest;
import com.hanyanan.tools.storage.Error.BusyInUsingError;
import com.hanyanan.tools.storage.disk.IStreamStorage;
import com.hanyanan.tools.storage.disk.FixSizeDiskStorage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by hanyanan on 2014/8/13.
 */
public class BitmapExecutor implements RequestExecutor<Bitmap,ImageRequest> {
    private static final String TAG = "BitmapExecutor";
    private static Response downLoad(HttpExecutor httpExecutor,FixSizeDiskStorage fixSizeDiskStorage,NetworkRequest request) throws NetworkError {
        byte[] buff = new byte[4098];
        try {
            InputStream inputStream = httpExecutor.performStreamRequest(request, request.getParams());
            Log.d(TAG, "performRequest InputStream " + inputStream);
            IStreamStorage.Editor editor = fixSizeDiskStorage.edit(request.getKey());
            Log.d(TAG, "performRequest editor "+editor);
            if(null == editor){
                return Response.error(new XError());
            }
            OutputStream out = editor.newOutputStream();
            int l = 0;
            while((l=inputStream.read(buff))>0){
                out.write(buff,0,l);
            }
            inputStream.close();
            out.close();
            editor.commit();
            editor.close();
            Log.d(TAG, "download "+request.getKey()+"    "+request.getUrl()+"  success");
            return Response.success(request.getKey());
        } catch (IOException e) {
            Log.d(TAG, "download "+request.getKey()+"    "+request.getUrl()+"  failed "+e.toString());
            e.printStackTrace();
            throw new NetworkError(e);
        }catch(BusyInUsingError error){
            Log.d(TAG, "download "+request.getKey()+"    "+request.getUrl()+"  failed "+error.toString());
            throw new NetworkError(error);
        }
    }
    private static final class Size{
        int mWidth, mHeight;
        public Size(int w,int h){
            mWidth = w;
            mHeight = h;
        }
    }
    private Size calculateDesiredSize(int maxWidth, int maxHeight, int actualWidth, int actualHeight){
        if(maxWidth == 0 && maxHeight == 0) return new Size(actualWidth, actualHeight);
        if(maxWidth == 0){
            return new Size(actualWidth,maxHeight<=actualHeight?maxHeight:actualHeight);
        }
        if(maxHeight == 0){
            return new Size(maxWidth>=actualWidth?actualWidth:maxWidth, actualHeight);
        }
        double wr = (double)maxWidth/actualWidth;
        double hr = (double)maxHeight/actualHeight;
        double r = Math.min(wr,hr);
        return new Size((int)(r*actualWidth),(int)(r*actualHeight));
    }

    private Bitmap parseBitmap(FixSizeDiskStorage cache, String key, int maxWidth, int maxHeight) throws IOException {
        IStreamStorage.Snapshot snapshot = cache.get(key);
        if(null == snapshot) return null;
        Bitmap.Config config = Bitmap.Config.RGB_565;
        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        if (maxWidth == 0 && maxHeight == 0) {
            decodeOptions.inPreferredConfig = config;
            Bitmap bitmap = BitmapFactory.decodeStream(snapshot.getInputStream(),null,decodeOptions);
            snapshot.close();
            return bitmap;
        }
        snapshot = cache.get(key);
        if(null == snapshot) return null;
        Bitmap bitmap = null;
        decodeOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(snapshot.getInputStream(), null, decodeOptions);
        int actualWidth = decodeOptions.outWidth;
        int actualHeight = decodeOptions.outHeight;
        Size size = calculateDesiredSize(maxWidth, maxHeight, actualWidth, actualHeight);
        int desiredWidth = size.mWidth;
        int desiredHeight = size.mHeight;
        // Decode to the nearest power of two scaling factor.
        decodeOptions.inJustDecodeBounds = false;
        decodeOptions.inSampleSize =
                findBestSampleSize(actualWidth, actualHeight, desiredWidth, desiredHeight);
        snapshot = cache.get(key);
        if(null == snapshot) return null;
        Bitmap tempBitmap =
                BitmapFactory.decodeStream(snapshot.getInputStream(),null,decodeOptions);
        snapshot.close();
        // If necessary, scale down to the maximal acceptable size.
        if (tempBitmap != null && (tempBitmap.getWidth() > desiredWidth ||
                tempBitmap.getHeight() > desiredHeight)) {
            bitmap = Bitmap.createScaledBitmap(tempBitmap,
                    desiredWidth, desiredHeight, true);
            tempBitmap.recycle();
        } else {
            bitmap = tempBitmap;
        }
        return bitmap;
    }



    @Override
    public Response<Bitmap> performRequest(ImageRequest request) throws XError {
        FixSizeDiskStorage cache = request.getFixSizeDiskStorage();
        IStreamStorage.Snapshot snapshot = null;
        try {
            snapshot = cache.get(request.getKey());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(null == snapshot) {
            downLoad(new HttpConnectionExecutor(), request.getFixSizeDiskStorage(), request);
            try {
                snapshot = cache.get(request.getKey());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(snapshot==null){
            //TODO
        }

        Bitmap bitmap = null;
        try {
            bitmap = parseBitmap(cache, request.getKey(),request.getMaxWidth(),request.getMaxHeight());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        try {
            snapshot.getInputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            //TODO
        }
        snapshot.close();
        if(null != bitmap) return Response.success(bitmap);
        return Response.error(new XError("Decode failed"));
    }
    /**
     * Returns the largest power-of-two divisor for use in downscaling a bitmap
     * that will not result in the scaling past the desired dimensions.
     *
     * @param actualWidth Actual width of the bitmap
     * @param actualHeight Actual height of the bitmap
     * @param desiredWidth Desired width of the bitmap
     * @param desiredHeight Desired height of the bitmap
     */
    // Visible for testing.
    static int findBestSampleSize(
            int actualWidth, int actualHeight, int desiredWidth, int desiredHeight) {
        double wr = (double) actualWidth / desiredWidth;
        double hr = (double) actualHeight / desiredHeight;
        double ratio = Math.min(wr, hr);
        float n = 1.0f;
        while ((n * 2) <= ratio) {
            n *= 2;
        }

        return (int) n;
    }
}
