package com.github.exbotanical.mug.router;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Assert public objects stringify in a human-readable manner.
 */
class ToStringTest {

  @DisplayName("Test RouteContext.toString")
  @Test
  void shouldPrettyStringifyRouteContext() {
    RouteContext r = new RouteContext(List.of(new Parameter("k", "v"), new Parameter("k2", "v2")));

    String expected =
        "RouteContext { params: [Parameter[key=k, value=v], Parameter[key=k2, value=v2]] }";
    String actual = r.toString();

    assertEquals(expected, actual);
  }
}
