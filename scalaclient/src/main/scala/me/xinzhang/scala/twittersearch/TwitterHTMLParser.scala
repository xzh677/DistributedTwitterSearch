package me.xinzhang.scala.twittersearch

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.Date

import scala.collection.JavaConverters._

trait TwitterHTMLParser {

  def tweets(): List[Map[String, String]]

}

object TwitterHTMLParser {
  def apply(html: String): TwitterHTMLParser = new TwitterHTMLParserImpl(html)
}


//parse the html file to retrieve each tweet.
//retrieving is based on class and attributes info of HTML DOMs
//one html represents one page of twitter search results.
//it usually has 15 - 20 tweets.
class TwitterHTMLParserImpl(html: String) extends TwitterHTMLParser {

  private val l = {
    val doc = Jsoup.parse(html)
    //<div class="tweet"> contains meta information such as user_id, user_name etc.
    val infos = doc match {
      case x: Document => doc.select("div.tweet").asScala
      case _ => List()
    }
    //<p class="TweetTextSize"> contains tweet message
    val tweets = doc match {
      case x: Document => doc.select("p.TweetTextSize").asScala
      case _ => List()
    }
    //<span class="timestamp"> contains timestamp in milliseconds
    val time = doc match {
      case x: Document => doc.select("span._timestamp").asScala
      case _ => List()
    }
    (infos zip tweets) zip time
  }

  //extract each tweets and process it
  def tweets(): List[Map[String, String]] = {
    l.toList.map{ case ((info, tweet), time) => extractTweet(info, tweet, time)}
  }

  //convert single tweet with its meta information into a dictionary as Map[Key, Value]
  def extractTweet(info: Element, tweet: Element, time: Element): Map[String, String] =
    Map[String, String] (
      "_id" -> info.attr("data-tweet-id"),
      "user_id" -> info.attr("data-user-id"),
      "user_name" -> info.attr("data-name"),
      "user_screen_name" -> info.attr("data-screen-name"),
      "permalink" -> info.attr("data-permalink-path"),
      "tweet" -> tweet.text(),
      "create_at" -> Util.dateTimeFormater.format(
        new Date(time.attr("data-time").toLong * 1000)
    )
  )
}
