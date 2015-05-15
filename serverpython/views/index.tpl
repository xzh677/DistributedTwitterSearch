<!DOCTYPE html>

<html>
	<head>
		<title>
			Tweets
		</title>
		<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css">
		<script src="https://code.jquery.com/jquery-1.11.3.min.js"></script>
		<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js"></script>
		<script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/1.0.2/Chart.min.js"></script>
		<script>
			var data = {
			    labels: [
					%for x in c_status["acur"]:
	            		'{{x["_id"]["year"]}} {{x["_id"]["month"]}}',
	            	%end
			    ],
			    datasets: [
			        {
			            label: "Tweets",
			            fillColor: "rgba(151,187,205,0.2)",
			            strokeColor: "rgba(151,187,205,1)",
			            pointColor: "rgba(151,187,205,1)",
			            pointStrokeColor: "#fff",
			            pointHighlightFill: "#fff",
			            pointHighlightStroke: "rgba(151,187,205,1)",
			            data: [
			            	%for x in c_status["acur"]:
			            		{{x["total"]}},
			            	%end
			            ]
			        }
			    ]
			};
			function copyToClipboard() {
			  window.prompt("Copy to clipboard: Ctrl+C, Enter", "java -jar twitter.jar");
			}
		</script>
	</head>
	<body>
		<div class="container">
			<div class="row">
				<p><h1>Progress</h1></p>
				<table class="table">
					<tr>
						<th>Remaining (Day)</th>
						<th>Processing (Day)</th>
						<th>Complete (Day)</th>
						<th>Total (Day)</th>
					</tr>
					<tr>
						<td>{{status["remain"]}}</td>
						<td>{{status["process"]}}</td>
						<td>{{status["complete"]}}</td>
						<td>{{status["total"]}}</td>
					</tr>
				</table>
				<h1>Completed</h1>
				<table class="table">
					<tr>
						<th>Total Days</th>
						<th>Ttotal Tweets</th>
						<th>Max Number Per Day</th>
						<th>Min Nmber Per Day</th>
						<th>Avg Tweets Per Day</th>
					</tr>
					<tr>
						<td>{{c_status["count"]}}</td>
						<td>{{c_status["total"]}}</td>
						<td>{{c_status["max"]}}</td>
						<td>{{c_status["min"]}}</td>
						<td>{{c_status["avg"]}}</td>
					</tr>
				</table>
			</div>
			<br />
			<br />
			<div class="row">
				<div class="text-center">
					<canvas id="tweets" width="1170" height="400"></canvas>
				</div>
				<script>
				    var tweets = document.getElementById('tweets').getContext('2d');
				    new Chart(tweets).Line(data);
				</script>
			</div>
			<br />
			<br />
			<div class="row">
				<div class="text-center">
					<h3><b><a href="static/client.jar">Download the client</a></b> and speed up the crawling. </h3>
				</div>
				<div class="text-center">
					You need to have <b><a href="https://java.com/en/download/">JAVA</a></b> installed. Run <a onclick="copyToClipboard()">java -jar twitter.jar</a> in command line or terminal.
				</div>
			</div>
			<br />
			<br />
			<div class="row">
				<p><h1>Assigned Incomplete Jobs</h1></p>
				<table class="table">
					<tr>
						<th>Client</th>
						<th>Start At</th>
						<th>Job</th>
					</tr>
					%for x in i_status:
					<tr>
						<td>{{x["client"]}}</td>
						<td>{{x["start_at"]}}</td>
						<td>{{x["_id"]}} to {{x["end_date"]}}</td>
					</tr>
					%end
				</table>
			</div>
			<br />
			<br />
		</div>
	</body>
</html>