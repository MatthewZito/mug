package com.github.exbotanical.mug.router;

import static com.github.exbotanical.mug.router.TestUtils.ExchangeMockFactory;
import static com.github.exbotanical.mug.router.TestUtils.TestRouter;
import static com.github.exbotanical.mug.router.TestUtils.toList;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.github.exbotanical.mug.constant.Method;
import com.github.exbotanical.mug.constant.Status;
import com.sun.net.httpserver.HttpExchange;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test default route handlers")
class DefaultRouteHandlerTest {
  TestRouter testRouter;

  @BeforeEach
  void setUp() {
    testRouter = new TestRouter();
  }

  @DisplayName("Test Router invokes default Not Found handler")
  @Test
  void shouldInvokeDefaultNotFoundHandler() {
    try {
      HttpExchange exchangeMock = ExchangeMockFactory.build("http://test.com/api", Method.GET);

      testRouter.handle(exchangeMock);

      verify(exchangeMock, atLeastOnce())
          .sendResponseHeaders(Status.NOT_FOUND.value, -1);
    } catch (Exception e) {
      fail("Did not expect an exception.", e);
    }
  }

  @DisplayName("Test Router invokes default Method Not Allowed handler")
  @Test
  void shouldInvokeDefaultMethodNotAllowedHandler() {
    try {
      RouteHandler handler = (exchange, context) -> {
      };
      testRouter.register(toList(Method.GET), "/api", handler, new ArrayList<>());

      HttpExchange exchangeMock = ExchangeMockFactory.build("http://test.com/api", Method.POST);
      testRouter.handle(exchangeMock);

      verify(exchangeMock, atLeastOnce())
          .sendResponseHeaders(Status.METHOD_NOT_ALLOWED.value, -1);
    } catch (Exception e) {
      fail("Did not expect an exception.", e);
    }
  }

  @DisplayName("Test Router invokes Not Found handler")
  @Test
  void shouldInvokeNotFoundHandler() {
    RouteHandler notFoundSpy = spy(RouteHandler.class);
    RouteHandler notFoundHandler = (exchange, context) -> {
      notFoundSpy.handle(exchange, context);
    };

    testRouter.handleNotFoundWith(notFoundHandler);

    try {
      HttpExchange exchangeMock = ExchangeMockFactory.build("http://test.com/api", Method.GET);
      testRouter.handle(exchangeMock);

      verify(
          notFoundSpy,
          times(1)).handle(
              exchangeMock,
              new RouteContext(new ArrayList<>()));
    } catch (Exception e) {
      fail("Did not expect an exception.", e);
    }
  }

  @DisplayName("Test Router invokes Method Not Allowed handler")
  @Test
  void shouldInvokeMethodNotAllowedHandler() {
    RouteHandler notAllowedSpy = spy(RouteHandler.class);
    RouteHandler notAllowedHandler = (exchange, context) -> {
      notAllowedSpy.handle(exchange, context);
    };
    RouteHandler handler = (exchange, context) -> {
    };

    testRouter.handleMethodNotAllowedWith(notAllowedHandler);
    testRouter.register(toList(Method.GET), "/api", handler, new ArrayList<>());

    try {
      HttpExchange exchangeMock = ExchangeMockFactory.build("http://test.com/api", Method.POST);
      testRouter.handle(exchangeMock);

      verify(
          notAllowedSpy,
          times(1)).handle(
              exchangeMock,
              new RouteContext(new ArrayList<>()));
    } catch (Exception e) {
      fail("Did not expect an exception.", e);
    }
  }
}
