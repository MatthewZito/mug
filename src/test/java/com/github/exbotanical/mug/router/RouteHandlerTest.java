package com.github.exbotanical.mug.router;

import static com.github.exbotanical.mug.router.TestUtils.ExchangeMockFactory;
import static com.github.exbotanical.mug.router.TestUtils.RouteRecord;
import static com.github.exbotanical.mug.router.TestUtils.SearchQuery;
import static com.github.exbotanical.mug.router.TestUtils.TestCase;
import static com.github.exbotanical.mug.router.TestUtils.TestRouter;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.github.exbotanical.mug.constant.Method;
import com.github.exbotanical.mug.router.annotations.Route;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

@DisplayName("Test RouteHandler and RouteContext")
class RouteHandlerTest {
  /**
   * Spies for use in auto-registration via `use`.
   */

  private static final RouteHandler handlerSpy = spy(RouteHandler.class);

  private static final RouteHandler handlerSpy2 = spy(RouteHandler.class);

  private static final RouteHandler handlerSpy3 = spy(RouteHandler.class);

  private static final RouteHandler handlerSpy4 = spy(RouteHandler.class);

  private static TestRouter testRouter;

  @BeforeEach
  void setUp() {
    testRouter = new TestRouter();
  }

  @DisplayName("Test route handler receives context")
  @TestFactory
  Stream<DynamicTest> shouldReceiveRouteContext() {
    final List<TestCase<Expected>> testCases = List.of(
        new TestCase<>(
            "RootHandler",
            new SearchQuery(Method.GET, "/"),
            new Expected(handlerSpy, new RouteContext(new ArrayList<>()))),

        new TestCase<>(
            "RootHandlerAlternateMethod",
            new SearchQuery(Method.POST, "/"),
            new Expected(handlerSpy2, new RouteContext(new ArrayList<>()))),

        new TestCase<>(
            "WithParams",
            new SearchQuery(Method.GET, "/dev/123"),
            new Expected(handlerSpy3, new RouteContext(
                List.of(new Parameter("key", "123"))))),

        new TestCase<>(
            "NestedPathWithParams",
            new SearchQuery(Method.GET, "/api/users/user1"),
            new Expected(handlerSpy4, new RouteContext(
                List.of(new Parameter("id", "user1")))))

    );

    final List<RouteRecord> records = List.of(
        new RouteRecord("/", List.of(Method.GET),
            handlerSpy),
        new RouteRecord("/", List.of(Method.POST), handlerSpy2),
        new RouteRecord("/dev/:key[^\\d+$]", List.of(Method.GET), handlerSpy3),
        new RouteRecord("/api/users/:id[(.+)]", List.of(Method.GET), handlerSpy4));

    for (final RouteRecord record : records) {
      testRouter.register(
          record.methods(),
          record.path(),
          record.handler(),
          new ArrayList<>());
    }

    return testCases.stream()
        .map(
            testCase -> DynamicTest.dynamicTest(
                testCase.name(),
                () -> {
                  // Grab the query details.
                  final SearchQuery query = testCase.input();
                  final Expected expected = testCase.expected();

                  try {
                    final HttpExchange exchangeMock =
                        ExchangeMockFactory.build("http://test.com" + query.path(),
                            query.method());

                    // Invoke the handler with the mock request.
                    assert exchangeMock != null;
                    testRouter.handle(exchangeMock);
                    // Assert the matched handler was resolved and invoked with the expected
                    // arguments.
                    verify(
                        expected.handler,
                        times(1)).handle(
                        exchangeMock,
                        expected.context);
                  } catch (Exception e) {
                    fail("Did not expect an exception.", e);
                  }
                }));
  }

  @DisplayName("Test usage of private router classes and methods")
  @Test
  void shouldInvokePrivateClassAndMethods() throws IOException {
    testRouter.use(PrivateTestRoute.class);

    final HttpExchange exchangeMock =
        ExchangeMockFactory.build("http://test.com" + "/", Method.GET);

    assert exchangeMock != null;
    assertDoesNotThrow(() -> testRouter.handle(exchangeMock));

    verify(
        handlerSpy,
        times(1)).handle(
        any(HttpExchange.class),
        any(RouteContext.class));
  }

  private static class PrivateTestRoute {
    private PrivateTestRoute() {
    }

    @Route(method = Method.GET, path = "/")
    public void handlerA(HttpExchange exchange, RouteContext context) throws IOException {
      handlerSpy.handle(exchange, context);
    }
  }

  /**
   * Container for storing more metadata in TestCase.
   */
  private record Expected(RouteHandler handler, RouteContext context) {

  }
}
