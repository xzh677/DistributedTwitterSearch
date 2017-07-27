package me.xinzhang.scala.twittersearch


trait ServerConnector {

  def save(m: Message)
  def update(m: Map[String, String])
  def checkpoint(m: Message)
  def complete(m: Message)

  def job(): Job

}

object ServerConnector {
  def apply(ip: String, port: String): ServerConnector =
    new ServerConnectorImpl("http://" + ip + ":" + port)
}

class ServerConnectorImpl(url: String) extends ServerConnector {

  private val DUMMY_SERVER = true
  private val sleepTime = 1000

  //due to infinite number of retries in HTTP connection, there is no need to return boolean values.
  def save(m: Message): Unit = {
    if (!DUMMY_SERVER) (new Post(url, m.generate(), "server")).connect(sleepTime)
  }
  def update(m: Map[String, String]): Unit = {
    if (!DUMMY_SERVER) (new Post(url, m, "server")).connect(sleepTime)
    else println(m)
  }
  def checkpoint(m: Message): Unit = {
    if (!DUMMY_SERVER) (new Post(url, m.generate(), "server")).connect(sleepTime)
  }
  def complete(m: Message): Unit = {
    if (!DUMMY_SERVER) (new Post(url, m.generate(), "server")).connect(sleepTime)
  }

  def job(): Job =
    if (!DUMMY_SERVER) {
      val m = (new Get(url, "server")).connect(sleepTime)
      Job(
        m("_id").asInstanceOf[String],
        m("end_date").asInstanceOf[String],
        m("cursor").asInstanceOf[String],
        m("count").asInstanceOf[Double].toInt,
        m("checkpoint").asInstanceOf[Double].toInt
      )
    } else {
      Job(
        "2015-02-01",
        "2015-02-02",
        "",
        0,
        100
      )
    }

}
