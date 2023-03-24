package com.lefpap.vertex_stock_broker.assets;

import com.lefpap.vertex_stock_broker.db.DBErrorResponse;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetAssetsFromDBHandler implements Handler<RoutingContext> {

  private static final Logger LOG = LoggerFactory.getLogger(GetAssetsFromDBHandler.class);
  private final Pool db;
  public GetAssetsFromDBHandler(final Pool db){
    this.db = db;
  }

  @Override
  public void handle(RoutingContext context) {

    db.query("SELECT a.value FROM broker.assets a")
      .execute()
      .onFailure(DBErrorResponse.errorHandler(context, "Failed to get assets from db!"))
        .onSuccess(result -> {
          var response = new JsonArray();
          result.forEach(row -> {
            response.add(row.getValue("value"));
          });
          LOG.info("Path: {} responds with: {}", context.normalizedPath(), response.encode());
          context.response()
            .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
            .end(response.toBuffer());
        });
  }


}
