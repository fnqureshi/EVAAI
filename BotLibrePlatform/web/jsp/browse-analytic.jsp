<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.analytic.AnalyticBean"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% if (loginBean.checkEmbed(request, response)) { return; } %>
<% DomainBean domainBean = loginBean.getBean(DomainBean.class); %>
<% AnalyticBean bean = loginBean.getBean(AnalyticBean.class); %>
<% loginBean.setActiveBean(bean); %>
<% boolean allowAccess = loginBean.checkDomainAccess(); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Browse Analytics - <%= Site.NAME %></title>
	<meta name="description" content="Browse the Mind analytic directory"/>
	<meta name="keywords" content="browse, directory, analytics, files, code"/>
</head>
<body>
	<% loginBean.setCategoryType("Analytic"); %>
	<% loginBean.setPageType(Page.Browse); %>
	<jsp:include page="banner.jsp"/>
	<%= bean.categoryHeaderHTML() %>
	<div id="mainbody">
		<div id="contents-full">
			<div class="browse">
				<jsp:include page="error.jsp"/>
				<% if (bean.getCategory() == null) { %>
					<h1><img src="images/analytic.png" class="admin-banner-pic"><%= loginBean.translate(" Accelerated Learning") %></h1>
					<% if (domainBean.hasValidInstance()) { %>
						<p>
							<%= loginBean.translate("Hear you get to associate different sounds and imagery. This where you train you diary to learn how to respond to different sounds and music.") %><br/>
							<%= loginBean.translate("Sound association is a great way to communicate with your subconcious.") %>
						</p>
					<% } else { %>
						<p>
.") %><br/>
							<%= loginBean.translate("Accelarated learning can be for image and audio classification, games, NLP, and many other usages.") %>
						</p>
					<% } %>
				<% } %>
				<% if (loginBean.checkDomainAccess()) { %>
					<%= bean.browseCategoriesHTML() %>
					<form action="analytic" method="get" class="message" style="display:inline">
						<input name="search-analytic" type="submit" value="<%= loginBean.translate("Search") %>"/>
						<% if (loginBean.isLoggedIn()) { %>
							<input name="my-instances" type="submit" value="<%= loginBean.translate("My Analytics") %>"/>
						<% } %>
						<% if (loginBean.getDomain().isCreationAllowed(loginBean.getUser())) { %>
							<input name="create-analytic" type="submit" value="<%= loginBean.translate("New Analytic") %>" title="Create a new Analytic"/>
							<input name="create-analytic-link" type="submit" value="<%= loginBean.translate("New Link") %>" title="Add a link to an external analytic or website to the analytic directory"/>
							<% if (loginBean.isLoggedIn()) { %>
								<input name="import-analytic" type="submit" value="<%= loginBean.translate("Import") %>" onclick="document.getElementById('upload-file').click(); return false;" title="Import a analytic export file"/>
							<% } %>
						<% } %>
						<br/>
					</form>
					<% if (bean.getCategory() == null) { %>
						<%= bean.browseFeaturedHTML() %>
					<% } %>
					<% if (loginBean.isLoggedIn()) { %>
						<form action="analytic-meta-import" method="post" enctype="multipart/form-data" style="display:inline">
							<%= loginBean.postTokenInput() %>
							<input id="upload-file" class="hidden" onchange="this.form.submit()" type="file" name="file" multiple/>
						</form>
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
