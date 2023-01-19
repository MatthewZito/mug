package com.github.exbotanical.mug.cors;

import com.github.exbotanical.mug.constant.Method;
import com.github.exbotanical.mug.constant.Status;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * A spec-compliant CORS middleware.
 */
public class Cors {
  /**
   * A list of allowed origins for CORS requests.
   */
  private final List<String> allowedOrigins = new ArrayList<>();

  /**
   * A list of allowed HTTP methods for CORS requests.
   */
  private final List<String> allowedMethods = new ArrayList<>();

  /**
   * A list of allowed non-simple headers for CORS requests.
   */
  private final List<String> allowedHeaders = new ArrayList<>();

  /**
   * A list of non-simple headers that may be exposed to clients making CORS requests.
   */
  private final List<String> exposedHeaders;

  /**
   * A flag indicating whether the request may include Cookies.
   */
  private final boolean allowCredentials;

  /**
   * Setting this flag to `true` will allow Preflight requests to propagate to the matched
   * HttpHandler. This is useful in cases where the handler or sequence of middleware needs to
   * inspect the request subsequent to the handling of the Preflight request.
   */
  private final boolean useOptionsPassthrough;

  /**
   * The suggested duration, in seconds, that a response should remain in the browser's cache before
   * another Preflight request is made.
   */
  private final int maxAge;

  /**
   * A flag indicating whether all origins should be allowed for CORS requests. If `allowedOrigins
   * contains a wildcard character i.e. "*", this flag will be set to `true`.
   */
  private boolean allowAllOrigins;

  /**
   * A flag indicating whether all non-simple headers should be allowed for CORS requests. If
   * `allowedHeaders` contains a wildcard character i.e. "*", this flag will be set to `true`.
   */
  private boolean allowAllHeaders;

  /**
   * Initialize CORS middleware with the provided configurations.
   */
  private Cors(Builder builder) {
    final List<String> allowedOrigins = builder.allowedOrigins;
    final List<Method> allowedMethods = builder.allowedMethods;
    final List<String> allowedHeaders = builder.allowedHeaders;

    this.allowCredentials = builder.allowCredentials;
    this.useOptionsPassthrough = builder.useOptionsPassthrough;
    this.maxAge = builder.maxAge;
    this.exposedHeaders = builder.exposeHeaders;

    // Register origins: if no given origins, default to allow all e.g. "*".
    if (allowedOrigins.size() == 0) {
      this.allowAllOrigins = true;
    } else {
      // For each origin, convert to lowercase and append.
      for (final String origin : allowedOrigins) {
        final String normalizedOrigin = origin.toLowerCase();
        // If wildcard origin, override and set to allow all e.g. "*".
        if ("*".equals(normalizedOrigin)) {
          this.allowAllOrigins = true;
          break;
        } else {
          // Append "null" to allow list to support testing / requests from files, redirects, etc.
          // Note: Used for redirects because the browser should not expose the origin of the new
          // server; redirects are followed automatically.
          this.allowedOrigins.add("null");
          this.allowedOrigins.add(normalizedOrigin);
        }
      }
    }

    // Register headers: if no given headers, default to those allowed per the spec.
    // Although these headers are allowed by default, we add them anyway for the sake of
    // consistency.
    if (allowedHeaders.size() == 0) {
      this.allowedHeaders.addAll(Defaults.defaultAllowedHeaders);
    } else {
      for (final String header : allowedHeaders) {
        final String normalizedHeader = header.toLowerCase();

        if ("*".equals(normalizedHeader)) {
          this.allowAllHeaders = true;
          break;
        } else {
          this.allowedHeaders.add(normalizedHeader);
        }
      }
    }

    if (allowedMethods.size() == 0) {
      this.allowedMethods.addAll(Defaults.defaultAllowedMethods);
    } else {
      for (final Method method : allowedMethods) {
        this.allowedMethods.add(method.toString());
      }
    }
  }

  @Override
  public String toString() {
    final StringBuilder result = new StringBuilder();
    final String newLine = System.getProperty("line.separator");

    result.append("Cors {");
    result.append(newLine);

    // determine fields declared in this class only (no fields of superclass)
    final Field[] fields = this.getClass().getDeclaredFields();

    // print field names paired with their values
    for (final Field field : fields) {
      result.append("  ");
      try {
        result.append(field.getName());
        result.append(": ");
        // requires access to private field:
        result.append(field.get(this));
      } catch (IllegalAccessException ex) {
        System.out.println(ex);
      }
      result.append(newLine);
    }
    result.append("}");

    return result.toString();
  }

  /**
   * Register the CORS middleware. `use` expects a Router or HttpHandler; the CORS middleware will
   * wrap this handler and process all CORS requests prior to handler execution.
   *
   * @param handler A Router or HttpHandler intended to be used as the root context / handler for an
   *                HTTP server.
   *
   * @return A modified HttpHandler wrapped in CORS processing logic.
   */
  public HttpHandler use(final HttpHandler handler) {
    return exchange -> {
      if (CorsUtils.isPreflightRequest(exchange)) {
        this.handlePreflightRequest(exchange);

        if (this.useOptionsPassthrough) {
          handler.handle(exchange);
          return;
        }

        exchange.sendResponseHeaders(Status.NO_CONTENT.value, -1);
      } else {
        this.handleRequest(exchange);
      }

      handler.handle(exchange);
    };
  }

  /**
   * Determines whether the given origin is allowed per the user-defined allow list.
   *
   * @param origin The request origin.
   *
   * @return A boolean indicating whether the origin is allowed per the CORS impl.
   */
  public boolean isOriginAllowed(final String origin) {
    if (this.allowAllOrigins) {
      return true;
    }

    final String normalizedOrigin = origin.toLowerCase();
    for (String allowedOrigin : this.allowedOrigins) {
      // TODO: regex
      if (normalizedOrigin.equals(allowedOrigin)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Determines whether the given method is allowed per the user-defined allow list.
   *
   * @param method The request method.
   *
   * @return A boolean indicating whether the method is allowed per the CORS impl.
   */
  public boolean isMethodAllowed(final String method) {
    if (this.allowedMethods.size() == 0) {
      return false;
    }

    final String normalizedMethod = method.toUpperCase();
    if (Method.OPTIONS.toString().equals(normalizedMethod)) {
      return true;
    }

    for (final String allowedMethod : this.allowedMethods) {
      if (normalizedMethod.equals(allowedMethod)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Determines whether the given headers are allowed per the user-defined allow list.
   *
   * @param headers The non-simple headers provided by the request via its
   *                Access-Control-Request-Headers header.
   *
   * @return A boolean indicating whether the headers are allowed per the CORS impl.
   */
  public boolean areHeadersAllowed(final List<String> headers) {
    if (this.allowAllHeaders || headers.size() == 0) {
      return true;
    }

    for (final String header : headers) {
      // TODO: canonicalize header
      boolean allowsHeader = false;

      for (final String allowedHeader : this.allowedHeaders) {
        if (header.equals(allowedHeader)) {
          allowsHeader = true;
          break;
        }
      }

      if (!allowsHeader) {
        return false;
      }
    }

    return true;
  }

  /**
   * Handle Preflight requests.
   *
   * @param exchange The HttpExchange containing the Preflight request.
   */
  private void handlePreflightRequest(final HttpExchange exchange) {
    final Headers reqHeaders = exchange.getRequestHeaders();
    final Headers resHeaders = exchange.getResponseHeaders();

    // Set the "vary" header to prevent proxy servers from sending cached responses for one client
    // to another.
    resHeaders.add(CommonHeader.VARY.value, CommonHeader.ORIGIN.value);
    resHeaders.add(CommonHeader.VARY.value, CommonHeader.REQUEST_METHOD.value);
    resHeaders.add(CommonHeader.VARY.value, CommonHeader.REQUEST_HEADERS.value);

    final String origin = NullSafe.getFirst(reqHeaders, CommonHeader.ORIGIN.value);
    // If no origin was specified, this is not a valid CORS request.
    if (origin == null || "".equals(origin)) {
      return;
    }

    // If the origin is not in the allow list, deny.
    if (!this.isOriginAllowed(origin)) {
      return;
    }

    // Validate the method; this is the crux of the Preflight.
    final String requestMethod = NullSafe.getFirst(reqHeaders, CommonHeader.REQUEST_METHOD.value);

    if (!this.isMethodAllowed(requestMethod)) {
      return;
    }

    // Validate request headers. Preflight requests are also used when requests include additional
    // headers from the client.
    final List<String> requestHeaders = CorsUtils.deriveHeaders(exchange);
    if (!this.areHeadersAllowed(requestHeaders)) {
      return;
    }

    if (this.allowAllOrigins) {
      // If all origins are allowed, use the wildcard value.
      resHeaders.set(CommonHeader.ALLOW_ORIGINS.value, "*");
    } else {
      // Otherwise, set the origin to the request origin.
      resHeaders.set(CommonHeader.ALLOW_ORIGINS.value, origin);
    }

    // Set the allowed methods, as a Preflight may have been sent if the client included non-simple
    // methods.
    resHeaders.set(CommonHeader.ALLOW_METHODS.value, requestMethod);

    // Set the allowed headers, as a Preflight may have been sent if the client included non-simple
    // headers.
    if (requestHeaders.size() > 0) {
      resHeaders.set(CommonHeader.ALLOW_HEADERS.value, String.join(", ", this.allowedHeaders));
    }

    // Allow the client to send credentials. If making an XHR request, the client must set
    // `withCredentials` to `true`.
    if (this.allowCredentials) {
      resHeaders.set(CommonHeader.ALLOW_CREDENTIALS.value, "true");
    }

    // Set the Max Age. This is only necessary for Preflights given the Max Age refers to
    // server-suggested duration, in seconds, a response should stay in the browser's cache before
    // another Preflight is made.
    if (this.maxAge > 0) {
      resHeaders.set(CommonHeader.MAX_AGE.value, String.valueOf(this.maxAge));
    }
  }

  /**
   * Handle a non-Preflight CORS request.
   *
   * @param exchange The HttpExchange containing the non-Preflight CORS request.
   */
  private void handleRequest(final HttpExchange exchange) {
    final Headers reqHeaders = exchange.getRequestHeaders();
    final Headers resHeaders = exchange.getResponseHeaders();
    final String origin = NullSafe.getFirst(reqHeaders, CommonHeader.ORIGIN.value);

    // Set the "vary" header to prevent proxy servers from sending cached responses for one client
    // to another.
    resHeaders.add(CommonHeader.VARY.value, CommonHeader.ORIGIN.value);

    // If no origin was specified, this is not a valid CORS request.
    if (origin == null || "".equals(origin)) {
      return;
    }

    // If the origin is not in the allow list, deny.
    if (!this.isOriginAllowed(origin)) {
      // TODO: 403
      return;
    }

    if (this.allowAllOrigins) {
      // If all origins are allowed, use the wildcard value.
      resHeaders.set(CommonHeader.ALLOW_ORIGINS.value, "*");
    } else {
      // Otherwise, set the origin to the request origin.
      resHeaders.set(CommonHeader.ALLOW_ORIGINS.value, origin);
    }

    // If we've exposed headers, set them.
    // If the consumer specified headers that are exposed by default, we'll still include them -
    // this is spec compliant.
    if (this.exposedHeaders.size() > 0) {
      resHeaders.set(CommonHeader.EXPOSE_HEADERS.value, String.join(", ", this.exposedHeaders));
    }

    // Allow the client to send credentials. If making an XHR request, the client must set
    // `withCredentials` to `true`.
    if (this.allowCredentials) {
      resHeaders.set(CommonHeader.ALLOW_CREDENTIALS.value, "true");
    }
  }

  /**
   * A builder for Cors middleware.
   */
  public static class Builder {
    List<String> allowedOrigins = new ArrayList<>();

    List<Method> allowedMethods = new ArrayList<>();

    List<String> allowedHeaders = new ArrayList<>();

    List<String> exposeHeaders = new ArrayList<>();

    boolean allowCredentials = false;

    boolean useOptionsPassthrough = false;

    int maxAge = 0;

    Builder() {
    }

    /**
     * Add a list of allowed origins for CORS requests.
     */
    public Builder allowedOrigins(final String... allowedOrigins) {
      this.allowedOrigins = List.of(allowedOrigins);
      return this;
    }

    /**
     * Set a list of allowed HTTP methods for CORS requests.
     */
    public Builder allowedMethods(final Method... allowedMethods) {
      this.allowedMethods = List.of(allowedMethods);
      return this;
    }

    /**
     * Set a list of allowed non-simple headers for CORS requests.
     */
    public Builder allowedHeaders(final String... allowedHeaders) {
      this.allowedHeaders = List.of(allowedHeaders);
      return this;
    }

    /**
     * Set a flag indicating whether the request may include Cookies.
     */
    public Builder allowCredentials(final boolean allowCredentials) {
      this.allowCredentials = allowCredentials;
      return this;
    }

    /**
     * Setting this flag to `true` will allow Preflight requests to propagate to the matched
     * HttpHandler. This is useful in cases where the handler or sequence of middleware needs to
     * inspect the request subsequent to the handling of the Preflight request.
     */
    public Builder useOptionsPassthrough(final boolean useOptionsPassthrough) {
      this.useOptionsPassthrough = useOptionsPassthrough;
      return this;
    }

    /**
     * Set the suggested duration, in seconds, that a response should remain in the browser's cache
     * before another Preflight request is made.
     */
    public Builder maxAge(final int maxAge) {
      this.maxAge = maxAge;
      return this;
    }

    /**
     * Set a list of non-simple headers that may be exposed to clients making CORS requests.
     */
    public Builder exposeHeaders(final String... exposeHeaders) {
      this.exposeHeaders = List.of(exposeHeaders);
      return this;
    }

    /**
     * Build the Cors instance with the provided options.
     *
     * @return Cors instance.
     */
    public Cors build() {
      return new Cors(this);
    }
  }
}
