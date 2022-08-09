package com.github.matthewzito.mug.router.errors;

/**
 * A 405 MethodNotAllowed exception.
 */
public class MethodNotAllowedException extends Exception {
  public MethodNotAllowedException(String message) {
    super(message);
  }
}
