from bottle import post, request, route, run, template, static_file
from pymongo import MongoClient
from datetime import datetime
from bson.son import SON
import json
import os

client = MongoClient("mongodb://localhost:27017/")
db = client["twitter"]
tweets_db = db["tweets"]
job_db = db["job"]


@route('/job')
def callback():
	data = job_db.find_one({
		"status":1,
		"client":request.remote_addr
	})
	if data is None:
		data = job_db.find_one({"status":0})
		if data is not None:
			job_db.update_one({
				"_id":data["_id"]
			},{
				"$set":{
					"status":1,
					"client":request.remote_addr,
					"start_at":datetime.now().strftime("%Y-%m-%d %H:%M:%S")
				}
			})
	if data is None:
		data = job_db.find_one({"status":4})
	if data is None:
		data = {"_id":"complete"}
	return data

@post('/done')
def callback():
	_id = request.forms.get('_id')
	count = int(request.forms.get('count'))
	job_db.update_one({
		"_id":_id
	}, {
		"$set":{
			"status":2,
			"count":count, 
			"last_visited": datetime.now().strftime("%Y-%m-%d %H:%M:%S")
	}})
	return {"success":True}

@post('/save')
def callback():
	_id = request.forms.get('_id')
	count = int(request.forms.get('count'))
	cusor = request.forms.get('cursor')
	job_db.update_one({
		"_id":_id
	}, {
		"$set":{
			"status":4,
			"count":count,
			"cursor":cursor,
			"last_visited": datetime.now().strftime("%Y-%m-%d %H:%M:%S")
	}})
	return {"success":True}

@post('/checkpoint')
def callback():
	_id = request.forms.get('_id')
	count = int(request.forms.get('count'))
	checkpoint = int(request.forms.get('checkpoint'))
	cursor = request.forms.get('cursor')
	job_db.update_one({
		"_id":_id
	}, {
		"$set":{
			"count":count,
			"cursor":cursor,
			"checkpoint":checkpoint,
			"last_visited": datetime.now().strftime("%Y-%m-%d %H:%M:%S")
	}})
	return {"success":True}

@post('/update')
def callback():
	_id = request.forms.get('_id')
	user_id = request.forms.get('user_id')
	user_name = request.forms.get('user_name')
	user_screen_name = request.forms.get('user_screen_name')
	permalink = request.forms.get('permalink')
	create_at = request.forms.get('create_at')
	tweet = request.forms.get('tweet')

	data = {"success":True}
	try:
		tweets_db.insert_one({
			"_id" : _id,
			"user_id" : user_id,
			"user_name" : user_name,
			"user_screen_name" : user_screen_name,
			"permalink" : permalink,
			"create_at" : create_at,
			"tweet" : tweet
		})
	except:
		data = {"success":False}
		
	return data

@route('/')
def callback():
	i_status = job_db.find({"status":1})
	total_job = job_db.count()
	process = job_db.count({"status":1})
	remain = job_db.count({"status":0})
	complete = total_job - process - remain
	cur = job_db.find({"status":{"$gt":1}})
	total = 0
	i = 0
	max_count = 0
	min_count = 10000
	for x in cur:
		count = int(x["count"])
		total = total + count
		i = i + 1
		if count > max_count:
			max_count = count
		if count < min_count:
			min_count = count
	if i > 0:
		avg = total / i
	else:
		avg = 0

	if cur is not None:
		 acur = job_db.aggregate([
		 	{"$group":{
		 		"_id":{
		 			"year":"$year",
		 			"month":"$month"
		 		}, 
		 		"total":{
		 			"$sum":"$count"
		 		}
		 	}},
		 	{"$sort": SON([("_id.year", 1), ("_id.month", 1)])}
		 ])

	a_data = []
	for x in acur:
		a_data.append(x)

	return template("index", i_status = i_status, c_status = {
		"total": total,
		"count": i,
		"max": max_count,
		"min": min_count,
		"avg": avg,
		"acur": a_data
	}, status = {
		"total":total_job,
		"process":process,
		"complete":complete,
		"remain":remain
	})


@route("/static/<filename>")
def server_static(filename):
	return static_file(filename, root=os.getcwd()+"/static")

run(host="0.0.0.0", port=8080, quiet=True)