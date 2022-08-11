package com.github.matthewzito.mug;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.github.matthewzito.mug.utils.NullSafe;
import com.github.matthewzito.mug.utils.TestUtils;

/**
 * Test shared mug utilities.
 */
@DisplayName("Test shared mug utilities")
class UtilsTest {

  @DisplayName("Test NullSafe.getFirst with Map")
  @Test
  void shouldBeNullSafeGetFirstMappable() {
    HashMap<String, List<String>> m = new HashMap<>();
    m.put("test", TestUtils.toList("value1", "value2"));

    assertEquals("value1", NullSafe.getFirst(m, "test"));
    assertNull(NullSafe.getFirst(m, "test2"));
  }

  @DisplayName("Test NullSafe.getFirst with List")
  @Test
  void shouldBeNullSafeGetFirstListable() {
    ArrayList<String> l = new ArrayList<>();

    assertNull(NullSafe.getFirst(l));

    l.add("test1");
    l.add("test2");

    assertEquals("test1", NullSafe.getFirst(l));
  }
}
