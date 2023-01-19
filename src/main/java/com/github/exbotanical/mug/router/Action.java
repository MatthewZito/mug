package com.github.exbotanical.mug.router;

import com.github.exbotanical.mug.router.middleware.Middleware;
import java.util.List;

/**
 * Represents a handler or sequence of handlers to be invoked upon a route
 * match.
 */
record Action(RouteHandler handler, List<Middleware> middlewares) {
}
