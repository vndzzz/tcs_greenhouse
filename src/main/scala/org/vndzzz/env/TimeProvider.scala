package org.vndzzz.env

import java.time.LocalDateTime

/**
  * Created by vn on 26.07.17.
  */
sealed trait TimeProvider {
  def currentDateTime: LocalDateTime
}

class FakeTimeProvider(var curDateTime: LocalDateTime) extends TimeProvider {
  override def currentDateTime: LocalDateTime = curDateTime
}

object TimeProvider{
  implicit object RealTimeProvider extends TimeProvider {
    override def currentDateTime: LocalDateTime = LocalDateTime.now()
  }
}


