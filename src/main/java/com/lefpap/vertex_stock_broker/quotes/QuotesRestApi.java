package com.lefpap.vertex_stock_broker.quotes;

import com.lefpap.vertex_stock_broker.assets.Asset;
import com.lefpap.vertex_stock_broker.assets.AssetsRestApi;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.ext.web.Router;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class QuotesRestApi {
  private static final Logger LOG = LoggerFactory.getLogger(QuotesRestApi.class);
  public static void attach(final Router parent, final Pool db){
    final Map<String, Quote> cachedQuotes = new HashMap<>();
    AssetsRestApi.ASSETS.forEach(symbol -> cachedQuotes.put(symbol, getRandomQuote(symbol)));
    parent.get("/quotes/:asset").handler(new GetQuotesHandler(cachedQuotes));
    parent.get("/pg/quotes/:asset").handler(new GetQuoteFromDatabaseHandler(db));
  }

  private static Quote getRandomQuote(String assetParam) {
    return Quote.builder()
      .asset(new Asset(assetParam))
      .volume(randomValue())
      .ask(randomValue())
      .bid(randomValue())
      .lastPrice(randomValue())
      .build();
  }

  private static BigDecimal randomValue() {
    return BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(1, 100));
  }
}
