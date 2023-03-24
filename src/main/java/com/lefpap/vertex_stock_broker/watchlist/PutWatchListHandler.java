package com.lefpap.vertex_stock_broker.watchlist;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.UUID;

public class PutWatchListHandler implements Handler<RoutingContext> {

  private static final Logger LOG = LoggerFactory.getLogger(PutWatchListHandler.class);
  private final HashMap<UUID, WatchList> watchListPerAccount;

  public PutWatchListHandler(final HashMap<UUID, WatchList> watchListPerAccount) {
    this.watchListPerAccount = watchListPerAccount;
  }

  @Override
  public void handle(RoutingContext context) {
    String accountId = WatchListRestApi.getAccountId(context);

    var json = context.body().asJsonObject();
    var watchList = json.mapTo(WatchList.class);
    WatchList updated = watchListPerAccount.put(UUID.fromString(accountId), watchList);
    LOG.info("UPDATED: {}", updated);

    context.response().end(json.toBuffer());
  }
}
