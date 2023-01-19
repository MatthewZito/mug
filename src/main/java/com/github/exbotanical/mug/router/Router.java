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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * An HTTP router / multiplexer.
 */
public class Router implements HttpHandler {

  /**
   * Cache for instantiated routes classes.
   */
  private static final HashMap<String, Object> routesClassCache = new HashMap<>();

  /**
   * The handler to be invoked when a route match is not found. Defaults to a 404
   * NotFound
   * empty-bodied response.
   */
  private static RouteHandler notFoundHandler = (exchange, context) -> {
    exchange.sendResponseHeaders(Status.NOT_FOUND.value, -1);
  };

  /**
   * The handler to be invoked when a route match for the specific HTTP method is
   * not found.
   * Defaults to a 405 MethodNotAllowed empty-bodied response.
   */
  private static RouteHandler methodNotAllowedHandler = (exchange, context) -> {
    exchange.sendResponseHeaders(Status.METHOD_NOT_ALLOWED.value, -1);
  };

  /**
   * The route trie.
   */
  protected final PathTrie trie;

  public Router() {
    trie = new PathTrie();
  }

  /**
   * Set the 404 Not Found fallback handler.
   *
   * @param handler A RouteHandler to be executed when no route match can be
   *                found.
   */
  public static void handleNotFoundWith(final RouteHandler handler) {
    notFoundHandler = handler;
  }

  /**
   * Set the 405 Method Not Allowed fallback handler.
   *
   * @param handler A RouteHandler to be executed when a route match was found but
   *                not for the requested HTTP method.
   */
  public static void handleMethodNotAllowedWith(final RouteHandler handler) {
    methodNotAllowedHandler = handler;
  }

  /**
   * Retrieve the class instance for the provided routes class. The purpose of
   * this method is to cache the instance so only one is ever instantiated.
   *
   * @param <T>         The instance type.
   * @param routesClass The routes class.
   *
   * @return The instantiated routes class instance.
   *
   * @throws NoSuchMethodException
   * @throws SecurityException
   * @throws InvocationTargetException
   * @throws InstantiationException
   * @throws IllegalAccessException
   */
  private static <T> T getRoutesClass(final Class<T> routesClass) throws
    NoSuchMethodException,
    SecurityException,
    InvocationTargetException,
    InstantiationException,
    IllegalAccessException {
    final String name = routesClass.getName();

    T instance = (T) routesClassCache.get(name);

    if (instance == null) {
      Constructor<T> constructor = routesClass.getDeclaredConstructor();

      if (!constructor.trySetAccessible()) {
        System.out.println("no workie");
      }

      instance = constructor.newInstance();
      routesClassCache.put(name, instance);
    }

    return instance;
  }

  /**
   * Implements the HttpHandler `handle` method. This allows the Router to be
   * passed directly into
   * the HttpServer.createContext method.
   */
  @Override
  public void handle(final HttpExchange exchange) throws IOException {
    final String path = exchange.getRequestURI().getPath();
    final SearchResult result = resolve(Method.valueOf(exchange.getRequestMethod()),
      path);

    final List<Middleware> mws = result.action().middlewares();
    RouteHandler handler = result.action().handler();

    // If there are any registered Middlewares, create a request chain.
    // TODO: relocate so invoked once - prob cache it and use a hash key computed from path+method
    for (int i = 0; i < mws.size(); i++) {
      final Middleware next = mws.get(mws.size() - 1 - i);
      handler = next.handle(handler);
    }

    handler.handle(exchange, new RouteContext(result.parameters()));

    exchange.close();
  }

  /**
   * Register a new route handler for each of the given HTTP methods at the
   * provided path.
   *
   * @param methods     A list of HTTP methods at which the handler should be
   *                    registered.
   * @param path        The route path at which the handler should be registered.
   * @param handler     A handler that is invoked to process HTTP exchanges.
   * @param middlewares A list of middleware handlers that are invoked in
   *                    sequence. Each middleware
   *                    method must invoke the next handler in the sequence to
   *                    continue on to the next
   *                    middleware, or else it must handle and close the
   *                    HttpExchange.
   *
   * @implNote The route handler must implement the RouteHandler interface.
   * @implNote If provided a handler for an existing route (both path and method),
   * the existing handler will be overridden with the provided handler.
   */
  public void register(final List<Method> methods, final String path, final RouteHandler handler,
                       List<Middleware> middlewares) {
    if (middlewares == null) {
      middlewares = new ArrayList<>();
    }

    trie.insert(methods, path, handler, middlewares);
  }

  /**
   * Auto-register a series of route handlers defined within a given class
   * `routesClass`. To be
   * registered, a handler must be annotated with `@Route` and implement the
   * HttpHandler interface.
   * Non-annotated and inaccessible methods will be ignored.
   *
   * @param routesClass A class containing annotated route handlers.
   *
   * @throws InvalidRouteClassException Unchecked RuntimeException.
   * @implNote If the provided class is an inner class, it must be static.
   * @implNote If provided a handler for an existing route (both path and method),
   * the existing handler will be overridden with the provided handler.
   */
  public <T> void use(final Class<T> routesClass) throws InvalidRouteClassException {
    for (java.lang.reflect.Method method : routesClass.getMethods()) {
      final Route[] annotations = method.getAnnotationsByType(Route.class);

      for (final Route annotation : annotations) {
        try {
          final T instance = getRoutesClass(routesClass);

          final java.lang.reflect.Method maybeHandler = instance
            .getClass()
            .getDeclaredMethod(method.getName(), HttpExchange.class, RouteContext.class);

          if (!maybeHandler.canAccess(instance) && !maybeHandler.trySetAccessible()) {
            // TODO: handle
            break;
          }

          final RouteHandler handler = (exchange, context) -> {
            try {
              maybeHandler.invoke(instance, exchange, context);
            } catch (InvocationTargetException e) {
              // TODO: handle
              e.printStackTrace();
              exchange.close();
            } catch (IllegalAccessException e) {
              e.printStackTrace();
              exchange.close();
            }
          };

          register(
            List.of(annotation.method()),
            annotation.path(),
            handler,
            new ArrayList<>());
        } catch (NoSuchMethodException | SecurityException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
          throw new InvalidRouteClassException(e);
        }
      }
    }
  }

  /**
   * Resolves a SearchResult for the given HTTP method and path, defaulting to the
   * `notFoundHandler`
   * or `methodNotAllowedHandler` contingent on the type of exception propagated
   * by the PathTrie's search method.
   *
   * @param method The HTTP method to search.
   * @param path   The path to search.
   *
   * @return A SearchResult record.
   */
  private SearchResult resolve(final Method method, final String path) {
    try {
      return trie.search(method, path);
    } catch (NotFoundException e) {
      return new SearchResult(new Action(notFoundHandler, new ArrayList<>()),
        new ArrayList<>());
    } catch (MethodNotAllowedException e) {
      return new SearchResult(new Action(methodNotAllowedHandler, new ArrayList<>()),
        new ArrayList<>());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
