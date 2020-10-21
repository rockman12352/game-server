package com.rockman.game.router

import com.rockman.game.Response
import com.rockman.game.util.Message
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.kotlin.core.json.get
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.Tuple

object Account : RouterInitializer() {
    override fun initRouter(vertx: Vertx, db: MySQLPool): Router {
        val router = Router.router(vertx)
        router.route(HttpMethod.POST, "/").handler { rc ->
            if (!rc.bodyAsJson.containsKey("username")) {
                rc.response().end(Response.fail(Message.FAILED_TO_QUERY))
            } else if (!rc.bodyAsJson.containsKey("password")) {
                rc.response().end(Response.fail(Message.FAILED_TO_QUERY))
            } else {
                db.preparedQuery("SELECT COUNT(*) FROM accounts WHERE username = ?").execute(Tuple.of(rc.bodyAsJson["username"])) { ar ->
                    if (ar.succeeded()) {
                        if (ar.result().first().getInteger(0) == 0) {
                            db.preparedQuery("INSERT INTO accounts (username, password) VALUES (?, ?)")
                                    .execute(Tuple.of(rc.bodyAsJson["username"], rc.bodyAsJson["password"])) { ar ->
                                        if (ar.succeeded()) {
                                            rc.response().end(Response.success(Message.ACCOUNT_CREATE))
                                        } else {
                                            rc.response().end(Response.fail(Message.FAILED_TO_QUERY))
                                        }
                                    }
                        } else {
                            rc.response().end(Response.fail(Message.ACCOUNT_EXIST))
                        }
                    } else {
                        rc.response().end(Response.fail(Message.FAILED_TO_QUERY))
                    }
                }
            }
        }

        return router
    }

}