package com.github.exbotanical.mug.cors;

import com.github.exbotanical.mug.constant.Method;
import java.util.ArrayList;

/**
 * Options for CORS middleware.
 *
 * @param allowedOrigins A list of allowed origins for CORS requests.
 * @param allowedMethods A list of allowed HTTP methods for CORS requests.
 * @param allowedHeaders A list of allowed non-simple headers for CORS requests.
 * @param allowCredentials A flag indicating whether the request may include Cookies.
 * @param useOptionsPassthrough Setting this flag to `true` will allow Preflight requests to
 *        propagate to the matched HttpHandler. This is useful in cases where the handler or
 *        sequence of middleware needs to inspect the request subsequent to the handling of the
 *        Preflight request.
 * @param maxAge The suggested duration, in seconds, that a response should remain in the browser's
 *        cache before another Preflight request is made.
 * @param exposeHeaders A list of non-simple headers that may be exposed to clients making CORS
 *        requests.
 */
public record CorsOptions(
        ArrayList<String> allowedOrigins,
        ArrayList<Method> allowedMethods,
        ArrayList<String> allowedHeaders,
        boolean allowCredentials,
        boolean useOptionsPassthrough,
        int maxAge,
        ArrayList<String> exposeHeaders) {
}
