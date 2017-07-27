package me.xinzhang.scala.twittersearch



trait Message {
  def generate(): Map[String, String]
}

case class CheckPointMessage(
    val startDate: String,
    val cursor: String,
    val count: Int,
    val checkPoint: Int
  ) extends Message {
  def generate(): Map[String, String] = Map(
    "_id" -> startDate,
    "count" -> count.toString,
    "cursor" -> cursor,
    "checkpoint" -> checkPoint.toString
  )
}

case class SaveMessage(
    val startDate: String,
    val cursor: String,
    val count: Int
  ) extends Message {
  def generate(): Map[String, String] = Map(
    "_id" -> startDate,
    "count" -> count.toString,
    "cursor" -> cursor,
    "checkpoint" -> "0"
  )
}