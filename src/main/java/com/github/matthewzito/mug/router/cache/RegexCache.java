package com.github.matthewzito.mug.router.cache;

import java.util.Hashtable;
import java.util.regex.Pattern;

/**
 * A thread-safe cache for compiled regex Patterns.
 */
public class RegexCache {
  /**
   * Thread-safe state mapping string patterns to their corresponding pre-compiled regex Patterns.
   */
  public Hashtable<String, Pattern> state;

  public RegexCache() {
    this.state = new Hashtable<>();
  }

  /**
   * Retrieve the compiled regex for a given string pattern. Subsequent invocations of this method
   * with the same input will yield a cached value.
   *
   * @param pattern A string pattern and valid regular expression.
   * @return A compiled regex Pattern.
   */
  public synchronized Pattern get(String pattern) {
    Pattern regex = this.state.get(pattern);
    if (regex != null) {
      return regex;
    }

    Pattern newRegex = Pattern.compile(pattern);

    this.state.put(pattern, newRegex);
    return newRegex;
  }
}
