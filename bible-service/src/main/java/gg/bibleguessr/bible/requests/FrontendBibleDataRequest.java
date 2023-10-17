package gg.bibleguessr.bible.requests;

import gg.bibleguessr.service_wrapper.Request;

import java.util.Map;

/**
 * The request to get the Bible Data needed by the
 * front end (version names, book names, data matrix, etc.)
 */
public class FrontendBibleDataRequest extends Request {

   /* ---------- CONSTANTS ---------- */

   /**
    * The path of this request. A bit shorter because the
    * class name is supposed to provide clarity for backend
    * devs (probably you, if you're reading this).
    */
   public static final String REQUEST_PATH = "get-bible-data";

   /* ---------- CONSTRUCTORS ---------- */

   /**
    * Just passes up the request path. However,
    * this request doesn't take any parameters
    * or have any other special characteristics.
    */
   public FrontendBibleDataRequest() {
      super(REQUEST_PATH);
   }

   /* ---------- METHODS ---------- */

   /***
    * There are no parameters to parse with this
    * request, so, always returns true, and
    * parameters map is not used (unless it
    * has a UUID).
    *
    * @param parameters the map of parameters
    * @return true
    */
   @Override
   public boolean parse(Map<String, String> parameters) {
      return true;
   }

}
