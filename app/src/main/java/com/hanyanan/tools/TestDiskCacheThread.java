package com.hanyanan.tools;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.hanyanan.tools.storage.Error.BusyInUsingError;
import com.hanyanan.tools.storage.IStreamStorage;
import com.hanyanan.tools.storage.disk.FixSizeDiskStorage;
import com.hanyanan.tools.xasynctask.DefaultResponseDelivery;
import com.hanyanan.tools.xasynctask.RequestExecutor;
import com.hanyanan.tools.xasynctask.RequestQueue;
import com.hanyanan.tools.xasynctask.Response;
import com.hanyanan.tools.xasynctask.ResponseDelivery;
import com.hanyanan.tools.xasynctask.XError;
import com.hanyanan.tools.xasynctask.network.HttpStack;
import com.hanyanan.tools.xasynctask.network.HurlStack;
import com.hanyanan.tools.xasynctask.network.NetworkError;
import com.hanyanan.tools.xasynctask.network.NetworkRequest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Created by hanyanan on 2014/8/6.
 */
public class TestDiskCacheThread extends Thread{
    private static final String TAG = "TestStorage";
    private static final String [] urls = new String[]{
            "http://file20.mafengwo.net/M00/A5/70/wKgB21A2RUiMftZQAA7bqi8IHxI08.jpeg",
            "http://file21.mafengwo.net/M00/A5/79/wKgB21A2RUzwJi4BAAk0KD0rHUA83.jpeg",
            "http://file21.mafengwo.net/M00/A5/81/wKgB21A2RVLZQvtBAAviJjczhZ889.jpeg",
            "http://file20.mafengwo.net/M00/A5/88/wKgB21A2RVjTgOhaAAm_VjZK8Po55.jpeg",
            "http://file20.mafengwo.net/M00/08/28/wKgB21Azm7uTMDN1AAzFXCEDKmM42.jpeg",
            "http://file20.mafengwo.net/M00/A5/A2/wKgB21A2RWrZJkzKAAu3g7dMgtU59.jpeg",
            "http://file20.mafengwo.net/M00/A5/9A/wKgB21A2RWSIJfdbAAnzXLZwY9U25.jpeg",
            "http://file20.mafengwo.net/M00/A5/B2/wKgB21A2RW-nrL4dAAjto2TzR5I79.jpeg",
            "http://file20.mafengwo.net/M00/A5/C8/wKgB21A2RXyauesGAArvnpu83RU89.jpeg",
            "http://file20.mafengwo.net/M00/A5/C0/wKgB21A2RXaxOVxaAAwCkcokxpc58.jpeg",
            "http://file21.mafengwo.net/M00/A5/CF/wKgB21A2RYLXh491AArbjHJDq5M40.jpeg",
            "http://file21.mafengwo.net/M00/A5/D5/wKgB21A2RYij-PcZAAlINZkzGKk17.jpeg",
            "http://file21.mafengwo.net/M00/A5/DD/wKgB21A2RY3xpf2tAAkmIYpAMMk76.jpeg",
            "http://file21.mafengwo.net/M00/A5/DF/wKgB21A2RZHKy6-3AAfMEREz0XA05.jpeg",
            "http://file21.mafengwo.net/M00/A5/E7/wKgB21A2RZfh_kgvAAq7E33m80s94.jpeg",
            "http://file20.mafengwo.net/M00/A5/70/wKgB21A2RUiMftZQAA7bqi8IHxI08.jpeg",
            "http://file21.mafengwo.net/M00/A5/79/wKgB21A2RUzwJi4BAAk0KD0rHUA83.jpeg",
            "http://file21.mafengwo.net/M00/A5/81/wKgB21A2RVLZQvtBAAviJjczhZ889.jpeg",
            "http://file20.mafengwo.net/M00/A5/88/wKgB21A2RVjTgOhaAAm_VjZK8Po55.jpeg",
            "http://file20.mafengwo.net/M00/08/28/wKgB21Azm7uTMDN1AAzFXCEDKmM42.jpeg",
            "http://file20.mafengwo.net/M00/A5/A2/wKgB21A2RWrZJkzKAAu3g7dMgtU59.jpeg",
            "http://file20.mafengwo.net/M00/A5/9A/wKgB21A2RWSIJfdbAAnzXLZwY9U25.jpeg",
            "http://file20.mafengwo.net/M00/A5/B2/wKgB21A2RW-nrL4dAAjto2TzR5I79.jpeg",
            "http://file20.mafengwo.net/M00/A5/C8/wKgB21A2RXyauesGAArvnpu83RU89.jpeg",
            "http://file20.mafengwo.net/M00/A5/C0/wKgB21A2RXaxOVxaAAwCkcokxpc58.jpeg",
            "http://file21.mafengwo.net/M00/A5/CF/wKgB21A2RYLXh491AArbjHJDq5M40.jpeg",
            "http://file21.mafengwo.net/M00/A5/D5/wKgB21A2RYij-PcZAAlINZkzGKk17.jpeg",
            "http://file21.mafengwo.net/M00/A5/DD/wKgB21A2RY3xpf2tAAkmIYpAMMk76.jpeg",
            "http://file21.mafengwo.net/M00/A5/DF/wKgB21A2RZHKy6-3AAfMEREz0XA05.jpeg",
            "http://file21.mafengwo.net/M00/A5/E7/wKgB21A2RZfh_kgvAAq7E33m80s94.jpeg",
            "http://file20.mafengwo.net/M00/A5/70/wKgB21A2RUiMftZQAA7bqi8IHxI08.jpeg",
            "http://file21.mafengwo.net/M00/A5/79/wKgB21A2RUzwJi4BAAk0KD0rHUA83.jpeg",
            "http://file21.mafengwo.net/M00/A5/81/wKgB21A2RVLZQvtBAAviJjczhZ889.jpeg",
            "http://file20.mafengwo.net/M00/A5/88/wKgB21A2RVjTgOhaAAm_VjZK8Po55.jpeg",
            "http://file20.mafengwo.net/M00/08/28/wKgB21Azm7uTMDN1AAzFXCEDKmM42.jpeg",
            "http://file20.mafengwo.net/M00/A5/A2/wKgB21A2RWrZJkzKAAu3g7dMgtU59.jpeg",
            "http://file20.mafengwo.net/M00/A5/9A/wKgB21A2RWSIJfdbAAnzXLZwY9U25.jpeg",
            "http://file20.mafengwo.net/M00/A5/B2/wKgB21A2RW-nrL4dAAjto2TzR5I79.jpeg",
            "http://file20.mafengwo.net/M00/A5/C8/wKgB21A2RXyauesGAArvnpu83RU89.jpeg",
            "http://file20.mafengwo.net/M00/A5/C0/wKgB21A2RXaxOVxaAAwCkcokxpc58.jpeg",
            "http://file21.mafengwo.net/M00/A5/CF/wKgB21A2RYLXh491AArbjHJDq5M40.jpeg",
            "http://file21.mafengwo.net/M00/A5/D5/wKgB21A2RYij-PcZAAlINZkzGKk17.jpeg",
            "http://file21.mafengwo.net/M00/A5/DD/wKgB21A2RY3xpf2tAAkmIYpAMMk76.jpeg",
            "http://file21.mafengwo.net/M00/A5/DF/wKgB21A2RZHKy6-3AAfMEREz0XA05.jpeg",
            "http://file21.mafengwo.net/M00/A5/E7/wKgB21A2RZfh_kgvAAq7E33m80s94.jpeg",
            "http://file20.mafengwo.net/M00/A5/70/wKgB21A2RUiMftZQAA7bqi8IHxI08.jpeg",
            "http://file21.mafengwo.net/M00/A5/79/wKgB21A2RUzwJi4BAAk0KD0rHUA83.jpeg",
            "http://file21.mafengwo.net/M00/A5/81/wKgB21A2RVLZQvtBAAviJjczhZ889.jpeg",
            "http://file20.mafengwo.net/M00/A5/88/wKgB21A2RVjTgOhaAAm_VjZK8Po55.jpeg",
            "http://file20.mafengwo.net/M00/08/28/wKgB21Azm7uTMDN1AAzFXCEDKmM42.jpeg",
            "http://file20.mafengwo.net/M00/A5/A2/wKgB21A2RWrZJkzKAAu3g7dMgtU59.jpeg",
            "http://file20.mafengwo.net/M00/A5/9A/wKgB21A2RWSIJfdbAAnzXLZwY9U25.jpeg",
            "http://file20.mafengwo.net/M00/A5/B2/wKgB21A2RW-nrL4dAAjto2TzR5I79.jpeg",
            "http://file20.mafengwo.net/M00/A5/C8/wKgB21A2RXyauesGAArvnpu83RU89.jpeg",
            "http://file20.mafengwo.net/M00/A5/C0/wKgB21A2RXaxOVxaAAwCkcokxpc58.jpeg",
            "http://file21.mafengwo.net/M00/A5/CF/wKgB21A2RYLXh491AArbjHJDq5M40.jpeg",
            "http://file21.mafengwo.net/M00/A5/D5/wKgB21A2RYij-PcZAAlINZkzGKk17.jpeg",
            "http://file21.mafengwo.net/M00/A5/DD/wKgB21A2RY3xpf2tAAkmIYpAMMk76.jpeg",
            "http://file21.mafengwo.net/M00/A5/DF/wKgB21A2RZHKy6-3AAfMEREz0XA05.jpeg",
            "http://file21.mafengwo.net/M00/A5/E7/wKgB21A2RZfh_kgvAAq7E33m80s94.jpeg",
    };
    private Context mContext;
    public TestDiskCacheThread(Context context){
        mContext = context;
    }
    public void run(){
        Looper.prepare();
        RequestQueue requestQueue = new RequestQueue(4);
        requestQueue.start();
        Log.d(TAG, "create RequestQueue 4");
        FixSizeDiskStorage fixSizeDiskStorage = null;
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/T/");
            Log.d(TAG, "disk cache path "+file.getAbsolutePath());
            fixSizeDiskStorage = FixSizeDiskStorage.open(file,1,10 * 1024 * 1024);
            Log.d(TAG, "create disk cache Success" );
        } catch (IOException e) {
            e.printStackTrace();
            return ;
        }
        DownloadRequestExecutor  executor = new DownloadRequestExecutor(new HurlStack());
        int count = 10;
        for(int i =0;i<count;++i){
            DownloadRequest nr = new DownloadRequest(urls[i%urls.length],fixSizeDiskStorage,""+i,executor,null);
            requestQueue.add(nr);
        }
    }

    public static class DownloadRequest extends NetworkRequest<String>{
        private final FixSizeDiskStorage mFixSizeDiskStorage;private final String mKey;

        public DownloadRequest(String url,FixSizeDiskStorage fixSizeDiskStorage,String key,
                               DownloadRequestExecutor requestExecutor, Response.ErrorListener listener) {
            super(url, null, requestExecutor, new DefaultResponseDelivery(new Handler()), listener);
            mFixSizeDiskStorage = fixSizeDiskStorage;
            mKey = key;
        }
        public String getKey(){return mKey;}
        public FixSizeDiskStorage getFixSizeDiskStorage(){return mFixSizeDiskStorage;}
    };

    public static class DownloadRequestExecutor implements RequestExecutor<String,DownloadRequest> {
        private final HttpStack mHttpStack;
        public DownloadRequestExecutor(HttpStack httpStack){
            mHttpStack = httpStack;
        }
        @Override
        public Response<String> performRequest(DownloadRequest  request) throws XError{
            Log.d(TAG, "performRequest "+request.getKey());
            byte[] buff = new byte[4098];
            try {
                InputStream inputStream = mHttpStack.performStreamRequest(request, request.getParams());
                Log.d(TAG, "performRequest InputStream "+inputStream);
                IStreamStorage.Editor editor = request.getFixSizeDiskStorage().edit(request.getKey());
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
    };
}
