package com.lefpap.vertex_stock_broker.quotes;

import com.lefpap.vertex_stock_broker.db.DBErrorResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

public class GetQuotesHandler implements Handler<RoutingContext> {

  private static final Logger LOG = LoggerFactory.getLogger(GetQuotesHandler.class);
  private final Map<String, Quote> cachedQuotes;

  public GetQuotesHandler(Map<String, Quote> cachedQuotes) {
    this.cachedQuotes = cachedQuotes;
  }

  @Override
  public void handle(RoutingContext context) {
    final String assetParam = context.pathParam("asset");
    LOG.debug("Asset parameter: {}", assetParam);

    var maybeQuote = Optional.ofNullable(cachedQuotes.get(assetParam));
    if (maybeQuote.isEmpty()) {
      DBErrorResponse.notFoundResponse(context, "quote for asset " + assetParam + " not avaliable.");
      return;
    }

    final JsonObject response = maybeQuote.get().toJsonObject();
    LOG.info("Path {} responds with {}", context.normalizedPath(), response.encode());
    context.response().end(response.toBuffer());
  }

}
