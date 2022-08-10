package com.github.matthewzito.mug.example;

import com.github.matthewzito.mug.router.annotations.Route;
import com.github.matthewzito.mug.router.constant.Method;
import com.sun.net.httpserver.HttpExchange;

public class TestRoute {
  @Route(method = Method.GET, path = "/")
  @Route(method = Method.POST, path = "/")
  public void doThing(HttpExchange exchange) {
    System.out.println("HERE " + exchange.getRequestURI());
  }
}
