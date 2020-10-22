package com.rockman.game.router

import com.rockman.game.Response
import com.rockman.game.util.*
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.kotlin.core.json.get
import io.vertx.mysqlclient.MySQLPool
import io.vertx.sqlclient.Tuple

object Account : RouterInitializer() {
    override fun initRouter(vertx: Vertx, db: MySQLPool): Router {
        val router = Router.router(vertx)
        router.route(HttpMethod.POST, "/")
                .handler(JSONObjectValidator.missing(listOf("username", "password", "email")))
                .handler { rc ->
                    if (!rc.response().ended()) {
                        val body = rc.bodyAsJson
                        db.preparedQuery("SELECT COUNT(*) FROM accounts WHERE username = ? or email = ?")
                                .execute(Tuple.of(body["username"], body["email"]), rc.get<QueryHandlerFactory>("queryFactory").create {
                                    if (it.result().first().getInteger(0) == 0) {
                                        db.preparedQuery("INSERT INTO accounts (username, password, email) VALUES (?, ?, ?)")
                                                .execute(Tuple.of(body["username"], body["password"], body["email"]), rc.get<QueryHandlerFactory>("queryFactory").create {
                                                    rc.response().end(Response.success(Message.ACCOUNT_CREATED))
                                                })
                                    } else {
                                        rc.response().end(Response.fail(Message.ACCOUNT_EXIST))
                                    }
                                })
                    }
                }

        router.route(HttpMethod.POST, "/login")
                .handler(JSONObjectValidator.missing(listOf("username", "password")))
                .handler { rc ->
                    if (!rc.response().ended()) {
                        val body = rc.bodyAsJson
                        db.preparedQuery("SELECT * FROM accounts WHERE username = ? and password = ?")
                                .execute(Tuple.of(body["username"], body["password"]), rc.get<QueryHandlerFactory>("queryFactory").create {
                                    if (it.result().size() == 0) {
                                        rc.response().end(Response.fail(Message.ACCOUNT_NOT_FOUND))
                                    } else {
                                        rc.response().end(Response.success(it.result().first().getString("username")))
                                    }
                                })
                    }
                }
        return router
    }

}