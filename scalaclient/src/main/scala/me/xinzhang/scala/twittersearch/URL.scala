package me.xinzhang.scala.twittersearch

trait URL {

  def create(start: String, end: String): String

}


case class TwitterURL(keyword: String) extends URL {

  //create url in following pattern
  //ttps://twitter.com/i/search/timeline?vertical=default&q=KEYWORD since:1970-01-01 until:1970-01-02
  // &l=en&src=typd&max_position=

  def create(start: String, end: String) : String = {

    var url = "https://twitter.com/i/search/timeline?vertical=default&q="
    url = url + keyword.replace(" ", "%20")
    url = url +  (start match {
      case x: String if x != "" => "%20since%3A" + x
      case _ => ""
    })
    url = url + (end match {
      case x: String if x != "" => "%20until%3A" + x
      case _ => ""
    })
    url = url + "&l=en&src=typd"

    url = url + "&max_position="
    url
  }
}


