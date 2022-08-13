package com.github.exbotanical.mug.router.middleware;

import com.sun.net.httpserver.HttpExchange;

/**
 * Represents an authentication middleware filter, to be used with a Server's internal, base
 * context.
 */
@FunctionalInterface
public interface AuthenticationMiddleware {
  public boolean handle(HttpExchange exchange);
}
