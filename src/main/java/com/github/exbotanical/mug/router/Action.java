package com.github.exbotanical.mug.router;

import com.github.exbotanical.mug.router.middleware.Middleware;
import com.sun.net.httpserver.HttpHandler;
import java.util.ArrayList;

/**
 * Represents a handler or sequence of handlers to be invoked upon a route match.
 */
record Action(RouteHandler handler, ArrayList<Middleware> middlewares) {
}
