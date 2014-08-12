package com.hanyanan.tools.schedule.network;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hanyanan.tools.schedule.RequestExecutor;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.XError;

import java.io.UnsupportedEncodingException;

/**
 * Created by hanyanan on 2014/7/30.
 */
public class GsonObjectRequestExecutor<T> implements RequestExecutor<T, NetworkRequest<T>> {
    /** Charset for request. */
    private static final String PROTOCOL_CHARSET = "utf-8";

    /** Content type for request. */
    private static final String PROTOCOL_CONTENT_TYPE = "application/json; charset=";
    private final Network mNetwork;
    public GsonObjectRequestExecutor(Network network){
        mNetwork = network;
    }
    @Override
    public Response<T> performRequest(NetworkRequest<T> request) throws XError {
        request.setBodyContentType(PROTOCOL_CONTENT_TYPE);
        request.setPramsEncoding(PROTOCOL_CHARSET);
        NetworkResponse res = mNetwork.performRequest(request);
        try {
            Gson gson = new Gson();

            String jsonString =
                    new String(res.data, StringRequestExecutor.parseCharset(res.headers));
            T t = (T)gson.fromJson(jsonString, new TypeToken<T>() {}.getType());
            return Response.success(t);
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }
}
