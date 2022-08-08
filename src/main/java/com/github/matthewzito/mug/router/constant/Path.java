package com.github.matthewzito.mug.router.constant;

public enum Path {
  PATH_ROOT("/"),
  PATH_DELIMITER("/"),
  PARAMETER_DELIMITER(":"),
  PATTERN_DELIMITER_START("["),
  PATTERN_DELIMITER_END("]"),
  PATTERN_WILDCARD("(.+)");

  public final String value;

  private Path(String value) {
    this.value = value;
  }
}
