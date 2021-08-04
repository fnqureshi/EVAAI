<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.web.admin.Domain"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.analytic.AnalyticBean"%>
<%@page import="java.util.List"%>
<%@page import="org.botlibre.web.Site"%>
<%@page contentType="text/html; charset=UTF-8" %>
<%@page import="org.eclipse.persistence.internal.helper.Helper"%>


<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<%
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	AnalyticBean bean = loginBean.getBean(AnalyticBean.class);
	String title = "Analytic";
	if (bean.getDisplayInstance() != null) {
		title = bean.getDisplayInstance().getName();
	}
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= title %><%= embed ? "" : " - " + Site.NAME %></title>
	<meta name="description" content="image recognition"/>
	<meta name="keywords" content="image, recognition, classification, classifier"/>
	<%= loginBean.getJQueryHeader() %>
</head>
<style>

#busy {
	position: relative; /* or absolute */
	left: 3%;
	display: none;
}
#busy-text{
	vertical-align: super;
	position: relative; /* or absolute */
	left: 4%;
	display: none;
}
#list-ul {
	list-style-type: none;
}
.loader {
  border: 3px solid grey;
  border-radius: 50%;
  border-top: 3px solid lightblue;
  width: 30px;
  height: 30px;
  -webkit-animation: spin 0.5s linear infinite; /* Safari */
  animation: spin 0.5s linear infinite;
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
	<jsp:include page="banner.jsp"/>
	<% bean.browseBannerHTML(out, proxy); %>
	<div id="mainbody">
	<div id="contents">
	<div class="browse">
<% } %>
<% boolean error = loginBean.getError() != null; %>
<jsp:include page="error.jsp"/>
	<% if (bean.getDisplayInstance() == null) { %>
		<%= loginBean.translate("No analytic selected") %>
	<% } else if (!bean.isValidUser()) { %>
		<%= loginBean.translate("This user does not have access to this analytic.") %>
	<% } else { %>
		<!--  h1> //bean.getDisplayInstance().getNameHTML() </h1-->
				<script>
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
					SDK.showError(error,"Server Error");
					document.getElementById("busy").style.display = "none";
					document.getElementById("busy-text").style.display = "none";
				}
				var divTestAnalytic;
				var divTestObject;
				function uploadImage(input){
					document.getElementById("busy").style.display = "inline-block";
					document.getElementById("busy-text").style.display = "inline-block";
					//clear old images
					try{
						divTestAnalytic = document.getElementsByClassName("test-analytic-image")[0];
						divTestObject = document.getElementsByClassName("test-object-image")[0];
						divTestAnalytic.innerHTML = "";
						divTestObject.innerHTML = "";
					}catch(error){}
					<% String analyticType = bean.getInstance().getAnalyticType(); %>
					if (input.files && input.files[0]) {
						<% if (analyticType.equals(bean.OBJECT_DETECTION)) { %>
							submitImageDetection(0,input.files);
						<%} else if(analyticType.contains("mob") || analyticType.contains("inc")){%>
							submitImageRecognition(0,input.files);
						<%} else if(analyticType.contains("conv") || analyticType.contains("low")){%>
							submitAnalyticAudio(input);//only one file at a time.
						<%}%>
					}
				}
				
				function submitImageRecognition(index, files){
					var form = document.getElementById('upload-test-image');
					var newDiv = document.createElement("div");
					newDiv.style.padding = "5px 10px 5px 5px";
					var newImage = document.createElement("img");
					newImage.src = URL.createObjectURL(files[index]);
					var name = files[index].name;
					var type = files[index].type;
					if(!type.includes("image")){
						SDK.showError("<%= loginBean.translate("Please select an image.")%>", "<%= loginBean.translate("Error") %>");
						document.getElementById("busy").style.display = "none";
						document.getElementById("busy-text").style.display = "none";
						return;
					}
					newImage.setAttribute("width", "30%");
					newImage.setAttribute("height", "30%");
					newDiv.appendChild(newImage);
					//create table
					var result = document.createElement("ul");
					result.id = "list-ul";
					result.style.display = "inline-block";
					result.style.verticalAlign = "top";
					classifierSDK.sendAnalyticImage(analyticConfig, function(response) {
						result.innerHTML = 
						"<li><b><%= loginBean.translate("File Name") %>: </b>" + name + "</li>"+ 
						"<li><b><%= loginBean.translate("Image Type") %>: </b>" + type + "</li><hr>";
						for (var i = 0; i < response.labels.length; i++) {
							if (i > 0) {
								result.innerHTML = result.innerHTML +
								"<li style='font-size: 14px;'><b><%= loginBean.translate("Label") %>: </b>" + response.labels[i] + "</li>"+
								"<li style='font-size: 14px;'><b><%= loginBean.translate("Confidence") %> : </b>" + response.confidences[i] + "%"+ "</li>";
							} else {
								result.innerHTML = result.innerHTML +
								"<li style='font-size: 16px;'><b><%= loginBean.translate("Label") %>: </b>" + response.labels[i] + "</li>"+
								"<li style='font-size: 16px;'><b><%= loginBean.translate("Confidence") %> : </b>" + response.confidences[i] + "%"+ "</li><hr>";
							}
						}
						newDiv.appendChild(result);
						divTestAnalytic.appendChild(newDiv);
						divTestAnalytic.appendChild(document.createElement("hr"));
						index++;
						if (index < files.length) {
							var continueUploading = checkUploadedImages(index);
							if (continueUploading) {
								submitImageRecognition(index, files);
							}
						} else {
							document.getElementById("busy").style.display = "none";
							document.getElementById("busy-text").style.display = "none";
						}
					}, files[index],form,600);
				}
				
				function submitImageDetection(index, files) {
					var form = document.getElementById('upload-test-image');
					var newDiv = document.createElement("div");
					newDiv.style.padding = "10px 10px 10px 10px";
					var name = files[index].name;
					var type = files[index].type;
					//check file type
					if (!type.includes("image")) {
						SDK.showError("<%=loginBean.translate("Please select an image.")%>", "<%= loginBean.translate("Error") %>");
						document.getElementById("busy").style.display = "none";
						document.getElementById("busy-text").style.display = "none";
						return;
					}
					//create table
					var result = document.createElement("ul");
					result.id = "list-ul";
					result.style.display = "inline-block";
					result.style.verticalAlign = "top";
					
					var analyticConfigTest = new AnalyticConfigTest();
					analyticConfigTest.instance = "<%= bean.getInstanceId() %>";
					
					classifierSDK.sendObjectDetectionAnalyticImage(analyticConfigTest, function(response) {
						var newImage = document.createElement("img");
						if(response.image){
							newImage.src = 'data:image/png;base64,' + response.image;
							newImage.setAttribute("width", "50%");
							newImage.setAttribute("height", "50%");
							newDiv.appendChild(newImage);
						}
						result.innerHTML = 
						"<li><b><%= loginBean.translate("File Name") %>: </b>" + name + "</li>"+ 
						"<li><b><%= loginBean.translate("Image Type") %>: </b>" + type + "</li>"+
						"<li><b><%= loginBean.translate("Results: ") %></b></li>";
						response.result.forEach(function(element) {
							result.innerHTML = result.innerHTML + "<li>" + element + "</li>";
							});	
						newDiv.appendChild(result);
						divTestAnalytic.appendChild(newDiv);
						divTestAnalytic.appendChild(document.createElement("hr"));
						index++;
						if (index < files.length) {
							var continueUploading = checkUploadedImages(index);
							if (continueUploading) {
								submitImageDetection(index, files);
							}
						} else {
							document.getElementById("busy").style.display = "none";
							document.getElementById("busy-text").style.display = "none";
						}
					}, files[index],form,600);
				}
				
				function submitAnalyticAudio(input) {
					var form = document.getElementById('upload-test-image');
					var newDiv = document.createElement("div");
					newDiv.style.padding = "10px 10px 10px 10px";
					var name = input.files[0].name;
					var type = input.files[0].type;
					if (!type.includes("wav")) {
						SDK.showError("<%= loginBean.translate("Please select an audio file (wav).") %>", "<%= loginBean.translate("Error") %>");
						document.getElementById("busy").style.display = "none";
						document.getElementById("busy-text").style.display = "none";
						return;
					}
					//create table
					var result = document.createElement("ul");
					result.id = "list-ul";
					result.style.display = "inline-block";
					result.style.verticalAlign = "top";
					form.appendChild(input);
					classifierSDK.sendAudioAnalytic(analyticConfig, function(response) {
						var newImage = document.createElement("img");
						newImage.src = 'images/audio.png';
						newImage.setAttribute("width", "50%");
						newImage.setAttribute("height", "50%");
						newDiv.appendChild(newImage);
						result.innerHTML = 
						"<li><b><%= loginBean.translate("File Name") %>: </b>" + name + "</li>"+ 
						"<li><b><%= loginBean.translate("Image Type") %>: </b>" + type + "</li>"+
						"<li><b><%= loginBean.translate("Label") %>: </b>" + response.label + "</li>"+
						"<li><b><%= loginBean.translate("Confidence") %> : </b>" + response.confidence + "%"+ "</li>";
						newDiv.appendChild(result);
						divTestAnalytic.appendChild(newDiv);
						document.getElementById("busy").style.display = "none";
						document.getElementById("busy-text").style.display = "none";
					},input.files[0],form);
				}
				
				//method to check the uploaded images and stop until it reachs the limit.
				function checkUploadedImages(index){
					<% if (Site.COMMERCIAL) { %>
						console.log(index);
						if (index >= 100) {
							document.getElementById("busy").style.display = "none";
							document.getElementById("busy-text").style.display = "none";
							SDK.showError("You have exceeded the uploading limit. Your limit is 100 images at a time.", "Error");
							return false;
						}
					<%} else {%>
						console.log(index);
						if (index >= 10) {
							document.getElementById("busy").style.display = "none";
							document.getElementById("busy-text").style.display = "none";
							SDK.showError("You have exceeded the uploading limit. Your limit is 10 images at a time.", "Error");
							return false;
						}
					<%}%>
					return true;
				}
				</script>

				<form id="upload-test-image" method="post" name="fileinfo" enctype="multipart/form-data" class="message">
					<%= loginBean.postTokenInput() %>
					<input style="vertical-align: middle;" id="import-icon" onclick="document.getElementById('upload-analytic-image').click(); return false" class="icon" type="submit" value="" title="Upload image to test analytic"/>
					<span><%= loginBean.translate("Upload File.") %> </span>
					<input id="upload-analytic-image" class="hidden" type="file" name="file" onchange="uploadImage(this);" accept="image/*,audio/wav" multiple>
				</form>
				<hr>

				<div id="container-analytics">
					<div class="test-analytic-image" style="display: inline-block;">
					</div>
					<div id="test-object-image" style="display: inline-block;">
					</div>
				</div>
				<br>
				<div id="busy" class="loader"></div>
				<span id="busy-text"><%= loginBean.translate("Analyzing") %>...</span>
				<span style="color: red;" id="error-message"></span>
			<% } %>
			</div>
		<% if (!embed) { %>
			</div>
			</div>
			<jsp:include page="footer.jsp"/>
		<% } else { %>
			<% loginBean.embedHTML(loginBean.getFooterURL(), out); %>
		<% } %>
		<% proxy.clear(); %>
	</body>
</html>
