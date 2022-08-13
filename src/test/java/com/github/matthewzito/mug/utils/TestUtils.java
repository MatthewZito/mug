package com.github.matthewzito.mug.utils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.matthewzito.mug.constant.Method;
import com.github.matthewzito.mug.router.Router;
import com.github.matthewzito.mug.router.annotations.Route;
import com.github.matthewzito.mug.router.trie.PathTrie;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.net.URI;
import java.util.ArrayList;

/**
 * Shared test utilities.
 */
public class TestUtils {
  /**
   * A route record, for use with iterative PathTrie insertion during testing.
   */
  public static record RouteRecord(String path, ArrayList<Method> methods, HttpHandler handler) {
  }

  /**
   * A search query record, for application against a PathTrie.
   */
  public static record SearchQuery(Method method, String path) {

  }

  /**
   * A test case record.
   */
  public static record TestCase<T> (String name, SearchQuery input, T expected) {

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
    @Route(method = Method.GET, path = "/")
    public void handlerA(HttpExchange exchange) {}

    @Route(method = Method.POST, path = "/")
    public void handlerB(HttpExchange exchange) {}

    @Route(method = Method.GET, path = "/api")
    public void handlerC(HttpExchange exchange) {}

    @Route(method = Method.GET, path = "/dev/api")
    public void handlerD(HttpExchange exchange) {}
  }

  /**
   * Factory for mock HttpExchange objects.
   */
  public static class ExchangeMockFactory {
    /**
     * Build a new HttpExchange mock.
     *
     * @param url The request URL of the exchange.
     * @param method The request method of the exchange.
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

  /**
   * Add n values to an ArrayList.
   *
   * @param <T> Element type.
   * @param vals Values to add.
   * @return ArrayList containing values `vals`.
   */
  @SafeVarargs
  public static <T> ArrayList<T> toList(T... vals) {
    ArrayList<T> list = new ArrayList<>();

    for (T val : vals) {
      list.add(val);
    }

    return list;
  }

  private TestUtils() {
    throw new AssertionError("Non-instantiable");
  }
}
