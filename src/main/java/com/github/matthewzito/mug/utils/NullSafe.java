package com.github.matthewzito.mug.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

// @todo test
// @todo document
public class NullSafe {
  public static <T> T getFirst(Iterable<T> iterable) {
    return getFirstOrDefault(iterable, null);
  }

  public static <T> T getFirst(List<T> listable) {
    return getFirstOrDefault(listable, null);
  }

  public static <K, V> V getFirst(Map<K, List<V>> mappable, K key) {
    return getFirstOrDefault(mappable.get(key), null);
  }

  public static <T> T getFirstOrDefault(Iterable<T> iterable, T defaultValue) {
    if (iterable == null) {
      return defaultValue;
    }

    Iterator<T> iterator = iterable.iterator();
    return iterator.hasNext() ? iterator.next() : defaultValue;
  }
}
