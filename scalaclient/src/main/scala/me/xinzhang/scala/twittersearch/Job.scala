package me.xinzhang.scala.twittersearch



//job contains startDate endDate and cursor to start
//if it's a new job, the cursor will be empty otherwise,
// it is a backup cursor.
case class Job(
         val startDate: String,
         val endDate: String,
         val cursor: String,
         val count: Int,
         val checkPoint: Int
         ) {
  val isCompleted = startDate == "complete"
}

