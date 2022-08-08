package com.github.matthewzito.mug.router.cache;

import java.util.Hashtable;
import java.util.regex.Pattern;

public class RegexCache {
  /**
   * Thread-safe state mapping string patterns to their corresponding pre-compiled
   * regex Patterns.
   */
  public Hashtable<String, Pattern> state;

  public RegexCache() {
    this.state = new Hashtable<>();
  }

  public Pattern get(String pattern) {

    Pattern regex = this.state.get(pattern);
    if (regex != null) {
      return regex;
    }

    Pattern newRegex = Pattern.compile(pattern);

    this.state.put(pattern, newRegex);
    return newRegex;
  }
}
