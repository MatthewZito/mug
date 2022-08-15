package com.github.exbotanical.mug.cors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.exbotanical.mug.constant.Method;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


/**
 * Assert public objects stringify in a human-readable manner.
 */
class ToStringTest {

  @DisplayName("Test Cors.toString")
  @Test
  void shouldPrettyStringifyCors() {
    Cors c = new Cors.Builder()
        .allowCredentials(true)
        .allowedHeaders("header-1")
        .allowedMethods(Method.GET, Method.PATCH)
        .allowedOrigins("*")
        .exposeHeaders("X-Powered-By")
        .maxAge(36000)
        .useOptionsPassthrough(true)
        .build();

    String expected =
        "Cors {\n  allowedOrigins: []\n  allowedMethods: [GET, PATCH]\n  allowedHeaders: [header-1]\n  exposedHeaders: [X-Powered-By]\n  allowCredentials: true\n  allowAllOrigins: true\n  allowAllHeaders: false\n  useOptionsPassthrough: true\n  maxAge: 36000\n}";
    String actual = c.toString();

    assertEquals(expected, actual);
  }
}
