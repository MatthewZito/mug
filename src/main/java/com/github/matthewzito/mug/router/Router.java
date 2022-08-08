package com.github.matthewzito.mug.router;

import java.io.IOException;
import java.util.ArrayList;

import com.github.matthewzito.mug.router.constant.Method;
import com.github.matthewzito.mug.router.errors.MethodNotAllowedException;
import com.github.matthewzito.mug.router.errors.NotFoundException;
import com.github.matthewzito.mug.router.trie.Action;
import com.github.matthewzito.mug.router.trie.PathTrie;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class Router implements HttpHandler {
  private HttpHandler notFoundHandler = (exchange) -> {
    exchange.sendResponseHeaders(404, -1);
  };

  private HttpHandler methodNotAllowedHandler = (exchange) -> {
    exchange.sendResponseHeaders(405, -1);
  };

  private final PathTrie trie;

  public Router() {
    this.trie = new PathTrie();
  }

  public void register(ArrayList<Method> methods, String path, HttpHandler handler) {
    this.trie.insert(methods, path, handler);
  }

  public void handle(HttpExchange exchange) throws IOException {
    String path = exchange.getRequestURI().getPath();
    PathTrie.SearchResult result = this.resolve(Method.valueOf(exchange.getRequestMethod()),
        path);

    result.action().handler().handle(exchange);

    exchange.close();
  }

  private PathTrie.SearchResult resolve(Method method, String path) {
    try {
      PathTrie.SearchResult result = this.trie.search(method, path);
      return result;

    } catch (NotFoundException e) {
      return new PathTrie.SearchResult(new Action(this.notFoundHandler), new ArrayList<>());
    } catch (MethodNotAllowedException e) {
      return new PathTrie.SearchResult(new Action(this.methodNotAllowedHandler), new ArrayList<>());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
