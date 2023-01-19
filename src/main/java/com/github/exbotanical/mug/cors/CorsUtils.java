package com.github.exbotanical.mug.cors;

import com.github.exbotanical.mug.constant.Method;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Common utilities for use with CORS impl.
 */
class CorsUtils {

  private CorsUtils() {
  }

  /**
   * Determine whether the given request satisfies the criteria of a Preflight request.
   *
   * <p>
   * A Preflight request must:
   * </p>
   *
   * <ul>
   * <li>use the OPTIONS method</li>
   * <li>include an Origin request header</li>
   * <li>Include an Access-Control-Request-Method header</li>
   * </ul>
   *
   * @param exchange The HttpExchange containing the prospective Preflight request.
   *
   * @return A boolean indicating whether the given request is a Preflight request.
   */
  static boolean isPreflightRequest(final HttpExchange exchange) {
    final boolean isOptionsReq = Method.OPTIONS.toString().equals(exchange.getRequestMethod());
    if (!isOptionsReq) {
      return false;
    }

    final Headers reqHeaders = exchange.getRequestHeaders();
    // TODO: box these - they cannot be null
    final boolean hasOriginHeader =
      NullSafe.getFirst(reqHeaders, CommonHeader.ORIGIN.value) != null;
    final boolean hasReqMethod =
      NullSafe.getFirst(reqHeaders, CommonHeader.REQUEST_METHOD.value) != null;

    return hasOriginHeader && hasReqMethod;
  }

  /**
   * Extracts headers from a given request's Access-Control-Request-Headers header.
   *
   * @param exchange The HttpExchange containing the request.
   *
   * @return A list of requested headers.
   */
  static List<String> deriveHeaders(final HttpExchange exchange) {
    // TODO: evaluate whether `getRequestHeaders` needs to be null-checked
    final String headersStr =
      NullSafe.getFirst(exchange.getRequestHeaders(), CommonHeader.REQUEST_HEADERS.value);
    final List<String> headers = new ArrayList<>();

    if (headersStr == null || "".equals(headersStr)) {
      return headers;
    }

    final int len = headersStr.length();
    final List<Character> tmp = new ArrayList<>();

    for (int i = 0; i < headersStr.length(); i++) {
      char c = headersStr.charAt(i);

      if ((c >= 'a' && c <= 'z') || c == '_' || c == '-' || c == '.' || (c >= '0' && c <= '9')) {
        tmp.add(c);
      }

      if (c >= 'A' && c <= 'Z') {
        tmp.add(Character.toLowerCase(c));
      }

      if (c == ' ' || c == ',' || i == len - 1) {
        if (tmp.size() > 0) {
          String b = tmp.stream()
            .map(Object::toString)
            .collect(Collectors.joining());

          headers.add(b);

          tmp.clear();
        }
      }
    }

    return headers;
  }
}
