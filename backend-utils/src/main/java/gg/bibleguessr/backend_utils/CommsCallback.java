package gg.bibleguessr.backend_utils;

/**
 * Class used whenever a communication is made to
 * a service wrapper or API gateway in the Bibleguessr
 * backend. Allows for information to be set about
 * success/failure, content response, error code, etc.
 */
public abstract class CommsCallback {

    /* ---------- METHODS ---------- */

    /**
     * Called if the communication was successful.
     *
     * @param content The content received in the
     *                response of the communication.
     */
    public abstract void onSuccess(String content);

    /**
     * Called if the communication failed.
     *
     * @param errorCode The error code received
     *                  in response to the communication
     *                  code.
     */
    public abstract void onFailure(StatusCode errorCode);

    /**
     * Attempts to call the onFailure() method with
     * the given error code.
     *
     * @param errorCode The error code to use.
     * @return Whether the onFailure() method was
     * called. Will return false if the parameter
     * is <code>null</code> or if it is the
     * OK status code.
     */
    public boolean commFailed(StatusCode errorCode) {

        // If the error code is null, or it is the OK error code,
        // return as a failure.
        if (errorCode == null || errorCode == StatusCode.OK) {
            return false;
        }

        onFailure(errorCode);
        return true;

    }

    /**
     * Attempts to call the onSuccess() method with
     * the given content.
     *
     * @param content The content to use.
     * @return Whether the onSuccess() method was
     * called. Will return false if the parameter
     * is <code>null</code>.
     */
    public boolean commSucceeded(String content) {

        // If the content is null, return as a failure.
        if (content == null) {
            return false;
        }

        onSuccess(content);
        return true;

    }

}
