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
      public HttpHandler handle(HttpHandler handler) {
        return (exchange) -> {
          System.out.println("before");
          // Invoke the handler.
          handler.handle(exchange);
          System.out.println("after");
        };
      }
    };

    // Create a route handler.
    HttpHandler routeHandler = (HttpExchange exchange) -> {
      // ...
    };

    // Instantiate a new Router.
    Router router = new Router();

    // Register the route handler + middleware at GET "/api".
    router.register(
      // A list of HTTP methods at which to register this handler.
      new ArrayList<>(Arrays.asList(Method.GET)),
      // The path at which this handler should be executed.
      "/api",
      // The route handler.
      routeHandler,
      // A list of Middleware to be invoked before and/or after the handler.
      new ArrayList<Middleware>(Arrays.asList(mw))
    );

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

### Automatic route registration

```java
public class Demo {
  // Each method annotated with @Route will get registered when invoking `Router.use`,
  // provided the method implements the HttpHandler interface.
  public static class RouteHandlers {
    @Route(method = Method.GET, path = "/")
    public void handlerA(HttpExchange exchange) {
      // ...
    }

    @Route(method = Method.POST, path = "/")
    public void handlerB(HttpExchange exchange) {
      // ...
    }

    @Route(method = Method.GET, path = "/api")
    public void handlerC(HttpExchange exchange) {
      // ...
    }

    @Route(method = Method.GET, path = "/dev/api")
    public void handlerD(HttpExchange exchange) {
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

### Middleware

```java
HttpHandler apiHandler = exchange -> {
  System.out.println("Inside handler");

  exchange.sendResponseHeaders(Status.OK.value, -1);
  output.flush();
};

Middleware mw = new Middleware() {
  @Override
  public HttpHandler handle(HttpHandler handler) {
    return (exchange) -> {
      System.out.println("before");
      handler.handle(exchange);
      System.out.println("after");
    };
  }
};

Middleware mw2 = new Middleware() {
  @Override
  public HttpHandler handle(HttpHandler handler) {
    return (exchange) -> {
      System.out.println("before2");
      handler.handle(exchange);
      System.out.println("after2");
    };
  }
};

Router router = new Router();

router.register(
  new ArrayList<>(Arrays.asList(Method.GET)),
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

Cors cors = new Cors(new CorsOptions(

  // A list of allowed origins for CORS requests.
  new ArrayList<>(Arrays.asList("*")),

  // A list of allowed HTTP methods for CORS requests.
  new ArrayList<>(Arrays.asList(Method.GET, Method.DELETE, Method.POST)),

  // A list of allowed non-simple headers for CORS requests.
  new ArrayList<>(Arrays.asList("X-Something-From-Client")),

  // A flag indicating whether the request may include Cookies.
  false,

  // Setting this flag to `true` will allow Preflight requests to
  // propagate to the matched HttpHandler. This is useful in cases where the handler or
  // sequence of middleware needs to inspect the request subsequent to the handling of the
  // Preflight request.
  false,

  // The suggested duration, in seconds, that a response should remain in the browser's
  // cache before another Preflight request is made.
  36000,

  // A list of non-simple headers that may be exposed to clients making CORS requests.
  new ArrayList<>(Arrays.asList("X-Powered-By"))
));

// Wrap the router in CORS middleware to handle all CORS requests.
Server server = new Server(PORT, cors.use(router));
```
