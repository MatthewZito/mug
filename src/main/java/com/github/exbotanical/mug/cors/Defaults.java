package com.github.exbotanical.mug.cors;

import com.github.exbotanical.mug.constant.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Common defaults for use with Cors middleware.
 */
class Defaults {
  // Default allowed headers. Defaults to the "Origin" header, though this should be included
  // automatically.
  static final List<String> defaultAllowedHeaders =
      List.of(CommonHeader.ORIGIN.value);

  // Default allowed methods. Defaults to simple methods (those that do not trigger a Preflight).
  static final List<String> defaultAllowedMethods =
      List.of(Method.GET.toString(), Method.POST
          .toString(), Method.HEAD.toString());

  private Defaults() {}
}
