package com.github.exbotanical.mug.router;

import static com.github.exbotanical.mug.router.TestUtils.ExchangeMockFactory;
import static com.github.exbotanical.mug.router.TestUtils.RouteRecord;
import static com.github.exbotanical.mug.router.TestUtils.SearchQuery;
import static com.github.exbotanical.mug.router.TestUtils.TestCase;
import static com.github.exbotanical.mug.router.TestUtils.TestRoute;
import static com.github.exbotanical.mug.router.TestUtils.TestRouter;
import static com.github.exbotanical.mug.router.TestUtils.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

@DisplayName("Test http router/multiplexer")
class RouterTest {
  TestRouter testRouter;

  @BeforeEach
  void setUp() {
    testRouter = new TestRouter();
  }

  @DisplayName("Test route class override")
  @TestFactory
  Stream<DynamicTest> shouldOverrideMethodsFromUse() {
    RouteHandler testHandler = (exchange, context) -> {
    };

    RouteHandler testHandler2 = (exchange, context) -> {
    };

    RouteHandler testHandler3 = (exchange, context) -> {
    };

    RouteHandler testHandler4 = (exchange, context) -> {
    };

    ArrayList<RouteRecord> records = toList(
        new RouteRecord("/", toList(Method.GET),
            testHandler),
        new RouteRecord("/", toList(Method.POST), testHandler2),
        new RouteRecord("/api", toList(Method.GET), testHandler3),
        new RouteRecord("/dev/api", toList(Method.GET), testHandler4));

    testRouter.use(TestRoute.class);

    PathTrie trie = testRouter.getTrie();

    ArrayList<TestCase<SearchResult>> testCases = toList(
        new TestCase<>(
            "OverrideRootHandler",
            new SearchQuery(Method.GET, "/"),
            new SearchResult(
                new Action(testHandler, new ArrayList<>()),
                new ArrayList<>())),

        new TestCase<>(
            "OverrideRootHandlerOtherMethod",
            new SearchQuery(Method.POST, "/"),
            new SearchResult(
                new Action(testHandler2, new ArrayList<>()),
                new ArrayList<>())),

        new TestCase<>(
            "OverrideChildPathHandler",
            new SearchQuery(Method.GET, "/api"),
            new SearchResult(
                new Action(testHandler3, new ArrayList<>()),
                new ArrayList<>())),

        new TestCase<>(
            "OverrideNestedPathHandler",
            new SearchQuery(Method.GET, "/dev/api"),
            new SearchResult(
                new Action(testHandler4, new ArrayList<>()),
                new ArrayList<>())));

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
                () -> assertEquals(testCase.expected(),
                    trie.search(testCase.input().method(),
                        testCase.input().path()))));
  }

  @DisplayName("Test correct route handler is matched and invoke upon request receipt")
  @TestFactory
  Stream<DynamicTest> shouldMatchAndInvokeRegisteredRoutes() {
    TestRouter testRouter = new TestRouter();

    ArrayList<TestCase<?>> testCases = toList(
        new TestCase<>(
            "InvokeRootHandler",
            new SearchQuery(Method.GET, "/"), null),
        new TestCase<>(
            "InvokeRootHandlerOtherMethod",
            new SearchQuery(Method.POST, "/"), null),
        new TestCase<>(
            "InvokeChildPathHandler",
            new SearchQuery(Method.GET, "/api"), null),
        new TestCase<>(
            "InvokeNestedPathHandler",
            new SearchQuery(Method.GET, "/dev/api"), null),
        new TestCase<>(
            "InvokeOverriddenRootHandler",
            new SearchQuery(Method.GET, "/"), null)

    );

    return testCases
        .stream()
        .map(testCase -> DynamicTest.dynamicTest(
            testCase.name(),
            () -> {
              // Grab the query details.
              SearchQuery query = testCase.input();

              // Initialize a route handler spy. We'll test whether this was actually
              // invoked when the router receives a mock request.
              RouteHandler handlerSpy = spy(RouteHandler.class);
              // Generate the mock HttpExchange. This represents an HTTP request that
              // will match the expected path and method.
              HttpExchange exchangeMock =
                  ExchangeMockFactory.build("http://test.com" + query.path(),
                      query.method());

              try {
                // Register the route using the handler spy.
                testRouter.register(
                    toList(query.method()),
                    query.path(),
                    handlerSpy,
                    new ArrayList<>());

                // Invoke the handler with the mock request.
                testRouter.handle(exchangeMock);
                // Assert the matched handler was resolved and invoked.
                verify(handlerSpy, times(1)).handle(exchangeMock,
                    new RouteContext(new ArrayList<>()));

              } catch (Exception e) {
                fail("Did not expect an exception.", e);
              }
            }));
  }

}
