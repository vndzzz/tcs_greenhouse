package org.vndzzz.devices

import monix.execution.Cancelable
import monix.execution.Scheduler.{global => scheduler}
import org.vndzzz.env.Environment

import scala.concurrent.duration.FiniteDuration

/**
  * Created by vn on 27.07.2017.
  */
class Controller(val env: Environment) {
  private implicit val _env = env
  val tempSensor = new TempSensor()
  val humiditySensor = new HumiditySensor()
  val pumpSensor = new PumpSensor()
  val windowSensor = new WindowSensor()

  private var runningCheckTask: Option[Cancelable] = None

  private def openWindowChecked() = if (!pumpSensor.value && !windowSensor.value) {
    if (env.openWindow()) println("window opened")
  }
  private def startPumpChecked() = if (!windowSensor.value && !pumpSensor.value) {
    if (env.startWaterPump()) println("water pump started")
  }

  private def stopWaterPump() = {
    if (pumpSensor.value) {
      env.stopWaterPump()
      println("water pump stopped")
    }
  }

  private def closeWindow() = {
    if(windowSensor.value) {
      env.closeWindow()
      println("window closed")
    }
  }

  /**
    * stupid algo which decides that wrong temp is worse than wrong humidity
    * in real it may be should use Pontryagin principle
    */
  def control(): Unit = {
    //in real we should take multiple values and interpolate
    val curTemp = tempSensor.value
    val curHumidity = humiditySensor.value

    if (curTemp > env.config.targetTemp + env.config.minTempDeviation) {
      if (pumpSensor.value) {
        println(s"pump is working, temp is $curTemp (High!)")
        if (curTemp > env.config.targetTemp + env.config.maxTempDeviation) {
          println(
            s"temperature is very high, stopping water pump to open window")
          stopWaterPump()
        }
      }
      openWindowChecked()
    }

    if (curTemp < env.config.targetTemp - env.config.minTempDeviation)
      closeWindow()

    if (curHumidity < env.config.targetHumidity - env.config.minHumidityDeviation) {
      if (windowSensor.value) {
        println(s"window is open, humidity is $curHumidity (High!)")
        if (curTemp < env.config.targetTemp + env.config.maxTempDeviation &&
            curHumidity < env.config.targetHumidity - env.config.maxHumidityDeviation)
          closeWindow()
      }
      startPumpChecked()
    }

    if (curHumidity > env.config.targetHumidity + env.config.minHumidityDeviation)
      stopWaterPump()
  }

  /**
    * starts background thread which runs controller repeatedly
    * @param checkInterval check interval in seconds
    */
  def start(checkInterval: FiniteDuration): Unit = {
    import scala.concurrent.duration._
    def cancelableTask = scheduler.scheduleWithFixedDelay(0 seconds, checkInterval) { control() }
    runningCheckTask = Some(runningCheckTask.fold(cancelableTask)(c => {
      c.cancel()
      cancelableTask
    }))
  }

  /**
    * stops controller if it was started
    */
  def stop(): Unit = {
    runningCheckTask.foreach(_.cancel())
  }

}
