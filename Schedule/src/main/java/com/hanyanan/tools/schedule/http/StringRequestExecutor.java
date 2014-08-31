package com.hanyanan.tools.schedule.http;

import com.hanyanan.tools.schedule.RequestExecutor;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.XError;

import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by hanyanan on 2014/7/30.
 */
public class StringRequestExecutor implements RequestExecutor<String, NetworkRequest> {
    private final HttpInterface mHttpInterface;
    public StringRequestExecutor(HttpInterface network){
        mHttpInterface = network;
    }
    @Override
    public Response<String> performRequest(NetworkRequest request) throws XError {
        try {
            BasicHttpResponse res = mHttpInterface.performSimpleRequest(request);
            if(null == res) return Response.success("");
            byte[] data = HttpUtils.entityToBytes(res.getEntity());

        } catch (IOException e) {
            e.printStackTrace();
        }
        String parsed;
        try {
            parsed = new String(res.data, parseCharset(res.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(res.data);
        }
        return Response.success(parsed);
    }

    /**
     * Returns the charset specified in the Content-Type of this header,
     * or the HTTP default (ISO-8859-1) if none can be found.
     */
    public static String parseCharset(Map<String, String> headers) {
        String contentType = headers.get(HTTP.CONTENT_TYPE);
        if (contentType != null) {
            String[] params = contentType.split(";");
            for (int i = 1; i < params.length; i++) {
                String[] pair = params[i].trim().split("=");
                if (pair.length == 2) {
                    if (pair[0].equals("charset")) {
                        return pair[1];
                    }
                }
            }
        }

        return HTTP.DEFAULT_CONTENT_CHARSET;
    }


}
