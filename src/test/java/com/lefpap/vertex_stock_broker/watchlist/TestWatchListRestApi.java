package com.lefpap.vertex_stock_broker.watchlist;

import com.lefpap.vertex_stock_broker.AbstractRestApiTest;
import com.lefpap.vertex_stock_broker.MainVerticle;
import com.lefpap.vertex_stock_broker.assets.Asset;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class TestWatchListRestApi extends AbstractRestApiTest {
  private static final Logger LOG = LoggerFactory.getLogger(TestWatchListRestApi.class);

  @Test
  void adds_and_returns_watchlist_for_account(Vertx vertx, VertxTestContext testContext) throws Throwable {
    var client = WebClient.create(vertx, new WebClientOptions().setDefaultPort(TEST_SERVER_PORT));
    var accountId = UUID.randomUUID();
    client.put("/account/watchlist/%s".formatted(accountId.toString()))
        .sendJsonObject(sendBody())
        .onComplete(testContext.succeeding(response -> {
          var json = response.bodyAsJsonObject();
          LOG.info("Response PUT: {}", json);
          assertEquals("{\"assets\":[{\"name\":\"AMZN\"},{\"name\":\"TSLA\"}]}", json.encode());
          assertEquals(200, response.statusCode());
          testContext.completeNow();
        })).compose(next -> {
          client.get("/account/watchlist/%s".formatted(accountId.toString()))
          .send()
          .onComplete(testContext.succeeding(asyncResp -> {
            var json = asyncResp.bodyAsJsonObject();
            LOG.info("Response GET: {}", json);
            assertEquals("{\"assets\":[{\"name\":\"AMZN\"},{\"name\":\"TSLA\"}]}", json.encode());
            assertEquals(200, asyncResp.statusCode());
            testContext.completeNow();
          }));

        return Future.succeededFuture();
      });
  }

  @Test
  void adds_and_deletes_watchlist_for_account(Vertx vertx, VertxTestContext testContext) {
    var client = WebClient.create(vertx, new WebClientOptions().setDefaultPort(TEST_SERVER_PORT));
    var accountId = UUID.randomUUID();
    client.put("/account/watchlist/%s".formatted(accountId.toString()))
      .sendJsonObject(sendBody())
      .onComplete(testContext.succeeding(response -> {
        var json = response.bodyAsJsonObject();
        LOG.info("Response PUT: {}", json);
        assertEquals("{\"assets\":[{\"name\":\"AMZN\"},{\"name\":\"TSLA\"}]}", json.encode());
        assertEquals(200, response.statusCode());
        testContext.completeNow();
      })).compose(next -> {
        client.delete("/account/watchlist/%s".formatted(accountId.toString()))
          .send()
          .onComplete(testContext.succeeding(asyncResp -> {
            var json = asyncResp.bodyAsJsonObject();
            LOG.info("Response DELETE: {}", json);
            assertEquals("{\"assets\":[{\"name\":\"AMZN\"},{\"name\":\"TSLA\"}]}", json.encode());
            assertEquals(200, asyncResp.statusCode());
            testContext.completeNow();
          }));

        return Future.succeededFuture();
      });

  }

  private static JsonObject sendBody() {
    return new WatchList(Arrays
      .asList(
        new Asset("AMZN"),
        new Asset("TSLA")))
        .toJsonObject();
  }
}
