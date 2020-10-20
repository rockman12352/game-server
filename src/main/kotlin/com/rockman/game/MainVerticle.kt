package com.rockman.game

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet

class MainVerticle : AbstractVerticle() {

  override fun start(startPromise: Promise<Void>) {
    vertx
      .createHttpServer()
      .requestHandler { req ->
        println(Main.count++)
        Main.dbClient.query("SELECT * FROM users WHERE id='julien'")
          .execute { ar ->
            if (ar.succeeded()) {
              val result: RowSet<Row> = ar.result()
              println("Got " + result.size().toString() + " rows ")
              req.response()
                .putHeader("content-type", "text/plain")
                .end("Hello from Vert.x!" + result.size().toString() )
            } else {
              println("Failure: " + ar.cause().message)
            }
          }
      }
      .listen(8888) { http ->
        if (http.succeeded()) {
          startPromise.complete()
          println("HTTP server started on port 8888")
        } else {
          startPromise.fail(http.cause());
        }
      }
  }
}
