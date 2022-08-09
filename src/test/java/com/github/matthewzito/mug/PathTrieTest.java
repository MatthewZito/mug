package com.github.matthewzito.mug;

import static com.github.matthewzito.mug.utils.TestUtils.RouteRecord;
import static com.github.matthewzito.mug.utils.TestUtils.SearchQuery;
import static com.github.matthewzito.mug.utils.TestUtils.TestCase;
import static com.github.matthewzito.mug.utils.TestUtils.toList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.matthewzito.mug.router.constant.Method;
import com.github.matthewzito.mug.router.constant.Path;
import com.github.matthewzito.mug.router.middleware.Middleware;
import com.github.matthewzito.mug.router.trie.Action;
import com.github.matthewzito.mug.router.trie.Parameter;
import com.github.matthewzito.mug.router.trie.PathTrie;
import com.github.matthewzito.mug.router.trie.SearchResult;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.util.ArrayList;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

@DisplayName("Test PathTrie implementation")
class PathTrieTest {

  static class TestMiddleware implements Middleware {
    @Override
    public HttpHandler handle(HttpHandler handler) {
      return exchange -> {
        handler.handle(exchange);
      };
    }
  }

  @DisplayName("Test insert does not throw")
  @Test
  void shouldInsertRouteRecord() {
    HttpHandler testHandler = (HttpExchange exchange) -> {
    };

    ArrayList<RouteRecord> records = toList(
        new RouteRecord(Path.PATH_ROOT.value, toList(Method.GET),
            testHandler),
        new RouteRecord(Path.PATH_ROOT.value, toList(Method.GET,
            Method.POST), testHandler),
        new RouteRecord("/test", toList(Method.GET), testHandler),
        new RouteRecord("/test/path", toList(Method.GET), testHandler),
        new RouteRecord("/test/path", toList(Method.POST), testHandler),
        new RouteRecord("/test/path/paths", toList(Method.GET),
            testHandler),
        new RouteRecord("/foo/bar", toList(Method.GET), testHandler));

    PathTrie trie = new PathTrie();

    for (RouteRecord record : records) {
      assertDoesNotThrow(() -> trie.insert(
          record.methods(),
          record.path(),
          record.handler(),
          new ArrayList<>()),
          String.format("Unexpected exception thrown when inserting a route record with path %s",
              record.path()));
    }
  }

  @DisplayName("Test search")
  @TestFactory
  Stream<DynamicTest> shouldYieldSearchResults() {
    HttpHandler testHandler = (HttpExchange exchange) -> {
    };

    ArrayList<RouteRecord> records = toList(
        new RouteRecord(Path.PATH_ROOT.value, toList(Method.GET),
            testHandler),
        new RouteRecord("/test", toList(Method.GET), testHandler),
        new RouteRecord("/test/path", toList(Method.GET), testHandler),
        new RouteRecord("/test/path", toList(Method.POST), testHandler),
        new RouteRecord("/test/path/paths", toList(Method.GET),
            testHandler),
        new RouteRecord("/test/path/:id[^\\d+$]", toList(Method.GET),
            testHandler),
        new RouteRecord("/foo", toList(Method.GET), testHandler),
        new RouteRecord("/bar/:id[^\\d+$]/:user[^\\D+$]",
            toList(Method.POST), testHandler),
        new RouteRecord("/:*[(.+)]", toList(Method.OPTIONS), testHandler));

    ArrayList<TestCase<SearchResult>> testCases = toList(
        new TestCase<>(
            "SearchRoot",
            new SearchQuery(Method.GET, "/"),
            new SearchResult(
                new Action(testHandler, new ArrayList<>()),
                new ArrayList<>())),

        new TestCase<>(
            "SearchTrailingPath",
            new SearchQuery(Method.GET, "/test/"),
            new SearchResult(
                new Action(testHandler, new ArrayList<>()),
                new ArrayList<>())),

        new TestCase<>(
            "SearchWithParams",
            new SearchQuery(Method.GET, "/test/path/12"),
            new SearchResult(
                new Action(testHandler, new ArrayList<>()),
                toList(new Parameter("id", "12")))),

        new TestCase<>(
            "SearchNestedPath",
            new SearchQuery(Method.GET, "/test/path/paths"),
            new SearchResult(
                new Action(testHandler, new ArrayList<>()),
                new ArrayList<>())),

        new TestCase<>(
            "SearchPartialPath",
            new SearchQuery(Method.POST, "/test/path"),
            new SearchResult(
                new Action(testHandler, new ArrayList<>()),
                new ArrayList<>())),

        new TestCase<>(
            "SearchPartialPathOtherMethod",
            new SearchQuery(Method.GET, "/test/path"),
            new SearchResult(
                new Action(testHandler, new ArrayList<>()),
                new ArrayList<>())),

        new TestCase<>(
            "SearchAdditionalBasePath",
            new SearchQuery(Method.GET, "/foo"),
            new SearchResult(
                new Action(testHandler, new ArrayList<>()),
                new ArrayList<>())),

        new TestCase<>(
            "SearchAdditionalBasePathTrailingSlash",
            new SearchQuery(Method.GET, "/foo/"),
            new SearchResult(
                new Action(testHandler, new ArrayList<>()),
                new ArrayList<>())),

        new TestCase<>(
            "SearchComplexRegex",
            new SearchQuery(Method.POST, "/bar/123/alice"),
            new SearchResult(
                new Action(testHandler, new ArrayList<>()),
                toList(new Parameter("id", "123"),
                    new Parameter("user",
                        "alice")))),

        new TestCase<>(
            "SearchWildcardRegex",
            new SearchQuery(Method.OPTIONS, "/wildcard"),
            new SearchResult(
                new Action(testHandler, new ArrayList<>()),
                toList(new Parameter("*",
                    "wildcard"))))

    );

    PathTrie trie = new PathTrie();
    for (RouteRecord record : records) {
      trie.insert(record.methods(), record.path(), record.handler(), new ArrayList<>());
    }

    return testCases.stream()
        .map(
            testCase -> DynamicTest.dynamicTest(
                testCase.name(),
                () -> assertEquals(testCase.expected(),
                    trie.search(testCase.input().method(),
                        testCase.input().path()))));
  }
}
