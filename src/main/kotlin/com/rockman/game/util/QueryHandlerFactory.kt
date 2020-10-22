package com.rockman.game.util

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet

class QueryHandlerFactory(private val routingContext: RoutingContext) {
    fun create(handler: (AsyncResult<RowSet<Row>>) -> Unit): Handler<AsyncResult<RowSet<Row>>> {
        return QueryHandler(routingContext, Handler(handler))
    }
}