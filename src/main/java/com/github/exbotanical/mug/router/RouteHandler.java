package com.github.exbotanical.mug.router;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

/**
 * A handler for a given route.
 */
@FunctionalInterface
public interface RouteHandler {
  /**
   * Implements a functional interface for the handler.
   *
   * @param exchange An HttpExchange object containing the request and response objects.
   * @param context A context object containing metadata and parameter matches for the route.
   * @throws IOException Exchange read/write exceptions.
   */
  public void handle(HttpExchange exchange, final RouteContext context) throws IOException;
}
