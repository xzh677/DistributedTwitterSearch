package me.xinzhang.twitter

/*
 * Xin Zhang
 * 15 May 2015
*/

import scala.annotation.tailrec
import scala.util.{Try, Success, Failure}
import util.parsing.json._
import java.util.Date

trait HttpConnection {
  
  //recursively send the http request until maxretry reached. If maxretry is -1, it will try to reconnect server forever.
  @tailrec
  private def tryToConnect(process: => Map[String, Any], sleepTime:Long, server:String, retry:Int):Map[String, Any] = {
    Try(process) match {
      case Success(x) => x
      case Failure(e) => {
        if (retry != 0) {
          var tTime = sleepTime
          var nextRetry = retry
          val d = new Date()
          if (retry > -1) {
            tTime = tTime * 2     //increase delay time for each retry
            nextRetry = retry - 1
          }
          println(d.toString + " : Unable to connect to "+server+", try to reconnect after "+(tTime/1000)+" seconds")
          Thread.sleep(tTime)
          tryToConnect(process, tTime, server, nextRetry)
        } else {
          println("Failed.")
          Map[String, Any]()
        }
      }
    }
  }
  
  //this is the function to pattern match different scenarios. 
  def connect(sleepTime:Long):Map[String, Any] = this match {
    case Post(url, data, server) => {
      def process = json {
        val l = (data map {case (key, value) => List((key, value))}).flatten //the data which is sending to server
        Http(url).postForm(l.asInstanceOf[Seq[(String,String)]]).asString.body
      }
      tryToConnect(process, sleepTime, server, -1)  //retry value -1 means infinite number of retires
    }
    case Get(url, server) => {
      def process = json {
        Http(url).asString.body
      }
      tryToConnect(process, sleepTime, server, -1) //retry value -1 means infinite number of retires
    }
    case GetWithRetry(url, maxRetry, server) => {
      def process = json {
        Http(url).asString.body
      }
      tryToConnect(process, sleepTime, server, maxRetry) //set the max number of retries
    }
  }
  
  //convert any given String to a Map with key and value pairs. The value can be any thing.
  private def json(data:String):Map[String, Any] = JSON.parseFull(data) match {
    case Some(e) => e.asInstanceOf[Map[String, Any]]
    case None => Map[String, Any]()
  }
  
}

case class Post(url:String, data:Map[String,String], serverName:String) extends HttpConnection()
case class Get(url:String, serverName:String) extends HttpConnection()
case class GetWithRetry(url:String, retry:Int, serverName:String) extends HttpConnection()
