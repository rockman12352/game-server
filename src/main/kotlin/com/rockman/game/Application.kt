package com.rockman.game

import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.kotlin.core.json.get
import io.vertx.mysqlclient.MySQLConnectOptions
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.PoolOptions


object Application {
  lateinit var dbClient: MySQLPool
  lateinit var config: JsonObject
  var count = 0
  val logger = LoggerFactory.getLogger("Init")

  @JvmStatic
  fun main(args: Array<String>) {
    val vertx = Vertx.vertx()
    val fileStore = ConfigStoreOptions().setType("file").setFormat("json").setConfig(JsonObject().put("path", "${System.getenv()["env"] ?: "default"}.json"))
    val retriever: ConfigRetriever = ConfigRetriever.create(vertx, ConfigRetrieverOptions().addStore(fileStore).addStore(ConfigStoreOptions().setType("sys")))
    retriever.getConfig { ar ->
      config = ar.result()
      initDB(config)
      vertx.deployVerticle(AccountVerticle())
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
    this::class.java.classLoader.getResource("db.sql").readText(Charsets.UTF_8).split(";").forEach { sql ->
      if(sql.isBlank()) return
      dbClient.query(sql).execute { result ->
        if (!result.succeeded()) {
          logger.error("SQL failed: $sql")
        }
      }
    }
  }
}
