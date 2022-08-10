package com.github.matthewzito.mug.cors;

import com.github.matthewzito.mug.constant.Method;
import com.github.matthewzito.mug.utils.NullSafe;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Common utilities for use with CORS impl.
 */
public class CorsUtils {

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
   * @return A boolean indicating whether the given request is a Preflight request.
   */
  public static boolean isPreflightRequest(HttpExchange exchange) {
    boolean isOptionsReq = Method.OPTIONS.toString().equals(exchange.getRequestMethod());
    if (!isOptionsReq) {
      return false;
    }

    Headers reqHeaders = exchange.getRequestHeaders();
    // @todo null safe
    boolean hasOriginHeader =
        NullSafe.getFirst(reqHeaders, CommonHeader.ORIGIN.value) != null;
    // @todo null safe
    boolean hasReqMethod =
        NullSafe.getFirst(reqHeaders, CommonHeader.REQUEST_METHOD.value) != null;

    return hasOriginHeader && hasReqMethod;
  }

  /**
   * Extracts headers from a given request's Access-Control-Request-Headers header.
   *
   * @param exchange The HttpExchange containing the request.
   * @return A list of requested headers.
   */
  public static ArrayList<String> deriveHeaders(HttpExchange exchange) {
    // @todo evaluate whether `getRequestHeaders` needs to be null-checked
    String headersStr =
        NullSafe.getFirst(exchange.getRequestHeaders(), CommonHeader.REQUEST_HEADERS.value);
    ArrayList<String> headers = new ArrayList<>();

    if (headersStr == null || "".equals(headersStr)) {
      return headers;
    }

    int len = headersStr.length();
    ArrayList<Character> tmp = new ArrayList<>();

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
              .map(e -> e.toString())
              .collect(Collectors.joining());

          headers.add(b);

          tmp.clear();
        }
      }
    }

    return headers;
  }
}
