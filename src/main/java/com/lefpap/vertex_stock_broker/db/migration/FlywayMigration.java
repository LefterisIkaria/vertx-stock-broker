package com.lefpap.vertex_stock_broker.db.migration;

import com.lefpap.vertex_stock_broker.config.DBConfig;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.html.Option;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class FlywayMigration {
  private static final Logger LOG = LoggerFactory.getLogger(FlywayMigration.class);



  public static Future<Void> migrate(final Vertx vertx, final DBConfig dbConfig) {
    LOG.debug("DB config: {}", dbConfig);
    return vertx.<Void>executeBlocking(promise -> {
      // Flyway migration is blocking
      execute(dbConfig);
      promise.complete();
    })
    .onFailure(err -> LOG.error("Failed to migrate db schema with error: ", err));
  }

  private static void execute(DBConfig dbConfig) {
    final String jdbcURL = "jdbc:postgresql://%s:%d/%s"
      .formatted(
        dbConfig.getHost(),
        dbConfig.getPort(),
        dbConfig.getDatabase()
      );

    LOG.debug("Migrating DB Schema using jdbc url: {}", jdbcURL);

    final Flyway flyway = Flyway.configure()
      .dataSource(jdbcURL, dbConfig.getUser(), dbConfig.getPassword())
      .schemas("broker")
      .defaultSchema("broker")
      .load();

    var current = Optional.ofNullable(flyway.info().current());
    current.ifPresent(migrationInfo -> LOG.info("db schema is at version: {}", migrationInfo.getVersion()));

    var pendingMigrations = flyway.info().pending();
    LOG.debug("Pending migrations are: {}", printMIgrationInfo(pendingMigrations));

    flyway.migrate();
  }

  private static String printMIgrationInfo(MigrationInfo[] pendingMigrations) {
    if (Objects.isNull(pendingMigrations))
      return "[]";

    return Arrays.stream(pendingMigrations)
      .map(each -> each.getVersion() + " - " + each.getDescription())
      .collect(Collectors.joining(",", "[", "]"));
  }
}
