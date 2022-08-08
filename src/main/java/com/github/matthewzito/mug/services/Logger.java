package com.github.matthewzito.mug.services;

import java.io.PrintStream;
import java.lang.reflect.Method;

import com.github.matthewzito.mug.config.ShouldMute;

public class Logger {
  private final String system;
  private final PrintStream stream;

  private final String formatPrefix;

  public Logger(String system, PrintStream stream) {
    this.system = system;
    this.stream = stream;

    formatPrefix = String.format("[%s] ", this.system);
  }

  @ShouldMute
  public void log(String format, Object... args) {
    boolean shouldMute = false;
    try {
      Method method = Logger.class.getMethod("log", new Class[] { String.class, Object[].class });
      shouldMute = method.getAnnotation(ShouldMute.class).shouldMute();

    } catch (NoSuchMethodException | SecurityException e) {
      System.out.println(e);
    }

    if (shouldMute) {
      return;
    }

    stream.println(formatPrefix + String.format(format, args));
  }
}
