<%@page import="org.botlibre.web.analytic.AnalyticMedia"%>
<%@page import="org.botlibre.web.analytic.AnalyticLabel"%>
<%@page import="org.botlibre.web.analytic.AnalyticBean"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>

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
	<title>Training Network<%= embed ? "" : " - " + Site.NAME %></title>
	<meta name="description" content="Start training your network"/>
	<meta name="keywords" content="analytic, network, training, inception"/>
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
					<%= loginBean.translate("The training page lets you regenerate a deep learning network trained to classify your images from your media repository.") %><br/>
					<%= loginBean.translate("Click 'Start' to train your network from its media repository.") %><br/>
					<%= loginBean.translate("Depending on the size of the network training may take up to several hours.") %><br/>
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
				<img src="images/train2.png" class="admin-banner-pic">
				<div>
					<p class="help">
						<%= loginBean.translate("Click 'Start' to train your network from its media repository.") %><br/>
						<%= loginBean.translate("Depending on the size of the network training may take up to several hours.") %><br/>
					</p>
				</div>
			</span> <%= loginBean.translate("Train Network") %>
		</h1>
		<script>
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
				<% if (bean.getInstance().getAnalyticType().equals(bean.OBJECT_DETECTION)){ %>
					SDK.showError("You can't train for object detection, it's not supported.","Object Detection");
					return;
				<%}%>
				document.getElementById('btn-train').disabled = true;
				document.getElementById("busy").style.display = "inline-block";
				document.getElementById("trainingStatus").innerHTML = "<b><%= loginBean.translate("Training Status: ")%></b><span id='status'>started...</span>"
				<% if (loginBean.isSuper()) { %>
					var isChecked = document.getElementById("force").checked;
					if (isChecked) {
						analyticConfig.trainingStatus = "force";
					}
				<% } %>
				classifierSDK.trainAnalytic(analyticConfig, function(response){
					document.getElementById("analyticType").innerHTML = "<b><%= loginBean.translate("Analytic Type: ")%></b>" + response.analyticType;
					<% if (bean.getInstance().getAnalyticType().contains("conv") || bean.getInstance().getAnalyticType().contains("low")){%>
						document.getElementById("analyticFeed").innerHTML = "<b><%= loginBean.translate("Analytic Audio Input: ")%></b>" + response.audioInputName;
						document.getElementById("analyticFetch").innerHTML = "<b><%= loginBean.translate("Analytic Audio Output: ")%></b>" + response.audioOutputName;
					<%} else {%>
						document.getElementById("analyticFeed").innerHTML = "<b><%= loginBean.translate("Analytic Feed: ")%></b>" + response.analyticFeed;
						document.getElementById("analyticFetch").innerHTML = "<b><%= loginBean.translate("Analytic Fetch: ")%></b>" + response.analyticFetch;
					<%}%>
					document.getElementById("trainingStatus").innerHTML = "<b><%= loginBean.translate("Training Status: ")%></b><span id='status'>" + response.trainingStatus;"</span>"
					statusOfTraining = response.isTraining;
				});
				refreshAnalytic();
				intvId = window.setInterval(function(){
					refreshAnalytic();
				}, TIMER);
			}
			
			function refreshAnalytic() {
				classifierSDK.checkTraining(analyticConfig, function(response) {
					document.getElementById("analyticType").innerHTML = "<b><%= loginBean.translate("Analytic Type: ")%></b>" + response.analyticType;
					<% if (bean.getInstance().getAnalyticType().contains("conv") || bean.getInstance().getAnalyticType().contains("low")){%>
						document.getElementById("analyticFeed").innerHTML = "<b><%= loginBean.translate("Analytic Audio Input: ")%></b>" + response.audioInputName;
						document.getElementById("analyticFetch").innerHTML = "<b><%= loginBean.translate("Analytic Audio Output: ")%></b>" + response.audioOutputName;
					<%} else {%>
						document.getElementById("analyticFeed").innerHTML = "<b><%= loginBean.translate("Analytic Feed: ")%></b>" + response.analyticFeed;
						document.getElementById("analyticFetch").innerHTML = "<b><%= loginBean.translate("Analytic Fetch: ")%></b>" + response.analyticFetch;
					<%}%>
					document.getElementById("trainingStatus").innerHTML = "<b><%= loginBean.translate("Training Status: ")%></b><span id='status'>" + response.trainingStatus;"</span>"
					statusOfTraining = response.isTraining;
				});
				count = count +1;
				if (statusOfTraining == false || statusOfTraining == "false") {
					document.getElementById("busy").style.display = "none";
					window.clearInterval(intvId);
				}
				if (count > 99) {
					document.getElementById("busy").style.display = "none";
					document.getElementById("analyticStatus").innerHTML = "Refresh page to check status";
					window.clearInterval(intvId);
				}
			}
		</script>
		
		<h2><%= loginBean.translate("Train Network: ") + bean.getDisplayInstance().getNameHTML() %></h2>
		<form class="message">
			<table>
				<tr>
					<td><input id="btn-train" class="ok" onClick="trainingNetwork(); return false;" type="submit" value="<%= loginBean.translate("Start") %>"></td>
					<td>
						<% if (loginBean.isSuper()) { %>
							<div>
								<input type="checkbox" id="force">
								<span for="force"><b>Force training</b></span>
			 				</div>
						<% } %>
					</td>
				</tr>
			</table>
		</form>
		<table>
			<tr>
				<th rowspan="6"><div id="busy" class="loader"></div></th>
			</tr>
			<tr>
				<td id="analyticType"></td>
			</tr>
			<tr>
				<td id="analyticFeed"></td>
			</tr>
			<tr>
				<td id="analyticFetch"></td>
			</tr>
			<tr>
				<td id="isTraining"></td>
			</tr>
			<tr>
				<td id="trainingStatus"></td>
			</tr>
			<tr>
				<td colspan="2"><p style="color: red;" id="error-message"></p></td>
			</tr>
		</table>
		
		<script>
			//if the page was refreshed.
			statusOfTraining = <%= bean.getInstance().isTraining() %>;
			if (statusOfTraining) {
				document.getElementById('btn-train').disabled = true;
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

