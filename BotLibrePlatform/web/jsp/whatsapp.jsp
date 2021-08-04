<%@page import="org.botlibre.web.bean.SMSBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>
<% SMSBean bean = loginBean.getBean(SMSBean.class); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= loginBean.translate("WhatsApp") %> - <%= Site.NAME %></title>
	<meta name="description" content="<%= loginBean.translate("Connect your bot to WhatsApp") %>"/>	
	<meta name="keywords" content="<%= loginBean.translate("whatsapp, mobile, texting, bot, whatsapp bot, whatsapp automation") %>"/>
</head>
<body>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("The WhatsApp tab allows you to connect your bot to WhatsApp via Twilio.") %><br/>
				</div>
					<%= loginBean.translate("Help") %>
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-sms.jsp"><%= loginBean.translate("Docs") %></a> 
			 : <a target="_blank" href="https://www.botlibre.com/forum-post?id=12896887"><%= loginBean.translate("How To Guide") %></a> 
			 : <a target="_blank" href="https://youtu.be/WpQNC_FuDcg"><%= loginBean.translate("Video") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
		<div class="browse">
			<h1>
				<span class="dropt-banner">
					<img src="images/whatsapp7.png" class="admin-banner-pic">
					<div>
						<p class="help">
							<%= loginBean.translate("Allow your bot to send, receive, and reply to WhatsApp messages.") %><br/>
						</p>
					</div>
				</span> <%= loginBean.translate("WhatsApp") %>
			</h1>
			<jsp:include page="error.jsp"/>
			<% if (!botBean.isConnected()) { %>
				<p class="help">
					<%= loginBean.translate("The WhatsApp tab allows you to connect your bot to WhatsApp via Twilio.") %>
				</p>
				<br/>
				<%= botBean.getNotConnectedMessage() %>
			<% } else if (!botBean.isAdmin()) { %>
				<p class="help">
					<%= loginBean.translate("The WhatsApp tab allows you to connect your bot to WhatsApp via Twilio.") %>
				</p>
				<br/>
				<%= botBean.getMustBeAdminMessage() %>
			<% } else { %>
				<p>
					<%= loginBean.translate("Please use with caution, you are not allowed to use your bot for spam, or violate our terms.") %>
				</p>
				<p>
					<%= loginBean.translate("Connect your bot to ") %> <a href="https://www.whatsapp.com/" target="_blank">WhatsApp.</a>
				</p>
				<p>
					<%= loginBean.translate("Only registering the webhook with your")%> <a href="https://www.twilio.com/" target="_blank">Twilio</a><%= loginBean.translate(" account is required to reply to WhatsApp messages.") %>
					<%= loginBean.translate("To send WhatsApp messages, you need to provide your Twilio account SID.") %>
				</p>
				<form action="sms" method="post" class="message">
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("Set this URL in your Twilio account to enable replying to WhatsApp messages.") %>
						</div>
					</span>
					<%= loginBean.translate("Twilio WhatsApp Webhook URL") %><br/>
					<input type="text" name="webhook" value="<%= bean.getWhatsAppWebhook() %>" /><br/>
				</form>

				<form action="sms" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<%= botBean.instanceInput() %>
					<h3>Twilio Properties</h3>
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("To send WhatsApp messages, or make voice calls, enter your Twilio account SID.") %>
						</div>
					</span>
					<%= loginBean.translate("Twilio SID") %><br/>
					<input type="text" name="sid" value="<%= bean.getSid() %>"/><br/>

					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("To send WhatsApp messages, or make voice calls, enter your Twilio account secret.") %>
						</div>
					</span>
					<%= loginBean.translate("Twilio Auth Token") %><br/>
					<input type="text" name="secret" value="<%= bean.getSecret() %>"/><br/>

					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("To send WhatsApp messages, or make voice calls, enter your Twilio account phone number.") %>
							<%= loginBean.translate("Use the full number, i.e. +16131234567") %>
						</div>
					</span>
					<%= loginBean.translate("Twilio Phone Number") %><br/>
					<input type="text" name="phone" value="<%= bean.getPhone() %>"/><br/>

					<input type="submit" name="save" value="<%= loginBean.translate("Save") %>"/><br/>
					<br/>
				</form>
			<% } %>
		</div>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>
