<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BotBean"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% if (loginBean.checkEmbed(request, response)) { return; } %>
<% DomainBean domainBean = loginBean.getBean(DomainBean.class); %>
<% BotBean bean = loginBean.getBotBean(); %>
<% loginBean.setActiveBean(bean); %>
<% boolean allowAccess = loginBean.checkDomainAccess(); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Browse Bots - <%= Site.NAME %></title>
	<meta name="description" content="Browse your Thoughts directory"/>	
	<meta name="keywords" content="browse, directory, thoughts, neural networks, Mind connections, predictive networks, open"/>
</head>
<body>	
	<% loginBean.setCategoryType("Bot"); %>
	<% loginBean.setPageType(Page.Browse); %>
	<jsp:include page="banner.jsp"/>
	<%= bean.categoryHeaderHTML() %>
	<div id="mainbody">
		<div id="contents-full">
			<div class="browse">
				<jsp:include page="error.jsp"/>
				<% if (bean.getCategory() == null) { %>
					<h1><img src="images/bot1.png" class="admin-banner-pic"><%= loginBean.translate("Thoughts") %></h1>
					<% if (domainBean.hasValidInstance()) { %>
						<p><%= loginBean.translate("Create a thought network for the web, mobile, or social media.") %></p>
					<% } else { %>
						<p><%= loginBean.translate("Create your thoughts by modelling them. Create thought networks to organise your mind, recall information and figure out the hidden patterns inside your subconcious. You can also give them a voice, and a face as a representation of what you visualise inside your mind.") %></p>
					<% } %>
				<% } %>
				<% if (loginBean.checkDomainAccess()) { %>
					<%= bean.browseCategoriesHTML() %>
					<form action="browse" method="get" class="message" style="display:inline">
						<input name="search-instance" type="submit" value="<%= loginBean.translate("Search") %>"/>
						<% if (loginBean.isLoggedIn()) { %>
							<input name="my-instances" type="submit" value="<%= loginBean.translate("My Thoughts") %>"/>
						<% } %>
						<% if (loginBean.getDomain().isCreationAllowed(loginBean.getUser())) { %>
							<input name="create-instance" type="submit" value="<%= loginBean.translate("New Thoughts") %>" title="<%= loginBean.translate("Create your own thought network") %>"/>
							<input name="create-link" type="submit" value="<%= loginBean.translate("New Link") %>" title="<%= loginBean.translate("Add a link to an external bot or website to the thought network directory") %>"/>
						<% } %>
						<br/>
					</form>
					<% if (bean.getCategory() == null) { %>
						<%= bean.browseFeaturedHTML() %>
					<% } %>
					<br/>
					<%= bean.browseHTML() %>
				<% } %>
			</div>
		</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>
