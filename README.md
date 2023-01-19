# mug

Learning Java for a new job. Mug is a simple, light-weight, and declarative server-side framework with support for routing, middleware and authentication/authorization.

Mug uses a trie under the hood for fast HTTP multiplexing. It exposes several helper annotations for automatic route registration so you can declaratively define
grouped route handlers and avoid unnecessary boilerplate.

## Usage

### Explicit route registration

```java
public class Demo {
  // Port number for server instance.
  private static final int PORT = 5000;

  public static void main(String[] args)  {

    // Create a Middleware handler.
    Middleware mw = new Middleware() {
      @Override
      public RouteHandler handle(RouteHandler handler) {
        return (exchange, context) -> {
          System.out.println("before");
          // Invoke the handler.
          handler.handle(exchange, context);
          System.out.println("after");
        };
      }
    };

    // Create a route handler.
    RouteHandler routeHandler = (exchange, context) -> {
      // ...
    };

    // Instantiate a new Router.
    Router router = new Router();

    // Register the route handler + middleware at GET "/api".
    router.register(
      // A list of HTTP methods at which to register this handler.
      List.of(Method.GET),
      // The path at which this handler should be executed.
      "/api",
      // The route handler.
      routeHandler,
      // A list of Middleware to be invoked before and/or after the handler.
      List.of(mw)
    );

    try {
      // Create a new server that listens on port `PORT` and uses the Router.
      Server server = new Server(PORT, router);

      // Start the server.
      server.start();

      System.out.printf("Listening on port %d...\n", PORT);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
```

### Automatic route registration

```java
public class Demo {
  // Each method annotated with @Route will get registered when invoking `Router.use`,
  // provided the method implements the RouteHandler interface.
  public static class RouteHandlers {
    @Route(method = Method.GET, path = "/")
    public void handlerA(HttpExchange exchange, RouteContext context) {
      // ...
    }

    @Route(method = Method.POST, path = "/")
    public void handlerB(HttpExchange exchange, RouteContext context) {
      // ...
    }

    @Route(method = Method.GET, path = "/api")
    public void handlerC(HttpExchange exchange, RouteContext context) {
      // ...
    }

    @Route(method = Method.GET, path = "/dev/api")
    public void handlerD(HttpExchange exchange, RouteContext context) {
      // ...
    }
  }

  // Port number for server instance.
  private static final int PORT = 5000;

  public static void main(String[] args)  {
    // Instantiate a new Router.
    Router router = new Router();

    // Register the route handler methods in RouteHandlers.
    router.use(RouteHandlers.class);

    try {
      // Create a new server that listens on port PORT and uses the Router.
      Server server = new Server(PORT, router);

      // Start the server.
      server.start();

      System.out.printf("Listening on port %d...\n", PORT);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
```

### Default / Fallback Route handlers

The Router instance ships with default route handlers that are invoked when a route match is either not found, or found but invoked with an unregistered method (404 Not found and 405 Method Not Allowed, respectively). To override these, use the Router setters:

```java

  RouteHandler notFoundHandler = /* ... */
  router.handleNotFoundWith(notFoundHandler);

  RouteHandler methodNotAllowedHandler = /* ... */
  router.handleMethodNotAllowedWith(methodNotAllowedHandler);
```

### Middleware

```java
RouteHandler apiHandler = (exchange, context) -> {
  System.out.println("Inside handler");

  exchange.sendResponseHeaders(Status.OK.value, -1);
  output.flush();
};

Middleware mw = new Middleware() {
  @Override
  public RouteHandler handle(RouteHandler handler) {
    return (exchange, context) -> {
      System.out.println("before");
      handler.handle(exchange, context);
      System.out.println("after");
    };
  }
};

Middleware mw2 = new Middleware() {
  @Override
  public RouteHandler handle(RouteHandler handler) {
    return (exchange, context) -> {
      System.out.println("before2");
      handler.handle(exchange, context);
      System.out.println("after2");
    };
  }
};

Router router = new Router();

router.register(
  List.ofMethod.GET),
  "/api",
  apiHandler,
  new ArrayList<Middleware>(Arrays.asList(mw, mw2))
);

// ...Server initialization

// curl -X GET <host>:<port>/api
// stdout:
// before
// before2
// Inside handler
// after2
// after
```

### CORS Middleware

```java
Router router = new Router();

// ...

Cors cors = new Cors.Builder()
  // A list of allowed origins for CORS requests.
  .allowedOrigins("*")
  // A list of allowed HTTP methods for CORS requests.
  .allowedMethods(Method.GET, Method.DELETE, Method.POST)
  // A list of allowed non-simple headers for CORS requests.
  .allowedHeaders("X-Something-From-Client")
  // A flag indicating whether the request may include Cookies.
  .allowCredentials(false)
  // Setting this flag to `true` will allow Preflight requests to
  // propagate to the matched RouteHandler. This is useful in cases where the handler or
  // sequence of middleware needs to inspect the request subsequent to the handling of the
  // Preflight request.
  .useOptionsPassthrough(false)
  // The suggested duration, in seconds, that a response should remain in the browser's
  // cache before another Preflight request is made.
  .maxAge(36000)
  // A list of non-simple headers that may be exposed to clients making CORS requests.
  .exposeHeaders("X-Powered-By");

// Wrap the router in CORS middleware to handle all CORS requests.
Server server = new Server(PORT, cors.use(router));
```
