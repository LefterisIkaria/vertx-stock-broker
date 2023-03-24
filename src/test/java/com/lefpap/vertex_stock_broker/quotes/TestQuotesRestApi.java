package com.lefpap.vertex_stock_broker.quotes;

import com.lefpap.vertex_stock_broker.AbstractRestApiTest;
import com.lefpap.vertex_stock_broker.MainVerticle;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class TestQuotesRestApi extends AbstractRestApiTest {
  private static final Logger LOG = LoggerFactory.getLogger(TestQuotesRestApi.class);
  @Test
  void returns_quote_for_asset(Vertx vertx, VertxTestContext testContext) throws Throwable {
    var client = WebClient.create(vertx, new WebClientOptions().setDefaultPort(TEST_SERVER_PORT));
    client.get("/quotes/AMZN")
        .send()
        .onComplete(testContext.succeeding(response -> {
          var json = response.bodyAsJsonObject();
          LOG.info("Response: {}", json);
          assertEquals("{\"name\":\"AMZN\"}", json.getJsonObject("asset").encode());
          assertEquals(200, response.statusCode());
          testContext.completeNow();
        }));
  }

  @Test
  void returns_not_found_for_unknown_assets(Vertx vertx, VertxTestContext testContext) throws Throwable {
    var client = WebClient.create(vertx, new WebClientOptions().setDefaultPort(TEST_SERVER_PORT));
    client.get("/quotes/UNKNOWN")
        .send()
        .onComplete(testContext.succeeding(response -> {
          var json = response.bodyAsJsonObject();
          LOG.info("Response: {}", json);
          assertEquals("{\"message\":\"quote for asset UNKNOWN not avaliable.\",\"path\":\"/quotes/UNKNOWN\"}", json.encode());
          assertEquals(404, response.statusCode());
          testContext.completeNow();
        }));
  }
}
