package org.vndzzz.devices

import java.time.LocalDateTime

import org.scalatest.{BeforeAndAfterEach, FunSuite}
import org.vndzzz.env.{EnvConfig, Environment, FakeTimeProvider}

/**
  * Created by vn on 27.07.2017.
  */
class ControllerSuite extends FunSuite with BeforeAndAfterEach{
  private implicit val fakeTimeProvider = new FakeTimeProvider(LocalDateTime.now())

  private val config = EnvConfig(
    currentTemp = 30,
    currentHumidity = 50,
    tempOpenRatio = 0.3,
    tempCloseRatio = 0.1,
    humidityOpenRatio = 5,
    humidityCloseRatio = 3,
    targetTemp = 30,
    targetHumidity = 50,
    minTempDeviation = 5,
    maxTempDeviation = 10,
    minHumidityDeviation = 5,
    maxHumidityDeviation = 20
  )

  test("if current temp is higher than opt + min then open window"){
    val env = new Environment(config.copy(currentTemp = 40))
    new Controller(env).control()
    assert(env.isWindowOpen)
  }

  test("if current temp is lower then opt + min then don't open window"){
    val env = new Environment(config.copy(currentTemp = 34))
    new Controller(env).control()
    assert(!env.isWindowOpen)
  }

  test("if current temp is higher than max deviation force window open"){
    val env = new Environment(config.copy(targetHumidity = config.currentHumidity, currentTemp = 41))
    assert(env.startWaterPump())

    new Controller(env).control()
    assert(env.isWindowOpen)
  }

  test("if current temp is lower than min deviation -> close window"){
    val env = new Environment(config.copy(currentTemp = 24))
    env.openWindow()
    assert(env.isWindowOpen)

    new Controller(env).control()
    assert(!env.isWindowOpen)
  }

  test("if current humidity is lower than target - dev -> start water pump"){
    val env = new Environment(config.copy(currentHumidity = 44))
    new Controller(env).control()
    assert(env.isWaterPumpWorking)
  }

  test("if current humidity is higher than target + dev -> stop water pump"){
    val env = new Environment(config.copy(currentHumidity = 56))
    env.startWaterPump()
    assert(env.isWaterPumpWorking)

    new Controller(env).control()
    assert(!env.isWaterPumpWorking)
  }

  test("if current humidity is higer than target + max dev -> don't start pump if temp is not lower then max temp"){
    val env = new Environment(config.copy(currentHumidity = 29, currentTemp = 41))
    env.openWindow()
    assert(env.isWindowOpen)

    new Controller(env).control()
    assert(!env.isWaterPumpWorking)
  }

  test("if current humidity is higher max and temp is lower than maxtemp then force close window"){
    val env = new Environment(config.copy(currentHumidity = 29, currentTemp = 39))

    env.openWindow()
    assert(env.isWindowOpen)

    new Controller(env).control()
    assert(env.isWaterPumpWorking)
    assert(!env.isWindowOpen)
  }

  test("if current humidity is less then max and window is open -> don't close window"){
    val env = new Environment(config.copy(currentHumidity = 40, currentTemp = 39))
    env.openWindow()
    assert(env.isWindowOpen)

    new Controller(env).control()
    assert(!env.isWaterPumpWorking)
    assert(env.isWindowOpen)
  }

}
