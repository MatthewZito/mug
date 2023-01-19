package com.github.exbotanical.mug.router;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Unit tests for PathUtils.
 */
@DisplayName("Test path utilities")
class PathUtilsTest {
  record TestCase<T> (String name, String input, T expected) {

  }

  @DisplayName("Test expandPath")
  @TestFactory
  Stream<DynamicTest> shouldExpandPath() {
    final List<TestCase<List<String>>> testInputs = List.of(
        new TestCase<>("BasicPath", "test", List.of("test")),
        new TestCase<>("NestedPath", "test/path", List.of("test", "path")),
        new TestCase<>("NestedPathTrailingSlash", "/some/test/path/",
            List.of("some", "test", "path")));

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
    final List<TestCase<String>> testInputs = List.of(
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
    final List<TestCase<String>> testInputs = List.of(
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
