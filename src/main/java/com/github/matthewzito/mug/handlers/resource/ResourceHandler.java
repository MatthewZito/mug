package com.github.matthewzito.mug.handlers.resource;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.github.matthewzito.mug.db.Database;
import com.github.matthewzito.mug.db.InMemoryDatabase;
import com.github.matthewzito.mug.models.format.Response;
import com.github.matthewzito.mug.models.resource.Resource;
import com.github.matthewzito.mug.models.resource.ResourceDeserializer;
import com.github.matthewzito.mug.services.ServerUtil;

public class ResourceHandler extends ServerUtil implements HttpHandler {
  Database database = new InMemoryDatabase();

  public ResourceHandler() {
    super("api/resource");
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    try {
      String requestMethod = exchange.getRequestMethod();

      if (RequestMethod.GET.toString().equals(requestMethod)) {
        logger.log("Received %s request at path /api/resource", requestMethod);

        Headers headers = exchange.getResponseHeaders();
        OutputStream output = exchange.getResponseBody();

        Map<String, String> queries = queryToMap(exchange.getRequestURI().getQuery());
        String id = queries.get("id");

        Gson rsGson = new Gson();

        Response rs;

        if (id == null) {
          rs = new Response(false, null);
        } else {
          Optional<Resource> payload = database.get(id);
          if (payload.isPresent()) {
            rs = new Response(true, payload.get());
          } else {
            rs = new Response(true, null);
          }
        }

        String rsJson = rsGson.toJson(rs);

        headers.add("Content-Type", "application/json");
        byte[] responseBytes = rsJson.getBytes();
        exchange.sendResponseHeaders(200, responseBytes.length);

        output.write(responseBytes);
        output.flush();
      } else if (RequestMethod.POST.toString().equals(requestMethod)) {
        logger.log("Received %s request at path /api/resource", requestMethod);

        Headers headers = exchange.getResponseHeaders();
        headers.add("Content-Type", "application/json");
        OutputStream output = exchange.getResponseBody();

        Response rs;
        byte[] responseBytes;

        Optional<String> body = getRequestBodyString(exchange);

        if (body.isEmpty()) {
          rs = new Response(false, null);

          String rsJson = new Gson().toJson(rs);
          responseBytes = rsJson.getBytes();
          exchange.sendResponseHeaders(400, responseBytes.length);
        } else {

          Gson gson = new GsonBuilder()
              .registerTypeAdapter(Resource.class, new ResourceDeserializer())
              .create();

          Resource r = gson.fromJson(body.get(), Resource.class);

          database.put(r.id(), r);

          rs = new Response(true, null);

          String rsJson = new Gson().toJson(rs);
          responseBytes = rsJson.getBytes();
          exchange.sendResponseHeaders(200, responseBytes.length);
        }

        output.write(responseBytes);
        output.flush();
      }

      else {
        exchange.sendResponseHeaders(405, -1);
      }

    } catch (Exception e) {
      e.printStackTrace();
      logger.log("error: " + e.getMessage());
    } finally {
      exchange.close();
    }
  }
}
