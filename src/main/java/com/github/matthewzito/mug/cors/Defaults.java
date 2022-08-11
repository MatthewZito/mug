package com.github.matthewzito.mug.cors;

import com.github.matthewzito.mug.constant.Method;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Common defaults for use with Cors middleware.
 */
public class Defaults {
  // Default allowed headers. Defaults to the "Origin" header, though this should be included
  // automatically.
  public static final ArrayList<String> defaultAllowedHeaders =
      new ArrayList<>(Arrays.asList(CommonHeader.ORIGIN.value));

  // Default allowed methods. Defaults to simple methods (those that do not trigger a Preflight).
  public static final ArrayList<String> defaultAllowedMethods =
      new ArrayList<>(Arrays.asList(Method.GET.toString(), Method.POST
          .toString(), Method.HEAD.toString()));

  private Defaults() {}
}
