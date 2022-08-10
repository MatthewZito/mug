package com.github.matthewzito.mug.example;

import com.github.matthewzito.mug.constant.Method;
import com.github.matthewzito.mug.constant.Status;
import com.github.matthewzito.mug.cors.Cors;
import com.github.matthewzito.mug.router.Router;
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
    Router router = new Router();

    router.use(Routes.class);

    try {
      // Cors cors = new Cors(true);

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
