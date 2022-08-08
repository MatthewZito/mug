package com.github.matthewzito.mug.router.utils;

import com.github.matthewzito.mug.router.constant.Path;
import java.util.ArrayList;

public class PathUtils {
  public static ArrayList<String> expandPath(String path) {
    ArrayList<String> r = new ArrayList<>();

    for (String str : path.split(Path.PATH_DELIMITER.value)) {
      if (!"".equals(str)) {
        r.add(str);
      }
    }

    return r;
  }

  /**
   * Derives from a given label a regex pattern.
   * e.g. :id[^\d+$] => ^\d+$
   * e.g. :id => (.+)
   *
   * @param label A label from which to derive a regular expression pattern.
   * @return The derived regular expression pattern.
   */
  public static String deriveLabelPattern(String label) {
    int start = label.indexOf(Path.PATTERN_DELIMITER_START.value);
    int end = label.indexOf(Path.PATTERN_DELIMITER_END.value);

    // If the label doesn't contain a pattern, default to the wildcard pattern.
    if (start == -1 || end == -1) {
      return Path.PATTERN_WILDCARD.value;
    }

    return label.substring(start + 1, end);
  }

  /**
   * Derives from a given label a regex pattern's key.
   * e.g. :id[^\d+$] → id
   * e.g. :id → id
   *
   * @param label A string entity that represents a key/value pattern pair.
   * @return The key of the given pattern.
   */
  public static String deriveParameterKey(String label) {
    int start = label.indexOf(Path.PARAMETER_DELIMITER.value);
    int end = label.indexOf(Path.PATTERN_DELIMITER_START.value);

    if (end == -1) {
      end = label.length();
    }

    return label.substring(start + 1, end);
  }

  private PathUtils() {
  }
}
