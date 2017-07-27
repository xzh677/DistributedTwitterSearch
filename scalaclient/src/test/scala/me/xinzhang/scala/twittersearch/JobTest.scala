package me.xinzhang.scala.twittersearch

import org.scalatest.FunSuite

class JobTest extends FunSuite {

  test("startDate is completed should be completed Job") {
    assert(Job("complete", "2015-02-02", "", 1, 1).isCompleted)
  }

  test("startDate is a date should be incompleted") {
    assert(!Job("2015-02-01", "2015-02-02", "", 1, 1).isCompleted)
  }

}
