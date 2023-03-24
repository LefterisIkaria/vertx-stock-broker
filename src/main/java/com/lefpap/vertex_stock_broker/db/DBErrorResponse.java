package com.lefpap.vertex_stock_broker.db;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBErrorResponse {

  private static final Logger LOG = LoggerFactory.getLogger(DBErrorResponse.class);
  public static Handler<Throwable> errorHandler(RoutingContext context, String message) {
    return err -> {
      LOG.error("Failure: ", err);
      context.response()
        .setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
        .end(new JsonObject()
          .put("message", message)
          .put("path", context.normalizedPath())
          .toBuffer()
        );
    };
  }

  public static void notFoundResponse(RoutingContext context, String message) {
    context.response()
      .setStatusCode(HttpResponseStatus.NOT_FOUND.code())
      .end(new JsonObject()
        .put("message", message)
        .put("path", context.normalizedPath())
        .toBuffer()
      );
  }
}
