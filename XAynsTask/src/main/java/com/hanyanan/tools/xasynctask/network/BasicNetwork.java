/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hanyanan.tools.xasynctask.network;

import android.os.SystemClock;
import com.hanyanan.tools.xasynctask.XError;
import com.hanyanan.tools.xasynctask.XLog;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.conn.ConnectTimeoutException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

/**
 * A network performing Volley requests over an {@link HttpStack}.
 */
public class BasicNetwork implements Network {
    protected static final boolean DEBUG = true;//TODO

    private static int SLOW_REQUEST_THRESHOLD_MS = 3000;

    protected final HttpStack mHttpStack;



    /**
     * @param httpStack HTTP stack to be used
     */
    public BasicNetwork(HttpStack httpStack) {
        mHttpStack = httpStack;
    }

    @Override
    public NetworkResponse performRequest(NetworkRequest<?> request) throws XError {
        long requestStart = SystemClock.elapsedRealtime();
        while (true) {
            HttpResponse httpResponse = null;
            byte[] responseContents = null;
            Map<String, String> responseHeaders = new HashMap<String, String>();
            try {
                // Gather headers.
                Map<String, String> headers = new HashMap<String, String>();
//                addCacheHeaders(headers, request.getCacheEntry());
                httpResponse = mHttpStack.performRequest(request, headers);
                StatusLine statusLine = httpResponse.getStatusLine();
                int statusCode = statusLine.getStatusCode();

                responseHeaders = convertHeaders(httpResponse.getAllHeaders());
//                // Handle cache validation.
//                if (statusCode == HttpStatus.SC_NOT_MODIFIED) {
//                    return new NetworkResponse(HttpStatus.SC_NOT_MODIFIED,
//                            request.getCacheEntry() == null ? null : request.getCacheEntry().data,
//                            responseHeaders, true);
//                }

                // Some responses such as 204s do not have content.  We must check.
                if (httpResponse.getEntity() != null) {
                  responseContents = entityToBytes(httpResponse.getEntity());
                } else {
                  // Add 0 byte response as a way of honestly representing a
                  // no-content request.
                  responseContents = new byte[0];
                }

                // if the request is slow, log it.
                long requestLifetime = SystemClock.elapsedRealtime() - requestStart;
//                logSlowRequests(requestLifetime, request, responseContents, statusLine);
//TODO
                if (statusCode < 200 || statusCode > 299) {
                    throw new IOException();
                }
                return new NetworkResponse(statusCode, responseContents, responseHeaders, false);
            } catch (SocketTimeoutException e) {
                throw new NetworkError(e);
            } catch (ConnectTimeoutException e) {
                throw new TimeoutError(e.toString());
            } catch (MalformedURLException e) {
                throw new RuntimeException("Bad URL " + request.getUrl(), e);
            } catch (IOException e) {
                int statusCode = 0;
                NetworkResponse networkResponse = null;
                if (httpResponse != null) {
                    statusCode = httpResponse.getStatusLine().getStatusCode();
                } else {
                    throw new NoConnectionError(e);
                }
                XLog.e("Unexpected response code %d for %s", statusCode, request.getUrl());
                if (responseContents != null) {
                    networkResponse = new NetworkResponse(statusCode, responseContents,
                            responseHeaders, false);
                    if (statusCode == HttpStatus.SC_UNAUTHORIZED ||
                            statusCode == HttpStatus.SC_FORBIDDEN) {
//                        attemptRetryOnException("auth",
//                                request, new AuthFailureError(networkResponse));
                        throw new AuthFailureError("auth error "+request.getUrl());
                    } else {
                        // TODO: Only throw ServerError for 5xx status codes.
                        throw new ServerError(networkResponse);
                    }
                } else {
                    throw new NetworkError(networkResponse);
                }
            }
        }
    }

    protected void logError(String what, String url, long start) {
        long now = SystemClock.elapsedRealtime();
        XLog.v("HTTP ERROR(%s) %d ms to fetch %s", what, (now - start), url);
    }

    /** Reads the contents of HttpEntity into a byte[]. */
    private byte[] entityToBytes(HttpEntity entity) throws IOException, ServerError {
//        PoolingByteArrayOutputStream bytes =
//                new PoolingByteArrayOutputStream(mPool, (int) entity.getContentLength());
        byte[] buffer = new byte[(int) entity.getContentLength()];
        try {
            InputStream in = entity.getContent();
            if (in == null) {
                throw new ServerError();
            }
            in.read(buffer);
//            buffer = mPool.getBuf(1024);
//            int count;
//            while ((count = in.read(buffer)) != -1) {
//                bytes.write(buffer, 0, count);
//            }
//            return bytes.toByteArray();
            return buffer;
        } finally {
            try {
                // Close the InputStream and release the resources by "consuming the content".
                entity.consumeContent();
            } catch (IOException e) {
                // This can happen if there was an exception above that left the entity in
                // an invalid state.
                XLog.v("Error occurred when calling consumingContent");
            }
        }
    }

    /**
     * Converts Headers[] to Map<String, String>.
     */
    private static Map<String, String> convertHeaders(Header[] headers) {
        Map<String, String> result = new HashMap<String, String>();
        for (int i = 0; i < headers.length; i++) {
            result.put(headers[i].getName(), headers[i].getValue());
        }
        return result;
    }
}
