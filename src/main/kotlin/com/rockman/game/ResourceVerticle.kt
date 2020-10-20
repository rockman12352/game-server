package com.rockman.game

import io.vertx.core.AbstractVerticle
import java.io.File

class ResourceVerticle : AbstractVerticle() {
  override fun stop() {
    println("exit db")
    var file = File("D:\\kotlin\\kk")
    file.createNewFile()
    Application.dbClient.close()
    super.stop()
  }
}
