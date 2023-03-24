package com.lefpap.vertex_stock_broker;

import com.lefpap.vertex_stock_broker.assets.AssetsRestApi;
import com.lefpap.vertex_stock_broker.config.BrokerConfig;
import com.lefpap.vertex_stock_broker.config.ConfigLoader;
import com.lefpap.vertex_stock_broker.quotes.QuotesRestApi;
import com.lefpap.vertex_stock_broker.watchlist.WatchListRestApi;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestApiVerticle extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(RestApiVerticle.class);

  @Override
  public void start(final Promise<Void> startPromise) throws Exception {
    ConfigLoader.load(vertx)
      .onFailure(startPromise::fail)
      .onSuccess(brokerConfig -> {
        LOG.info("Retrieved Configuration: {}", brokerConfig);

        startHttpServer(startPromise, brokerConfig);
      });
  }

  private void startHttpServer(Promise<Void> startPromise, BrokerConfig config) {

    final Router router = createHttpRoutes(config);

    vertx.createHttpServer()
      .requestHandler(router)
      .exceptionHandler(error -> LOG.error("HTTP Server error: ", error))
      .listen(config.getServerPort(), http -> {
        if (http.succeeded()) {
          startPromise.complete();
          LOG.info("HTTP server started on port {}", config.getServerPort()  );
        } else {
          startPromise.fail(http.cause());
        }
      });
  }

  @NonNull
  private PgPool createDBPool(BrokerConfig config) {
    final var connectOptions = new PgConnectOptions()
      .setHost(config.getDbConfig().getHost())
      .setPort(config.getDbConfig().getPort())
      .setDatabase(config.getDbConfig().getDatabase())
      .setUser(config.getDbConfig().getUser())
      .setPassword(config.getDbConfig().getPassword());

    final var poolOptions = new PoolOptions()
      .setMaxSize(4);


    return PgPool.pool(vertx, connectOptions, poolOptions);
  }

  private Router createHttpRoutes(BrokerConfig config) {
    final Pool db = createDBPool(config);
    final Router router = Router.router(vertx);
    router.route()
      .handler(BodyHandler.create())
      .failureHandler(handleFailure());
    AssetsRestApi.attach(router, db);
    QuotesRestApi.attach(router, db);
    WatchListRestApi.attach(router, db);
    return router;
  }

  private static Handler<RoutingContext> handleFailure() {
    return errorContext -> {
      if (errorContext.response().ended()) {
        // Ignore
        return;
      }
      LOG.error("Route Error: ", errorContext.failure());
      errorContext.response()
        .setStatusCode(500)
        .end(new JsonObject().put("message", "Something went wrong :(").toBuffer());
    };
  }
}
