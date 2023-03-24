package com.lefpap.vertex_stock_broker.assets;

import io.vertx.ext.web.Router;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class AssetsRestApi {

  private static final Logger LOG = LoggerFactory.getLogger(AssetsRestApi.class);
  public static final List<String> ASSETS = Arrays.asList("AAPL", "AMZN", "FB", "GOOG", "MSFT", "NFLX", "TSLA");


  public static void attach(final Router parent, Pool db) {
    parent.get("/assets").handler(new GetAssetsHandler());
    parent.get("/pg/assets").handler(new GetAssetsFromDBHandler(db));
  }
}
