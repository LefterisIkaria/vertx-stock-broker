package com.lefpap.vertex_stock_broker.watchlist;

import com.lefpap.vertex_stock_broker.db.DBErrorResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.templates.SqlTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class PutWatchListFromDatabaseHandler implements Handler<RoutingContext> {

  private static final Logger LOG = LoggerFactory.getLogger(PutWatchListFromDatabaseHandler.class);
  private final Pool db;

  public PutWatchListFromDatabaseHandler(final Pool db) {
    this.db = db;
  }

  @Override
  public void handle(final RoutingContext context) {
    var accountId = WatchListRestApi.getAccountId(context);

    var json = context.body().asJsonObject();
    var watchList = json.mapTo(WatchList.class);

    var parameterBatch = Stream.of(watchList.getAssets())
      .flatMap(Collection::stream)
      .map(asset -> {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("account_id", accountId);
        parameters.put("asset", asset.getName());
        return parameters;
      }).toList();

    SqlTemplate.forUpdate(db,
        "INSERT INTO broker.watchlist VALUES (#{account_id}, #{asset})" +
        " ON CONFLICT (account_id, asset) DO NOTHING")
      .executeBatch(parameterBatch)
      .onFailure(DBErrorResponse.errorHandler(context, "Failed to insert into watchlist"))
      .onSuccess(result -> context.response()
          .setStatusCode(HttpResponseStatus.NO_CONTENT.code())
          .end()
      );

  }


}
