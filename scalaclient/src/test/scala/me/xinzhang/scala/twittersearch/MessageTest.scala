package me.xinzhang.scala.twittersearch

import org.scalatest.FunSuite

class MessageTest extends FunSuite {

  test("CheckPointMessage should generate a dictionary with four attributes") {
    assert(
      CheckPointMessage("2015-02-01", "", 1, 100).generate() ==
      Map(
        "_id" -> "2015-02-01",
        "count" -> "1",
        "cursor" -> "",
        "checkpoint" -> "100"
      )
    )
  }

  test("SaveMessage should generate a dictionary with four attributes, " +
    "the checkPoint attribute should set to zero") {
    assert(
      SaveMessage("2015-02-01", "", 1).generate() ==
        Map(
          "_id" -> "2015-02-01",
          "count" -> "1",
          "cursor" -> "",
          "checkpoint" -> "0"
        )
    )
  }
}
