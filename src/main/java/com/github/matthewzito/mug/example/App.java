package com.github.matthewzito.mug.example;

import com.github.matthewzito.mug.router.Router;
import com.github.matthewzito.mug.router.constant.Method;
import com.github.matthewzito.mug.router.constant.Status;
import com.github.matthewzito.mug.router.middleware.Middleware;
import com.github.matthewzito.mug.server.Server;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class App {
  private static final int PORT = 5000;

  public static void main(String[] args) {
    HttpHandler apiHandler = exchange -> {

      System.out.println(exchange.getRequestMethod());

      String rs = "HELLO";
      byte[] responseBytes = rs.getBytes();
      exchange.sendResponseHeaders(Status.OK.value, responseBytes.length);
      OutputStream output = exchange.getResponseBody();
      output.write(responseBytes);
      output.flush();
    };

    Middleware mw = new Middleware() {
      @Override
      public HttpHandler handle(HttpHandler handler) {
        return exchange -> {
          System.out.println("before");
          handler.handle(exchange);
          System.out.println("after");
        };
      }
    };

    Middleware mw2 = new Middleware() {
      @Override
      public HttpHandler handle(HttpHandler handler) {
        return exchange -> {
          System.out.println("before2");
          handler.handle(exchange);
          System.out.println("after2");
        };
      }
    };

    Router router = new Router();

    router.register(new ArrayList<>(
        Arrays.asList(Method.GET)),
        "/api",
        apiHandler,
        new ArrayList<Middleware>(Arrays.asList(mw, mw2)));

    // router.use(TestRoute.class);

    try {
      Server server = new Server(PORT, router);

      server.withAuthentication(
          exchange -> {
            String method = exchange.getRequestMethod();
            System.out.printf("received a %s request\n", method);
            return true;
          });

      server.start();

      System.out.printf("Listening on port %d...\n", PORT);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
