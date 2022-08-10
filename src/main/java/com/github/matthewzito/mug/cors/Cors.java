package com.github.matthewzito.mug.cors;

import com.github.matthewzito.mug.constant.Method;
import com.github.matthewzito.mug.constant.Status;
import com.github.matthewzito.mug.utils.NullSafe;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * A spec-compliant CORS middleware.
 */
public class Cors {
  /**
   * A list of allowed origins for CORS requests.
   */
  public final ArrayList<String> allowedOrigins = new ArrayList<>();

  /**
   * A list of allowed HTTP methods for CORS requests.
   */
  public final ArrayList<String> allowedMethods = new ArrayList<>();

  /**
   * A list of allowed non-simple headers for CORS requests.
   */
  public final ArrayList<String> allowedHeaders = new ArrayList<>();

  /**
   * A list of non-simple headers that may be exposed to clients making CORS requests.
   */
  public final ArrayList<String> exposedHeaders;

  /**
   * A flag indicating whether the request may include Cookies.
   */
  public boolean allowCredentials;


  /**
   * A flag indicating whether all origins should be allowed for CORS requests. If `allowedOrigins
   * contains a wildcard character i.e. "*", this flag will be set to `true`.
   */
  public boolean allowAllOrigins;

  /**
   * A flag indicating whether all non-simple headers should be allowed for CORS requests. If
   * `allowedHeaders` contains a wildcard character i.e. "*", this flag will be set to `true`.
   */
  public boolean allowAllHeaders;

  /**
   * Setting this flag to `true` will allow Preflight requests to propagate to the matched
   * HttpHandler. This is useful in cases where the handler or sequence of middleware needs to
   * inspect the request subsequent to the handling of the Preflight request.
   */
  public boolean useOptionsPassthrough;

  /**
   * The suggested duration, in seconds, that a response should remain in the browser's cache before
   * another Preflight request is made.
   */
  public int maxAge;

  /**
   * Initialize CORS middleware with the provided configurations.
   *
   * @param allowedOrigins A list of allowed origins for CORS requests.
   * @param allowedMethods A list of allowed HTTP methods for CORS requests.
   * @param allowedHeaders A list of allowed non-simple headers for CORS requests.
   * @param allowCredentials A flag indicating whether the request may include Cookies.
   * @param useOptionsPassthrough Setting this flag to `true` will allow Preflight requests to
   *        propagate to the matched HttpHandler. This is useful in cases where the handler or
   *        sequence of middleware needs to inspect the request subsequent to the handling of the
   *        Preflight request.
   * @param maxAge The suggested duration, in seconds, that a response should remain in the
   *        browser's cache before another Preflight request is made.
   * @param exposeHeaders A list of non-simple headers that may be exposed to clients making CORS
   *        requests.
   */
  public Cors(CorsOptions options) {
    ArrayList<String> allowedOrigins = options.allowedOrigins();
    ArrayList<Method> allowedMethods = options.allowedMethods();
    ArrayList<String> allowedHeaders = options.allowedHeaders();

    this.allowCredentials = options.allowCredentials();
    this.useOptionsPassthrough = options.useOptionsPassthrough();
    this.maxAge = options.maxAge();
    this.exposedHeaders = options.exposeHeaders();

    // Register origins: if no given origins, default to allow all e.g. "*".
    if (allowedOrigins.size() == 0) {
      this.allowAllOrigins = true;
    } else {
      // For each origin, convert to lowercase and append.
      for (String origin : allowedOrigins) {
        origin = origin.toLowerCase();
        // If wildcard origin, override and set to allow all e.g. "*".
        if ("*".equals(origin)) {
          this.allowAllOrigins = true;
          break;
        } else {
          // Append "null" to allow list to support testing / requests from files, redirects, etc.
          // Note: Used for redirects because the browser should not expose the origin of the new
          // server; redirects are followed automatically.
          this.allowedOrigins.add("null");
          this.allowedOrigins.add(origin);
        }
      }
    }


    // Register headers: if no given headers, default to those allowed per the spec.
    // Although these headers are allowed by default, we add them anyway for the sake of
    // consistency.
    if (allowedHeaders.size() == 0) {
      this.allowedHeaders.addAll(Defaults.defaultAllowedHeaders);
    } else {
      for (String header : allowedHeaders) {
        header = header.toLowerCase();

        if ("*".equals(header)) {
          this.allowAllHeaders = true;
          break;
        } else {
          this.allowedHeaders.add(header);
        }
      }
    }

    if (allowedMethods.size() == 0) {
      this.allowedMethods.addAll(Defaults.defaultAllowedMethods);
    } else {
      for (Method method : allowedMethods) {
        this.allowedMethods.add(method.toString());
      }
    }
  }

  /**
   * Register the CORS middleware. `use` expects a Router or HttpHandler; the CORS middleware will
   * wrap this handler and process all CORS requests prior to handler execution.
   *
   * @param handler A Router or HttpHandler intended to be used as the root context / handler for an
   *        HTTP server.
   * @return A modified HttpHandler wrapped in CORS processing logic.
   */
  public HttpHandler use(HttpHandler handler) {
    return (exchange) -> {
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
   * @return A boolean indicating whether the origin is allowed per the CORS impl.
   */
  protected boolean isOriginAllowed(String origin) {
    if (this.allowAllOrigins) {
      return true;
    }

    origin = origin.toLowerCase();
    for (String allowedOrigin : this.allowedOrigins) {
      // @todo regex
      if (origin.equals(allowedOrigin)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Determines whether the given method is allowed per the user-defined allow list.
   *
   * @param method The request method.
   * @return A boolean indicating whether the method is allowed per the CORS impl.
   */
  protected boolean isMethodAllowed(String method) {
    if (this.allowedMethods.size() == 0) {
      return false;
    }

    method = method.toUpperCase();
    if (Method.OPTIONS.toString().equals(method)) {
      return true;
    }

    for (String allowedMethod : this.allowedMethods) {
      if (method.equals(allowedMethod)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Determines whether the given headers are allowed per the user-defined allow list.
   *
   * @param headers The non-simple headers provided by the request via its
   *        Access-Control-Request-Headers header.
   * @return A boolean indicating whether the headers are allowed per the CORS impl.
   */
  protected boolean areHeadersAllowed(ArrayList<String> headers) {
    if (this.allowAllHeaders || headers.size() == 0) {
      return true;
    }

    for (String header : headers) {
      // @todo canonicalize header
      boolean allowsHeader = false;

      for (String allowedHeader : this.allowedHeaders) {
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
  private void handlePreflightRequest(HttpExchange exchange) {
    Headers reqHeaders = exchange.getRequestHeaders();
    Headers resHeaders = exchange.getResponseHeaders();

    // Set the "vary" header to prevent proxy servers from sending cached responses for one client
    // to another.
    resHeaders.add(CommonHeader.VARY.value, CommonHeader.ORIGIN.value);
    resHeaders.add(CommonHeader.VARY.value, CommonHeader.REQUEST_METHOD.value);
    resHeaders.add(CommonHeader.VARY.value, CommonHeader.REQUEST_HEADERS.value);

    String origin = NullSafe.getFirst(reqHeaders, CommonHeader.ORIGIN.value);
    // If no origin was specified, this is not a valid CORS request.
    if (origin == null || "".equals(origin)) {
      return;
    }


    // If the origin is not in the allow list, deny.
    if (!this.isOriginAllowed(origin)) {
      return;
    }


    // Validate the method; this is the crux of the Preflight.
    String requestMethod = NullSafe.getFirst(reqHeaders, CommonHeader.REQUEST_METHOD.value);

    if (!this.isMethodAllowed(requestMethod)) {
      return;
    }

    // Validate request headers. Preflight requests are also used when requests include additional
    // headers from the client.
    ArrayList<String> requestHeaders = CorsUtils.deriveHeaders(exchange);
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
      resHeaders.set(CommonHeader.ALLOW_HEADERS.value, this.allowedHeaders.stream().collect(
          Collectors.joining(", ")));
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
  private void handleRequest(HttpExchange exchange) {
    Headers reqHeaders = exchange.getRequestHeaders();
    Headers resHeaders = exchange.getResponseHeaders();
    String origin = NullSafe.getFirst(reqHeaders, CommonHeader.ORIGIN.value);


    // Set the "vary" header to prevent proxy servers from sending cached responses for one client
    // to another.
    resHeaders.add(CommonHeader.VARY.value, CommonHeader.ORIGIN.value);

    // If no origin was specified, this is not a valid CORS request.
    if (origin == null || "".equals(origin)) {
      return;
    }

    // If the origin is not in the allow list, deny.
    if (!this.isOriginAllowed(origin)) {
      // @todo 403
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
      resHeaders.set(CommonHeader.EXPOSE_HEADERS.value, this.exposedHeaders.stream().collect(
          Collectors.joining(", ")));
    }

    // Allow the client to send credentials. If making an XHR request, the client must set
    // `withCredentials` to `true`.
    if (this.allowCredentials) {
      resHeaders.set(CommonHeader.ALLOW_CREDENTIALS.value, "true");
    }
  }
}
