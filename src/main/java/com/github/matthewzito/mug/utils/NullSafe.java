package com.github.matthewzito.mug.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Utilities for performing common operations in a null-safe manner.
 */
public class NullSafe {
  /**
   * Retrieve the first value, or null if not extant.
   *
   * @param <T> Iterable type.
   * @param iterable An iterable object.
   * @return The first value, or null if not extant.
   */
  public static <T> T getFirst(Iterable<T> iterable) {
    return getFirstOrDefault(iterable, null);
  }

  /**
   * Retrieve the first value, or null if not extant.
   *
   * @param <T> List type.
   * @param listable An listable object.
   * @return The first value, or null if not extant.
   */
  public static <T> T getFirst(List<T> listable) {
    return getFirstOrDefault(listable, null);
  }

  /**
   * Retrieve the first value, or null if not extant.
   *
   * @param <K> Map key type.
   * @param <V> Map value type.
   * @param mappable An mappable object.
   * @param key
   * @return The first value, or null if not extant.
   */
  public static <K, V> V getFirst(Map<K, List<V>> mappable, K key) {
    return getFirstOrDefault(mappable.get(key), null);
  }

  /**
   * Retrieve the first value, or some provided default T if not extant.
   *
   * @param <T> The iterable type.
   * @param iterable An object that implements Iterable.
   * @param defaultValue The value to return if the first value of `iterable` is not extant.
   * @return The first value, or T if not extant.
   */
  public static <T> T getFirstOrDefault(Iterable<T> iterable, T defaultValue) {
    if (iterable == null) {
      return defaultValue;
    }

    Iterator<T> iterator = iterable.iterator();
    return iterator.hasNext() ? iterator.next() : defaultValue;
  }

  private NullSafe() {}
}
