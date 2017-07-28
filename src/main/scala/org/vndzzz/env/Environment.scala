package org.vndzzz.env

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
  * Created by vn on 26.07.17.
  */
class Environment(var config: EnvConfig)(implicit timeProvider: TimeProvider) {

  private var lastHumidity: Double = config.currentHumidity
  private var lastTemp: Double = config.currentTemp
  private var lastTime: LocalDateTime = timeProvider.currentDateTime

  private var windowOpen: Boolean = false
  private var pumpWorking: Boolean = false

  private def diffInSeconds(): Long =
    lastTime.until(timeProvider.currentDateTime, ChronoUnit.SECONDS)

  def humidity(): Double = {
    val result = if (pumpWorking) {
      lastHumidity + config.humidityOpenRatio * diffInSeconds()
    } else {
      lastHumidity - config.humidityCloseRatio * diffInSeconds()
    }
    if (result < 0)
      0
    else if (result > 100)
      100
    else
      result
  }

  def temp(): Double = {
    val result = if (windowOpen) {
      lastTemp - config.tempOpenRatio * diffInSeconds()
    } else {
      lastTemp + config.tempCloseRatio * diffInSeconds()
    }
    result
  }
  def isWindowOpen: Boolean = windowOpen
  def isWaterPumpWorking: Boolean = pumpWorking

  private def updateValues() = {
    lastHumidity = humidity()
    lastTemp = temp()
    lastTime = timeProvider.currentDateTime
  }

  def openWindow(): Boolean = this.synchronized {
    if (pumpWorking)
      false
    else {
      updateValues()
      windowOpen = true
      true
    }
  }

  def closeWindow(): Unit = this.synchronized {
    if (windowOpen) {
      updateValues()
      windowOpen = false
    }
  }

  def startWaterPump(): Boolean = this.synchronized {
    if (windowOpen)
      false
    else {
      updateValues()
      pumpWorking = true
      true
    }
  }

  def stopWaterPump(): Unit = this.synchronized {
    if (pumpWorking) {
      updateValues()
      pumpWorking = false
    }
  }
}
