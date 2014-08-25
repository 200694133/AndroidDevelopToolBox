package com.hanyanan.tools.schedule.http;

import com.hanyanan.tools.schedule.RequestExecutor;
import com.hanyanan.tools.schedule.Response;
import com.hanyanan.tools.schedule.XError;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;

/**
 * Created by hanyanan on 2014/7/30.
 */
public class JsonArrayRequestExecutor implements RequestExecutor<JSONArray, NetworkRequest<JSONArray>> {
    /** Charset for request. */
    private static final String PROTOCOL_CHARSET = "utf-8";

    /** Content type for request. */
    private static final String PROTOCOL_CONTENT_TYPE = "application/json; charset=";
    private final Network mNetwork;
    public JsonArrayRequestExecutor(Network network){
        mNetwork = network;
    }
    @Override
    public Response<JSONArray> performRequest(NetworkRequest<JSONArray> request) throws XError {
        request.setBodyContentType(PROTOCOL_CONTENT_TYPE);
        request.setPramsEncoding(PROTOCOL_CHARSET);
        NetworkResponse res = mNetwork.performRequest(request);
        try {
            String jsonString =
                    new String(res.data, StringRequestExecutor.parseCharset(res.headers));
            return Response.success(new JSONArray(jsonString));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }
}
