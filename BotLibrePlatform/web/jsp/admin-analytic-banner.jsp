<%@page import="org.botlibre.web.analytic.AnalyticBean"%>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<%
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	AnalyticBean bean = loginBean.getBean(AnalyticBean.class);
%>

<% if (!embed) { %>
	<% bean.browseBannerHTML(out, proxy); %>
<% } %>
<div id="admin-topper-banner" align="left">
	<% if (!embed) { %>
		<div class="clearfix">
	<% } %>
	<div class="toolbar">
	<a href="<%= "analytic?admin"  + proxy.proxyString() + bean.instanceString() %>"><img class="admin-banner-pic" src="images/admin.svg"></a>
	<a href="analytic-users.jsp" title="<%= loginBean.translate("Configure users, and administrators of the analytic") %>"><img src="images/user1.png" class="admin-banner-pic"></a>
	<a href="analytic-network.jsp" title="<%= loginBean.translate("Configure analytic network")%>"><img src="images/analytic.png" class="admin-banner-pic"></a>
	<a href="analytic-media.jsp" title="<%= loginBean.translate("Browse analytic media repository")%>"><img src="images/analytic-media.svg" class="admin-banner-pic"></a>
	<a href="analytic-train.jsp" title="<%= loginBean.translate("Train analytic network")%>"><img src="images/train2.png" class="admin-banner-pic"></a>
	<a href="analytic-test-media.jsp"  title="<%= loginBean.translate("Browse analytic test media repository")%>"><img src="images/analytic-media.svg" class="admin-banner-pic"></a>
	<a href="analytic-test-result.jsp"  title="<%= loginBean.translate("Test analytic network and view last test result")%>"><img src="images/script1.png" class="admin-banner-pic"></a>
	</div>
	<% if (!embed) { %>
		</div>
	<% } %>
</div>

<script>
	window.addEventListener("scroll", function() {scrollAdminTopper()});
	var isAdminScrolled = false;

	function scrollAdminTopper() {
		if (document.body.scrollTop > 50 || document.documentElement.scrollTop > 50) {
			if (!isAdminScrolled) {
				isAdminScrolled = true;
				document.getElementById("admin-topper-banner").style.position = "fixed";
				document.getElementById("admin-topper-banner").style.width = "100%";
				document.getElementById("admin-topper-banner").style.top = "40px";
				document.getElementById("admin-topper-banner").style.zIndex = 10;
			}
		} else {
			if (isAdminScrolled) {
				isAdminScrolled = false;
				document.getElementById("admin-topper-banner").style.position = "relative";
				document.getElementById("admin-topper-banner").style.width = null;
				document.getElementById("admin-topper-banner").style.top = null;
			}
		}
	}
</script>
