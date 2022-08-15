package com.github.exbotanical.mug.router;

import java.util.ArrayList;

/**
 * A context object containing any matched parameters for the route.
 */
public record RouteContext(ArrayList<Parameter> params) {

  @Override
  public String toString() {
    String out = String.format("RouteContext { params: %s }", params);
    return out;
  }
}
