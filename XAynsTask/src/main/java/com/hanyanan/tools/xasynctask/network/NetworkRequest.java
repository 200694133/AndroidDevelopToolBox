package com.hanyanan.tools.xasynctask.network;

import com.hanyanan.tools.xasynctask.Request;
import com.hanyanan.tools.xasynctask.RequestExecutor;
import com.hanyanan.tools.xasynctask.Response;
import com.hanyanan.tools.xasynctask.ResponseDelivery;
import com.hanyanan.tools.xasynctask.RetryPolicy;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hanyanan on 2014/7/29.
 */
public class NetworkRequest<T> extends Request{
    /**
     * Default encoding for POST or PUT parameters. See {@link #getParamsEncoding()}.
     */
    private static final String DEFAULT_PARAMS_ENCODING = "UTF-8";

    private static final String DEFAULT_BODY_TYPE = "application/x-www-form-urlencoded; charset=";

    /**
     * Supported request methods.
     */
    public interface Method {
        int GET = 0;
        int POST = 1;
        int PUT = 2;
        int DELETE = 3;
        int HEAD = 4;
        int OPTIONS = 5;
        int TRACE = 6;
        int PATCH = 7;
    }
    /**
     * Request method of this request.  Currently supports GET, POST, PUT, DELETE, HEAD, OPTIONS,
     * TRACE, and PATCH.
     */
    private int mMethod = Method.GET;

    /** URL of this request. */
    private final String mUrl;
    /** Threshold at which we should log the request (even when debug logging is not enabled). */
    private static final long SLOW_REQUEST_THRESHOLD_MS = 3000;
    protected HashMap<String, String> mParams;
    /**
     * Creates a new request with the given method (one of the values from {@link Method}),
     * URL, and so on....
     */
    public NetworkRequest(String url, int method, HashMap<String, String> params,
                          RequestExecutor<T, NetworkRequest<?>> requestExecutor,
                          ResponseDelivery responseDelivery, Response.ErrorListener listener) {
        super(requestExecutor, responseDelivery, new NetworkRetryPolicy(), listener);
        mParams = params;
        mUrl = url;
        mMethod = method;
    }
    public NetworkRequest(String url, HashMap<String, String> params,
                          RequestExecutor<T, NetworkRequest<?>> requestExecutor,
                          ResponseDelivery responseDelivery, Response.ErrorListener listener) {
        this(url, Method.GET,params,requestExecutor,responseDelivery, listener);
    }

    public NetworkRequest(String url,RequestExecutor<T, NetworkRequest<?>> requestExecutor,
                          ResponseDelivery responseDelivery, Response.ErrorListener listener) {
        this(url, Method.GET, new HashMap<String, String>(),requestExecutor,responseDelivery, listener);
    }

    public void setMethod(int method){
        mMethod = method;
    }

    /**
     * Return the method for this request.  Can be one of the values in {@link Method}.
     */
    public int getMethod() {
        return mMethod;
    }

    /**
     * Returns the URL of this request.
     */
    public String getUrl() {
        return mUrl;
    }

    /**
     * Returns a Map of parameters to be used for a POST or PUT request.  Can throw
     * {@link AuthFailureError} as authentication may be required to provide these values.
     *
     * <p>Note that you can directly override {@link #getBody()} for custom data.</p>
     *
     * @throws AuthFailureError in the event of auth failure
     */
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParams;
    }

    /**
     * Returns which encoding should be used when converting POST or PUT parameters returned by
     * {@link #getParams()} into a raw POST or PUT body.
     *
     * <p>This controls both encodings:
     * <ol>
     *     <li>The string encoding used when converting parameter names and values into bytes prior
     *         to URL encoding them.</li>
     *     <li>The string encoding used when converting the URL encoded parameters into a raw
     *         byte array.</li>
     * </ol>
     */
    public String getParamsEncoding() {
        return mParamEncoding;
    }

    public void setPramsEncoding(String encoding){
        mParamEncoding = encoding;
    }
    private String mParamEncoding = DEFAULT_PARAMS_ENCODING;
    private String mBodyContentType = DEFAULT_BODY_TYPE;
    public void setBodyContentType(String type){
        mBodyContentType = type;
    }
    public String getBodyContentType() {
        return mBodyContentType + getParamsEncoding();
    }

    public int getTimeoutMs(){
        return 5000;
    }
    /**
     * Returns the raw POST or PUT body to be sent.
     *
     * @throws AuthFailureError in the event of auth failure
     */
    public byte[] getBody() throws AuthFailureError {
        Map<String, String> params = getParams();
        if (params != null && params.size() > 0) {
            return encodeParameters(params, getParamsEncoding());
        }
        return null;
    }

    /**
     * Converts <code>params</code> into an application/x-www-form-urlencoded encoded string.
     */
    private byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
        StringBuilder encodedParams = new StringBuilder();
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                encodedParams.append('&');
            }
            return encodedParams.toString().getBytes(paramsEncoding);
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
    }

}