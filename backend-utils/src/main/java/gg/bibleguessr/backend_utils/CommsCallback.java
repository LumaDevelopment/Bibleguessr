package gg.bibleguessr.backend_utils;

/**
 * Class used whenever a communication is made to
 * a service wrapper or API gateway in the Bibleguessr
 * backend. Allows for information to be set about
 * success/failure, content response, error code, etc.
 *
 */
public abstract class CommsCallback {

    /* ---------- INSTANCE VARIABLES ---------- */

    /**
     * The String content retrieved in response
     * to our request. Null if the communication
     * was a failure.
     */
    private String content;

    /**
     * The status code received when attempting
     * to make the request. Could be an OK status,
     * otherwise, it is an error code.
     */
    private StatusCode statusCode;

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
     * Set the content of this callback to represent the
     * attempted communication was a failure.
     *
     * @param errorCode The error code to set.
     * @return Whether the set was successful. May return
     * false if this callback was already set as a success,
     * or if the parameter is invalid.
     */
    public boolean commFailed(StatusCode errorCode) {

        // If the error code is null, or it is the OK error code,
        // or this callback has already been set as a success,
        // return as a failure.
        if (errorCode == null || errorCode == StatusCode.OK || this.content != null) {
            return false;
        }

        this.statusCode = errorCode;
        return true;

    }

    /**
     * Set the content of this callback to represent the
     * attempted communication was a success.
     *
     * @param content The content to set.
     * @return Whether the set was successful. May return
     * false if this callback was already set as a failure,
     * or if the parameter is invalid.
     */
    public boolean commSucceeded(String content) {

        // If the content is null or this callback has already
        // been set as a failure, return as a failure.
        if (content == null || this.statusCode != null) {
            return false;
        }

        this.content = content;
        this.statusCode = StatusCode.OK;
        return true;

    }

    /**
     * Returns whether the communication this
     * callback was made for failed.
     *
     * @return Whether the communication failed.
     * Will return false if the content of the
     * callback has not been set yet.
     */
    public boolean didCommFail() {
        return this.statusCode != null && this.statusCode != StatusCode.OK;
    }

    /**
     * Whether the content of this CommsCallback has been set.
     * If true, this means that commSucceeded() or commFailed()
     * has been called on this object and succeeded.
     *
     * @return Whether the content of this CommsCallback has been set.
     */
    public boolean resultSet() {
        return this.content != null || (this.statusCode != null && this.statusCode != StatusCode.OK);
    }

}
