<%@page import="org.botlibre.web.Site"%>
<%@ page contentType="text/html; charset=UTF-8" %>

<%@ page import="org.botlibre.web.bean.BotBean"%>
<%@ page import="org.botlibre.web.admin.ClientType"%>
<%@ page import="org.eclipse.persistence.internal.helper.Helper" %>
<%@ page import="org.botlibre.web.bean.LoginBean.Page" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Admin Console - <%= Site.NAME %></title>
	<meta name="description" content="The Admin Console lets you configure the bot properties and settings"/>	
	<meta name="keywords" content="admin, console, settings, config, properties, bot, chatbot"/>
</head>
<body>
	<style>
		#admin-topper-banner {
			display: none;
		}
	</style>
	<% loginBean.setPageType(Page.Admin); %>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="mainbody">
		<div id="contents-full">
			<div class="browse">
				<% if (botBean.getInstance() != null && !botBean.getInstance().isExternal()) { %>
					<% botBean.connect(ClientType.WEB, request); %>
				<% } %>
				<h1><img class="admin-banner-pic" src="images/admin.svg"> <%= loginBean.translate("Admin Console") %></h1>
				<jsp:include page="error.jsp"/>
				<% if (botBean.getInstance() == null || (!botBean.isConnected() && !botBean.getInstance().isExternal())) { %>
					<%= botBean.getNotConnectedMessage() %>
				<% } else if (!botBean.isAdmin()) { %>
					<%= botBean.getMustBeAdminMessage() %>
				<% } %>
				<p>
				<% if (botBean.getInstance() == null || !botBean.getInstance().isExternal()) { %>
					
					<div class='browse-div'>
						<span class='dropt'>
						<table style='border-style:solid;border-color:grey;border-width:1px'>
							<tr><td class='browse-thumb' align='center' valign='middle'>
							<a href='admin-users.jsp'><img class='browse-thumb' src='images/users.gif' alt='users'/></a>
							</td></tr>
						</table>
						<div style='text-align:left'>
							<a class='menu' href='admin-users.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("Users") %></span></span></a>
							<br/>
							<span class='details'><%= loginBean.translate("Configure who can access, and administer your bot.") %></span>
						</div>
						</span>
						<div class='browse-thumb'>
							<a class='menu' href='admin-users.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("Users") %></span></span></a>
						</div>
					</div>
					<div class='browse-div'>
						<span class='dropt'>
						<table style='border-style:solid;border-color:grey;border-width:1px'>
							<tr><td class='browse-thumb' align='center' valign='middle'>
							<a href='instance-avatar.jsp'><img class='browse-thumb' src='images/avatar1.png' alt='avatar'/></a>
							</td></tr>
						</table>
						<div style='text-align:left'>
							<a class='menu' href='instance-avatar.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("Avatar") %></span></span></a>
							<br/>
							<span class='details'><%= loginBean.translate("Configure your bot's appearance. Choose an animated avatar, or create your own.") %></span>
						</div>
						</span>
						<div class='browse-thumb'>
							<a class='menu' href='instance-avatar.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("Avatar") %></span></span></a>
						</div>
					</div>
					<div class='browse-div'>
						<span class='dropt'>
						<table style='border-style:solid;border-color:grey;border-width:1px'>
							<tr><td class='browse-thumb' align='center' valign='middle'>
							<a href='voice.jsp'><img class='browse-thumb' src='images/voice.gif' alt='voice'/></a>
							</td></tr>
						</table>
						<div style='text-align:left'>
							<a class='menu' href='voice.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("Change Voice") %></span></span></a>
							<br/>
							<span class='details'><%= loginBean.translate("Configure your bot's language and voice.") %></span>
						</div>
						</span>
						<div class='browse-thumb'>
							<a class='menu' href='voice.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("Voice") %></span></span></a>
						</div>
					</div>
					<div class='browse-div'>
						<span class='dropt'>
						<table style='border-style:solid;border-color:grey;border-width:1px'>
							<tr><td class='browse-thumb' align='center' valign='middle'>
							<a href='learning.jsp'><img class='browse-thumb' src='images/learning.gif' alt='learning'/></a>
							</td></tr>
						</table>
						<div style='text-align:left'>
							<a class='menu' href='learning.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("Learning & Settings") %></span></span></a>
							<br/>
							<span class='details'><%= loginBean.translate("Configure your bot's learning ability and other settings.") %></span>
						</div>
						</span>
						<div class='browse-thumb'>
							<a class='menu' href='learning.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("Learning & Settings") %></span></span></a>
						</div>
					</div>
					<div class='browse-div'>
						<span class='dropt'>
						<table style='border-style:solid;border-color:grey;border-width:1px'>
							<tr><td class='browse-thumb' align='center' valign='middle'>
							<a href='chatlogs.jsp'><img class='browse-thumb' src='images/training.gif' alt='learning'/></a>
							</td></tr>
						</table>
						<div style='text-align:left'>
							<a class='menu' href='chatlogs.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("Conversation Design") %></span></span></a>
							<br/>
							<span class='details'><%= loginBean.translate("Train your bot's responses, greetings, and default responses. View your bot's conversations. Import and export chat logs, response lists, CSV, and AIML files.") %></span>
						</div>
						</span>
						<div class='browse-thumb'>
							<a class='menu' href='chatlogs.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("Training & Chat Logs") %></span></span></a>
						</div>
					</div>
					<% if (loginBean.isAdmin()) { %>
						<div class='browse-div'>
							<span class='dropt'>
							<table style='border-style:solid;border-color:grey;border-width:1px'>
								<tr><td class='browse-thumb' align='center' valign='middle'>
								<a href='self.jsp'><img class='browse-thumb' src='images/scripts.gif' alt='scripts'/></a>
								</td></tr>
							</table>
							<div style='text-align:left'>
								<a class='menu' href='self.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("Scripts") %></span></span></a>
								<br/>
								<span class='details'><%= loginBean.translate("Add, create, edit, import, and export Self or AIML scripting programs.") %></span>
							</div>
							</span>
							<div class='browse-thumb'>
								<a class='menu' href='self.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("Scripts") %></span></span></a>
							</div>
						</div>
					<% } %>
					<div class='browse-div'>
						<span class='dropt'>
						<table style='border-style:solid;border-color:grey;border-width:1px'>
							<tr><td class='browse-thumb' align='center' valign='middle'>
							<a href='twitter.jsp'><img class='browse-thumb' src='images/twitter1.png' alt='twitter'/></a>
							</td></tr>
						</table>
						<div style='text-align:left'>
							<a class='menu' href='twitter.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("Twitter") %></span></span></a>
							<br/>
							<span class='details'><%= loginBean.translate("Allow your bot to manage a Twitter account and post and chat on Twitter.") %></span>
						</div>
						</span>
						<div class='browse-thumb'>
							<a class='menu' href='twitter.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("Twitter") %></span></span></a>
						</div>
					</div>
					<div class='browse-div'>
						<span class='dropt'>
						<table style='border-style:solid;border-color:grey;border-width:1px'>
							<tr><td class='browse-thumb' align='center' valign='middle'>
							<a href='facebook.jsp'><img class='browse-thumb' src='images/facebook4.png' alt='facebook'/></a>
							</td></tr>
						</table>
						<div style='text-align:left'>
							<a class='menu' href='facebook.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("Facebook") %></span></span></a>
							<br/>
							<span class='details'><%= loginBean.translate("Allow your bot to manage a Facebook account or page and chat on Facebook Messenger.") %></span>
						</div>
						</span>
						<div class='browse-thumb'>
							<a class='menu' href='facebook.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("Facebook") %></span></span></a>
						</div>
					</div>
					<div class='browse-div'>
						<span class='dropt'>
						<table style='border-style:solid;border-color:grey;border-width:1px'>
							<tr><td class='browse-thumb' align='center' valign='middle'>
							<a href='whatsapp.jsp'><img class='browse-thumb' src='images/whatsapp7.png' alt='whatsapp'/></a>
							</td></tr>
						</table>
						<div style='text-align:left'>
							<a class='menu' href='whatsapp.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("WhatsApp") %></span></span></a>
							<br/>
							<span class='details'><%= loginBean.translate("Allow your bot to send, receive, and reply to WhatsApp messages.") %></span>
						</div>
						</span>
						<div class='browse-thumb'>
							<a class='menu' href='whatsapp.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("WhatsApp") %></span></span></a>
						</div>
					</div>
					<div class='browse-div'>
						<span class='dropt'>
						<table style='border-style:solid;border-color:grey;border-width:1px'>
							<tr><td class='browse-thumb' align='center' valign='middle'>
							<a href='telegram.jsp'><img class='browse-thumb' src='images/telegram1.png' alt='telegram'/></a>
							</td></tr>
						</table>
						<div style='text-align:left'>
							<a class='menu' href='telegram.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("Telegram") %></span></span></a>
							<br/>
							<span class='details'><%= loginBean.translate("Allow bot to manage a Telegram channel, group, or chat on Telegram.") %></span>
						</div>
						</span>
						<div class='browse-thumb'>
							<a class='menu' href='telegram.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("Telegram") %></span></span></a>
						</div>
					</div>
					<div class='browse-div'>
						<span class='dropt'>
						<table style='border-style:solid;border-color:grey;border-width:1px'>
							<tr><td class='browse-thumb' align='center' valign='middle'>
							<a href='slack.jsp'><img class='browse-thumb' src='images/slack1.png' alt='slack'/></a>
							</td></tr>
						</table>
						<div style='text-align:left'>
							<a class='menu' href='slack.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("Slack") %></span></span></a>
							<br/>
							<span class='details'><%= loginBean.translate("Allow your bot to send, receive, and reply to Slack messages.") %></span>
						</div>
						</span>
						<div class='browse-thumb'>
							<a class='menu' href='slack.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("Slack") %></span></span></a>
						</div>
					</div>
					<div class='browse-div'>
						<span class='dropt'>
						<table style='border-style:solid;border-color:grey;border-width:1px'>
							<tr><td class='browse-thumb' align='center' valign='middle'>
							<a href='skype.jsp'><img class='browse-thumb' src='images/skype1.png' alt='skype'/></a>
							</td></tr>
						</table>
						<div style='text-align:left'>
							<a class='menu' href='skype.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("Skype") %></span></span></a>
							<br/>
							<span class='details'><%= loginBean.translate("Allow your bot to send, receive, and reply to Skype messages.") %></span>
						</div>
						</span>
						<div class='browse-thumb'>
							<a class='menu' href='skype.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("Skype") %></span></span></a>
						</div>
					</div>
					<div class='browse-div'>
						<span class='dropt'>
						<table style='border-style:solid;border-color:grey;border-width:1px'>
							<tr><td class='browse-thumb' align='center' valign='middle'>
							<a href='wechat.jsp'><img class='browse-thumb' src='images/wechat1.png' alt='wechat'/></a>
							</td></tr>
						</table>
						<div style='text-align:left'>
							<a class='menu' href='wechat.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("WeChat") %></span></span></a>
							<br/>
							<span class='details'><%= loginBean.translate("Allow your bot to send, receive, and reply to WeChat messages.") %></span>
						</div>
						</span>
						<div class='browse-thumb'>
							<a class='menu' href='wechat.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("WeChat") %></span></span></a>
						</div>
					</div>
					<div class='browse-div'>
						<span class='dropt'>
						<table style='border-style:solid;border-color:grey;border-width:1px'>
							<tr><td class='browse-thumb' align='center' valign='middle'>
							<a href='kik.jsp'><img class='browse-thumb' src='images/kik.png' alt='kik'/></a>
							</td></tr>
						</table>
						<div style='text-align:left'>
							<a class='menu' href='kik.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("Kik") %></span></span></a>
							<br/>
							<span class='details'><%= loginBean.translate("Allow your bot to send, receive, and reply to Kik messages.") %></span>
						</div>
						</span>
						<div class='browse-thumb'>
							<a class='menu' href='kik.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("Kik") %></span></span></a>
						</div>
					</div>
					<div class='browse-div'>
						<span class='dropt'>
						<table style='border-style:solid;border-color:grey;border-width:1px'>
							<tr><td class='browse-thumb' align='center' valign='middle'>
							<a href='email.jsp'><img class='browse-thumb' src='images/email.png' alt='email'/></a>
							</td></tr>
						</table>
						<div style='text-align:left'>
							<a class='menu' href='email.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("Email") %></span></span></a>
							<br/>
							<span class='details'><%= loginBean.translate("Allow your bot to manage an email account and send, receive, and reply to emails.") %></span>
						</div>
						</span>
						<div class='browse-thumb'>
							<a class='menu' href='email.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("Email") %></span></span></a>
						</div>
					</div>
					<div class='browse-div'>
						<span class='dropt'>
						<table style='border-style:solid;border-color:grey;border-width:1px'>
							<tr><td class='browse-thumb' align='center' valign='middle'>
							<a href='sms.jsp'><img class='browse-thumb' src='images/twilio.svg' alt='twilio'/></a>
							</td></tr>
						</table>
						<div style='text-align:left'>
							<a class='menu' href='sms.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("Twilio SMS & IVR") %></span></span></a>
							<br/>
							<span class='details'><%= loginBean.translate("Allow your bot to send, receive, and reply to SMS messages and response to a voice phone using Interactive Voice Response (IVR).") %></span>
						</div>
						</span>
						<div class='browse-thumb'>
							<a class='menu' href='sms.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("Twilio SMS & IVR") %></span></span></a>
						</div>
					</div>
					<div class='browse-div'>
						<span class='dropt'>
						<table style='border-style:solid;border-color:grey;border-width:1px'>
							<tr><td class='browse-thumb' align='center' valign='middle'>
							<a href='google.jsp'><img class='browse-thumb' src='images/google.png' alt='twilio'/></a>
							</td></tr>
						</table>
						<div style='text-align:left'>
							<a class='menu' href='google.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("Google") %></span></span></a>
							<br/>
							<span class='details'><%= loginBean.translate("Allow your bot to connect to Google services such as Google Calendar.") %></span>
						</div>
						</span>
						<div class='browse-thumb'>
							<a class='menu' href='google.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("Google") %></span></span></a>
						</div>
					</div>
					<div class='browse-div'>
						<span class='dropt'>
						<table style='border-style:solid;border-color:grey;border-width:1px'>
							<tr><td class='browse-thumb' align='center' valign='middle'>
							<a href='wolframalpha.jsp'><img class='browse-thumb' src='images/wolframalpha1.png' alt='wolframalpha'/></a>
							</td></tr>
						</table>
						<div style='text-align:left'>
							<a class='menu' href='wolframalpha.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("Wolfram Alpha") %></span></span></a>
							<br/>
							<span class='details'><%= loginBean.translate("Allow your bot to connect to Wolfram Alpha services.") %></span>
						</div>
						</span>
						<div class='browse-thumb'>
							<a class='menu' href='wolframalpha.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("Wolfram Alpha") %></span></span></a>
						</div>
					</div>
					<div class='browse-div'>
						<span class='dropt'>
						<table style='border-style:solid;border-color:grey;border-width:1px'>
							<tr><td class='browse-thumb' align='center' valign='middle'>
							<a href='alexa.jsp'><img class='browse-thumb' src='images/alexa1.png' alt='alexa'/></a>
							</td></tr>
						</table>
						<div style='text-align:left'>
							<a class='menu' href='alexa.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("Alexa") %></span></span></a>
							<br/>
							<span class='details'><%= loginBean.translate("Allow your bot to connect to Amazon Alexa.") %></span>
						</div>
						</span>
						<div class='browse-thumb'>
							<a class='menu' href='alexa.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("Alexa") %></span></span></a>
						</div>
					</div>
					<div class='browse-div'>
						<span class='dropt'>
						<table style='border-style:solid;border-color:grey;border-width:1px'>
							<tr><td class='browse-thumb' align='center' valign='middle'>
							<a href='assistant.jsp'><img class='browse-thumb' src='images/google-assistant1.png' alt='google assistant'/></a>
							</td></tr>
						</table>
						<div style='text-align:left'>
							<a class='menu' href='assistant.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("Google Assistant") %></span></span></a>
							<br/>
							<span class='details'><%= loginBean.translate("Allow your bot to connect to Google Assistant.") %></span>
						</div>
						</span>
						<div class='browse-thumb'>
							<a class='menu' href='assistant.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("Google Assistant") %></span></span></a>
						</div>
					</div>
					<div class='browse-div'>
						<span class='dropt'>
						<table style='border-style:solid;border-color:grey;border-width:1px'>
							<tr><td class='browse-thumb' align='center' valign='middle'>
							<a href='irc.jsp'><img class='browse-thumb' src='images/irc.png' alt='irc'/></a>
							</td></tr>
						</table>
						<div style='text-align:left'>
							<a class='menu' href='irc.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("IRC") %></span></span></a>
							<br/>
							<span class='details'><%= loginBean.translate("Allow your bot to chat with others on an IRC chat channel.") %></span>
						</div>
						</span>
						<div class='browse-thumb'>
							<a class='menu' href='irc.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("IRC") %></span></span></a>
						</div>
					</div>
					<div class='browse-div'>
						<span class='dropt'>
						<table style='border-style:solid;border-color:grey;border-width:1px'>
							<tr><td class='browse-thumb' align='center' valign='middle'>
							<a href='timers.jsp'><img class='browse-thumb' src='images/timer.gif' alt='timers'/></a>
							</td></tr>
						</table>
						<div style='text-align:left'>
							<a class='menu' href='timers.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("Timers") %></span></span></a>
							<br/>
							<span class='details'><%= loginBean.translate("Setup your bot to run scripts at various time intervals to automate web tasks.") %></span>
						</div>
						</span>
						<div class='browse-thumb'>
							<a class='menu' href='timers.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("Timers") %></span></span></a>
						</div>
					</div>
					<div class='browse-div'>
						<span class='dropt'>
						<table style='border-style:solid;border-color:grey;border-width:1px'>
							<tr><td class='browse-thumb' align='center' valign='middle'>
							<a href='import.jsp'><img class='browse-thumb' src='images/web.gif' alt='web'/></a>
							</td></tr>
						</table>
						<div style='text-align:left'>
							<a class='menu' href='import.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("Web") %></span></span></a>
							<br/>
							<span class='details'><%= loginBean.translate("Import data from the WikiData, Wiktionary, and other websites.") %></span>
						</div>
						</span>
						<div class='browse-thumb'>
							<a class='menu' href='import.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("Web") %></span></span></a>
						</div>
					</div>
					<div class='browse-div'>
						<span class='dropt'>
						<table style='border-style:solid;border-color:grey;border-width:1px'>
							<tr><td class='browse-thumb' align='center' valign='middle'>
							<a href='memory.jsp'><img class='browse-thumb' src='images/knowledge.gif' alt='knowledge'/></a>
							</td></tr>
						</table>
						<div style='text-align:left'>
							<a class='menu' href='memory.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("Knowledge") %></span></span></a>
							<br/>
							<span class='details'><%= loginBean.translate("Browse your bot's knowledge database.") %></span>
						</div>
						</span>
						<div class='browse-thumb'>
							<a class='menu' href='memory.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("Knowledge") %></span></span></a>
						</div>
					</div>
					<div class='browse-div'>
						<span class='dropt'>
						<table style='border-style:solid;border-color:grey;border-width:1px'>
							<tr><td class='browse-thumb' align='center' valign='middle'>
							<a href='log.jsp'><img class='browse-thumb' src='images/log.gif' alt='log'/></a>
							</td></tr>
						</table>
						<div style='text-align:left'>
							<a class='menu' href='log.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("Log") %></span></span></a>
							<br/>
							<span class='details'><%= loginBean.translate("View the bot's log for errors and debugging info.") %></span>
						</div>
						</span>
						<div class='browse-thumb'>
							<a class='menu' href='log.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("Log") %></span></span></a>
						</div>
					</div>
					<div class='browse-div'>
						<span class='dropt'>
						<table style='border-style:solid;border-color:grey;border-width:1px'>
							<tr><td class='browse-thumb' align='center' valign='middle'>
							<a href='instance-stats.jsp'><img class='browse-thumb' src='images/analytics.gif' alt='log'/></a>
							</td></tr>
						</table>
						<div style='text-align:left'>
							<a class='menu' href='instance-stats.jsp'><span class='browse-thumb' style='font-weight:bold'><span class='admin-user'><%= loginBean.translate("Analytics") %></span></span></a>
							<br/>
							<span class='details'><%= loginBean.translate("View the bot's statistic and graphs.") %></span>
						</div>
						</span>
						<div class='browse-thumb'>
							<a class='menu' href='instance-stats.jsp'><span style='margin: 0 0 0;'><span class='admin-user'><%= loginBean.translate("Analytics") %></span></span></a>
						</div>
					</div>

					<br/>
					<% if (botBean.isConnected()) { %>
						<form action="bot" method="post" class="message">
							<%= loginBean.postTokenInput() %>
							<input id="cancel" name="disconnect" type="submit" value="<%= loginBean.translate("Disconnect") %>"/><br/>
						</form>
					<% } %>
					<h2><%= loginBean.translate("Tasks") %></h2>
					<ul>
						<li><a href="edit-instance.jsp"><%= loginBean.translate("Edit your bot's details including, name, description, and access.") %></a></li>
						<li><a href="edit-instance.jsp"><%= loginBean.translate("Change your bot's knowledge limit from its details page.") %></a></li>
						<li><a href="instance.jsp"><%= loginBean.translate("Delete your bot from its main page.") %></a></li>
						<li><a href="memory?status=true"><%= loginBean.translate("Delete the bot's entire memory, all responses, scripts, and data from its Knowledge page.") %></a></li>
						<li><a href="chatlogs.jsp"><%= loginBean.translate("Add a new response to your bot from its Training page.") %></a></li>
					</ul>
					
				<% } else { %>
					<a href="instance-avatar.jsp"><img src="images/avatar1.png" class="admin-pic"></a> <a href="instance-avatar.jsp"><%= loginBean.translate("Avatar") %></a> - <span><%= loginBean.translate("Configure your bot's appearance.Choose an animated avatar, or create your own.") %></span><br/>
					<a href="voice.jsp"><img src="images/voice1.png" class="admin-pic"></a> <a href="voice.jsp"><%= loginBean.translate("Voice") %></a> - <span><%= loginBean.translate("Configure your bot's language and voice.") %></span><br/>
				<% } %>
				</p>
				
			</div>
		</div>
	</div>
	<jsp:include page="footer.jsp"/>
</body>
</html>