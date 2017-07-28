package org.vndzzz.env

import better.files.{File => ScalaFile, _}
import com.typesafe.config.ConfigFactory

import scala.util.Try

/**
  * Created by vn on 26.07.17.
  */
case class EnvConfig(
    currentTemp: Int,
    currentHumidity: Int,
    tempOpenRatio: Double,
    humidityOpenRatio: Double,
    tempCloseRatio: Double,
    humidityCloseRatio: Double,
    targetTemp: Int,
    targetHumidity: Int,
    minTempDeviation: Int,
    maxTempDeviation: Int,
    minHumidityDeviation: Int,
    maxHumidityDeviation: Int
)

object EnvConfig {
  import pureconfig._

  private val configFile = ScalaFile("application.conf")

  type EnvCallback = EnvConfig => Unit

  private var registeredCallback: Option[EnvCallback] = None

  /**
    * set function called when config is read first or changed
    * @param callback EnvConfig => Unit
    */
  def onConfigChange(callback: EnvCallback): Unit = {
    registeredCallback = Some(callback)
  }

  private def readConfig = {

    val confPath = "env"
    lazy val appConfig = ConfigFactory.defaultApplication()

    lazy val reloadedConfig = Try({
      val conf = ConfigFactory
        .parseFile(configFile.toJava)
        .getConfig(confPath)
      println("loading config from file")
      loadConfig[EnvConfig](conf)
    })

    val envConfig =
      if (configFile.exists && !configFile.isDirectory && reloadedConfig.isSuccess) {
        reloadedConfig.get
      } else {
        val conf = appConfig.getConfig(confPath)
        println("loading default config")
        loadConfig[EnvConfig](conf)
      }

    envConfig
  }

  private def configChanged(newConfig: EnvConfig) =
    registeredCallback.foreach(c => c(newConfig))

  /**
    * reads config file, which should be named application.conf
    * '''method should be used after callback is defined with [[org.vndzzz.env.EnvConfig#onConfigChange]]'''
    */
  def init(): Unit = {
    def readConfigInt() =
      readConfig.fold(failures => println(failures.toString), configChanged)

    import java.nio.file.{Path, WatchEvent}
    val watcher = new ThreadBackedFileMonitor(configFile, 1) {
      override def onEvent(eventType: WatchEvent.Kind[Path],
                           file: ScalaFile): Unit = {
        readConfigInt()
      }
    }
    watcher.start()
    readConfigInt()
  }

  def default = EnvConfig(
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

  def format(config: EnvConfig): String = {
    s"""Initial temp: ${config.currentTemp}
       |Initial humidity: ${config.currentHumidity}
       |
       |Optimal temp: ${config.targetTemp - config.minTempDeviation} - ${config.targetTemp + config.minTempDeviation}
       |Possible temp: ${config.targetTemp - config.maxTempDeviation} - ${config.targetTemp + config.maxTempDeviation}
       |
       |Optimal humidity: ${config.targetHumidity - config.minHumidityDeviation} - ${config.targetHumidity + config.minHumidityDeviation}
       |Possible humidity:  ${config.targetHumidity - config.maxHumidityDeviation} - ${config.targetHumidity + config.maxHumidityDeviation}
       |
       |Temp increase ratio: ${config.tempCloseRatio}
       |Temp decrease ratio: ${config.tempOpenRatio}
       |
       |Humidity decrease ratio: ${config.humidityCloseRatio}
       |Humidity increase ratio: ${config.humidityOpenRatio}
     """.stripMargin
  }
}
