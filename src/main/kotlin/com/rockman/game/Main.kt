package com.rockman.game

import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.Verticle
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.get
import io.vertx.mysqlclient.MySQLConnectOptions
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.PoolOptions


object Main {
  lateinit var dbClient: MySQLPool
  lateinit var config: JsonObject
  var count = 0

  @JvmStatic
  fun main(args: Array<String>) {
    val vertx = Vertx.vertx()
    val fileStore = ConfigStoreOptions().setType("file").setFormat("json").setConfig(JsonObject().put("path", "${System.getenv()["env"] ?: "default"}.json"))
    val retriever: ConfigRetriever = ConfigRetriever.create(vertx, ConfigRetrieverOptions().addStore(fileStore).addStore(ConfigStoreOptions().setType("sys")))
    retriever.getConfig { ar ->
      config = ar.result()
      initDB(config)
      vertx.deployVerticle(MainVerticle())
      //vertx.deployVerticle(ResourceVerticle())
    }
  }

  private fun initDB(config: JsonObject) {
    val connectOptions = MySQLConnectOptions()
      .setPort(3306)
      .setHost("localhost")
      .setDatabase("game")
      .setUser(config.getJsonObject("DB")["username"])
      .setPassword(config.getJsonObject("DB")["password"])
    dbClient = MySQLPool.pool(connectOptions, PoolOptions().setMaxSize(5))
  }

}
