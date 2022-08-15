package com.github.exbotanical.mug.router;

import static com.github.exbotanical.mug.router.TestUtils.ExchangeMockFactory;
import static com.github.exbotanical.mug.router.TestUtils.RouteRecord;
import static com.github.exbotanical.mug.router.TestUtils.SearchQuery;
import static com.github.exbotanical.mug.router.TestUtils.TestCase;
import static com.github.exbotanical.mug.router.TestUtils.TestRouter;
import static com.github.exbotanical.mug.router.TestUtils.toList;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.github.exbotanical.mug.constant.Method;
import com.sun.net.httpserver.HttpExchange;
import java.util.ArrayList;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

@DisplayName("Test RouteHandler and RouteContext")
class RouteHandlerTest {
  /**
   * Container for storing more metadata in TestCase.
   */
  static record Expected(RouteHandler handler, RouteContext context) {

  }

  TestRouter testRouter;


  /**
   * Spies for use in auto-registration via `use`.
   */

  public static RouteHandler handlerSpy = spy(RouteHandler.class);

  public static RouteHandler handlerSpy2 = spy(RouteHandler.class);

  public static RouteHandler handlerSpy3 = spy(RouteHandler.class);

  public static RouteHandler handlerSpy4 = spy(RouteHandler.class);


  @BeforeEach
  void setUp() {
    testRouter = new TestRouter();
  }

  @DisplayName("Test route handler receives context")
  @TestFactory
  Stream<DynamicTest> shouldReceiveRouteContext() {
    RouteHandler testHandler = (exchange, context) -> {
      handlerSpy.handle(exchange, context);
    };

    RouteHandler testHandler2 = (exchange, context) -> {
      handlerSpy2.handle(exchange, context);
    };

    RouteHandler testHandler3 = (exchange, context) -> {
      handlerSpy3.handle(exchange, context);
    };

    RouteHandler testHandler4 = (exchange, context) -> {
      handlerSpy4.handle(exchange, context);
    };


    ArrayList<TestCase<Expected>> testCases = toList(
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
                toList(new Parameter("key", "123"))))),

        new TestCase<>(
            "NestedPathWithParams",
            new SearchQuery(Method.GET, "/api/users/user1"),
            new Expected(handlerSpy4, new RouteContext(
                toList(new Parameter("id", "user1")))))

    );

    ArrayList<RouteRecord> records = toList(
        new RouteRecord("/", toList(Method.GET),
            testHandler),
        new RouteRecord("/", toList(Method.POST), testHandler2),
        new RouteRecord("/dev/:key[^\\d+$]", toList(Method.GET), testHandler3),
        new RouteRecord("/api/users/:id[(.+)]", toList(Method.GET), testHandler4));

    for (RouteRecord record : records) {
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
                  SearchQuery query = testCase.input();
                  Expected expected = testCase.expected();

                  try {
                    HttpExchange exchangeMock =
                        ExchangeMockFactory.build("http://test.com" + query.path(),
                            query.method());

                    // Invoke the handler with the mock request.
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
}
