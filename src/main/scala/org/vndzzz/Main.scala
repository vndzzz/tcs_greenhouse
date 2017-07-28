package org.vndzzz

import monix.execution.Scheduler.{global => scheduler}
import org.vndzzz.devices.Controller
import org.vndzzz.env.TimeProvider._
import org.vndzzz.env.{EnvConfig, Environment}

import scala.concurrent.duration._

/**
  * Created by vn on 28.07.2017.
  */
object Main extends App {
  val env: Environment = new Environment(EnvConfig.default)
  println("Press any key to stop ....")
  EnvConfig.onConfigChange(c => {
    env.config = c
    println("Loaded config:\n" + EnvConfig.format(c))
  })
  EnvConfig.init()
  val controller = new Controller(env)
  controller.start(1.second)
  val cancelable = scheduler.scheduleWithFixedDelay(2 seconds, 5 seconds) {
    println(s"""
             |current temp: ${Math.round(controller.tempSensor.value*100)/100.0}
             |current humidity: ${Math.round(controller.humiditySensor.value*100)/100.0}
             |window is ${if (controller.windowSensor.value) "open"
               else "closed"}
             |pump is ${if (controller.pumpSensor.value) "working" else "idle"}
           """.stripMargin)
  }
  scala.io.StdIn.readChar()
  cancelable.cancel()
  controller.stop()
}
