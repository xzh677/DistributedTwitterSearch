package me.xinzhang.scala.twittersearch


import java.util.Date

import org.scalatest.FunSuite

class URLTest extends FunSuite {

  test("empty date should generate url without date") {
    assert(TwitterURL("ebola").create(null, "") ==
      "https://twitter.com/i/search/timeline?vertical=default&q=ebola&l=en" +
        "&src=typd&max_position=")

  }

  test("correct date with cursor should return a twitter recognisable url") {
    assert(TwitterURL("ebola").create("2017-07-02", "2017-07-03") ==
      "https://twitter.com/i/search/timeline?vertical=default&q=ebola%20since" +
        "%3A2017-07-02%20until%3A2017-07-03&l=en&src=typd&max_position=")
  }
}
