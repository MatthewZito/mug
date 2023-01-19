package com.github.exbotanical.mug.cors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.exbotanical.mug.constant.Method;
import com.github.exbotanical.mug.constant.Status;
import com.github.exbotanical.mug.router.TestUtils.ExchangeMockFactory;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Unit tests for Cors.
 */
@DisplayName("Test Cors")
class CorsTest {
  record CorsTestCase(
      String name,
      Cors cors,
      Method method,
      Map<String, String> reqHeaders,
      Map<String, String> resHeaders,
      int code) {
  }

  record HeaderTestCase(
      String name,
      Cors cors,
      List<String> testHeaders,
      boolean isAllowed) {
  }

  record MethodTestCase(
      String name,
      Cors cors,
      Method testMethod,
      boolean isAllowed) {
  }

  record OriginTestCase(
      String name,
      Cors cors,
      Map<String, Boolean> tests) {
  }

  HttpHandler testHandler = (HttpExchange exchange) -> {};

  @DisplayName("Test CORS impl")
  @TestFactory
  Stream<DynamicTest> shouldProcessCorsRequest() {
    final List<CorsTestCase> testCases = List.of(
        new CorsTestCase(
            "AllOriginAllowed",
            new Cors.Builder()
                .allowedOrigins("*")
                .build(),
            Method.GET,
            Map.of(CommonHeader.ORIGIN.value, "http://foo.com"),
            Map.of(
                CommonHeader.VARY.value, CommonHeader.ORIGIN.value,
                CommonHeader.ALLOW_ORIGINS.value, "*"),
            0),


        new CorsTestCase("OriginAllowed",
            new Cors.Builder()
                .allowedOrigins("http://foo.com")
                .build(),
            Method.GET,
            Map.of(
                CommonHeader.ORIGIN.value, "http://foo.com"),
            Map.of(
                CommonHeader.VARY.value, CommonHeader.ORIGIN.value,
                CommonHeader.ALLOW_ORIGINS.value, "http://foo.com"),
            0),

        new CorsTestCase("OriginAllowedMultipleProvided",
            new Cors.Builder()
                .allowedOrigins("http://foo.com", "http://bar.com")
                .build(),

            Method.GET,
            Map.of(
                CommonHeader.ORIGIN.value, "http://bar.com"),
            Map.of(
                CommonHeader.VARY.value, CommonHeader.ORIGIN.value,
                CommonHeader.ALLOW_ORIGINS.value, "http://bar.com"),
            0),

        new CorsTestCase("GETMethodAllowedDefault",
            new Cors.Builder()
                .allowedOrigins("http://foo.com")
                .build(),
            Method.GET,
            Map.of(
                CommonHeader.ORIGIN.value, "http://foo.com"),
            Map.of(
                CommonHeader.VARY.value, CommonHeader.ORIGIN.value,
                CommonHeader.ALLOW_ORIGINS.value, "http://foo.com"),
            0),

        new CorsTestCase("POSTMethodAllowedDefault",
            new Cors.Builder()
                .allowedOrigins("http://foo.com")
                .build(),
            Method.POST,
            Map.of(
                CommonHeader.ORIGIN.value, "http://foo.com"),
            Map.of(
                CommonHeader.VARY.value, CommonHeader.ORIGIN.value,
                CommonHeader.ALLOW_ORIGINS.value, "http://foo.com"),
            0),

        new CorsTestCase("HEADMethodAllowedDefault",
            new Cors.Builder()
                .allowedOrigins("http://foo.com")
                .build(),
            Method.HEAD,
            Map.of(
                CommonHeader.ORIGIN.value, "http://foo.com"),
            Map.of(
                CommonHeader.VARY.value, CommonHeader.ORIGIN.value,
                CommonHeader.ALLOW_ORIGINS.value, "http://foo.com"),
            0),

        new CorsTestCase("MethodAllowed",
            new Cors.Builder()
                .allowedOrigins("*")
                .allowedMethods(Method.DELETE)
                .build(),
            Method.OPTIONS,
            Map.of(
                CommonHeader.ORIGIN.value, "http://foo.com",
                CommonHeader.REQUEST_METHOD.value, Method.DELETE.toString()),
            Map.of(
                CommonHeader.VARY.value,
                "Origin, Access-Control-Request-Method, Access-Control-Request-Headers",
                CommonHeader.ALLOW_ORIGINS.value, "*",
                CommonHeader.ALLOW_METHODS.value, Method.DELETE.toString()),
            Status.NO_CONTENT.value),

        new CorsTestCase("HeadersAllowed",
            new Cors.Builder()
                .allowedOrigins("*")
                .allowedHeaders("X-Testing")
                .build(),
            Method.OPTIONS,
            Map.of(
                CommonHeader.ORIGIN.value, "http://foo.com",
                CommonHeader.REQUEST_METHOD.value, Method.GET.toString(),
                CommonHeader.REQUEST_HEADERS.value, "X-Testing"),
            Map.of(
                CommonHeader.VARY.value,
                "Origin, Access-Control-Request-Method, Access-Control-Request-Headers",
                CommonHeader.ALLOW_ORIGINS.value, "*",
                CommonHeader.ALLOW_HEADERS.value, "X-Testing",
                CommonHeader.ALLOW_METHODS.value, Method.GET.toString()),
            Status.NO_CONTENT.value),

        new CorsTestCase("HeadersAllowedMultiple",
            new Cors.Builder()
                .allowedOrigins("*")
                .allowedHeaders("X-Testing", "X-Testing-2", "X-Testing-3")
                .build(),
            Method.OPTIONS,
            Map.of(
                CommonHeader.ORIGIN.value, "http://foo.com",
                CommonHeader.REQUEST_METHOD.value, Method.GET.toString(),
                CommonHeader.REQUEST_HEADERS.value,
                "X-Testing, X-Testing-2, X-Testing-3"),
            Map.of(
                CommonHeader.VARY.value,
                "Origin, Access-Control-Request-Method, Access-Control-Request-Headers",
                CommonHeader.ALLOW_ORIGINS.value, "*",
                CommonHeader.ALLOW_HEADERS.value,
                "X-Testing, X-Testing-2, X-Testing-3",
                CommonHeader.ALLOW_METHODS.value, Method.GET.toString()),
            Status.NO_CONTENT.value),

        new CorsTestCase("CredentialsAllowed",
            new Cors.Builder()
                .allowedOrigins("*")
                .allowCredentials(true)
                .build(),
            Method.OPTIONS,
            Map.of(
                CommonHeader.ORIGIN.value, "http://foo.com",
                CommonHeader.REQUEST_METHOD.value, Method.GET.toString()),
            Map.of(
                CommonHeader.VARY.value,
                "Origin, Access-Control-Request-Method, Access-Control-Request-Headers",
                CommonHeader.ALLOW_CREDENTIALS.value, "true",
                CommonHeader.ALLOW_ORIGINS.value, "*",
                CommonHeader.ALLOW_METHODS.value, Method.GET.toString()),
            Status.NO_CONTENT.value),

        new CorsTestCase("ExposeHeaders",
            new Cors.Builder()
                .allowedOrigins("http://foo.com")
                .exposeHeaders(
                    "x-test")
                .build(),
            Method.POST,
            Map.of(
                CommonHeader.ORIGIN.value, "http://foo.com"),
            Map.of(
                CommonHeader.VARY.value, CommonHeader.ORIGIN.value,
                CommonHeader.ALLOW_ORIGINS.value, "http://foo.com",
                CommonHeader.EXPOSE_HEADERS.value, "x-test"),
            0),

        new CorsTestCase("ExposeHeadersMultiple",
            new Cors.Builder()
                .allowedOrigins("http://foo.com")
                .exposeHeaders("x-test-1", "x-test-2")
                .build(),
            Method.POST,
            Map.of(
                CommonHeader.ORIGIN.value, "http://foo.com"),
            Map.of(
                CommonHeader.VARY.value, CommonHeader.ORIGIN.value,
                CommonHeader.ALLOW_ORIGINS.value, "http://foo.com",
                CommonHeader.EXPOSE_HEADERS.value, "x-test-1, x-test-2"),
            0),

        // CORS Rejections
        new CorsTestCase("OriginNotAllowed",
            new Cors.Builder()
                .allowedOrigins("http://foo.com")
                .build(),
            Method.GET,
            Map.of(
                CommonHeader.ORIGIN.value, "http://bar.com"),
            Map.of(
                CommonHeader.VARY.value, CommonHeader.ORIGIN.value),
            0),

        new CorsTestCase("OriginNotAllowedPortMismatch",
            new Cors.Builder()
                .allowedOrigins("http://foo.com:443")
                .build(),
            Method.GET,
            Map.of(
                CommonHeader.ORIGIN.value, "http://foo.com:444"),
            Map.of(
                CommonHeader.VARY.value, CommonHeader.ORIGIN.value),
            0),

        new CorsTestCase("MethodNotAllowed",
            new Cors.Builder()
                .allowedOrigins("*")
                .build(),
            Method.OPTIONS,
            Map.of(
                CommonHeader.ORIGIN.value, "http://foo.com",
                CommonHeader.REQUEST_METHOD.value, Method.DELETE.toString()),
            Map.of(
                CommonHeader.VARY.value,
                "Origin, Access-Control-Request-Method, Access-Control-Request-Headers"),
            Status.NO_CONTENT.value),

        new CorsTestCase("HeadersNotAllowed",
            new Cors.Builder()
                .allowedOrigins("*")
                .build(),
            Method.OPTIONS,
            Map.of(
                CommonHeader.ORIGIN.value, "http://foo.com",
                CommonHeader.REQUEST_METHOD.value, Method.GET.toString(),
                CommonHeader.REQUEST_HEADERS.value, "X-Testing"),
            Map.of(
                CommonHeader.VARY.value,
                "Origin, Access-Control-Request-Method, Access-Control-Request-Headers"),
            Status.NO_CONTENT.value)

    );

    return testCases.stream()
        .map(testCase -> DynamicTest.dynamicTest(
            testCase.name,
            () -> {
              // Initialize a mock HttpExchange.
              HttpExchange exchangeMock =
                  ExchangeMockFactory.build("http://test.com/", testCase.method);

              // Initialize headers to add to the exchangeMock.
              Headers reqHeaders = new Headers();

              // Add test headers
              for (Entry<String, String> entry : testCase.reqHeaders.entrySet()) {
                reqHeaders.set(entry.getKey(), entry.getValue());
              }

              Headers resHeaders = new Headers();
              when(exchangeMock.getRequestHeaders()).thenReturn(reqHeaders);
              when(exchangeMock.getResponseHeaders()).thenReturn(resHeaders);

              Cors cors = testCase.cors;

              cors.use(testHandler).handle(exchangeMock);

              Headers resolvedResHeaders = exchangeMock.getResponseHeaders();

              // Assert each expected header was set
              for (Entry<String, String> entry : testCase.resHeaders.entrySet()) {
                String headerValue = resolvedResHeaders.get(entry.getKey()).stream()
                    .collect(Collectors.joining(", "));

                assertTrue(entry.getValue().toLowerCase()
                    .equals(headerValue.toLowerCase()));
              }

              // Check the status code if we expect the CORS middleware to have
              // explicitly set one.
              if (testCase.code != 0) {
                verify(exchangeMock, atLeastOnce())
                    .sendResponseHeaders(testCase.code, -1);
              }
            }));
  }

  @DisplayName("Test validate allowed headers")
  @TestFactory
  Stream<DynamicTest> shouldValidateAllowedHeaders() {
    final List<HeaderTestCase> testCases = List.of(
        new HeaderTestCase(
            "ExplicitHeaders",
            new Cors.Builder()
                .allowedOrigins("*")
                .allowedHeaders("x-test-1",
                    "x-test-2")
                .build(),
            List.of("x-test-1", "x-test-2"),
            true),

        new HeaderTestCase(
            "ExtraneousHeaderNotAllowed",
            new Cors.Builder()
                .allowedOrigins("*")
                .allowedHeaders("x-test-1", "x-test-2")
                .build(),
            List.of("x-test",
                "x-test-1",
                "x-test-2"),
            false),

        new HeaderTestCase(
            "EmptyHeaderNotAllowed",
            new Cors.Builder()
                .allowedOrigins("*")
                .allowedHeaders("x-test-1", "x-test-2")
                .build(),
            List.of(""),
            false),

        new HeaderTestCase(
            "WildcardHeaders",
            new Cors.Builder()
                .allowedOrigins("*")
                .allowedHeaders("*")
                .build(),
            List.of("x-test-1", "x-test-2"),
            true),

        new HeaderTestCase(
            "WildcardHeadersAlt",
            new Cors.Builder()
                .allowedOrigins("*")
                .allowedHeaders("*")
                .build(),
            List.of("x-test", "x-test-1", "x-test-2"),
            true),

        new HeaderTestCase(
            "WildcardHeadersAltEmpty",
            new Cors.Builder()
                .allowedOrigins("*")
                .allowedHeaders("*")
                .build(),
            List.of(""),
            true)

    );

    return testCases.stream()
        .map(testCase -> DynamicTest.dynamicTest(
            testCase.name,
            () -> {
              boolean actual = testCase.cors.areHeadersAllowed(testCase.testHeaders);
              assertEquals(testCase.isAllowed, actual);
            }));
  }

  @DisplayName("Test validate allowed methods")
  @TestFactory
  Stream<DynamicTest> shouldValidateAllowedMethods() {
    final List<MethodTestCase> testCases = List.of(
        new MethodTestCase(
            "ExplicitMethodOk",
            new Cors.Builder()
                .allowedOrigins("*")
                .allowedMethods(Method.DELETE, Method.PUT)
                .build(),
            Method.DELETE,
            true),

        new MethodTestCase(
            "ExplicitMethodOkAlt",
            new Cors.Builder()
                .allowedOrigins("*")
                .allowedMethods(Method.DELETE, Method.PUT)
                .build(),
            Method.PUT,
            true),

        new MethodTestCase(
            "ExplicitMethodNotAllowed",
            new Cors.Builder()
                .allowedOrigins("*")
                .allowedMethods(Method.DELETE, Method.PUT)
                .build(),
            Method.PATCH,
            false),

        new MethodTestCase(
            "DefaultMethodsOkGET",
            new Cors.Builder()
                .allowedOrigins("*")
                .build(),
            Method.GET,
            true),

        new MethodTestCase(
            "DefaultMethodsOkPOST",
            new Cors.Builder()
                .allowedOrigins("*")
                .build(),
            Method.POST,
            true),

        new MethodTestCase(
            "DefaultMethodsOkHEAD",
            new Cors.Builder()
                .allowedOrigins("*")
                .build(),
            Method.HEAD,
            true),

        new MethodTestCase(
            "DefaultMethodsNotAllowedDELETE",
            new Cors.Builder()
                .allowedOrigins("*")
                .build(),
            Method.DELETE,
            false),

        new MethodTestCase(
            "DefaultMethodsNotAllowedPATCH",
            new Cors.Builder()
                .allowedOrigins("*")
                .build(),
            Method.PATCH,
            false),

        new MethodTestCase(
            "DefaultMethodsNotAllowedPUT",
            new Cors.Builder()
                .allowedOrigins("*")
                .build(),
            Method.PUT,
            false));

    return testCases.stream()
        .map(testCase -> DynamicTest.dynamicTest(
            testCase.name,
            () -> {
              boolean actual =
                  testCase.cors.isMethodAllowed(testCase.testMethod.toString());
              assertEquals(testCase.isAllowed, actual);
            }));
  }

  @DisplayName("Test validate allowed origins")
  @TestFactory
  Stream<DynamicTest> shouldValidateAllowedOrigins() {
    final List<OriginTestCase> testCases = List.of(
        new OriginTestCase(
            "ExplicitOrigin",
            new Cors.Builder()
                .allowedOrigins("http://foo.com", "http://bar.com", "baz.com")
                .build(),
            Map.of(
                "http://foo.com", true,
                "http://bar.com", true,
                "baz.com", true,
                "http://foo.com/", false,
                "https://bar.com", false,
                "http://baz.com", false)),

        new OriginTestCase(
            "WildcardOrigin",
            new Cors.Builder()
                .allowedOrigins("*")
                .build(),
            Map.of(
                "http://foo.com", true,
                "http://bar.com", true,
                "baz.com", true,
                "http://foo.com/", true,
                "https://bar.com", true,
                "http://baz.com", true))

    );

    return testCases.stream()
        .map(testCase -> DynamicTest.dynamicTest(
            testCase.name,
            () -> {
              // Test each origin and eval against the expected result.
              for (final Entry<String, Boolean> entry : testCase.tests.entrySet()) {
                final boolean actual = testCase.cors.isOriginAllowed(entry.getKey());
                assertEquals(entry.getValue(), actual);
              }
            }));
  }
}
