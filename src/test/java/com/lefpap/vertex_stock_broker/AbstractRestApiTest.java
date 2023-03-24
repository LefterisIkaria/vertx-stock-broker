package com.lefpap.vertex_stock_broker;

import com.lefpap.vertex_stock_broker.config.ConfigLoader;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
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
public abstract class AbstractRestApiTest {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractRestApiTest.class);
  protected static final int TEST_SERVER_PORT = 9000;
  protected static final String TEST_DB_HOST = "localhost";
  protected static final String TEST_DB_PORT = "5432";
  protected static final String TEST_DB_DATABASE = "stock-broker";
  protected static final String TEST_DB_USER = "postgres";
  protected static final String TEST_DB_PASSWORD = "1234";

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    System.setProperty(ConfigLoader.SERVER_PORT, String.valueOf(TEST_SERVER_PORT));
    System.setProperty(ConfigLoader.DB_HOST, TEST_DB_HOST);
    System.setProperty(ConfigLoader.DB_PORT, TEST_DB_PORT);
    System.setProperty(ConfigLoader.DB_DATABASE, TEST_DB_DATABASE);
    System.setProperty(ConfigLoader.DB_USER, TEST_DB_USER);
    System.setProperty(ConfigLoader.DB_PASSWORD, TEST_DB_PASSWORD);
    LOG.warn("!!! Tests are using local database !!!");
    vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
  }

}
