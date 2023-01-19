package com.github.exbotanical.mug.router;

import com.github.exbotanical.mug.constant.Method;
import com.github.exbotanical.mug.router.annotations.Route;
import com.sun.net.httpserver.HttpExchange;

import java.net.URI;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Shared test utilities.
 */
public final class TestUtils {
  private TestUtils() {
    throw new AssertionError("Non-instantiable");
  }

  /**
   * A route record, for use with iterative PathTrie insertion during testing.
   */
  public static record RouteRecord(String path, List<Method> methods, RouteHandler handler) {
  }

  /**
   * A search query record, for application against a PathTrie.
   */
  public static record SearchQuery(Method method, String path) {

  }

  /**
   * A test case record.
   */
  public static record TestCase<T>(String name, SearchQuery input, T expected) {

  }

  /**
   * A Router with an accessible state.
   */
  public static class TestRouter extends Router {
    public PathTrie getTrie() {
      return this.trie;
    }
  }

  /**
   * A route handlers class for testing auto-registration via `use`.
   */
  public static class TestRoute {
    public TestRoute() {
    }

    @Route(method = Method.GET, path = "/")
    public void handlerA(HttpExchange exchange, RouteContext context) {
    }

    @Route(method = Method.POST, path = "/")
    private void handlerB(HttpExchange exchange, RouteContext context) {
    }

    @Route(method = Method.GET, path = "/api")
    public void handlerC(HttpExchange exchange, RouteContext context) {
    }

    @Route(method = Method.GET, path = "/dev/api")
    private void handlerD(HttpExchange exchange, RouteContext context) {
    }
  }

  /**
   * Factory for mock HttpExchange objects.
   */
  public static class ExchangeMockFactory {
    /**
     * Build a new HttpExchange mock.
     *
     * @param url    The request URL of the exchange.
     * @param method The request method of the exchange.
     *
     * @return HttpExchange mock.
     */
    public static HttpExchange build(String url, Method method) {
      URI uri;

      try {
        uri = new URI(url);
      } catch (Exception e) {
        return null;
      }

      HttpExchange exchangeMock = mock(HttpExchange.class);
      when(exchangeMock.getRequestURI()).thenReturn(uri);
      when(exchangeMock.getRequestMethod()).thenReturn(method.toString());

      return exchangeMock;
    }
  }
}
