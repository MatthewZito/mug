package com.github.exbotanical.mug.router;

import com.github.exbotanical.mug.constant.Method;
import com.github.exbotanical.mug.constant.Status;
import com.github.exbotanical.mug.router.annotations.Route;
import com.github.exbotanical.mug.router.errors.InvalidRouteClassException;
import com.github.exbotanical.mug.router.errors.MethodNotAllowedException;
import com.github.exbotanical.mug.router.errors.NotFoundException;
import com.github.exbotanical.mug.router.middleware.Middleware;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * An HTTP router / multiplexer.
 */
public class Router implements HttpHandler {

  /**
   * Cache for instantiated routes classes.
   */
  private HashMap<String, Object> routesClassCache = new HashMap<>();

  /**
   * The handler to be invoked when a route match is not found. Defaults to a 404 NotFound
   * empty-bodied response.
   */
  private RouteHandler notFoundHandler = (exchange, context) -> {
    exchange.sendResponseHeaders(Status.NOT_FOUND.value, -1);
  };

  /**
   * The handler to be invoked when a route match for the specific HTTP method is not found.
   * Defaults to a 405 MethodNotAllowed empty-bodied response.
   */
  private RouteHandler methodNotAllowedHandler = (exchange, context) -> {
    exchange.sendResponseHeaders(Status.METHOD_NOT_ALLOWED.value, -1);
  };

  /**
   * The route trie.
   */
  protected final PathTrie trie;

  /**
   * Implements the HttpHandler `handle` method. This allows the Router to be passed directly into
   * the HttpServer.createContext method.
   */
  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String path = exchange.getRequestURI().getPath();
    SearchResult result = this.resolve(Method.valueOf(exchange.getRequestMethod()),
        path);

    RouteHandler handler = result.action().handler();

    ArrayList<Middleware> mws = result.action().middlewares();

    // If there are any registered Middlewares, create a request chain.
    // @todo relocate so invoked once
    for (int i = 0; i < mws.size(); i++) {
      Middleware next = mws.get(mws.size() - 1 - i);
      handler = next.handle(handler);
    }

    handler.handle(exchange, null);

    exchange.close();
  }

  public Router() {
    this.trie = new PathTrie();
  }

  /**
   * Register a new route handler for each of the given HTTP methods at the provided path.
   *
   * @implNote The route handler must implement the RouteHandler interface.
   * @implNote If provided a handler for an existing route (both path and method), the existing
   *           handler will be overridden with the provided handler.
   * @param methods A list of HTTP methods at which the handler should be registered.
   * @param path The route path at which the handler should be registered.
   * @param handler A handler that is invoked to process HTTP exchanges.
   * @param middlewares A list of middleware handlers that are invoked in sequence. Each middleware
   *        method must invoke the next handler in the sequence to continue on to the next
   *        middleware, or else it must handle and close the HttpExchange.
   * @throws InvalidRouteClassException Unchecked RuntimeException.
   */
  public void register(ArrayList<Method> methods, String path,
      RouteHandler handler,
      ArrayList<Middleware> middlewares) {
    // @todo test and research whether this is best practice
    if (middlewares == null) {
      middlewares = new ArrayList<>();
    }

    this.trie.insert(methods, path, handler, middlewares);
  }

  /**
   * Auto-register a series of route handlers defined within a given class `routesClass`. To be
   * registered, a handler must be annotated with `@Route` and implement the HttpHandler interface.
   * Non-annotated and inaccessible methods will be ignored.
   *
   * @implNote If the provided class is an inner class, it must be static.
   * @implNote If provided a handler for an existing route (both path and method), the existing
   *           handler will be overridden with the provided handler.
   * @param routesClass A class containing annotated route handlers.
   * @throws InvalidRouteClassException
   *
   */
  public <T> void use(Class<T> routesClass) throws InvalidRouteClassException {
    for (java.lang.reflect.Method method : routesClass.getMethods()) {
      Route[] annotations = method.getAnnotationsByType(Route.class);

      for (Route annotation : annotations) {
        try {
          T instance = getRoutesClass(routesClass);

          java.lang.reflect.Method maybeHandler = instance
              .getClass()
              .getDeclaredMethod(method.getName(), HttpExchange.class);

          if (!maybeHandler.canAccess(instance)) {
            break;
          }

          RouteHandler handler = (exchange, context) -> {
            try {
              maybeHandler.invoke(instance, exchange);
            } catch (InvocationTargetException e) {
              e.printStackTrace();
              exchange.close();
            } catch (IllegalAccessException e) {
              e.printStackTrace();
              exchange.close();
            }
          };

          this.register(
              new ArrayList<>(Arrays.asList(annotation.method())),
              annotation.path(),
              handler,
              new ArrayList<>());

        } catch (Exception e) {
          e.printStackTrace();

          throw new InvalidRouteClassException(e);
        }
      }
    }
  }

  /**
   * Retrieve the class instance for the provided routes class. The purpose of this method is to
   * cache the instance so only one is ever instantiated.
   *
   * @param <T> The instance type.
   * @param routesClass The routes class.
   * @return The instantiated routes class instance.
   * @throws Exception Reflection exceptions galore.
   */
  private <T> T getRoutesClass(Class<T> routesClass) throws Exception {
    String name = routesClass.getClass().getName();

    T instance = (T) routesClassCache.get(name);

    if (instance == null) {
      instance = routesClass.getDeclaredConstructor().newInstance();
      routesClassCache.put(name, instance);
    }

    return instance;
  }

  /**
   * Resolves a SearchResult for the given HTTP method and path, defaulting to the `notFoundHandler`
   * or `methodNotAllowedHandler` contingent on the type of exception propagated by the PathTrie's
   * search method.
   *
   * @param method The HTTP method to search.
   * @param path The path to search.
   * @return A SearchResult record.
   */
  private SearchResult resolve(Method method, String path) {
    try {
      SearchResult result = this.trie.search(method, path);
      return result;

    } catch (NotFoundException e) {
      return new SearchResult(new Action(this.notFoundHandler, new ArrayList<>()),
          new ArrayList<>());
    } catch (MethodNotAllowedException e) {
      return new SearchResult(new Action(this.methodNotAllowedHandler, new ArrayList<>()),
          new ArrayList<>());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
