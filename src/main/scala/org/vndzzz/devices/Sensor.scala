package org.vndzzz.devices

import org.vndzzz.env.Environment

/**
  * Created by vn on 26.07.17.
  */
sealed trait Sensor[T] {
  def value: T
}

class HumiditySensor(implicit env: Environment) extends Sensor[Double] {
  override def value: Double = env.humidity()
}

class TempSensor(implicit env: Environment) extends Sensor[Double] {
  override def value: Double = env.temp()
}

class PumpSensor(implicit env: Environment) extends Sensor[Boolean]{
  override def value: Boolean = env.isWaterPumpWorking
}

class WindowSensor(implicit env: Environment) extends Sensor[Boolean]{
  override def value: Boolean = env.isWindowOpen
}

