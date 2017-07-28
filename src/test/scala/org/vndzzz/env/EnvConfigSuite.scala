package org.vndzzz.env

import better.files.{File => ScalaFile}
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, FunSuite}

import scala.concurrent.Promise
import scala.concurrent.duration._

/**
  * Created by vn on 27.07.2017.
  */
class EnvConfigSuite
    extends FunSuite
    with ScalaFutures
    with BeforeAndAfterEach {

  val extFile = ScalaFile("application.conf")

  override protected def beforeEach(): Unit = extFile.delete(true)
  override protected def afterEach(): Unit = extFile.delete(true)

  val targetConf = EnvConfig(
    currentTemp = 0,
    currentHumidity = 100,
    tempOpenRatio = 10.0,
    humidityOpenRatio = 10.0,
    tempCloseRatio = 10.0,
    humidityCloseRatio = 10.0,
    targetTemp = 40,
    targetHumidity = 80,
    minTempDeviation = 5,
    maxTempDeviation = 10,
    minHumidityDeviation = 10,
    maxHumidityDeviation = 20
  )

  test("EnvConfig should load defaults") {
    var conf: Option[EnvConfig] = None
    EnvConfig.onConfigChange(c => conf = Some(c))
    EnvConfig.init()

    assert(conf.contains(targetConf))
  }

  /**
    * may fail on windows because of WatchService platform specific implementation
    */
  ignore("EnvConfig should reload config if file changed") {
    var conf: Option[EnvConfig] = None
    EnvConfig.init()

    val p = Promise[Unit]()

    EnvConfig.onConfigChange(c => {
      conf = Some(c)
      p.success(())
    })

    extFile.overwrite("""env {
        |    current-temp: 0
        |    current-humidity: 30
        |    temp-open-ratio: 10,
        |    humidity-open-ratio: 10,
        |    temp-close-ratio: 10,
        |    humidity-close-ratio: 10
        |    target-temp: 40
        |    target-humidity: 80
        |    min-temp-deviation: 5
        |    max-temp-deviation: 10
        |    min-humidity-deviation: 10
        |    max-humidity-deviation: 20
        |}""".stripMargin)

    whenReady(p.future, timeout = Timeout(2.second)) { _ =>
      assert(conf.contains(targetConf.copy(currentHumidity = 30)))
    }
  }
}
