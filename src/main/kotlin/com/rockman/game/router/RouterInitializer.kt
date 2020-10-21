package com.rockman.game.router

import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.mysqlclient.MySQLPool

abstract class RouterInitializer {
    abstract fun initRouter(vertx: Vertx, db: MySQLPool): Router
}