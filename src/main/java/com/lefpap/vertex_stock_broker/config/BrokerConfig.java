package com.lefpap.vertex_stock_broker.config;

import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.util.Objects;

@Builder
@Value
@ToString
public class BrokerConfig {

  String version;
  int serverPort;
  DBConfig dbConfig;

  public static BrokerConfig from(final JsonObject config){
    final Integer serverPort = config.getInteger(ConfigLoader.SERVER_PORT);
    if (Objects.isNull(serverPort)) throw new RuntimeException("%s not configured".formatted(ConfigLoader.SERVER_PORT));

    final String version = config.getString("version");
    if (Objects.isNull(version)) throw new RuntimeException("version is not configured in config file!");

    return BrokerConfig.builder()
      .version(version)
      .serverPort(serverPort)
      .dbConfig(parseDBConfig(config))
      .build();
  }

  private static DBConfig parseDBConfig(final JsonObject config) {
    return DBConfig.builder()
      .host(config.getString(ConfigLoader.DB_HOST))
      .port(config.getInteger(ConfigLoader.DB_PORT))
      .database(config.getString(ConfigLoader.DB_DATABASE))
      .user(config.getString(ConfigLoader.DB_USER))
      .password(config.getString(ConfigLoader.DB_PASSWORD))
      .build();
  }

}
