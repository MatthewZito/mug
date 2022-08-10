package com.github.matthewzito.mug.router.annotations;

import com.github.matthewzito.mug.constant.Method;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the annotated method as a route handler. If the method's parent class is passed to a
 * Router's `use` method, each `Route` annotated method will be registered as a route handler for
 * the specified HTTP method and path.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(Routes.class)
public @interface Route {

  /**
   * The route method.
   */
  Method method() default Method.GET;

  /**
   * The route path.
   */
  String path() default "/";
}
