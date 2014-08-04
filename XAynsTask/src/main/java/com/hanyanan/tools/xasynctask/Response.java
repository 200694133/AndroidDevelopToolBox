package com.hanyanan.tools.xasynctask;

/**
 * Encapsulates a parsed response for delivery.
 *
 * @param <T> Parsed type of this response
 */
public class Response<T> {

    /** Callback interface for delivering parsed responses. */
    public interface Listener<T> {
        /** Called when a response is received. */
        public void onResponse(T response);
    }

    /** Callback interface for delivering error responses. */
    public interface ErrorListener {
        /**
         * Callback method that an error has been occurred with the
         * provided error code and optional user-readable message.
         */
        public void onErrorResponse(XError error);
    }

    /** Returns a successful response containing the parsed result. */
    public static <T> Response<T> success(T result) {
        return new Response<T>(result);
    }

    /**
     * Returns a failed response containing the given error code and an optional
     * localized message displayed to the user.
     */
    public static <T> Response<T> error(XError error) {
        return new Response<T>(error);
    }

    /** Parsed response, or null in the case of error. */
    public final T result;

    /** Detailed error information if <code>errorCode != OK</code>. */
    public final XError error;
    /**
     * Returns whether this response is considered successful.
     */
    public boolean isSuccess() {
        return error == null;
    }


    private Response(T result) {
        this.result = result;
        this.error = null;
    }

    private Response(XError error) {
        this.result = null;
        this.error = error;
    }
}
