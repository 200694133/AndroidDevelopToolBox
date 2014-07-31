package com.hanyanan.tools.xasynctask.network;

import com.hanyanan.tools.xasynctask.RequestExecutor;
import com.hanyanan.tools.xasynctask.Response;
import com.hanyanan.tools.xasynctask.XError;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by hanyanan on 2014/7/30.
 */
public class JsonObjectRequestExecutor implements RequestExecutor<JSONObject, NetworkRequest<JSONObject>> {
    /** Charset for request. */
    private static final String PROTOCOL_CHARSET = "utf-8";

    /** Content type for request. */
    private static final String PROTOCOL_CONTENT_TYPE = "application/json; charset=";
    private final Network mNetwork;
    public JsonObjectRequestExecutor(Network network){
        mNetwork = network;
    }
    @Override
    public Response<JSONObject> performRequest(NetworkRequest<JSONObject> request) throws XError {
        request.setBodyContentType(PROTOCOL_CONTENT_TYPE);
        request.setPramsEncoding(PROTOCOL_CHARSET);
        NetworkResponse res = mNetwork.performRequest(request);
        try {
            String jsonString =
                    new String(res.data, StringRequestExecutor.parseCharset(res.headers));
            return Response.success(new JSONObject(jsonString));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }
}
