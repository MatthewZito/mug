package com.github.matthewzito.mug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.matthewzito.mug.constant.Method;
import com.github.matthewzito.mug.constant.Status;
import com.github.matthewzito.mug.cors.CommonHeader;
import com.github.matthewzito.mug.cors.Cors;
import com.github.matthewzito.mug.cors.CorsOptions;
import com.github.matthewzito.mug.utils.TestUtils;
import com.github.matthewzito.mug.utils.TestUtils.ExchangeMockFactory;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.util.ArrayList;
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
  static class TestCors extends Cors {
    TestCors(CorsOptions options) {
      super(options);

    }

    boolean testAreHeadersAllowed(ArrayList<String> headers) {
      return this.areHeadersAllowed(headers);
    }

    boolean testIsMethodAllowed(String method) {
      return this.isMethodAllowed(method);
    }

    boolean testIsOriginAllowed(String origin) {
      return this.isOriginAllowed(origin);
    }
  }

  static record CorsTestCase(
      String name,
      CorsOptions options,
      Method method,
      Map<String, String> reqHeaders,
      Map<String, String> resHeaders,
      int code) {
  }

  static record HeaderTestCase(
      String name,
      CorsOptions options,
      ArrayList<String> testHeaders,
      boolean isAllowed) {
  }

  static record MethodTestCase(
      String name,
      CorsOptions options,
      Method testMethod,
      boolean isAllowed) {
  }

  static record OriginTestCase(
      String name,
      CorsOptions options,
      Map<String, Boolean> tests) {
  }

  HttpHandler testHandler = (HttpExchange exchange) -> {
  };

  @DisplayName("Test CORS impl")
  @TestFactory
  Stream<DynamicTest> shouldProcessCorsRequest() {
    ArrayList<CorsTestCase> testCases = TestUtils.toList(
        new CorsTestCase(
            "AllOriginAllowed",
            new CorsOptions(
                TestUtils.toList("*"),
                TestUtils.toList(),
                TestUtils.toList(),
                false,
                false,
                0,
                TestUtils.toList()),
            Method.GET,
            Map.of(CommonHeader.ORIGIN.value, "http://foo.com"),
            Map.of(
                CommonHeader.VARY.value, CommonHeader.ORIGIN.value,
                CommonHeader.ALLOW_ORIGINS.value, "*"),
            0),


        new CorsTestCase("OriginAllowed",
            new CorsOptions(
                TestUtils.toList("http://foo.com"),
                TestUtils.toList(),
                TestUtils.toList(),
                false,
                false,
                0,
                TestUtils.toList()),
            Method.GET,
            Map.of(
                CommonHeader.ORIGIN.value, "http://foo.com"),
            Map.of(
                CommonHeader.VARY.value, CommonHeader.ORIGIN.value,
                CommonHeader.ALLOW_ORIGINS.value, "http://foo.com"),
            0),

        new CorsTestCase("OriginAllowedMultipleProvided",
            new CorsOptions(
                TestUtils.toList("http://foo.com", "http://bar.com"), TestUtils.toList(),
                TestUtils.toList(), false, false, 0, TestUtils.toList()),
            Method.GET,
            Map.of(
                CommonHeader.ORIGIN.value, "http://bar.com"),
            Map.of(
                CommonHeader.VARY.value, CommonHeader.ORIGIN.value,
                CommonHeader.ALLOW_ORIGINS.value, "http://bar.com"),
            0),

        new CorsTestCase("GETMethodAllowedDefault",
            new CorsOptions(TestUtils.toList("http://foo.com"), TestUtils.toList(),
                TestUtils.toList(), false, false, 0, TestUtils.toList()),
            Method.GET,
            Map.of(
                CommonHeader.ORIGIN.value, "http://foo.com"),
            Map.of(
                CommonHeader.VARY.value, CommonHeader.ORIGIN.value,
                CommonHeader.ALLOW_ORIGINS.value, "http://foo.com"),
            0),

        new CorsTestCase("POSTMethodAllowedDefault",
            new CorsOptions(TestUtils.toList("http://foo.com"), TestUtils.toList(),
                TestUtils.toList(), false, false, 0, TestUtils.toList()),
            Method.POST,
            Map.of(
                CommonHeader.ORIGIN.value, "http://foo.com"),
            Map.of(
                CommonHeader.VARY.value, CommonHeader.ORIGIN.value,
                CommonHeader.ALLOW_ORIGINS.value, "http://foo.com"),
            0),

        new CorsTestCase("HEADMethodAllowedDefault",
            new CorsOptions(TestUtils.toList("http://foo.com"), TestUtils.toList(),
                TestUtils.toList(), false, false, 0, TestUtils.toList()),
            Method.HEAD,
            Map.of(
                CommonHeader.ORIGIN.value, "http://foo.com"),
            Map.of(
                CommonHeader.VARY.value, CommonHeader.ORIGIN.value,
                CommonHeader.ALLOW_ORIGINS.value, "http://foo.com"),
            0),

        new CorsTestCase("MethodAllowed",
            new CorsOptions(
                TestUtils.toList("*"),
                TestUtils.toList(Method.DELETE),
                TestUtils.toList(),
                false,
                false,
                0,
                TestUtils.toList()),
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
            new CorsOptions(TestUtils.toList("*"), TestUtils.toList(),
                TestUtils.toList("X-Testing"), false, false, 0, TestUtils.toList()),
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
            new CorsOptions(TestUtils.toList("*"), TestUtils.toList(),
                TestUtils.toList("X-Testing", "X-Testing-2", "X-Testing-3"), false, false, 0,
                TestUtils.toList()),
            Method.OPTIONS,
            Map.of(
                CommonHeader.ORIGIN.value, "http://foo.com",
                CommonHeader.REQUEST_METHOD.value, Method.GET.toString(),
                CommonHeader.REQUEST_HEADERS.value, "X-Testing, X-Testing-2, X-Testing-3"),
            Map.of(
                CommonHeader.VARY.value,
                "Origin, Access-Control-Request-Method, Access-Control-Request-Headers",
                CommonHeader.ALLOW_ORIGINS.value, "*",
                CommonHeader.ALLOW_HEADERS.value, "X-Testing, X-Testing-2, X-Testing-3",
                CommonHeader.ALLOW_METHODS.value, Method.GET.toString()),
            Status.NO_CONTENT.value),

        new CorsTestCase("CredentialsAllowed",
            new CorsOptions(TestUtils.toList("*"), TestUtils.toList(), TestUtils.toList(), true,
                false, 0, TestUtils.toList()),
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
            new CorsOptions(TestUtils.toList("http://foo.com"), TestUtils.toList(),
                TestUtils.toList(), false, false, 0, TestUtils.toList("x-test")),
            Method.POST,
            Map.of(
                CommonHeader.ORIGIN.value, "http://foo.com"),
            Map.of(
                CommonHeader.VARY.value, CommonHeader.ORIGIN.value,
                CommonHeader.ALLOW_ORIGINS.value, "http://foo.com",
                CommonHeader.EXPOSE_HEADERS.value, "x-test"),
            0),

        new CorsTestCase("ExposeHeadersMultiple",
            new CorsOptions(TestUtils.toList("http://foo.com"), TestUtils.toList(),
                TestUtils.toList(), false, false, 0, TestUtils.toList("x-test-1", "x-test-2")),
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
            new CorsOptions(TestUtils.toList("http://foo.com"), TestUtils.toList(),
                TestUtils.toList(), false, false, 0, TestUtils.toList()),
            Method.GET,
            Map.of(
                CommonHeader.ORIGIN.value, "http://bar.com"),
            Map.of(
                CommonHeader.VARY.value, CommonHeader.ORIGIN.value),
            0),

        new CorsTestCase("OriginNotAllowedPortMismatch",
            new CorsOptions(TestUtils.toList("http://foo.com:443"), TestUtils.toList(),
                TestUtils.toList(), false, false, 0, TestUtils.toList()),
            Method.GET,
            Map.of(
                CommonHeader.ORIGIN.value, "http://foo.com:444"),
            Map.of(
                CommonHeader.VARY.value, CommonHeader.ORIGIN.value),
            0),

        new CorsTestCase("MethodNotAllowed",
            new CorsOptions(TestUtils.toList("*"), TestUtils.toList(), TestUtils.toList(), false,
                false, 0, TestUtils.toList()),
            Method.OPTIONS,
            Map.of(
                CommonHeader.ORIGIN.value, "http://foo.com",
                CommonHeader.REQUEST_METHOD.value, Method.DELETE.toString()),
            Map.of(
                CommonHeader.VARY.value,
                "Origin, Access-Control-Request-Method, Access-Control-Request-Headers"),
            Status.NO_CONTENT.value),

        new CorsTestCase("HeadersNotAllowed",
            new CorsOptions(TestUtils.toList("*"), TestUtils.toList(), TestUtils.toList(), false,
                false, 0, TestUtils.toList()),
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

              Cors cors = new Cors(testCase.options);

              cors.use(testHandler).handle(exchangeMock);

              Headers resolvedResHeaders = exchangeMock.getResponseHeaders();

              // Assert each expected header was set
              for (Entry<String, String> entry : testCase.resHeaders.entrySet()) {
                String headerValue = resolvedResHeaders.get(entry.getKey()).stream()
                    .collect(Collectors.joining(", "));

                assertTrue(entry.getValue().toLowerCase().equals(headerValue.toLowerCase()));
              }

              // Check the status code if we expect the CORS middleware to have explicitly set one.
              if (testCase.code != 0) {
                verify(exchangeMock, atLeastOnce()).sendResponseHeaders(testCase.code, -1);
              }
            }));
  }

  @DisplayName("Test validate allowed headers")
  @TestFactory
  Stream<DynamicTest> shouldValidateAllowedHeaders() {
    ArrayList<HeaderTestCase> testCases = TestUtils.toList(
        new HeaderTestCase(
            "ExplicitHeaders",
            new CorsOptions(
                TestUtils.toList("*"),
                TestUtils.toList(),
                TestUtils.toList("x-test-1", "x-test-2"),
                false,
                false,
                0,
                TestUtils.toList()),
            TestUtils.toList("x-test-1", "x-test-2"),
            true),

        new HeaderTestCase(
            "ExtraneousHeaderNotAllowed",
            new CorsOptions(
                TestUtils.toList("*"),
                TestUtils.toList(),
                TestUtils.toList("x-test-1", "x-test-2"),
                false,
                false,
                0,
                TestUtils.toList()),
            TestUtils.toList("x-test",
                "x-test-1",
                "x-test-2"),
            false),

        new HeaderTestCase(
            "EmptyHeaderNotAllowed",
            new CorsOptions(
                TestUtils.toList("*"),
                TestUtils.toList(),
                TestUtils.toList("x-test-1", "x-test-2"),
                false,
                false,
                0,
                TestUtils.toList()),
            TestUtils.toList(""),
            false),

        new HeaderTestCase(
            "WildcardHeaders",
            new CorsOptions(
                TestUtils.toList("*"),
                TestUtils.toList(),
                TestUtils.toList("*"),
                false,
                false,
                0,
                TestUtils.toList()),
            TestUtils.toList("x-test-1", "x-test-2"),
            true),

        new HeaderTestCase(
            "WildcardHeadersAlt",
            new CorsOptions(
                TestUtils.toList("*"),
                TestUtils.toList(),
                TestUtils.toList("*"),
                false,
                false,
                0,
                TestUtils.toList()),
            TestUtils.toList("x-test", "x-test-1", "x-test-2"),
            true),

        new HeaderTestCase(
            "WildcardHeadersAltEmpty",
            new CorsOptions(
                TestUtils.toList("*"),
                TestUtils.toList(),
                TestUtils.toList("*"),
                false,
                false,
                0,
                TestUtils.toList()),
            TestUtils.toList(""),
            true)

    );

    return testCases.stream()
        .map(testCase -> DynamicTest.dynamicTest(
            testCase.name,
            () -> {

              TestCors cors = new TestCors(testCase.options);

              boolean actual = cors.testAreHeadersAllowed(testCase.testHeaders);
              assertEquals(testCase.isAllowed, actual);
            }));
  }

  @DisplayName("Test validate allowed methods")
  @TestFactory
  Stream<DynamicTest> shouldValidateAllowedMethods() {
    ArrayList<MethodTestCase> testCases = TestUtils.toList(
        new MethodTestCase(
            "ExplicitMethodOk",
            new CorsOptions(
                TestUtils.toList("*"),
                TestUtils.toList(Method.DELETE, Method.PUT),
                TestUtils.toList(),
                false,
                false,
                0,
                TestUtils.toList()),
            Method.DELETE,
            true),

        new MethodTestCase(
            "ExplicitMethodOkAlt",
            new CorsOptions(
                TestUtils.toList("*"),
                TestUtils.toList(Method.DELETE, Method.PUT),
                TestUtils.toList(),
                false,
                false,
                0,
                TestUtils.toList()),
            Method.PUT,
            true),

        new MethodTestCase(
            "ExplicitMethodNotAllowed",
            new CorsOptions(
                TestUtils.toList("*"),
                TestUtils.toList(Method.DELETE, Method.PUT),
                TestUtils.toList(),
                false,
                false,
                0,
                TestUtils.toList()),
            Method.PATCH,
            false),

        new MethodTestCase(
            "DefaultMethodsOkGET",
            new CorsOptions(
                TestUtils.toList("*"),
                TestUtils.toList(),
                TestUtils.toList(),
                false,
                false,
                0,
                TestUtils.toList()),
            Method.GET,
            true),

        new MethodTestCase(
            "DefaultMethodsOkPOST",
            new CorsOptions(
                TestUtils.toList("*"),
                TestUtils.toList(),
                TestUtils.toList(),
                false,
                false,
                0,
                TestUtils.toList()),
            Method.POST,
            true),

        new MethodTestCase(
            "DefaultMethodsOkHEAD",
            new CorsOptions(
                TestUtils.toList("*"),
                TestUtils.toList(),
                TestUtils.toList(),
                false,
                false,
                0,
                TestUtils.toList()),
            Method.HEAD,
            true),

        new MethodTestCase(
            "DefaultMethodsNotAllowedDELETE",
            new CorsOptions(
                TestUtils.toList("*"),
                TestUtils.toList(),
                TestUtils.toList(),
                false,
                false,
                0,
                TestUtils.toList()),
            Method.DELETE,
            false),

        new MethodTestCase(
            "DefaultMethodsNotAllowedPATCH",
            new CorsOptions(
                TestUtils.toList("*"),
                TestUtils.toList(),
                TestUtils.toList(),
                false,
                false,
                0,
                TestUtils.toList()),
            Method.PATCH,
            false),

        new MethodTestCase(
            "DefaultMethodsNotAllowedPUT",
            new CorsOptions(
                TestUtils.toList("*"),
                TestUtils.toList(),
                TestUtils.toList(),
                false,
                false,
                0,
                TestUtils.toList()),
            Method.PUT,
            false));

    return testCases.stream()
        .map(testCase -> DynamicTest.dynamicTest(
            testCase.name,
            () -> {

              TestCors cors = new TestCors(testCase.options);

              boolean actual = cors.testIsMethodAllowed(testCase.testMethod.toString());
              assertEquals(testCase.isAllowed, actual);
            }));
  }

  @DisplayName("Test validate allowed origins")
  @TestFactory
  Stream<DynamicTest> shouldValidateAllowedOrigins() {
    ArrayList<OriginTestCase> testCases = TestUtils.toList(
        new OriginTestCase(
            "ExplicitOrigin",
            new CorsOptions(
                TestUtils.toList("http://foo.com", "http://bar.com", "baz.com"),
                TestUtils.toList(),
                TestUtils.toList(),
                false,
                false,
                0,
                TestUtils.toList()),
            Map.of(
                "http://foo.com", true,
                "http://bar.com", true,
                "baz.com", true,
                "http://foo.com/", false,
                "https://bar.com", false,
                "http://baz.com", false)),

        new OriginTestCase(
            "WildcardOrigin",
            new CorsOptions(
                TestUtils.toList("*"),
                TestUtils.toList(),
                TestUtils.toList(),
                false,
                false,
                0,
                TestUtils.toList()),
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

              TestCors cors = new TestCors(testCase.options);

              // Test each origin and eval against the expected result.
              for (Entry<String, Boolean> entry : testCase.tests.entrySet()) {
                boolean actual = cors.testIsOriginAllowed(entry.getKey());
                assertEquals(entry.getValue(), actual);
              }
            }));
  }
}
