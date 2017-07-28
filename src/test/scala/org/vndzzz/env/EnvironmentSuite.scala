package org.vndzzz.env

import java.time.LocalDateTime

import org.scalatest.{BeforeAndAfterEach, FunSuite}

/**
  * Created by vn on 27.07.2017.
  */
class EnvironmentSuite extends FunSuite with BeforeAndAfterEach {

  private implicit val fakeTimeProvider = new FakeTimeProvider(LocalDateTime.now())
  private var env: Environment = _

  override protected def beforeEach(): Unit = {
    fakeTimeProvider.curDateTime = LocalDateTime.now()
    val config = EnvConfig(
      currentTemp = 40,
      currentHumidity = 50,
      tempOpenRatio = 0.3,
      tempCloseRatio = 0.1,
      humidityOpenRatio = 5,
      humidityCloseRatio = 3,
      targetTemp = 30,
      targetHumidity = 80,
      minTempDeviation = 0,
      maxTempDeviation = 0,
      minHumidityDeviation = 0,
      maxHumidityDeviation = 0
    )
    env = new Environment(config)
  }

  test("humidity should fall when pump not working") {
    fakeTimeProvider.curDateTime = fakeTimeProvider.curDateTime.plusSeconds(5)
    assert(env.humidity() == 35)
  }

  test("humidity should grow when pump working") {
    env.startWaterPump()
    fakeTimeProvider.curDateTime = fakeTimeProvider.curDateTime.plusSeconds(5)
    assert(env.humidity() == 75)
  }

  test("humidity can't be less than 0") {
    fakeTimeProvider.curDateTime =
      fakeTimeProvider.curDateTime.plusSeconds(1000)
    assert(env.humidity() == 0)
  }

  test("humidity can't be more than 100") {
    env.startWaterPump()
    fakeTimeProvider.curDateTime =
      fakeTimeProvider.curDateTime.plusSeconds(1000)
    assert(env.humidity() == 100)
  }

  test("temp should grow when window is closed") {
    fakeTimeProvider.curDateTime = fakeTimeProvider.curDateTime.plusSeconds(10)
    assert(env.temp() == 41)
  }

  test("temp should fall when window is open") {
    env.openWindow()
    fakeTimeProvider.curDateTime = fakeTimeProvider.curDateTime.plusSeconds(20)
    assert(env.temp() == 34)
  }

  test(
    "should be impossible to open window if waterpump is working and possible otherwise") {
    env.startWaterPump()
    assert(!env.openWindow())
    env.stopWaterPump()
    assert(env.openWindow())
  }

  test(
    "should be impossible to start waterpump if window is open and possible otherwise") {
    env.openWindow()
    assert(!env.startWaterPump())
    env.closeWindow()
    assert(env.startWaterPump())
  }

  test("if window state is changed humidity should fall") {
    fakeTimeProvider.curDateTime = fakeTimeProvider.curDateTime.plusSeconds(5)
    env.openWindow()
    fakeTimeProvider.curDateTime = fakeTimeProvider.curDateTime.plusSeconds(5)
    assert(env.humidity() == 20)
    env.closeWindow()
    fakeTimeProvider.curDateTime = fakeTimeProvider.curDateTime.plusSeconds(5)
    assert(env.humidity() == 5)
  }

  test("if pump state is changed temp should grow") {
    fakeTimeProvider.curDateTime = fakeTimeProvider.curDateTime.plusSeconds(5)
    env.startWaterPump()
    fakeTimeProvider.curDateTime = fakeTimeProvider.curDateTime.plusSeconds(5)
    assert(env.temp() == 41)
    env.stopWaterPump()
    fakeTimeProvider.curDateTime = fakeTimeProvider.curDateTime.plusSeconds(5)
    assert(env.temp == 41.5)
  }

}
