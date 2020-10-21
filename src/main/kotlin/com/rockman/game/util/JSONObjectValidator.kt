package com.rockman.game.util

import com.rockman.game.Response
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

object JSONObjectValidator {
    fun missing(list: List<String>): Handler<RoutingContext>{
        return Handler { rc->
            val body = rc.bodyAsJson
            val missing = list.filterNot { body.containsKey(it) }.joinToString()
            if(missing.isNotEmpty())
                rc.response().end(Response.fail("${Message.MISSING_PARAMETER}: $missing"))
            rc.next()
        }
    }
}