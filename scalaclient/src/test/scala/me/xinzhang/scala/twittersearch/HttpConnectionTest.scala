package me.xinzhang.scala.twittersearch

import org.scalatest.FunSuite

class HttpConnectionTest extends FunSuite {

  test("getWithRetries method with" +
    "https://www.xinzhang.me as URL" +
    "10 retry times" +
    "should return a map with ip as key and 128.199.192.229 as value" +
    "it will automatically reconnect after 1 second" +
    "if there are 10 fails, it will stop connecting") {
    assert(
      (new GetWithRetry("https://www.xinzhang.me", 10, "server")).connect(1000) ==
      Map ("ip" -> "128.199.192.229")
    )
  }

  test("getWithRetries method with" +
    "https://www.xinzhang.me as URL" +
    "10 retry times" +
    "should return a map with ip as key and 128.199.192.229 as value" +
    "it will automatically reconnect with 1 second interval" +
    "until get a response") {
    assert(
      (new Get("https://www.xinzhang.me", "server")).connect(1000) ==
      Map ("ip" -> "128.199.192.229")
    )
  }
}
