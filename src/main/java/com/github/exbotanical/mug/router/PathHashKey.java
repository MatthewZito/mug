package com.github.exbotanical.mug.router;

import java.util.Objects;

public class PathHashKey {
  private final String path;

  private final String method;

  public PathHashKey(final String path, final String method) {
    this.path = path;
    this.method = method;
  }

  @Override
  public int hashCode() {
    int hash = 17;
    hash = hash * 31 + path.hashCode();
    hash = hash * 31 + method.hashCode();
    return hash;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PathHashKey that = (PathHashKey) o;
    return Objects.equals(path, that.path) && Objects.equals(method, that.method);
  }
}
