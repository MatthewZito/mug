package com.github.matthewzito.mug.cors;

/**
 * Common headers for use with CORS middleware.
 */
public enum CommonHeader {

  // Set by server and specifies the allowed origin. Must be a single value, or a wildcard for allow
  // all origins.
  ALLOW_ORIGINS("Access-Control-Allow-Origin"),

  // Set by server and specifies the allowed methods. May be multiple values.
  ALLOW_METHODS("Access-Control-Allow-Methods"),

  // Set by server and specifies the allowed headers. May be multiple values.
  ALLOW_HEADERS("Access-Control-Allow-Headers"),

  // Set by server and specifies whether the client may send credentials. The client may still send
  // credentials if the request was
  // not preceded by a Preflight and the client specified `withCredentials`.
  ALLOW_CREDENTIALS("Access-Control-Allow-Credentials"),

  // Set by server and specifies which non-simple response headers may be visible to the client.
  EXPOSE_HEADERS("Access-Control-Expose-Headers"),

  // Set by server and specifies how long, in seconds, a response can stay in the browser's cache
  // before another Preflight is made.
  MAX_AGE("Access-Control-Max-Age"),

  // Sent via Preflight when the client is using a non-simple HTTP method.
  REQUEST_METHOD("Access-Control-Request-Method"),

  // Sent via Preflight when the client has set additional headers. May be multiple values.
  REQUEST_HEADERS("Access-Control-Request-Headers"),

  // Specifies the origin of the request or response.
  ORIGIN("Origin"),

  // Set by server and tells proxy servers to take into account the Origin header when deciding
  // whether to send cached content.
  VARY("Vary");

  public final String value;

  private CommonHeader(String value) {
    this.value = value;
  }
}
