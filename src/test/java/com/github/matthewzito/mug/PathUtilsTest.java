package com.github.matthewzito.mug;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import com.github.matthewzito.mug.router.utils.PathUtils;
import com.github.matthewzito.mug.utils.TestUtils;

/**
 * Unit tests for PathUtils.
 */
@DisplayName("Test path utilities")
class PathUtilsTest {
  static record TestCase<T> (String name, String input, T expected) {

  }

  @DisplayName("Test expandPath")
  @TestFactory
  Stream<DynamicTest> shouldExpandPath() {
    ArrayList<TestCase<ArrayList<String>>> testInputs = TestUtils.toList(
        new TestCase<>("BasicPath", "test", TestUtils.toList("test")),
        new TestCase<>("NestedPath", "test/path", TestUtils.toList("test", "path")),
        new TestCase<>("NestedPathTrailingSlash", "/some/test/path/",
            TestUtils.toList("some", "test", "path")));

    return testInputs.stream()
        .map(testCase -> DynamicTest.dynamicTest(
            testCase.name,
            () -> assertEquals(
                testCase.expected,
                PathUtils.expandPath(testCase.input))));
  }

  @DisplayName("Test deriveLabelPattern")
  @TestFactory
  Stream<DynamicTest> shouldDeriveLabelPattern() {
    ArrayList<TestCase<String>> testInputs = TestUtils.toList(
        new TestCase<>("BasicRegex", ":id[^\\d+$]", "^\\d+$"),
        new TestCase<>("EmptyRegex", ":id[]", ""),
        new TestCase<>("NoRegex", ":id", "(.+)"),
        new TestCase<>("LiteralRegex", ":id[xxx]", "xxx"),
        new TestCase<>("WildcardRegex", ":id[*]", "*"));

    return testInputs.stream()
        .map(testCase -> DynamicTest.dynamicTest(
            testCase.name,
            () -> assertEquals(
                testCase.expected,
                PathUtils.deriveLabelPattern(testCase.input))));
  }

  @DisplayName("Test deriveParameterKey")
  @TestFactory
  Stream<DynamicTest> shouldDeriveParameterKey() {
    ArrayList<TestCase<String>> testInputs = TestUtils.toList(
        new TestCase<>("BasicKey", ":id[^\\d+$]", "id"),
        new TestCase<>("BasicKeyEmptyRegex", ":val[]", "val"),
        new TestCase<>("BasicKeyWildcardRegex", ":ex[(.*)]", "ex"),
        new TestCase<>("BasicKeyNoRegex", ":id", "id")

    );

    return testInputs.stream()
        .map(testCase -> DynamicTest.dynamicTest(
            testCase.name,
            () -> assertEquals(
                testCase.expected,
                PathUtils.deriveParameterKey(testCase.input))));
  }
}
