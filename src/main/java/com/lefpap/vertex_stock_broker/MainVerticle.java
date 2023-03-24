package com.lefpap.vertex_stock_broker;

import com.lefpap.vertex_stock_broker.config.ConfigLoader;
import com.lefpap.vertex_stock_broker.db.migration.FlywayMigration;
import io.vertx.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class MainVerticle extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(MainVerticle.class);
  public static void main(String[] args) {
    System.setProperty(ConfigLoader.SERVER_PORT, "9000");

    var vertx = Vertx.vertx();
    vertx.exceptionHandler(error -> LOG.error("Unhandled Error: ", error));
    vertx.deployVerticle(new MainVerticle())
      .onFailure(err -> LOG.error("Failed to deploy: ", err))
      .onSuccess(id -> LOG.info("Deployed: {} with id: {}", MainVerticle.class.getSimpleName(), id));
  }


  @Override
  public void start(Promise<Void> startPromise) {

    vertx.deployVerticle(VersionInfoVerticle.class.getName())
        .onFailure(startPromise::fail)
        .onSuccess(id -> LOG.info("Deployed: {} with id: {}", VersionInfoVerticle.class.getSimpleName(), id))
        .compose(next -> migrateDatabase())
        .onFailure(startPromise::fail)
        .onSuccess(id -> LOG.info("Migrated db Schema to latest version!"))
        .compose(next -> deployRestApiVerticle(startPromise));
  }

  private Future<Void> migrateDatabase() {
    return ConfigLoader.load(vertx)
      .compose(config ->  FlywayMigration.migrate(vertx, config.getDbConfig()));
  }

  private Future<String> deployRestApiVerticle(Promise<Void> startPromise) {
    return vertx.deployVerticle(RestApiVerticle.class.getName(),
        new DeploymentOptions().setInstances(getAvailableProcessors())
      )
      .onFailure(startPromise::fail)
      .onSuccess(id -> {
        LOG.info("Deployed {} with id {}", RestApiVerticle.class.getSimpleName(), id);
        startPromise.complete();
      });
  }

  private static int getAvailableProcessors() {
    return Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
  }


}
