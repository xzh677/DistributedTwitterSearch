package me.xinzhang.scala.twittersearch



import scala.annotation.tailrec

case class TwitterSearch(
            val serverIP: String = "192.168.100.1",
            val serverPort: String = "8080",
            val numOfRetries: Int = 10,
            val sleepTime: Int = 1000
          ) {

  def getWithRetry(url:String) = (new GetWithRetry(url, numOfRetries, "twitter")).connect(sleepTime)
  val serverConnector = ServerConnector(serverIP, serverPort)

  //the wrapper function of searchHandler
  //searchHandler needs to be tail recursion to optimize the memory usage
  //thus it needs to be private
  def search(keyword: String): Unit  = {
    searchHandler(keyword)
    println("Completed!")
  }

  //for a given keyword, requesting an uncompleted job from server
  @tailrec
  private def searchHandler(keyword: String): Unit = {

    def getURL(startDate:String, endDate: String) =
      (new TwitterURL(keyword)).create(startDate, endDate)

    val job = serverConnector.job()
    if (!job.isCompleted) {

      val res = searchThroughCursor(
        getURL(job.startDate, job.endDate),
        job.startDate,
        job.endDate,
        1000,// 1000 is the initial search condition, assume there are 1000 tweets available
        job.cursor,
        job.count,
        sleepTime,
        job.checkPoint)
      val msg = SaveMessage(job.startDate, res._2, res._3)
      //if we successfully retrieve all the tweets for one job, we mark this job is done
      //otherwise, we save the progress and try it later.
      if (res._1) serverConnector.complete(msg)
      else serverConnector.save(msg)
      searchHandler(keyword)
    }
  }


  //this function go through a cursor from twitter for particular keyword within a time period
  @tailrec
  private def searchThroughCursor(url: String, startDate:String, endDate:String, lastCount:Int, cursor:String,
             total:Int, sleepTime:Long, checkPoint:Int):(Boolean, String, Int) = {
    //lastCount indicates is there anymore tweets
    //if last time we get an empty list from twitter,
    // which means there is is no more tweets.
    if (lastCount > 0) {
      val data = getWithRetry(url + cursor)
      //empty data means we didn't get any reply from twitter,
      // it usually is due to the network failure.
      if (!data.isEmpty) {
        val html = data("items_html").asInstanceOf[String]
        val cursor = data("min_position").asInstanceOf[String]
        val listOfTweets = TwitterHTMLParser(html).tweets()
        listOfTweets.map(serverConnector.update(_))
        val count = listOfTweets.length
        val totalNext = total + count
        var nextCheckPoint = checkPoint
        //we need to back up our cursor to database after certain 100 tweets.
        //this allows us to reaccess this point on the cursor of the current keyword and date range
        if (totalNext > nextCheckPoint) {
          nextCheckPoint = nextCheckPoint + 100
          val msg = CheckPointMessage(startDate, cursor, totalNext,  nextCheckPoint)
          serverConnector.checkpoint(msg)
        }
        Thread.sleep(sleepTime)
        searchThroughCursor(url, startDate, endDate, count, cursor, totalNext, sleepTime, nextCheckPoint)
      } else
        (false, cursor, total)
    } else
      (true,  cursor, total)
  }
}
