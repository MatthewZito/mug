package com.github.exbotanical.mug.router.errors;

/**
 * A 405 MethodNotAllowed exception.
 */
public class MethodNotAllowedException extends Exception {
  public MethodNotAllowedException(final String message) {
    super(message);
  }
}
