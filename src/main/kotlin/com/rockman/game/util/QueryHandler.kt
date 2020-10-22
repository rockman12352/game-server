package com.rockman.game.util

import com.rockman.game.Response
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.RoutingContext
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet

class QueryHandler(private val routingContext: RoutingContext, private val subHandler: Handler<AsyncResult<RowSet<Row>>>) : Handler<AsyncResult<RowSet<Row>>> {
    companion object {
        val logger = LoggerFactory.getLogger("QueryHandler")!!
    }

    override fun handle(event: AsyncResult<RowSet<Row>>?) {
        try {
            if (event!!.succeeded()) {
                subHandler.handle(event)
            } else {
                logger.info(Message.FAILED_TO_QUERY, event.cause())
                routingContext.response().end(Response.fail(Message.FAILED_TO_QUERY))
            }
        } catch (exp: Exception) {
            logger.info(Message.FAILED_TO_QUERY, exp)
            routingContext.response().end(Response.fail(exp.message ?: "exception in query"))
        }
    }
}