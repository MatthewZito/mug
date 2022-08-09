package com.github.matthewzito.mug.router.trie;

import com.github.matthewzito.mug.router.middleware.Middleware;
import com.sun.net.httpserver.HttpHandler;
import java.util.ArrayList;

/**
 * Represents a handler or sequence of handlers to be invoked upon a route match.
 */
public record Action(HttpHandler handler, ArrayList<Middleware> middlewares) {
}
