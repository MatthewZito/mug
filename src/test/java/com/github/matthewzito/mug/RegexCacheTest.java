package com.github.matthewzito.mug;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.github.matthewzito.mug.router.cache.RegexCache;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RegexCacheTest {

  @DisplayName("Test RegexCache")
  @Test
  void shouldCachePattern() {
    RegexCache cache = new RegexCache();

    String pattern1 = "(.*)";
    String pattern2 = "^\\d+$";

    // Must ensure retrieval is executed directly on cache state, otherwise we will
    // incite a cache op.
    assertNull(cache.state.get(pattern1));
    assertNull(cache.state.get(pattern2));
    cache.get(pattern1);
    assertNotNull(cache.state.get(pattern1));
    assertNull(cache.state.get(pattern2));

    assertNull(cache.state.get(pattern2));
    cache.get(pattern2);
    assertNotNull(cache.state.get(pattern1));
    assertNotNull(cache.state.get(pattern2));
  }
}
