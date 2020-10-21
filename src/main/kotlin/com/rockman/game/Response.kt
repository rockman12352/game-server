package com.rockman.game

import io.vertx.core.json.Json

class Response(val success: Boolean, val message: String, val data: Any? = null) {
    companion object {
        fun fail(message: String, data: Any? = null): String {
            return Response(false, message, data).toJson()
        }

        fun success(message: String, data: Any? = null): String {
            return Response(true, message, data).toJson()
        }
    }

    fun toJson(): String = Json.encodePrettily(this)
}