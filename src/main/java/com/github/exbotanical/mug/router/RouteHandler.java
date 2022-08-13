package com.github.exbotanical.mug.router;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

public interface RouteHandler {
  public void handle(HttpExchange exchange, RouteContext context) throws IOException;
}
