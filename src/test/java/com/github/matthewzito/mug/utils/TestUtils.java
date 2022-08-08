package com.github.matthewzito.mug.utils;

import java.util.ArrayList;

public class TestUtils {
  @SafeVarargs
  public static <T> ArrayList<T> toList(T... vals) {
    ArrayList<T> list = new ArrayList<>();

    for (T val : vals) {
      list.add(val);
    }

    return list;
  }

  private TestUtils() {
  }

}
