package com.github.matthewzito.mug.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import com.github.matthewzito.mug.constant.Method;
import com.github.matthewzito.mug.constant.Status;
import com.github.matthewzito.mug.cors.Cors;
import com.github.matthewzito.mug.router.annotations.Route;
import com.sun.net.httpserver.HttpExchange;

public class Routes {
  @Route(method = Method.GET, path = "/api")
  public void getHandler(HttpExchange exchange) throws IOException {
    byte[] response = "{ data: { state: 1 }, ok: true }".getBytes();
    OutputStream output = exchange.getResponseBody();

    exchange.sendResponseHeaders(Status.OK.value, response.length);
    output.write(response);
    output.flush();
  }

  @Route(method = Method.POST, path = "/api")
  public void postHandler(HttpExchange exchange) throws IOException {
    InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
    BufferedReader br = new BufferedReader(isr);

    int b;
    StringBuilder buf = new StringBuilder(512);
    while ((b = br.read()) != -1) {
      buf.append((char) b);
    }


    String req = buf.toString();
    byte[] response = String.format("{ data: %s, ok: true }", req).getBytes();

    OutputStream output = exchange.getResponseBody();

    exchange.sendResponseHeaders(Status.OK.value, response.length);
    output.write(response);
    output.flush();
  }
}
