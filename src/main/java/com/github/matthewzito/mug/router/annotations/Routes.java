package com.github.matthewzito.mug.router.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation to assist the compiler in supporting repeated @Route annotations.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Routes {
  /**
   * Container for @Route annotations.
   */
  Route[] value();
}
