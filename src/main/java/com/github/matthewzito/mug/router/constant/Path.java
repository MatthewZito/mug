package com.github.matthewzito.mug.router.constant;

/**
 * Path constants for use with a PathTrie.
 */
public enum Path {
  /**
   * The root path.
   */
  PATH_ROOT("/"),

  /**
   * The character used to demarcate paths.
   */
  PATH_DELIMITER("/"),

  /**
   * The character used to demarcate parameter keys from the path.
   */
  PARAMETER_DELIMITER(":"),

  /**
   * The character used to mark the start of a pattern.
   */
  PATTERN_START("["),

  /**
   * The character used to mark the end of a pattern.
   */
  PATTERN_END("]"),

  /**
   * The character used to represent a wildcard / "any" parameter value.
   */
  PATTERN_WILDCARD("(.+)");

  public final String value;

  private Path(String value) {
    this.value = value;
  }
}
