package com.github.exbotanical.mug.server;

import com.github.exbotanical.mug.constant.Path;
import com.github.exbotanical.mug.router.middleware.AuthenticationMiddleware;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * An HTTP server.
 */
public class Server {
  /**
   * The HttpServer instance.
   */
  private final HttpServer instance;

  /**
   * The HttpContext object for the registered router.
   */
  private final HttpContext ctx;

  /**
   * Constructor. Initialize a new server.
   *
   * @param port The port number at which the server will listen.
   * @param rootHandler The root HttpHandler to register.
   * @throws IOException Server initialization failed.
   */
  public Server(int port, HttpHandler rootHandler) throws IOException {
    this.instance = HttpServer.create(new InetSocketAddress(port), 0);

    this.ctx = this.instance.createContext(Path.ROOT.value, rootHandler);
  }

  /**
   * Register global authentication middleware to be applied to all registered routes.
   *
   * @param authMiddleware The authentication middleware. Must implement the
   *        AuthenticationMiddleware interface.
   *
   * @todo Implement, test, document.
   */
  public void withAuthentication(AuthenticationMiddleware authMiddleware) {
    List<Filter> filters = this.ctx.getFilters();

    Filter f = new Filter() {
      public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        if (authMiddleware.handle(exchange)) {
          chain.doFilter(exchange);
        }
      }

      public String description() {
        return "Authentication";
      }
    };

    filters.add(f);
  }

  public void setExecutor(Executor executor) {
    this.instance.setExecutor(executor);
  }

  public void start() {
    this.instance.start();
  }

  public void stop(int delay) {
    this.instance.stop(delay);
  }
}
