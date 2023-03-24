package com.lefpap.vertex_stock_broker.watchlist;

import com.lefpap.vertex_stock_broker.db.DBErrorResponse;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.templates.SqlTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class GetWatchListFromDatabase implements Handler<RoutingContext> {

  private static final Logger LOG = LoggerFactory.getLogger(GetWatchListFromDatabase.class);
  private final Pool db;
  public GetWatchListFromDatabase(final Pool db) {
    this.db = db;
  }

  @Override
  public void handle(RoutingContext context) {
    String accountId = WatchListRestApi.getAccountId(context);

    SqlTemplate.forQuery(db,
      "SELECT w.asset FROM broker.watchlist w WHERE w.account_id=#{account_id}")
      .mapTo(Row::toJson)
      .execute(Collections.singletonMap("account_id", accountId))
      .onFailure(DBErrorResponse.errorHandler(context, "Failed to fetch watchlist for account_id: `" + accountId + "`"))
      .onSuccess(assets -> {
        if (!assets.iterator().hasNext()) {
          DBErrorResponse.notFoundResponse(context, "watchlist for `account_id: " + accountId + "` is not available");
          return;
        }
        var response = new JsonArray();
        assets.forEach(response::add);
        LOG.info("Path {} responds with {}", context.normalizedPath(), response.encode());
        context.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
          .end(response.toBuffer());
      });
  }
}
