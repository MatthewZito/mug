package com.github.matthewzito.mug;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.github.matthewzito.mug.router.Router;
import com.github.matthewzito.mug.router.constant.Method;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;

public class App {
  private static final int PORT = 5000;

  public static void main(String[] args) throws IOException {

    HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
    HttpHandler apiHandler = (exchange) -> {
      String rs = "HELLO";
      byte[] responseBytes = rs.getBytes();
      exchange.sendResponseHeaders(200, responseBytes.length);
      OutputStream output = exchange.getResponseBody();
      output.write(responseBytes);
      output.flush();
    };

    Router router = new Router();
    router.register(new ArrayList<>(Arrays.asList(Method.GET)), "/api", apiHandler);

    server.createContext("/", router);

    server.setExecutor(null);

    server.start();
    System.out.printf("Listening on port %d...\n", PORT);
  }
}
