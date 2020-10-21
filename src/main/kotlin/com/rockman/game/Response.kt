package com.rockman.game

import io.vertx.core.json.Json

class Response(val success: Boolean, val message: String, val obj: Any? = null) {
    companion object {
        fun fail(message: String, obj: Any? = null): String {
            return Response(false, message, obj).toJson()
        }

        fun success(message: String, obj: Any? = null): String {
            return Response(true, message, obj).toJson()
        }
    }

    open fun toJson(): String = Json.encodePrettily(this)
}