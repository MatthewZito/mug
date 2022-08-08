package com.github.matthewzito.mug;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.github.matthewzito.mug.handlers.LivenessHandler;
import com.github.matthewzito.mug.handlers.resource.ResourceHandler;
import com.github.matthewzito.mug.services.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;

public class App {
  private static final int PORT = 5000;

  public static void main(String[] args) throws IOException {
    Logger logger = new Logger("server", System.out);

    HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
    HttpHandler livenessHandler = new LivenessHandler();
    HttpHandler resourceHandler = new ResourceHandler();

    server.createContext("/api", livenessHandler);
    server.createContext("/api/resource", resourceHandler);

    server.setExecutor(null);

    server.start();
    logger.log("Listening on port %d...\n", PORT);
  }
}
