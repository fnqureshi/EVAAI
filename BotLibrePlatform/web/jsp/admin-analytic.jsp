<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.analytic.AnalyticBean"%>
<%@page import="org.botlibre.web.admin.ClientType"%>
<%@page import="org.eclipse.persistence.internal.helper.Helper" %>
<%@page import="org.botlibre.web.bean.LoginBean.Page" %>

<%@ page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<%
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	AnalyticBean bean = loginBean.getBean(AnalyticBean.class);
	String title = "Analytic";
	if (bean.getInstance() != null) {
		title = bean.getInstance().getName();
	}
	bean.setEmptyLabel();//setting selectedLabel to nothing
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= title %> Admin Console - <%= Site.NAME %></title>
	<meta name="description" content="The Admin Console lets you configure the analytic properties and settings"/>
	<meta name="keywords" content="admin, console, settings, config, properties, analytic"/>
</head>
<% if (embed) { %>
	<body style="background-color: #fff;">
	<jsp:include page="analytic-banner.jsp"/>
	<jsp:include page="admin-analytic-banner.jsp"/>
	<div id="mainbody">
	<div class="about">
<% } else { %>
	<body>
	<% loginBean.setPageType(Page.Admin); %>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-analytic-banner.jsp"/>
	<div id="mainbody">
	<div id="contents">
	<div class="browse">
<% } %>
<h1><img class="title" src="images/admin.svg"> <%= loginBean.translate("Admin Console") %></h1>
	<jsp:include page="error.jsp"/>
	<% if (!bean.isAdmin()) { %>
		<p style="color:#E00000;"><%= loginBean.translate("Must be admin") %></p>
	<% } %>
		<p>
			<a href="analytic-users.jsp"><img src="images/user1.png" class="admin-pic"></a> <a href="analytic-users.jsp"><%= loginBean.translate("Users") %></a> - <span><%= loginBean.translate("Configure users, and administrators of the analytic.") %></span><br/>
			<a href="analytic-network.jsp"><img src="images/analytic.png"  class="admin-pic"></a>   <a href="analytic-network.jsp"><%= loginBean.translate("Analytic Network") %></a> - <span><%= loginBean.translate("Configure analytic network settings.") %></span><br/>
			<a href="analytic-media.jsp"><img src="images/analytic-media.svg" class="admin-pic"></a> <a href="analytic-media.jsp"><%= loginBean.translate("Media Repository") %></a> - <span><%= loginBean.translate("Browse analytic media repository.") %></span><br/>
			<a href="analytic-train.jsp"><img src="images/train2.png" class="admin-pic"></a> <a href="analytic-train.jsp"><%= loginBean.translate("Train Network") %></a> - <span><%= loginBean.translate("Train analytic network.") %></span><br/>
			<a href="analytic-test-media.jsp"><img src="images/analytic-media.svg" class="admin-pic"></a> <a href="analytic-test-media.jsp"><%= loginBean.translate("Test Media Repository") %></a> - <span><%= loginBean.translate("Browse analytic test media repository.") %></span><br/>
			<a href="analytic-test-result.jsp"><img src="images/script1.png" class="admin-pic"></a> <a href="analytic-test-result.jsp"><%= loginBean.translate("Test Network") %></a> - <span><%= loginBean.translate("Test analytic network and view last test result.") %></span><br/>
		</p>
	</div>
	</div>
<% if (!embed) { %>
	</div>
	<jsp:include page="footer.jsp"/>
<% } %>
<% proxy.clear(); %>
</body>
</html>
