package gg.bibleguessr.guess_counter.requests;

import gg.bibleguessr.service_wrapper.Request;

import java.util.Map;

/**
 * A request to increment the guess counter.
 * Has no parameters.
 */
public class IncrementCountRequest extends Request {

    /**
     * Request that takes in the unique path of
     * the request. Should be called by
     * subclass blank constructor.
     */
    public IncrementCountRequest() {
        super("increment-count");
    }

    @Override
    public boolean parse(Map<String, String> parameters) {
        // No parameters, this cannot fail.
        return true;
    }

}
