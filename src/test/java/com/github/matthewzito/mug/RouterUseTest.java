package com.github.matthewzito.mug;

import static com.github.matthewzito.mug.utils.TestUtils.ExchangeMockFactory;
import static com.github.matthewzito.mug.utils.TestUtils.SearchQuery;
import static com.github.matthewzito.mug.utils.TestUtils.TestCase;
import static com.github.matthewzito.mug.utils.TestUtils.TestRoute;
import static com.github.matthewzito.mug.utils.TestUtils.TestRouter;
import static com.github.matthewzito.mug.utils.TestUtils.toList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.github.matthewzito.mug.router.annotations.Route;
import com.github.matthewzito.mug.router.constant.Method;
import com.github.matthewzito.mug.router.trie.PathTrie;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Test http router/multiplexer `use` method.
 */
@DisplayName("Test http router/multiplexer `use` method")
public class RouterUseTest {

  /**
   * Spies for use in auto-registration via `use`.
   */

  public static HttpHandler handlerSpy = spy(HttpHandler.class);

  public static HttpHandler handlerSpy2 = spy(HttpHandler.class);

  public static HttpHandler handlerSpy3 = spy(HttpHandler.class);

  public static HttpHandler handlerSpy4 = spy(HttpHandler.class);

  /**
   * Implements a routes configuration for use with `Router.use`.
   */
  public static class TestRoutes {
    @Route(method = Method.GET, path = "/")
    public void handlerA(HttpExchange exchange) throws IOException {
      handlerSpy.handle(exchange);
    }

    @Route(method = Method.POST, path = "/")
    public void handlerB(HttpExchange exchange) throws IOException {
      handlerSpy2.handle(exchange);
    }

    @Route(method = Method.GET, path = "/api")
    public void handlerC(HttpExchange exchange) throws IOException {
      handlerSpy3.handle(exchange);
    }

    @Route(method = Method.GET, path = "/dev/api")
    public void handlerD(HttpExchange exchange) throws IOException {
      handlerSpy4.handle(exchange);
    }
  }

  TestRouter testRouter;

  @BeforeEach
  void setUp() {
    testRouter = new TestRouter();
  }

  @DisplayName("Test route class registration via `use`")
  @TestFactory
  Stream<DynamicTest> shouldRegisterRoutesFromUse() {
    // Auto-register the routes.
    testRouter.use(TestRoute.class);

    PathTrie trie = testRouter.getTrie();

    ArrayList<TestCase<?>> testCases = toList(
        new TestCase<>(
            "SearchRootHandler",
            new SearchQuery(Method.GET, "/"), null),
        new TestCase<>(
            "SearchRootHandlerOtherMethod",
            new SearchQuery(Method.POST, "/"), null),
        new TestCase<>(
            "SearchChildPathHandler",
            new SearchQuery(Method.GET, "/api"), null),
        new TestCase<>(
            "SearchNestedPathHandler",
            new SearchQuery(Method.GET, "/dev/api"), null));

    return testCases.stream()
        .map(
            testCase -> DynamicTest.dynamicTest(
                testCase.name(),
                () -> {
                  assertDoesNotThrow(
                      () -> trie.search(testCase.input().method(), testCase.input().path()));
                }));
  }

  @DisplayName("Test correct route handler from `use` is matched and invoke upon request receipt")
  @TestFactory
  Stream<DynamicTest> shouldMatchAndInvokeRegisteredRoutesFromUse() {
    TestRouter testRouter = new TestRouter();

    testRouter.use(TestRoutes.class);

    ArrayList<TestCase<HttpHandler>> testCases = toList(
        new TestCase<>(
            "InvokeRootHandler",
            new SearchQuery(Method.GET, "/"), RouterUseTest.handlerSpy),
        new TestCase<>(
            "InvokeRootHandlerOtherMethod",
            new SearchQuery(Method.POST, "/"), RouterUseTest.handlerSpy2),
        new TestCase<>(
            "InvokeChildPathHandler",
            new SearchQuery(Method.GET, "/api"), RouterUseTest.handlerSpy3),
        new TestCase<>(
            "InvokeNestedPathHandler",
            new SearchQuery(Method.GET, "/dev/api"), RouterUseTest.handlerSpy4)

    );

    return testCases
        .stream()
        .map(testCase -> DynamicTest.dynamicTest(
            testCase.name(),
            () -> {
              // Grab the query details.
              SearchQuery query = testCase.input();

              HttpExchange exchangeMock =
                  ExchangeMockFactory.build("http://test.com" + query.path(),
                      query.method());

              try {
                System.out.printf("XXX HANDLER %s", testCase.expected());
                // Register the route using the handler spy.
                testRouter.register(
                    toList(query.method()),
                    query.path(),
                    testCase.expected(),
                    new ArrayList<>());

                // Invoke the handler with the mock request.
                testRouter.handle(exchangeMock);
                // Assert the matched handler was resolved and invoked.
                verify(testCase.expected(), times(1)).handle(exchangeMock);

              } catch (Exception e) {
                fail("Did not expect an exception.", e);
              }
            }));
  }

}
