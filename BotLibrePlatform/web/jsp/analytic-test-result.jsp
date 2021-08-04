<%@page import="org.botlibre.web.analytic.AnalyticTestMedia"%>
<%@page import="org.botlibre.web.analytic.AnalyticTestMediaLabel"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page"%>
<%@page import="org.botlibre.web.analytic.AnalyticBean"%>
<%@page import="java.util.List"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.eclipse.persistence.internal.helper.Helper"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	AnalyticBean bean = loginBean.getBean(AnalyticBean.class);
	bean.refreshEntity();
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Testing Network<%= embed ? "" : " - " + Site.NAME %></title>
	<meta name="description" content="Start testing your network"/>
	<meta name="keywords" content="analytic, network, testing, inception"/>
	<%= loginBean.getJQueryHeader() %>
</head>
<style>

#busy {
	display: none;
	padding: 4px
}
.loader {
  border: 2px solid #f3f3f3;
  border-radius: 50%;
  border-top: 2px solid #3498db;
  width: 24px;
  height: 24px;
  -webkit-animation: spin 0.4s linear infinite; /* Safari */
  animation: spin 0.4s linear infinite;
  vertical-align: middle;
}

table#test-media-report-table td, table#test-media-report-table th{
  border: 1px solid black;
  border-collapse: collapse;
  width: 100%;
  text-align: left;
}

table#test-media-report-table-0 td, table#test-media-report-table-0 th{
  border: 1px solid black;
  border-collapse: collapse;
  width: 100%;
  text-align: left;
}
/* Safari */
@-webkit-keyframes spin {
	0% { -webkit-transform: rotate(0deg); }
	100% { -webkit-transform: rotate(360deg); }
}

@keyframes spin {
	0% { transform: rotate(0deg); }
	100% { transform: rotate(360deg); }
}
</style>
<% if (embed) { %>
	<body style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<% loginBean.embedHTML(loginBean.getBannerURL(), out); %>
	<jsp:include page="analytic-banner.jsp"/>
	<div id="embedbody" style="background-color: <%= loginBean.getBackgroundColor() %>;">
<% } else { %>
	<body>
	<% loginBean.setPageType(Page.Admin); %>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-analytic-banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("The testing page lets you test your network accuracy.") %><br/>
					<%= loginBean.translate("Click 'Start' to test your network from its media repository.") %><br/>
					<%= loginBean.translate("Depending on the size of the network testing may take up to several hours.") %><br/>
				</div>
				<%= loginBean.translate("Help") %>
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-analytic.jsp"><%= loginBean.translate("Docs") %></a>
			 : <a target="_blank" href="https://www.botlibre.com/forum-post?id=22972468"><%= loginBean.translate("How To Guide") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
	<div class="browse">
<% } %>
	<jsp:include page="error.jsp"/>
	<% if (bean.getInstance() == null) { %>
		<%= loginBean.translate("No analytic network selected.") %>
	<% } else if (!bean.isAdmin()) { %>
		<p style="color:#E00000;"><%= loginBean.translate("Must be admin") %></p>
	<% } else { %>
		<h1>
			<span class="dropt">
				<img src="images/train2.png" class="admin-banner-pic" style="vertical-align:middle">
				<div>
					<p class="help">
						<%= loginBean.translate("Click 'Start' to test your network from its media repository.") %><br/>
						<%= loginBean.translate("Depending on the size of the network testing may take up to several hours.") %><br/>
					</p>
				</div>
			</span> <%= loginBean.translate("Test Network") %>
		</h1>
		<script>
			//SDK.debug = true;
			var statusOfTraining;
			var intvId;
			var count = 0;
			var TIMER = 30000;
			SDK.applicationId = "<%= AdminDatabase.getTemporaryApplicationId() %>";
			var classifierSDK = new SDKConnection();
			var analyticConfig = new AnalyticConfig();
			analyticConfig.id = "<%=bean.getInstanceId()%>";
			var euser = new UserConfig();
			<% if (loginBean.isLoggedIn()) { %>
				euser.user = "<%= loginBean.getUser().getUserId() %>";
				euser.token = "<%= loginBean.getUser().getToken() %>";
				classifierSDK.user = euser;
			<% } %>
			classifierSDK.error = function(error) {
				SDK.showError(error,"Error");
				document.getElementById("busy").style.display = "none";
			}
			
			function trainingNetwork() {
				document.getElementById('btn-train').disabled = true;
				document.getElementById("busy").style.display = "inline-block";
				document.getElementById("trainingStatus").innerHTML = "<b><%= loginBean.translate("Testing Status: ")%></b><span id='status'>started...</span>"
				<% if (loginBean.isSuper()) { %>
					var isChecked = document.getElementById("force").checked;
					if (isChecked) {
						analyticConfig.processingTestMediaStatus = "force";
					}
				<% } %>
				classifierSDK.reportMediaAnalytic(analyticConfig, function(response){
					<%--
					document.getElementById("analyticType").innerHTML = "<b><%= loginBean.translate("Analytic Type: ")%></b>" + response.analyticType;
					<% if (bean.getInstance().getAnalyticType().contains("conv") || bean.getInstance().getAnalyticType().contains("low")){%>
						document.getElementById("analyticFeed").innerHTML = "<b><%= loginBean.translate("Analytic Audio Input: ")%></b>" + response.audioInputName;
						document.getElementById("analyticFetch").innerHTML = "<b><%= loginBean.translate("Analytic Audio Output: ")%></b>" + response.audioOutputName;
					<%} else {%>
						document.getElementById("analyticFeed").innerHTML = "<b><%= loginBean.translate("Analytic Feed: ")%></b>" + response.analyticFeed;
						document.getElementById("analyticFetch").innerHTML = "<b><%= loginBean.translate("Analytic Fetch: ")%></b>" + response.analyticFetch;
					<%}%>
					--%>
					document.getElementById("trainingStatus").innerHTML = "<b><%= loginBean.translate("Processing Status: ")%></b><span id='status'>" + response.processingTestMediaStatus;"</span>"
					statusOfTraining = response.isProcessingMedia;
					refreshAnalytic();
				});
				refreshAnalytic();
				intvId = window.setInterval(function(){
					refreshAnalytic();
				}, TIMER);
			}
			
			//this is used to check status of the test media report
			function refreshAnalytic() {
				classifierSDK.checkTraining(analyticConfig, function(response) {
					<%--
					document.getElementById("analyticType").innerHTML = "<b><%= loginBean.translate("Analytic Type: ")%></b>" + response.analyticType;
					<% if (bean.getInstance().getAnalyticType().contains("conv") || bean.getInstance().getAnalyticType().contains("low")){%>
						document.getElementById("analyticFeed").innerHTML = "<b><%= loginBean.translate("Analytic Audio Input: ")%></b>" + response.audioInputName;
						document.getElementById("analyticFetch").innerHTML = "<b><%= loginBean.translate("Analytic Audio Output: ")%></b>" + response.audioOutputName;
					<%} else {%>
						document.getElementById("analyticFeed").innerHTML = "<b><%= loginBean.translate("Analytic Feed: ")%></b>" + response.analyticFeed;
						document.getElementById("analyticFetch").innerHTML = "<b><%= loginBean.translate("Analytic Fetch: ")%></b>" + response.analyticFetch;
					<%}%>
					--%>
					document.getElementById("trainingStatus").innerHTML = "<b><%= loginBean.translate("Testing Status: ")%></b><span id='status'>" + response.processingTestMediaStatus;"</span>"
					statusOfTraining = response.isProcessingMedia;
					if (statusOfTraining == "false") {
						document.getElementById("busy").style.display = "none";
						window.clearInterval(intvId);
						location.reload();
					}
				});
				count = count +1;
				if (count > 99) {
					document.getElementById("busy").style.display = "none";
					document.getElementById("analyticStatus").innerHTML = "Refresh page to check status";
					window.clearInterval(intvId);
				}
			}
		</script>
		
		<h2><%= loginBean.translate("Test Network: ") + bean.getDisplayInstance().getNameHTML() %></h2>
		<form class="message">
			<table>
				<tr>
					<td><input id="btn-train" class="ok" onClick="trainingNetwork(); return false;" type="submit" value="<%= loginBean.translate("Start") %>"></td>
					<td>
						<% if (loginBean.isSuper()) { %>
							<div>
								<input type="checkbox" id="force">
								<span for="force"><b>Force testing</b></span>
			 				</div>
						<% } %>
					</td>
				</tr>
			</table>
		</form>
		<table>
			<%--
			<tr>
				<td id="analyticType"></td>
			</tr>
			<tr>
				<td id="analyticFeed"></td>
			</tr>
			<tr>
				<td id="analyticFetch"></td>
			</tr>
			--%>
			<tr>
				<td id="isTraining"></td>
			</tr>
			<tr>
				<td><div id="busy" class="loader"></div></td>
				<td id="trainingStatus"></td>
			</tr>
			<tr>
				<td colspan="2"><p style="color: red;" id="error-message"></p></td>
			</tr>
		</table>
		<hr>
		<% boolean result = (bean.getInstance().getTestMediaResult() != null); %>
		<% if (result) {%>
			<script>
			classifierSDK.getTestMediaResult(analyticConfig, function(response) {
				let totalConfidenceSum = 0;
				let totalAccurateSum = 0;
				let totalCount = 0;

				let resultsByLabel = {};

				document.getElementById("start-time").innerHTML = response.startTime;
				document.getElementById("end-time").innerHTML = response.endTime;
				document.getElementById("elapsed-time").innerHTML = response.elapsedTime + "s";
				
				const table = document.getElementById("test-media-report-table");
				
				for (let res of response.listOfResponses) { 
					const row = table.insertRow(-1);
					const filename = row.insertCell(0);
					// const rawImage = row.insertCell(1);
					const actualLabel = row.insertCell(1);
					const actualConfidence = row.insertCell(2);
					const expectedLabel = row.insertCell(3);
					const expectedConfidence = row.insertCell(4);
					const accuracy = row.insertCell(5);
					const time = row.insertCell(6);

					// rawImage.innerHTML = "<a href='data:image/png;base64,"+ response.listOfResponses[e].image +"'><img src='data:image/png;base64," +response.listOfResponses[e].image +"' width='50px' height='50px'></a>";
					
					filename.innerHTML = res.name;
					
					actualLabel.innerHTML = res.actualLabel;

					if (res.actualConfidence != null && res.actualConfidence !== ""){
						actualConfidence.innerHTML = (parseFloat(res.actualConfidence) * 100).toFixed(1) + "%";
					}

					expectedLabel.innerHTML = res.expectedLabel;

					if (res.expectedConfidence != null){
						expectedConfidence.innerHTML = (parseFloat(res.expectedConfidence) * 100).toFixed(1) + "%";
					}
					

					if (res.expectedLabel === res.actualLabel && res.expectedConfidence != null && res.expectedConfidence != 0) {
						accuracy.innerHTML = "Passed";
					}
					else {
						accuracy.innerHTML = "Failed";
					}

					time.innerHTML = parseFloat(res.testTime).toFixed(2);

					// initialize stats for a new label
					if (!(res.expectedLabel in resultsByLabel)) {
						resultsByLabel[res.expectedLabel] = {
							confidenceSum: 0,
							accurateSum: 0,
							count: 0
						};
					}

					// update stats for a label
					if (res.expectedConfidence != null && res.expectedConfidence != 0) {
						totalConfidenceSum += parseFloat(res.expectedConfidence);
						resultsByLabel[res.expectedLabel].confidenceSum += parseFloat(res.expectedConfidence);
						if (res.expectedLabel === res.actualLabel) {
							totalAccurateSum += 1;
							resultsByLabel[res.expectedLabel].accurateSum += 1;
						}
					}
					totalCount += 1;
					resultsByLabel[res.expectedLabel].count += 1;
				}

				printAverageTable(resultsByLabel, totalConfidenceSum, totalAccurateSum, totalCount);
			});
			
			function printAverageTable(resultsByLabel, totalConfidenceSum, totalAccurateSum, totalCount) {
				const table = document.getElementById("test-media-report-table-0");
				const row = table.insertRow(-1);
				const total = row.insertCell(0);
				const totalCountCell = row.insertCell(1);
				const totalConfidence = row.insertCell(2);
				const totalAccuracy = row.insertCell(3);

				total.innerHTML = "Total";
				totalCountCell.innerHTML = totalCount.toString();
				totalConfidence.innerHTML = (totalConfidenceSum / totalCount * 100).toFixed(1) + "%";
				totalAccuracy.innerHTML = (totalAccurateSum / totalCount * 100).toFixed(1) + "%";

				for (let label in resultsByLabel){
					let row = table.insertRow(-1);
					let labelCell = row.insertCell(0);
					let count = row.insertCell(1);
					let confidence = row.insertCell(2);
					let accuracy = row.insertCell(3);

					labelCell.innerHTML = label;
					count.innerHTML = resultsByLabel[label].count.toString();
					confidence.innerHTML = (resultsByLabel[label].confidenceSum / resultsByLabel[label].count * 100).toFixed(1) + "%";
					accuracy.innerHTML = (resultsByLabel[label].accurateSum / resultsByLabel[label].count * 100).toFixed(1) + "%";
				}
			}
			</script>
			
			<h3>Last Test Result: <%= bean.getDisplayInstance().getNameHTML() %></h3>
			Start time: <span id="start-time"></span>
			<br/>
			End time: &nbsp;<span id="end-time"></span>
			<br/>
			Elapsed time: <span id="elapsed-time"></span>
			<br/><br/>
			
			<table id="test-media-report-table-0" style="border-collapse: collapse;">
				<tr>
					<th>Label</th>
					<th>Results</th>
					<th>Confidence</th>
					<th>Accuracy</th>
				</tr>
			</table>
			<hr>
			<table id="test-media-report-table" style="border-collapse: collapse;">
				<tr>
					<th>Name</th>
					<!--  <th>Image</th> -->
					<th>Actual Label</th>
					<th>Actual Confidence</th>
					<th>Expected Label</th>
					<th>Expected Confidence</th>
					<th>Accuracy</th>
					<th>Test Time (s)</th>
					
				</tr>
			</table>
		<% } else { %>
			<p>No test data.</p>
		<% } %>
		
		<script>
			//if the page was refreshed.
			statusOfTraining = <%= bean.getInstance().isProcessingTestMedia() %>;
			if (statusOfTraining) {
				//document.getElementById('btn-train').disabled = true;
				document.getElementById("busy").style.display = "inline-block";
				refreshAnalytic();
				intvId = window.setInterval(function(){
					refreshAnalytic();
				}, TIMER);
			} else {
				document.getElementById('btn-train').disabled = false;
				document.getElementById("busy").style.display = "none";
			}
		</script>
	<% } %>
	</div>
<% if (!embed) { %>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
<% } %>
<% proxy.clear(); %>
</body>
</html>

