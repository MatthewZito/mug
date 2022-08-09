package com.github.matthewzito.mug.router.errors;

/**
 * A 404 NotFound exception.
 */
public class NotFoundException extends Exception {
  public NotFoundException(String message) {
    super(message);
  }
}
