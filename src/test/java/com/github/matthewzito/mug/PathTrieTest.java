package com.github.matthewzito.mug;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.github.matthewzito.mug.router.constant.Method;
import com.github.matthewzito.mug.router.constant.Path;
import com.github.matthewzito.mug.router.trie.Action;
import com.github.matthewzito.mug.router.trie.PathTrie;
import com.github.matthewzito.mug.utils.TestUtils;

@DisplayName("Test PathTrie implementation")
class PathTrieTest {
  static record TestCase(String name, SearchQuery input, PathTrie.SearchResult expected) {

  }

  static record SearchQuery(Method method, String path) {

  }

  static record RouteRecord(String path, ArrayList<Method> methods, HttpHandler handler) {
  }

  @DisplayName("Test insert does not throw")
  @Test
  void shouldInsertRouteRecord() {
    HttpHandler testHandler = (HttpExchange exchange) -> {
    };

    ArrayList<RouteRecord> records = TestUtils.toList(
        new RouteRecord(Path.PATH_ROOT.value, TestUtils.toList(Method.GET),
            testHandler),
        new RouteRecord(Path.PATH_ROOT.value, TestUtils.toList(Method.GET,
            Method.POST), testHandler),
        new RouteRecord("/test", TestUtils.toList(Method.GET), testHandler),
        new RouteRecord("/test/path", TestUtils.toList(Method.GET), testHandler),
        new RouteRecord("/test/path", TestUtils.toList(Method.POST), testHandler),
        new RouteRecord("/test/path/paths", TestUtils.toList(Method.GET),
            testHandler),
        new RouteRecord("/foo/bar", TestUtils.toList(Method.GET), testHandler));

    PathTrie trie = new PathTrie();

    for (RouteRecord record : records) {
      assertDoesNotThrow(() -> trie.insert(record.methods, record.path,
          record.handler),
          String.format("Unexpected exception thrown when inserting a route record with path %s",
              record.path));
    }
  }

  @DisplayName("Test search")
  @TestFactory
  Stream<DynamicTest> shouldYieldSearchResults() {
    HttpHandler testHandler = (HttpExchange exchange) -> {
    };

    ArrayList<RouteRecord> records = TestUtils.toList(
        new RouteRecord(Path.PATH_ROOT.value, TestUtils.toList(Method.GET),
            testHandler),
        new RouteRecord("/test", TestUtils.toList(Method.GET), testHandler),
        new RouteRecord("/test/path", TestUtils.toList(Method.GET), testHandler),
        new RouteRecord("/test/path", TestUtils.toList(Method.POST), testHandler),
        new RouteRecord("/test/path/paths", TestUtils.toList(Method.GET),
            testHandler),
        new RouteRecord("/test/path/:id[^\\d+$]", TestUtils.toList(Method.GET),
            testHandler),
        new RouteRecord("/foo", TestUtils.toList(Method.GET), testHandler),
        new RouteRecord("/bar/:id[^\\d+$]/:user[^\\D+$]",
            TestUtils.toList(Method.POST), testHandler),
        new RouteRecord("/:*[(.+)]", TestUtils.toList(Method.OPTIONS), testHandler));

    ArrayList<TestCase> testCases = TestUtils.toList(
        new TestCase(
            "SearchRoot",
            new SearchQuery(Method.GET, "/"),
            new PathTrie.SearchResult(
                new Action(testHandler),
                new ArrayList<>())),

        new TestCase(
            "SearchTrailingPath",
            new SearchQuery(Method.GET, "/test/"),
            new PathTrie.SearchResult(
                new Action(testHandler),
                new ArrayList<>())),

        new TestCase(
            "SearchWithParams",
            new SearchQuery(Method.GET, "/test/path/12"),
            new PathTrie.SearchResult(
                new Action(testHandler),
                TestUtils.toList(new PathTrie.Parameter("id", "12")))),

        new TestCase(
            "SearchNestedPath",
            new SearchQuery(Method.GET, "/test/path/paths"),
            new PathTrie.SearchResult(
                new Action(testHandler),
                new ArrayList<>())),

        new TestCase(
            "SearchPartialPath",
            new SearchQuery(Method.POST, "/test/path"),
            new PathTrie.SearchResult(
                new Action(testHandler),
                new ArrayList<>())),

        new TestCase(
            "SearchPartialPathOtherMethod",
            new SearchQuery(Method.GET, "/test/path"),
            new PathTrie.SearchResult(
                new Action(testHandler),
                new ArrayList<>())),

        new TestCase(
            "SearchAdditionalBasePath",
            new SearchQuery(Method.GET, "/foo"),
            new PathTrie.SearchResult(
                new Action(testHandler),
                new ArrayList<>())),

        new TestCase(
            "SearchAdditionalBasePathTrailingSlash",
            new SearchQuery(Method.GET, "/foo/"),
            new PathTrie.SearchResult(
                new Action(testHandler),
                new ArrayList<>())),

        new TestCase(
            "SearchComplexRegex",
            new SearchQuery(Method.POST, "/bar/123/alice"),
            new PathTrie.SearchResult(
                new Action(testHandler),
                TestUtils.toList(new PathTrie.Parameter("id", "123"),
                    new PathTrie.Parameter("user",
                        "alice")))),

        new TestCase(
            "SearchWildcardRegex",
            new SearchQuery(Method.OPTIONS, "/wildcard"),
            new PathTrie.SearchResult(
                new Action(testHandler),
                TestUtils.toList(new PathTrie.Parameter("*",
                    "wildcard"))))

    );

    PathTrie trie = new PathTrie();
    for (RouteRecord record : records) {
      trie.insert(record.methods, record.path, record.handler);
    }

    return testCases.stream()
        .map(
            (testCase) -> DynamicTest.dynamicTest(
                testCase.name,
                () -> assertEquals(testCase.expected,
                    trie.search(testCase.input.method,
                        testCase.input.path))));
  }
}
