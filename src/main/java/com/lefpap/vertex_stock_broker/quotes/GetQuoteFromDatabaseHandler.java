package com.lefpap.vertex_stock_broker.quotes;

import com.lefpap.vertex_stock_broker.db.DBErrorResponse;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.templates.SqlTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class GetQuoteFromDatabaseHandler implements Handler<RoutingContext> {
  private static final Logger LOG = LoggerFactory.getLogger(GetQuoteFromDatabaseHandler.class);

  private final Pool db;
  public GetQuoteFromDatabaseHandler(Pool db) {
    this.db = db;
  }

  @Override
  public void handle(RoutingContext context) {
    final String assetParam = context.pathParam("asset");
    LOG.debug("Asset parameter: {}", assetParam);

    SqlTemplate.forQuery(db,
      "SELECT q.asset, q.bid, q.ask, q.last_price, q.volume FROM broker.quotes q WHERE q.asset=#{asset}")
      .mapTo(QuoteEntity.class)
      .execute(Collections.singletonMap("asset", assetParam))
      .onFailure(DBErrorResponse.errorHandler(context, "Failed to get quote for asset `"+ assetParam +"` from db!"))
      .onSuccess(quotes -> {
        if(!quotes.iterator().hasNext()) {
          DBErrorResponse.notFoundResponse(context, "quote for asset " + assetParam + "not available!");
        }
        var response = quotes.iterator().next().toJsonObject();
        LOG.info("Path {} responds with {}", context.normalizedPath(), response.encode());
        context.response()
          .putHeader(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
          .end(response.toBuffer());
      });
  }
}
