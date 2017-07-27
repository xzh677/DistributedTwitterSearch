package me.xinzhang.twitter

/*
 * Xin Zhang
 * 15 May 2015
*/

import scala.annotation.tailrec
import scala.collection.JavaConversions._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements
import java.util.Date
import java.text.SimpleDateFormat

object Client {

  def parameter(args:List[String]):Map[String, String] = {
    val (a, b) = args partition (_(0)=='-')
    val c = a map (x=>x.substring(1))
    val d = c zip b
    //default settings
    var ip = "localhost"
    var port = "8080"
    var keyword = "twitter"
    d map { case (k, v) =>
      if (k == "ip")
        ip = v
      else if (k == "port")
        port = v
    }
    Map[String, String](
      "ip" -> ip,
      "port" -> port,
      "keyword" -> keyword
    )
  }
  
  def main(args: Array[String]) {
    val m = parameter(args.toList)
    val tc = new TwitterClient(m("ip"), m("port"))
    tc.search(m("keyword"))
  }
}

class TwitterClient(server_ip:String, server_port:String) {
  
  private val serverUrl = "http://" + server_ip + ":" + server_port
  private val tweeterSleepTime = 2000
  private val tweeterRetry = 10
  private val serverSleepTime = 60000
  
  //create url in following pattern
  //https://twitter.com/i/search/timeline?q=KEYWORD lang:en since:1970-01-01 until:1970-01-02&scroll_cursor=
  private def create_url(keyword:String, startDate:String, endDate:String):String = {
    var url = "https://twitter.com/i/search/timeline?q="
    url = url + keyword.replace(" ", "%20")
    url = url + "%20lang%3A" + "en"
    url = url + "%20since%3A" + startDate
    url = url + "%20until%3A" + endDate
    url = url + "&scroll_cursor="
    url
  }
  
  //initialize the http connections with sleeptime
  private def get(url:String) = (new Get(url, "server")).connect(this.serverSleepTime)
  private def getWithRetry(url:String) = (new GetWithRetry(url, this.tweeterRetry, "twitter")).connect(this.tweeterSleepTime)
  private def post(url:String, data:Map[String,String]) = (new Post(url, data, "server")).connect(this.serverSleepTime)
  
  //parse the html file to retrieve each tweet.
  //retrieving is based on class and attributes info of HTML DOMs
  //one html represents one page of twitter search results.
  //it usually has 15 - 20 tweets.
  private def updateTweets(html:String):Int= {
    var count = 0
    val doc = Jsoup.parse(html);
    val infos = doc.select("div.tweet")
    val tweets = doc.select("p.TweetTextSize")
    val time = doc.select("span._timestamp")
    
    val l = (infos zip tweets) zip time
    val d = for {
      ((x, y), z) <- l
    } yield {
      val ft = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss");
      val createAt = ft.format(new Date(z.attr("data-time").toLong * 1000))
      val m = Map[String, String] (
        "_id" -> x.attr("data-tweet-id"),
        "user_id" -> x.attr("data-user-id"),
        "user_name" -> x.attr("data-name"),
        "user_screen_name" -> x.attr("data-screen-name"),
        "permalink" -> x.attr("data-permalink-path"),
        "create_at" -> createAt,
        "tweet" -> y.text()
      )
      count = count + 1
      this.post(this.serverUrl+"/update", m)
    }
    count
  }
  
  //the wrapper function of searchHandler
  //searchHandler needs to be tail recursion to optimize the memory usage
  //thus it needs to be private
  def search(keyword:String) {
    this.searchHandler(keyword)
    println("Completed!")
  }
  
  @tailrec
  private def searchHandler(keyword:String):Unit = {
    
    //searchRec function iterates the cursor which given by twitter search
    @tailrec
    def searchRec(url:String, startDate:String, endDate:String, lastCount:Int, cursor:String, total:Int, sleep_time:Long, 
        checkPoint:Int):(Boolean, Int, String) = {
      //lastCount indicates is there anymore tweets
      //if last time we get an empty list from twitter, which means there is is no more tweets.
      if (lastCount > 0) {
        val data = this.getWithRetry(url + cursor)
        //empty data means we didn't get any reply from twitter, it usually is due to the network failure.
        if (!data.isEmpty) {
          val html = data("items_html").asInstanceOf[String]
          val scrollCursor = data("scroll_cursor").asInstanceOf[String]
          val count = updateTweets(html)
          val totalNext = total + count
          var nextCheckPoint = checkPoint
          //we need to back up our cursor to database after certain 100 tweets.
          //this allows us to reaccess this point on the cursor of the current keyword and date range
          if (totalNext > nextCheckPoint) {
            nextCheckPoint = nextCheckPoint + 100
            this.post(this.serverUrl+"/checkpoint", Map[String, String](
              "_id" -> startDate,
              "count" -> totalNext.toString,
              "cursor"->scrollCursor,
              "checkpoint" -> nextCheckPoint.toString
            ))
          }
          println{
            var temp = startDate + "-" + endDate + ": last response " + count + " tweets, total " + totalNext 
            temp += ", next checkpoint " + nextCheckPoint
            temp
          }
          println("sleep " + sleep_time/1000 + " seconds")
          Thread.sleep(sleep_time)
          //after the sleep, we use the same keyword, date range and new scrollCursor to search for next page
          searchRec(url, startDate, endDate, count, scrollCursor, totalNext, sleep_time, nextCheckPoint)
        } else
          (false, total, cursor)
      } else
        (true, total, cursor)
    }
    //get job from our server 
    val job = this.get(this.serverUrl+"/job")
    val startDate = job("_id").asInstanceOf[String]
    //if our server returns complete, there is no more job left
    if (startDate != "complete") {
      //job contains startDate endDate and cursor to start
      //if it's a new job, the cursor will be empty otherwise, it's the backuped scroll cursor.
      val endDate = job("end_date").asInstanceOf[String]
      val cursor = job("cursor").asInstanceOf[String]
      val total = job("count").asInstanceOf[Double].toInt
      val checkpoint = job("checkpoint").asInstanceOf[Double].toInt
      val url = this.create_url(keyword, startDate, endDate)
      val temp = searchRec(url, startDate, endDate, 1000, cursor, total, this.tweeterSleepTime, checkpoint)
      val m = Map[String, String](
        "_id" -> startDate,
        "count" -> temp._2.toString,
        "cursor" -> temp._3
      )
      //if we successfully retrieve all the tweets for one job, we mark this job is done
      //otherwise, we save the progress and try it later.
      if (temp._1) this.post(this.serverUrl+"/done", m)
      else this.post(this.serverUrl+"/save", m)
      searchHandler(keyword)
    }
  }
  
}