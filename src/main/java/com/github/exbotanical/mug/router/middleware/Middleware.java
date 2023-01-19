package com.github.exbotanical.mug.router.middleware;

import com.github.exbotanical.mug.router.RouteHandler;

/**
 * Represents a single method in a continuation sequence of HttpHandlers. A Middleware method is
 * used to pre- and post-process inbound requests. Each Middleware handler in the chain invokes the
 * next within its `handle` implementation.
 */
@FunctionalInterface
public interface Middleware {
  public RouteHandler handle(final RouteHandler handler);
}
