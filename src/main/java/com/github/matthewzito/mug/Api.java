package com.github.matthewzito.mug;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Api {
  private final int PORT;
  private final HttpServer instance;

  public Api(int port) throws IOException {
    this.PORT = port;
    this.instance = HttpServer.create(new InetSocketAddress(PORT), 0);
  }

  public void createRoute(String path, HttpHandler handler) {
    this.instance.createContext(path, handler);
  }
}
