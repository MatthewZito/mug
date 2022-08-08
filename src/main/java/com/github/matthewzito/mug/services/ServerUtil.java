package com.github.matthewzito.mug.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.sun.net.httpserver.HttpExchange;

public class ServerUtil {
  protected enum RequestMethod {
    GET,
    HEAD,
    POST,
    PUT,
    PATCH,
    DELETE,
    OPTIONS,
    TRACE
  }

  protected Logger logger;

  public ServerUtil(String system) {
    logger = new Logger(system, System.out);
  }

  public Map<String, String> queryToMap(String query) {
    Map<String, String> result = new HashMap<String, String>();

    for (String param : query.split("&")) {
      String pair[] = param.split("=");

      if (pair.length > 1) {
        result.put(pair[0], pair[1]);
      } else {
        result.put(pair[0], "");
      }
    }

    return result;
  }

  public Optional<String> getRequestBodyString(HttpExchange exchange) {
    InputStream input = exchange.getRequestBody();
    InputStreamReader isr;

    try {
      isr = new InputStreamReader(input, "utf-8");
      BufferedReader br = new BufferedReader(isr);

      int b;
      StringBuilder buf = new StringBuilder(512);
      while ((b = br.read()) != -1) {
        buf.append((char) b);
      }

      br.close();
      isr.close();

      return Optional.ofNullable(buf.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }

    return Optional.empty();
  }
}
