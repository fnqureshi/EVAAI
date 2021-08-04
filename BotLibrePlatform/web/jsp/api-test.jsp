<%@page import="org.botlibre.web.Site"%>
<%@ page import="org.eclipse.persistence.internal.helper.Helper" %>
<%@ page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% if (loginBean.checkEmbed(request, response)) { return; } %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= loginBean.translate("API") %> - <%= Site.NAME %></title>
	<meta name="description" content="<%= loginBean.translate("") %>"/>	
	<meta name="keywords" content="<%= loginBean.translate("") %>"/>
	<!-- Added for ace -->
	<script src="scripts/ace/ace.js" type="text/javascript" charset="utf-8"></script>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
		<div id="contents-full">
			<div class="browse">
				<h1><%= loginBean.translate("Web API Console") %></h1>
				<form class="message">
					<span class='search-spa' >
						Choose an API
					</span>
					<select id="api" name="selection" onchange="apiChanged()">
						<option selected>Select</option>
						<option value="form-chat">form-chat</option> 
						<option value="form-check-instance">form-check-instance</option>
						<option value="form-check-user">form-check-user</option>
						<option value="form-get-all-instances">form-get-all-instances</option>
						<option value="chat">chat</option>
						<option value="check-instance">check-instance</option>
						<option value="check-user">check-user</option>
						<option value="get-all-instances">get-all-instances</option>
					</select>
				</form>
				<br/>
				<%= loginBean.translate("Example Code") %>
				<br/>
				<div id="exampleText" class="ace_absolute" style="width:100%"></div>
				<br/><br/>
				<button type="button" onclick="executeCode()">Execute</button>  
				<br/><br/>
				<%= loginBean.translate("Result") %>
				<br/>
				<div id="exampleResult" class="ace_absolute" style="width:100%"></div>

				<%
					Long appId = loginBean.getUserAppId();
					String url = Site.SECUREURLLINK;
				%>
				<script>
					var exampleText = ace.edit("exampleText");
					exampleText.getSession().setMode("ace/mode/javascript");

					var exampleResult = ace.edit("exampleResult");
					exampleResult.getSession().setMode("ace/mode/xml");

					var apiChanged = function() {
						var apiValue = document.getElementById("api").value;
						var formSuffix = "request.onreadystatechange = function() {\n\texampleResult.setValue(request.responseText, -1);\n};\nrequest.send();\n";
						var xmlSuffix = "request.onreadystatechange = function() {\n\texampleResult.setValue(request.responseText, -1);\n};\nrequest.send(xml);\n";
						if (apiValue == "form-chat") {
							exampleText.setValue("var request = new XMLHttpRequest();\n"
								+ "request.open('GET', '<%= url %>/rest/api/form-chat?&application=<%= appId %>&instance=165&message=hello+world', true);\n"
								+ formSuffix, -1);
						} else if (apiValue == "form-check-instance") {
							exampleText.setValue("var request = new XMLHttpRequest();\n"
								+ "request.open('GET', '<%= url %>/rest/api/form-check-instance?&application=<%= appId %>&id=165', true);\n"
								+ formSuffix, -1);
						} else if (apiValue == "form-check-user") {
							exampleText.setValue("var request = new XMLHttpRequest();\n"
								+ "var application='<%= appId %>';\n"
								+ "var user='userid';\n"
								+ "var password='password';\n"
								+ "var url = '<%= url %>/rest/api/form-check-user?application=' + application + '&user=' + user + '&password=' + password;"
								+ "request.open('GET', url, true);\n"
								+ formSuffix, -1);
						} else if (apiValue == "form-get-all-instances") {
							exampleText.setValue("var request = new XMLHttpRequest();\n"
								+ "request.open('GET', '<%= url %>/rest/api/form-get-all-instances?&application=<%= appId %>', true);\n"
								+ formSuffix, -1);
						} else if (apiValue == "chat") {
							exampleText.setValue("var request = new XMLHttpRequest();\n"
								+ "request.open('POST', '<%= url %>/rest/api/chat', true);\n"
								+ "request.setRequestHeader('Content-Type', 'application/xml');\n"
								+ "var xml = \"<chat application='<%= appId %>' instance='165'><message>hello world</message></chat>\";\n"
								+ xmlSuffix, -1);
						} else if (apiValue == "check-instance") {
							exampleText.setValue("var request = new XMLHttpRequest();\n"
								+ "request.open('POST', '<%= url %>/rest/api/check-instance', true);\n"
								+ "request.setRequestHeader('Content-Type', 'application/xml');\n"
								+ "var xml = \"<instance application='<%= appId %>' id='165'></instance>\";\n"
								+ xmlSuffix, -1);
						} else if (apiValue == "check-user") {
							exampleText.setValue("var request = new XMLHttpRequest();\n"
								+ "request.open('POST', '<%= url %>/rest/api/check-user', true);\n"
								+ "request.setRequestHeader('Content-Type', 'application/xml');\n"
								+ "var xml = \"<user application='<%= appId %>' user='userid' password='password'></user>\";\n"
								+ xmlSuffix, -1);
						} else if (apiValue == "get-all-instances") {
							exampleText.setValue("var request = new XMLHttpRequest();\n"
								+ "request.open('POST', '<%= url %>/rest/api/get-all-instances', true);\n"
								+ "request.setRequestHeader('Content-Type', 'application/xml');\n"
								+ "var xml = \"<browse application='<%= appId %>' ></browse>\";\n"
								+ xmlSuffix, -1);
						}
					};

					var executeCode = function() {
						var code = exampleText.getSession().getValue();
						eval(code);
					} 
				</script>
			</div>
		</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>
