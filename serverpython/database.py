from pymongo import MongoClient
import datetime

# Initial Database 
# Setup all the jobs in MongoDB
# Server will retrieve jobs from MongoDB and distribute to Clients
# Job is split based on date, one day is one job
# The job is a date range with some meta data
# _id : 2015-05-10, end_date : 2015-05-11
# this is one job. It asks Client to search from 10th of May to 11th of May

class DatabaseHandler():

	def __init__(self):
		self.client = MongoClient("mongodb://localhost:27017/")
		self.db = self.client["twitter"]
		self.tweets = self.db["tweets"]
		self.job = self.db["job"]

	def initial_database(self, days_ago):
		self.tweets.remove({})
		self.job.remove({})
		self.create_jobs(days_ago)

	def create_jobs(self, days_ago):
		today = datetime.date.today() 
		days_ago = days_ago

		for i in range(1, days_ago):
			date1 = str(today - datetime.timedelta(i))
			date2 = str(today - datetime.timedelta(i-1))
			
			key = date1
			year = int(key[:4])
			month = int(key[5:7])
			date = int(key[8:])
			try:
				self.job.insert_one({
					"_id":date1,
					"year":year,
					"month":month,
					"date":date,
					"end_date":date2,
					"status":0,
					"count":0,
					"checkpoint":100,
					"cursor":""
				})
			except:
				pass

	def close(self):
		self.client.close()


dh = DatabaseHandler()
dh.initial_database(860) #860 day from 2015-05-10 is 2013-01-01
