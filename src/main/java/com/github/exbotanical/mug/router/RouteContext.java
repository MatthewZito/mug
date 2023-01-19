package com.github.exbotanical.mug.router;

import java.util.List;

/**
 * A context object containing any matched parameters for the route.
 */
public record RouteContext(List<Parameter> params) {

  @Override
  public String toString() {
    return String.format("RouteContext { params: %s }", params);
  }
}
