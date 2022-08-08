package com.github.matthewzito.mug.handlers;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.Date;

import com.google.gson.Gson;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.github.matthewzito.mug.models.status.LivenessReport;
import com.github.matthewzito.mug.models.status.SystemStatus;
import com.github.matthewzito.mug.services.ServerUtil;

public class LivenessHandler extends ServerUtil implements HttpHandler {

  public LivenessHandler() {
    super("api");
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    if (RequestMethod.GET.toString().equals(exchange.getRequestMethod())) {
      logger.log("Received %s request at path /api", RequestMethod.GET);

      var livenessReportGson = new Gson();
      var livenessReport = new LivenessReport("api", SystemStatus.OK, new Timestamp(new Date().getTime()));
      var livenessReportJson = livenessReportGson.toJson(livenessReport);

      byte[] responseBytes = livenessReportJson.getBytes();

      Headers headers = exchange.getResponseHeaders();

      headers.add("Content-Type", "application/json");

      exchange.sendResponseHeaders(200, responseBytes.length);
      OutputStream output = exchange.getResponseBody();
      output.write(responseBytes);
      output.flush();
    } else {
      exchange.sendResponseHeaders(405, -1);
    }

    exchange.close();

  }
}
