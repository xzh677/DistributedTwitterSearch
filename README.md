# DistributedTwitterSearch
Searching and storing tweets from twitter without using the official APIs. This project is implemented in a RESTful Client/Server way. We can run multiple instance of clients on different machines and update to the server. The server will automatically schedule the jobs among clients.

All these codes are free to use and modify. 

Server side requirements:
	python is required
	python bottle is required
	python pymongo 3.0+ is required
	mongodb 3.0+ is required


Client side requirements:
	JRE is required


Java Build Path Libraries
jsoup-1.8.2.jar needs to add from lib folder
scala-parser-commbinators_2.11-1.0.3.jar needs to add from lib in your scala root folder


Read database.py before you start anything.

Start Server: 
python database.py  //ONLY run at the FIRST time to initial database. 
python server.py

Start Client:
java -jar client.jar 
java -jar client.jar -ip 10.0.0.2

where you can hardcode the default ip or set it manually by command -ip

Once you started, you can check the status on http://localhost:8080/


File Structure
- clientscala
- 	lib // contains jsoup-1.8.2.jar 
- 	EVERYTHING ELSE //standard scala sbt project folder
- serverpython
- 	views
- 		index.tpl   //index template for server
- 	static
- 		client.jar  
- 	database.py
- 	server.py
- README.md


Http.scala and OAuth.scala are from https://github.com/scalaj/scalaj-http


Creating jar in scala, see https://github.com/sbt/sbt-assembly
