package me.xinzhang.scala.twittersearch

import java.util.Date

import org.scalatest.FunSuite

class UtilTest extends FunSuite {

  test("1500809477 timestamp should be convert to '1970-01-18 18:53:29'") {
    assert(Util.dateTimeFormater.format(new Date(1500809477)) ==
      "1970-01-18 18:53:29")
  }

}
