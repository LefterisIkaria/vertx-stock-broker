package com.lefpap.vertex_stock_broker.watchlist;

import com.lefpap.vertex_stock_broker.db.DBErrorResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.templates.SqlTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class DeleteWatchListFromDatabaseHandler implements Handler<RoutingContext> {

  private static final Logger LOG = LoggerFactory.getLogger(DeleteWatchListFromDatabaseHandler.class);
  private final Pool db;
  public DeleteWatchListFromDatabaseHandler(final Pool db) {
    this.db = db;
  }

  @Override
  public void handle(RoutingContext context) {
    var accountId = WatchListRestApi.getAccountId(context);

    SqlTemplate.forUpdate(db,
      "DELETE FROM broker.watchlist where account_id=#{account_id}")
      .execute(Collections.singletonMap("account_id", accountId))
      .onFailure(DBErrorResponse.errorHandler(context, "Failed to delete watchlist for `account_id: " + accountId + "`"))
      .onSuccess(result -> {
        LOG.debug("Deleted {} rows for `account_id: {}`", result.rowCount(), accountId);
        context.response()
        .setStatusCode(HttpResponseStatus.NO_CONTENT.code())
        .end();
      });
  }
}
