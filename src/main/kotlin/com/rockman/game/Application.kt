package com.rockman.game

import com.rockman.game.router.Account
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.Vertx
import io.vertx.core.http.HttpHeaders
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.core.json.get
import io.vertx.mysqlclient.MySQLConnectOptions
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.PoolOptions


object Application {
    val vertx = Vertx.vertx()
    lateinit var dbClient: MySQLPool
    lateinit var config: JsonObject
    var count = 0
    val logger = LoggerFactory.getLogger(this.javaClass.name)

    @JvmStatic
    fun main(args: Array<String>) {
        val fileStore = ConfigStoreOptions().setType("file").setFormat("json").setConfig(JsonObject().put("path", "${System.getenv()["env"] ?: "default"}.json"))
        val retriever: ConfigRetriever = ConfigRetriever.create(vertx, ConfigRetrieverOptions().addStore(fileStore).addStore(ConfigStoreOptions().setType("sys")))
        retriever.getConfig { ar ->
            config = ar.result()
            initDB(config)
            initServer()
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
            if (sql.isBlank()) return
            dbClient.query(sql).execute { result ->
                if (!result.succeeded()) {
                    logger.error("SQL failed: $sql")
                }
            }
        }
    }

    private fun initServer() {
        var server = vertx.createHttpServer()
        var router = Router.router(vertx)

        //init global handler
        router.route().handler(BodyHandler.create())
        router.route().handler { rc ->
            val start = System.currentTimeMillis()
            logger.info("request on: ${rc.request().path()}")
            logger.info("request body: ${rc.bodyAsString}")
            rc.response().endHandler { _ -> logger.info("response: ${System.currentTimeMillis() - start}ms") }
            rc.next()
        }
        router.route().handler { rc -> rc.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json"); rc.next() }
        router.errorHandler(500) { rc ->
            rc.failure().printStackTrace()
            rc.response().end(Response.fail("internal error"))
        }

        //init sub module
        router.mountSubRouter("/accounts", Account.initRouter(vertx, dbClient))

        server.requestHandler(router).listen(8080)
        logger.info("server started!")
    }
}
