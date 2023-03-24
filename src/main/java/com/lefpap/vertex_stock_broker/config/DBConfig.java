package com.lefpap.vertex_stock_broker.config;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DBConfig {
  String host;
  int port;
  String database;
  String user;
  String password;

  @Override
  public String toString() {
    return ("%s {" +
      "host=%s\'" +
      ", port=%s\'" +
      ", database=%s\'" +
      ", user=%s\'" +
      ", password=***"
    ).formatted(
      DBConfig.class.getSimpleName(),
      host,
      port,
      database,
      user
    );
  }
}
