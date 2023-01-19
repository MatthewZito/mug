package com.github.exbotanical.mug.router;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * A thread-safe cache for compiled regex Patterns.
 */
class RegexCache {
  /**
   * Thread-safe state mapping string patterns to their corresponding pre-compiled regex Patterns.
   */
  final ConcurrentHashMap<String, Pattern> state;

  RegexCache() {
    this.state = new ConcurrentHashMap<>();
  }

  /**
   * Retrieve the compiled regex for a given string pattern. Subsequent invocations of this method
   * with the same input will yield a cached value.
   *
   * @param pattern A string pattern and valid regular expression.
   * @return A compiled regex Pattern.
   */
  synchronized Pattern get(final String pattern) {
    return this.state.computeIfAbsent(pattern, Pattern::compile);
  }
}
