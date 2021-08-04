<%@page import="org.botlibre.web.service.ReferrerStats"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Collections"%>
<%@page import="org.botlibre.web.service.BotStats"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.admin.ErrorMessage"%>
<%@page import="org.botlibre.web.bean.BotBean"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% BotBean botBean = loginBean.getBotBean(); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= loginBean.translate("Analytics") %> - <%= Site.NAME %></title>
	<meta name="description" content="<%= loginBean.translate("View your bot's analytics and statistics") %>"/>	
	<meta name="keywords" content="<%= loginBean.translate("analytics, stats, reports, monitor, bot") %>"/>
	<link rel="stylesheet" href="css/stats-charts.css" type="text/css">
	<link rel="stylesheet" href="//cdn.jsdelivr.net/chartist.js/latest/chartist.min.css">
	<link rel="stylesheet" href="scripts/tablesorter/tablesorter.css" type="text/css">
	<script src="scripts/jquery/jquery.js"></script>
	<script src="scripts/jquery/jquery-ui.min.js"></script>
	<script src="//cdn.jsdelivr.net/chartist.js/latest/chartist.min.js"></script>
	<script type="text/javascript" src="scripts/tablesorter/tablesorter.js"></script>
	<script src="https://momentjs.com/downloads/moment.min.js"></script>
	<style>
		.select-div {
			width: 215px;
			padding-bottom: 2px;
			display: inline-block;
		}
		#chatType, #datesRangeSelect, #groupBySelect {
			width: 130px;
			margin: 0px;
		}
		.selectiveCharts {
			list-style-type: none;
		    border: 2px solid #CDCDCD;
		}
		.selectiveCharts li {
			display: inline;
		}
		.selectiveCharts li div {
			display: inline-block;
			padding: 7px;
		}
		.selectiveCharts li div span {
			margin-left: 7px;
		}
		.selectTitle {
			width: 62px;
			color: #818181;
		font-size: 12px;
		display: inline-block;
		}
		.ct-series-a .ct-line {
			stroke: #5f6287; /* dark blue gray */
		}
		.ct-series-a .ct-point {
			stroke: #5f6287; /* dark blue gray */
		}
		.ct-series-b .ct-line {
			stroke: #ff832c; /* orange pink or this color ff9000 */
		}
		.ct-series-b .ct-point {
			stroke: #ff832c; /* orange pink or this color ff9000 */
		}
		.ct-series-c .ct-line {
			stroke: #ff5c5c; /* pink */
		}
		.ct-series-c .ct-point {
			stroke: #ff5c5c; /* pink */
		}
		.ct-series-d .ct-line {
			stroke: #5271ff; /* navy blue */
		}
		.ct-series-d .ct-point {
			stroke: #5271ff; /* navy blue */
		}
		.ct-series-e .ct-line {
			stroke: #ffe557; /* light yellow */
		}
		.ct-series-e .ct-point {
			stroke: #ffe557; /* light yellow */
		}
		.ct-series-f .ct-line {
			stroke: #7fca58; /* light green */
		}
		.ct-series-f .ct-point {
			stroke: #7fca58; /* light green */
		}
		.ct-series-g .ct-line {
			stroke: #999966; /* gray yellow */
		}
		.ct-series-g .ct-point {
			stroke: #999966; /* gray yellow */
		}
		.ct-series-h .ct-line {
			stroke: #996633; /* brown */
		}
		.ct-series-h .ct-point {
			stroke: #996633; /* brown */
		}
		.ct-series-i .ct-line {
			stroke: #cc9900;  /* brown yellow */
		}
		.ct-series-i .ct-point {
			stroke: #cc9900;  /* brown yellow */
		}
		.ct-series-j .ct-line {
			stroke: #339966; /* dull green */
		}
		.ct-series-j .ct-point {
			stroke: #339966; /* dull green */
		}
		.ct-series-k .ct-line {
			stroke: #0099ff; /* sky blue */
		}
		.ct-series-k .ct-point {
			stroke: #0099ff; /* sky blue */
		}
		.ct-series-l .ct-line {
			stroke: #ff6666; /* red pink */
		}
		.ct-series-l .ct-point {
			stroke: #ff6666; /* red pink */
		}
		.ct-series-m .ct-line {
			stroke: #99ccff; /* light blue */
		}
		.ct-series-m .ct-point {
			stroke: #99ccff; /* light blue */
		}
		.ct-series-n .ct-line {
			stroke: #ffcc99; /* beige */
		}
		.ct-series-n .ct-point {
			stroke: #ffcc99; /* beige */
		}
		.ct-series-o .ct-line {
			stroke: #3366cc; /* dull blue */
		}
		.ct-series-o .ct-point {
			stroke: #3366cc; /* dull blue */
		}
		.ct-series-p .ct-line {
			stroke: #ff9999; /* pink */
		}
		.ct-series-p .ct-point {
			stroke: #ff9999; /* pink */
		}
		.ct-series-q .ct-line {
			stroke: #00ffff; /* aqua */
		}
		.ct-series-q .ct-point {
			stroke: #00ffff; /* aqua */
		}
		.ct-series-r .ct-line {
			stroke: #b32d00; /* dark brown red */
		}
		.ct-series-r .ct-point {
			stroke: #b32d00; /* dark brown red */
		}
		.ct-series-s .ct-line {
			stroke: #cc66ff; /* light purple */
		}
		.ct-series-s .ct-point {
			stroke: #cc66ff; /* light purple */
		}
		.ct-series-t .ct-line {
			stroke: #666699; /* dark blue purple */
		}
		.ct-series-t .ct-point {
			stroke: #666699; /* dark blue purple */
		}
		.ct-series-u .ct-line {
			stroke: #6666ff; /* navy blue */
		}
		.ct-series-u .ct-point {
			stroke: #6666ff; /* navy blue */
		}
		.ct-series-v .ct-line {
			stroke: #8cff66; /* light green */
		}
		.ct-series-v .ct-point {
			stroke: #8cff66; /* light green */
		}
		.ct-series-w .ct-line {
			stroke: #ffdab3; /* light sandy yellow */
		}
		.ct-series-w .ct-point {
			stroke: #ffdab3; /* light sandy yellow */
		}
		.ct-series-x .ct-line {
			stroke: #ff8400; /* orange */
		}
		.ct-series-x .ct-point {
			stroke: #ff8400; /* orange */
		}
		.ct-series-y .ct-line {
			stroke: #888844; /* brown dark green */
		}
		.ct-series-y .ct-point {
			stroke: #888844; /* brown dark green */
		}
		.ct-series-z .ct-line {
			stroke: #b399ff; /* purple light blue */
		}
		.ct-series-z .ct-point {
			stroke: #b399ff; /* purple light blue */
		}
	</style>
</head>
<body>
	<script>
		$(document).ready(function() {
			$(".tablesorter").tablesorter({widgets: ['zebra']});
		});
	</script>
	<jsp:include page="banner.jsp"/>
	<jsp:include page="admin-banner.jsp"/>
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("The analytics tab allows you to view bot's analytics and charts.") %><br/>
				</div>
				<%= loginBean.translate("Help") %> 
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-analytics.jsp"><%= loginBean.translate("Docs") %></a>
			 : <a target="_blank" href="https://www.botlibre.com/forum-post?id=20172135"><%= loginBean.translate("How To Guide") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
		<div class="browse">
			<h1>
				<span class="dropt-banner">
					<img src="images/analytics.png" class="admin-banner-pic">
					<div>
						<p class="help">
							<%= loginBean.translate("View the bot's analytics and charts.") %><br/>
						</p>
					</div>
				</span> <%= loginBean.translate("Analytics") %>
			</h1>
			<jsp:include page="error.jsp"/>
			<% if (!botBean.isConnected()) { %>
					<%= botBean.getNotConnectedMessage() %>
				<% } else if (!botBean.isAdmin()) { %>
					<%= botBean.getMustBeAdminMessage() %>
			<% } else { %>
				<% BotStats stats = BotStats.getStats(loginBean.getBotBean().getInstanceId(), loginBean.getBotBean().getInstanceName()); %>
				<div class="select-div">
					<span id="mediaSelectSpan" class="selectTitle">Chart</span>
					<select id="chatType">
						<option value="chat"><%= loginBean.translate("Chat") %></option>
						<option value="twitter"><%= loginBean.translate("Twitter") %></option>
						<option value="facebook"><%= loginBean.translate("Facebook") %></option>
						<option value="skype"><%= loginBean.translate("Skype") %></option>
						<option value="kik"><%= loginBean.translate("Kik") %></option>
						<option value="wechat"><%= loginBean.translate("WeChat") %></option>
						<option value="slack"><%= loginBean.translate("Slack") %></option>
						<option value="telegram"><%= loginBean.translate("Telegram") %></option>
						<option value="email"><%= loginBean.translate("Email") %></option>
						<option value="sms"><%= loginBean.translate("SMS") %></option>
						<option value="ivr"><%= loginBean.translate("IVR") %></option>
						<option value="whatsapp"><%= loginBean.translate("WhatsApp") %></option>
						<option value="alexa"><%= loginBean.translate("Alexa") %></option>
						<option value="googleAssistant"><%= loginBean.translate("Google Assistant") %></option>
						<option value="all"><%= loginBean.translate("All") %></option>
					</select>
				</div>
				<div class="select-div">
					<span id="dateRangeSpan" class="selectTitle">Duration</span>
					<select id="datesRangeSelect">
						<option value="today"><%= loginBean.translate("current day") %></option>
						<option value="weekly" selected="selected"><%= loginBean.translate("current week") %></option>
						<option value="monthly"><%= loginBean.translate("current month") %></option>
						<option value="allDatesRange"><%= loginBean.translate("all time") %></option>
					</select>
				</div>
				<div class="select-div"> 
					<span id="groupByStatsSpan" class="selectTitle">Group By</span>
					<select id="groupBySelect">
						<option value="none"><%= loginBean.translate("day") %></option>
						<option value="weekly"><%= loginBean.translate("week") %></option>
						<option value="monthly"><%= loginBean.translate("month") %></option>
					</select>
				</div>
				<div id="chatDiv" style="display:block;">
					<ul id="chatList" class="selectiveCharts">
						<li><div><input class="chatConversations" type="checkbox" name="Chat Conversations" checked><span><%= loginBean.translate("Conversations") %></span></div></li>
						<li><div><input class="chatMessages" type="checkbox" name="Chat Messages" checked><span><%= loginBean.translate("Messages") %></span></div></li>
						<li><div><input class="chatConvLength" type="checkbox" name="Avg Conv Len"><span><%= loginBean.translate("Conversation Length") %></span></div></li>
						<li><div><input class="engagedConversations" type="checkbox" name="Engaged Conversations"><span><%= loginBean.translate("Engaged Conversations") %></span></div></li>
						<li><div><input class="defaultResponses" type="checkbox" name="Default Responses"><span><%= loginBean.translate("Default Responses") %></span></div></li>
						<li><div><input class="avgConfidence" type="checkbox" name="Avg Confidence"><span><%= loginBean.translate("Confidence") %></span></div></li>
						<li><div><input class="avgSentiment" type="checkbox" name="Avg Sentiment"><span><%= loginBean.translate("Sentiment") %></span></div></li>
						<li><div><input class="avgResTime" type="checkbox" name="Response Time"><span><%= loginBean.translate("Response Time") %></span></div></li>
						<li><div><input class="chatConnects" type="checkbox" name="Chat Connects"><span><%= loginBean.translate("Connects") %></span></div></li>
						<li><div><input class="chats" type="checkbox" name="Chats"><span><%= loginBean.translate("Chats") %></span></div></li>
						<li><div><input class="liveChat" type="checkbox" name="Live Chat"><span><%= loginBean.translate("Live Chats") %></span></div></li>
						<li><div><input class="chatErrors" type="checkbox" name="Chat Errors"><span><%= loginBean.translate("Errors") %></span></div></li>
						<li><div><input class="chatImports" type="checkbox" name="Chat Imports"><span><%= loginBean.translate("Imports") %></span></div></li>
					</ul>
					<div id="chatChart" class="ct-chart ct-perfect-fourth" style="height:300px;"></div>
					<h2><%= loginBean.translate("Chat") %></h2>
					<div id="chatStatsDiv">
						<table class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("Conversations") %></th>
									<th><%= loginBean.translate("Messages") %></th>
									<th><%= loginBean.translate("Conversation Length") %></th>
									<th><%= loginBean.translate("Engaged Conversations") %></th>
									<th><%= loginBean.translate("Default Responses") %></th>
									<th><%= loginBean.translate("Confidence") %></th>
									<th><%= loginBean.translate("Sentiment") %></th>
									<th><%= loginBean.translate("Response Time") %></th>
									<th><%= loginBean.translate("Connects") %></th>
									<th><%= loginBean.translate("Chats") %></th>
									<th><%= loginBean.translate("Live Chats") %></th>
									<th><%= loginBean.translate("Errors") %></th>
									<th><%= loginBean.translate("Imports") %></th>
								</tr>
							</thead>
							<tbody>
								<% for (BotStats stat : AdminDatabase.instance().getAllBotStats(String.valueOf(loginBean.getBotBean().getInstanceId()))) { %>
									<tr>
										<td><%= stat.date %></td>
										<td><%= stat.conversations %></td>
										<td><%= stat.messages %></td>
										<% if (stat.conversations != 0) { %>
											<td><%= Math.round((stat.messages / stat.conversations) * 100) / 100 %></td>
										<% } else { %>
											<td>0</td>
										<% } %>
										<td><%= stat.engaged %></td>
										<td><%= stat.defaultResponses %></td>
										<% if (stat.messages != 0) { %>
											<td><%= Math.round((stat.confidence / stat.messages) * 100) / 100 %></td>
										<% } else { %>
											<td>0</td>
										<% } %>
										<td><%= stat.sentiment %></td>
										<% if (stat.messages != 0) { %>
											<td><%= stat.chatTotalResponseTime / stat.messages %></td>
										<% } else { %>
											<td>0</td>
										<% } %>
										<td><%= stat.connects %></td>
										<td><%= stat.chats %></td>
										<td><%= stat.livechats %></td>
										<td><%= stat.errors %></td>
										<td><%= stat.imports %></td>
									</tr>
								<% } %>
							</tbody>
						</table>
					</div>
					<div id="chatStatsTodayDiv" style="display:none;">
						<table class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("Conversations") %></th>
									<th><%= loginBean.translate("Messages") %></th>
									<th><%= loginBean.translate("Conversation Length") %></th>
									<th><%= loginBean.translate("Engaged Conversations") %></th>
									<th><%= loginBean.translate("Default Responses") %></th>
									<th><%= loginBean.translate("Confidence") %></th>
									<th><%= loginBean.translate("Sentiment") %></th>
									<th><%= loginBean.translate("Response Time") %></th>
									<th><%= loginBean.translate("Connects") %></th>
									<th><%= loginBean.translate("Chats") %></th>
									<th><%= loginBean.translate("Live Chats") %></th>
									<th><%= loginBean.translate("Errors") %></th>
									<th><%= loginBean.translate("Imports") %></th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td><%= stats.date %></td>
									<td><%= stats.conversations %></td>
									<td><%= stats.messages %></td>
									<td><%= Math.round((stats.messages / Math.max(stats.conversations, 1)) * 100) / 100 %></td>
									<td><%= stats.engaged %></td>
									<td><%= stats.defaultResponses %></td>
									<td><%= Math.round((stats.confidence / Math.max(stats.messages, 1)) * 100) / 100 %></td>
									<td><%= Math.round((stats.sentiment / Math.max(stats.messages, 1)) * 100) %></td>
									<td><%= stats.chatTotalResponseTime / Math.max(stats.messages, 1) %></td>
									<td><%= stats.connects %></td>
									<td><%= stats.chats %></td>
									<td><%= stats.livechats %></td>
									<td><%= stats.errors %></td>
									<td><%= stats.imports %></td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
				<div id="twitterDiv" style="display:none;">
					<ul id="twitterList" class="selectiveCharts">
						<li><div><input class="tweets" type="checkbox" name="Tweets" checked><span><%= loginBean.translate("Tweets") %></span></div></li>
						<li><div><input class="retweets" type="checkbox" name="Retweets"><span><%= loginBean.translate("Retweets") %></span></div></li>
						<li><div><input class="tweetsProcessed" type="checkbox" name="Tweets Processed"><span><%= loginBean.translate("Tweets Processed") %></span></div></li>
						<li><div><input class="directMessages" type="checkbox" name="Direct Messages"><span><%= loginBean.translate("Direct Messages") %></span></div></li>
					</ul>
					<div id="twitterChart" class="ct-chart ct-perfect-fourth" style="height:300px;"></div>
					<h2><%= loginBean.translate("Twitter") %></h2>
					<div id="twitterStatsDiv">
						<table class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("Tweets") %></th>
									<th><%= loginBean.translate("Retweets") %></th>
									<th><%= loginBean.translate("Tweets Processed") %></th>
									<th><%= loginBean.translate("Direct Messages") %></th>
								</tr>
							</thead>
							<tbody>
								<% for (BotStats stat : AdminDatabase.instance().getAllBotStats(String.valueOf(loginBean.getBotBean().getInstanceId()))) { %>
									<tr>
										<td><%= stat.date %></td>
										<td><%= stat.tweets %></td>
										<td><%= stat.retweets %></td>
										<td><%= stat.tweetsProcessed %></td>
										<td><%= stat.directMessagesProcessed %></td>
									</tr>
								<% } %>
							</tbody>
						</table>
					</div>
					<div id="twitterStatsTodayDiv" style="display:none;">
						<table class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("Tweets") %></th>
									<th><%= loginBean.translate("Retweets") %></th>
									<th><%= loginBean.translate("Tweets Processed") %></th>
									<th><%= loginBean.translate("Direct Messages") %></th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td><%= stats.date %></td>
									<td><%= stats.tweets %></td>
									<td><%= stats.retweets %></td>
									<td><%= stats.tweetsProcessed %></td>
									<td><%= stats.directMessagesProcessed %></td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
				<div id="facebookDiv" style="display:none;">
					<ul id="facebookList" class="selectiveCharts">
						<li><div><input class="facebookMsgProcessed" type="checkbox" name="Facebook Messages Processed" checked><span><%= loginBean.translate("Facebook Messages") %></span></div></li>
						<li><div><input class="facebookPosts" type="checkbox" name="Facebook Posts"><span><%= loginBean.translate("Facebook Posts") %></span></div></li>
						<li><div><input class="facebookLikes" type="checkbox" name="Facebook Likes"><span><%= loginBean.translate("Facebook Likes") %></span></div></li>
						<li><div><input class="facebookProcessed" type="checkbox" name="Facebook Processed"><span><%= loginBean.translate("Facebook Processed") %></span></div></li>
					</ul>
					<div id="facebookChart" class="ct-chart ct-perfect-fourth" style="height:300px;"></div>
					<h2><%= loginBean.translate("Facebook") %></h2>
					<div id="facebookStatsDiv">
						<table class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("Facebook Posts") %></th>
									<th><%= loginBean.translate("Facebook Likes") %></th>
									<th><%= loginBean.translate("Facebook Processed") %></th>
									<th><%= loginBean.translate("Facebook Messages") %></th>
								</tr>
							</thead>
							<tbody>
								<% for (BotStats stat : AdminDatabase.instance().getAllBotStats(String.valueOf(loginBean.getBotBean().getInstanceId()))) { %>
									<tr>
										<td><%= stat.date %></td>
										<td><%= stat.facebookPosts %></td>
										<td><%= stat.facebookLikes %></td>
										<td><%= stat.facebookProcessed %></td>
										<td><%= stat.facebookMessagesProcessed %></td>
									</tr>
								<% } %>
							</tbody>
						</table>
					</div>
					<div id="facebookStatsTodayDiv" style="display:none;">
						<table class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("Facebook Posts") %></th>
									<th><%= loginBean.translate("Facebook Likes") %></th>
									<th><%= loginBean.translate("Facebook Processed") %></th>
									<th><%= loginBean.translate("Facebook Messages") %></th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td><%= stats.date %></td>
									<td><%= stats.facebookPosts %></td>
									<td><%= stats.facebookLikes %></td>
									<td><%= stats.facebookProcessed %></td>
									<td><%= stats.facebookMessagesProcessed %></td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
				<div id="skypeDiv" style="display:none;">
					<ul id="skypeList" class="selectiveCharts">
						<li><div><input class="skypeMessages" type="checkbox" name="Skype Messages" checked><span><%= loginBean.translate("Skype Messages") %></span></div></li>
					</ul>
					<div id="skypeChart" class="ct-chart ct-perfect-fourth" style="height:300px;"></div>
					<h2><%= loginBean.translate("Skype") %></h2>
					<div id="skypeStatsDiv">
						<table id="skypestats" class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("Skype Messages") %></th>
								</tr>
							</thead>
							<tbody>
								<% for (BotStats stat : AdminDatabase.instance().getAllBotStats(String.valueOf(loginBean.getBotBean().getInstanceId()))) { %>
									<tr>
										<td><%= stat.date %></td>
										<td><%= stat.skypeMessagesProcessed %></td>
									</tr>
								<% } %>
							</tbody>
						</table>
					</div>
					<div id="skypeStatsTodayDiv" style="display:none;">
						<table class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("Skype Messages") %></th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td><%= stats.date %></td>
									<td><%= stats.skypeMessagesProcessed %></td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
				<div id="weChatDiv" style="display:none;">
					<ul id="weChatList" class="selectiveCharts">
						<li><div><input class="weChatMessages" type="checkbox" name="WeChat Messages" checked><span><%= loginBean.translate("WeChat Messages") %></span></div></li>
					</ul>
					<div id="weChatChart" class="ct-chart ct-perfect-fourth" style="height:300px;"></div>
					<h2><%= loginBean.translate("WeChat") %></h2> 
					<div id="wechatStatsDiv">
						<table id="wechatstats" class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("WeChat Messages") %></th>
								</tr>
							</thead>
							<tbody>
								<% for (BotStats stat : AdminDatabase.instance().getAllBotStats(String.valueOf(loginBean.getBotBean().getInstanceId()))) { %>
									<tr>
										<td><%= stat.date %></td>
										<td><%= stat.wechatMessagesProcessed %></td>
									</tr>
								<% } %>
							</tbody>
						</table>
					</div>
					<div id="wechatStatsTodayDiv" style="display:none;">
						<table class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("WeChat Messages") %></th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td><%= stats.date %></td>
									<td><%= stats.wechatMessagesProcessed %></td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
				<div id="kikDiv" style="display:none;">
					<ul id="kikList" class="selectiveCharts">
						<li><div><input class="kikMessages" type="checkbox" name="Kik Messages" checked><span><%= loginBean.translate("Kik Messages") %></span></div></li>
					</ul>
					<div id="kikChart" class="ct-chart ct-perfect-fourth" style="height:300px;"></div>
					<h2><%= loginBean.translate("Kik") %></h2>
					<div id="kikStatsDiv">
						<table id="kikstats" class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("Kik Messages") %></th>
								</tr>
							</thead>
							<tbody>
								<% for (BotStats stat : AdminDatabase.instance().getAllBotStats(String.valueOf(loginBean.getBotBean().getInstanceId()))) { %>
									<tr>
										<td><%= stat.date %></td>
										<td><%= stat.kikMessagesProcessed %></td>
									</tr>
								<% } %>
							</tbody>
						</table>
					</div>
					<div id="kikStatsTodayDiv" style="display:none;">
						<table class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("Kik Messages") %></th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td><%= stats.date %></td>
									<td><%= stats.kikMessagesProcessed %></td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
				<div id="slackDiv" style="display:none;">
					<ul id="slackList" class="selectiveCharts">
						<li><div><input class="slackMessages" type="checkbox" name="Slack Messages" checked><span><%= loginBean.translate("Slack Messages") %></span></div></li>
						<li><div><input class="slackPosts" type="checkbox" name="Slack Posts"><span><%= loginBean.translate("Slack Posts") %></span></div></li>
					</ul>
					<div id="slackChart" class="ct-chart ct-perfect-fourth" style="height:300px;"></div>
					<h2><%= loginBean.translate("Slack") %></h2>
					<div id="slackStatsDiv">
						<table id="slackstats" class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("Slack Posts") %></th>
									<th><%= loginBean.translate("Slack Messages") %></th>
								</tr>
							</thead>
							<tbody>
								<% for (BotStats stat : AdminDatabase.instance().getAllBotStats(String.valueOf(loginBean.getBotBean().getInstanceId()))) { %>
									<tr>
										<td><%= stat.date %></td>
										<td><%= stat.slackPosts %></td>
										<td><%= stat.slackMessagesProcessed %></td>
									</tr>
								<% } %>
							</tbody>
						</table>
					</div>
					<div id="slackStatsTodayDiv" style="display:none;">
						<table class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("Slack Posts") %></th>
									<th><%= loginBean.translate("Slack Messages") %></th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td><%= stats.date %></td>
									<td><%= stats.slackPosts %></td>
									<td><%= stats.slackMessagesProcessed %></td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
				<div id="telegramDiv" style="display:none;">
					<ul id="telegramList" class="selectiveCharts">
						<li><div><input class="telegramMessages" type="checkbox" name="Telegram Messages" checked><span><%= loginBean.translate("Telegram Messages") %></span></div></li>
						<li><div><input class="telegramPosts" type="checkbox" name="Telegram Posts"><span><%= loginBean.translate("Telegram Posts") %></span></div></li>
					</ul>
					<div id="telegramChart" class="ct-chart ct-perfect-fourth" style="height:300px;"></div>
					<h2><%= loginBean.translate("Telegram") %></h2>
					<div id="telegramStatsDiv">
						<table id="telegramstats" class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("Telegram Posts") %></th>
									<th><%= loginBean.translate("Telegram Messages") %></th>
								</tr>
							</thead>
							<tbody>
								<% for (BotStats stat : AdminDatabase.instance().getAllBotStats(String.valueOf(loginBean.getBotBean().getInstanceId()))) { %>
									<tr>
										<td><%= stat.date %></td>
										<td><%= stat.telegramPosts %></td>
										<td><%= stat.telegramMessagesProcessed %></td>
									</tr>
								<% } %>
							</tbody>
						</table>
					</div>
					<div id="telegramStatsTodayDiv" style="display:none;">
						<table class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("Telegram Posts") %></th>
									<th><%= loginBean.translate("Telegram Messages") %></th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td><%= stats.date %></td>
									<td><%= stats.telegramPosts %></td>
									<td><%= stats.telegramMessagesProcessed %></td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
				<div id="emailDiv" style="display:none;">
					<ul id="emailList" class="selectiveCharts">
						<li><div><input class="email" type="checkbox" name="Emails" checked><span><%= loginBean.translate("Email") %></span></div></li>
						<li><div><input class="emailProcessed" type="checkbox" name="Emails Processed"><span><%= loginBean.translate("Emails Processed") %></span></div></li>
					</ul>
					<div id="emailChart" class="ct-chart ct-perfect-fourth" style="height:300px;"></div>
					<h2><%= loginBean.translate("Email") %></h2>
					<div id="emailStatsDiv"> 
						<table id="emailstats" class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("Emails") %></th>
									<th><%= loginBean.translate("Emails Processed") %></th>
								</tr>
							</thead>
							<tbody>
								<% for (BotStats stat : AdminDatabase.instance().getAllBotStats(String.valueOf(loginBean.getBotBean().getInstanceId()))) { %>
									<tr>
										<td><%= stat.date %></td>
										<td><%= stat.emails %></td>
										<td><%= stat.emailsProcessed %></td>
									</tr>
								<% } %>
							</tbody>
						</table>
					</div>
					<div id="emailStatsTodayDiv" style="dislay:none;">
						<table class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("Emails") %></th>
									<th><%= loginBean.translate("Emails Processed") %></th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td><%= stats.date %></td>
									<td><%= stats.emails %></td>
									<td><%= stats.emailsProcessed %></td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
				<div id="smsDiv" style="display:none;">
					<ul id="smsList" class="selectiveCharts">
						<li><div><input class="smsSent" type="checkbox" name="SMS Sent" checked><span><%= loginBean.translate("SMS Sent") %></span></div></li>
						<li><div><input class="smsProcessed" type="checkbox" name="SMS Processed"><span><%= loginBean.translate("SMS Processed") %></span></div></li>
					</ul>
					<div id="smsChart" class="ct-chart ct-perfect-fourth" style="height:300px;"></div>
					<h2><%= loginBean.translate("SMS") %></h2> 
					<div id="smsStatsDiv"> 
						<table id="smsstats" class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("SMS Sent") %></th>
									<th><%= loginBean.translate("SMS Processed") %></th>
								</tr>
							</thead>
							<tbody>
								<% for (BotStats stat : AdminDatabase.instance().getAllBotStats(String.valueOf(loginBean.getBotBean().getInstanceId()))) { %>
									<tr>
										<td><%= stat.date %></td>
										<td><%= stat.smsSent %></td>
										<td><%= stat.smsProcessed %></td>
									</tr>
								<% } %>
							</tbody>
						</table>
					</div>
					<div id="smsStatsTodayDiv" style="display:none;">
						<table class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("SMS Sent") %></th>
									<th><%= loginBean.translate("SMS Processed") %></th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td><%= stats.date %></td>
									<td><%= stats.smsSent %></td>
									<td><%= stats.smsProcessed %></td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
				<div id="ivrDiv" style="display:none;">
					<ul id="ivrList" class="selectiveCharts">
						<li><div><input class="voiceCalls" type="checkbox" name="Voice Calls" checked><span><%= loginBean.translate("Voice Calls") %></span></div></li>
						<li><div><input class="voiceCallsProcessed" type="checkbox" name="Voice Calls Processed"><span><%= loginBean.translate("Voice Calls Processed") %></span></div></li>
					</ul>
					<div id="ivrChart" class="ct-chart ct-perfect-fourth" style="height:300px;"></div>
					<h2><%= loginBean.translate("IVR") %></h2> 
					<div id="ivrStatsDiv"> 
						<table id="ivrstats" class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("Voice Calls") %></th>
									<th><%= loginBean.translate("Voice Processed") %></th>
								</tr>
							</thead>
							<tbody>
								<% for (BotStats stat : AdminDatabase.instance().getAllBotStats(String.valueOf(loginBean.getBotBean().getInstanceId()))) { %>
									<tr>
										<td><%= stat.date %></td>
										<td><%= stat.twilioVoiceCalls %></td>
										<td><%= stat.twilioVoiceProcessed %></td>
									</tr>
								<% } %>
							</tbody>
						</table>
					</div>
					<div id="ivrStatsTodayDiv" style="display:none;">
						<table class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("IVR Sent") %></th>
									<th><%= loginBean.translate("IVR Processed") %></th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td><%= stats.date %></td>
									<td><%= stats.twilioVoiceCalls %></td>
									<td><%= stats.twilioVoiceProcessed %></td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
				<div id="whatsappDiv" style="display:none;">
					<ul id="whatsappList" class="selectiveCharts">
						<li><div><input class="whatsappSent" type="checkbox" name="WhatsApp Sent" checked><span><%= loginBean.translate("WhatsApp Sent") %></span></div></li>
						<li><div><input class="whatsappProcessed" type="checkbox" name="WhatsApp Processed"><span><%= loginBean.translate("WhatsApp Processed") %></span></div></li>
					</ul>
					<div id="whatsappChart" class="ct-chart ct-perfect-fourth" style="height:300px;"></div>
					<h2><%= loginBean.translate("WhatsApp") %></h2>
					<div id="whatsappStatsDiv"> 
						<table id="whatsappstats" class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("WhatsApp Sent") %></th>
									<th><%= loginBean.translate("WhatsApp Processed") %></th>
								</tr>
							</thead>
							<tbody>
								<% for (BotStats stat : AdminDatabase.instance().getAllBotStats(String.valueOf(loginBean.getBotBean().getInstanceId()))) { %>
									<tr>
										<td><%= stat.date %></td>
										<td><%= stat.whatsappSent %></td>
										<td><%= stat.whatsappProcessed %></td>
									</tr>
								<% } %>
							</tbody>
						</table>
					</div>
					<div id="whatsappStatsTodayDiv" style="display:none;">
						<table class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("WhatsApp Sent") %></th>
									<th><%= loginBean.translate("WhatsApp Processed") %></th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td><%= stats.date %></td>
									<td><%= stats.whatsappSent %></td>
									<td><%= stats.whatsappProcessed %></td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
				<div id="alexaDiv" style="display:none;">
					<ul id="alexaList" class="selectiveCharts">
						<li><div><input class="alexaMessages" type="checkbox" name="Alexa Messages" checked><span><%= loginBean.translate("Alexa Messages") %></span></div></li>
					</ul>
					<div id="alexaChart" class="ct-chart ct-perfect-fourth" style="height:300px;"></div>
					<h2><%= loginBean.translate("Alexa") %></h2> 
					<div id="alexaStatsDiv">
						<table id="alexastats" class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("Alexa Messages") %></th>
								</tr>
							</thead>
							<tbody>
								<% for (BotStats stat : AdminDatabase.instance().getAllBotStats(String.valueOf(loginBean.getBotBean().getInstanceId()))) { %>
									<tr>
										<td><%= stat.date %></td>
										<td><%= stat.alexaMessagesProcessed %></td>
									</tr>
								<% } %>
							</tbody>
						</table>
					</div>
					<div id="alexaStatsTodayDiv" style="display:none;">
						<table class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("Alexa Messages") %></th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td><%= stats.date %></td>
									<td><%= stats.alexaMessagesProcessed %></td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
				<div id="googleAssistantDiv" style="display:none;">
					<ul id="googleAssistantList" class="selectiveCharts">
						<li><div><input class="googleAssistantMessages" type="checkbox" name="Google Assistant Messages" checked><span><%= loginBean.translate("Google Assistant Messages") %></span></div></li>
					</ul>
					<div id="googleAssistantChart" class="ct-chart ct-perfect-fourth" style="height:300px;"></div>
					<h2><%= loginBean.translate("Google Assistant") %></h2> 
					<div id="googleAssistantStatsDiv">
						<table id="google-assistantstats" class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("Google Assistant Messages") %></th>
								</tr>
							</thead>
							<tbody>
								<% for (BotStats stat : AdminDatabase.instance().getAllBotStats(String.valueOf(loginBean.getBotBean().getInstanceId()))) { %>
									<tr>
										<td><%= stat.date %></td>
										<td><%= stat.googleAssistantMessagesProcessed %></td>
									</tr>
								<% } %>
							</tbody>
						</table>
					</div>
					<div id="googleAssistantStatsTodayDiv" style="display:none;">
						<table class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("Google Assistant Messages") %></th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td><%= stats.date %></td>
									<td><%= stats.googleAssistantMessagesProcessed %></td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
				<div id="allChartsDiv" style="display:none;">
					<ul id="allChartsList" class="selectiveCharts">
						<li><div><input class="chatConversations" type="checkbox" name="Chat Conversations" checked><span><%= loginBean.translate("Conversations") %></span></div></li>
						<li><div><input class="chatMessages" type="checkbox" name="Chat Messages" checked><span><%= loginBean.translate("Messages") %></span></div></li>
						<li><div><input class="chatConvLength" type="checkbox" name="Avg Conv Len"><span><%= loginBean.translate("Conversation Length") %></span></div></li>
						<li><div><input class="engagedConversations" type="checkbox" name="Engaged Conversations"><span><%= loginBean.translate("Engaged Conversations") %></span></div></li>
						<li><div><input class="defaultResponses" type="checkbox" name="Default Responses"><span><%= loginBean.translate("Default Responses") %></span></div></li>
						<li><div><input class="avgConfidence" type="checkbox" name="Avg Confidence"><span><%= loginBean.translate("Confidence") %></span></div></li>
						<li><div><input class="avgSentiment" type="checkbox" name="Avg Sentiment"><span><%= loginBean.translate("Sentiment") %></span></div></li>
						<li><div><input class="avgResTime" type="checkbox" name="Response Time"><span><%= loginBean.translate("Response Time") %></span></div></li>
						<li><div><input class="chatConnects" type="checkbox" name="Chat Connects"><span><%= loginBean.translate("Connects") %></span></div></li>
						<li><div><input class="chats" type="checkbox" name="Chats"><span><%= loginBean.translate("Chats") %></span></div></li>
						<li><div><input class="liveChat" type="checkbox" name="Live Chat"><span><%= loginBean.translate("Live Chats") %></span></div></li>
						<li><div><input class="chatErrors" type="checkbox" name="Chat Errors"><span><%= loginBean.translate("Errors") %></span></div></li>
						<li><div><input class="chatImports" type="checkbox" name="Chat Imports"><span><%= loginBean.translate("Imports") %></span></div></li>
						<li><div><input class="tweets" type="checkbox" name="Tweets" checked><span><%= loginBean.translate("Tweets") %></span></div></li>
						<li><div><input class="retweets" type="checkbox" name="Retweets"><span><%= loginBean.translate("Retweets") %></span></div></li>
						<li><div><input class="tweetsProcessed" type="checkbox" name="Tweets Processed"><span><%= loginBean.translate("Tweets Processed") %></span></div></li>
						<li><div><input class="directMessages" type="checkbox" name="Direct Messages"><span><%= loginBean.translate("Direct Messages") %></span></div></li>
						<li><div><input class="facebookMsgProcessed" type="checkbox" name="Facebook Messages Processed" checked><span><%= loginBean.translate("Facebook Messages") %></span></div></li>
						<li><div><input class="facebookPosts" type="checkbox" name="Facebook Posts"><span><%= loginBean.translate("Facebook Posts") %></span></div></li>
						<li><div><input class="facebookLikes" type="checkbox" name="Facebook Likes"><span><%= loginBean.translate("Facebook Likes") %></span></div></li>
						<li><div><input class="facebookProcessed" type="checkbox" name="Facebook Processed"><span><%= loginBean.translate("Facebook Processed") %></span></div></li>
						<li><div><input class="skypeMessages" type="checkbox" name="Skype Messages" checked><span><%= loginBean.translate("Skype Messages") %></span></div></li>
						<li><div><input class="weChatMessages" type="checkbox" name="WeChat Messages" checked><span><%= loginBean.translate("WeChat Messages") %></span></div></li>
						<li><div><input class="kikMessages" type="checkbox" name="Kik Messages" checked><span><%= loginBean.translate("Kik Messages") %></span></div></li>
						<li><div><input class="slackMessages" type="checkbox" name="Slack Messages" checked><span><%= loginBean.translate("Slack Messages") %></span></div></li>
						<li><div><input class="slackPosts" type="checkbox" name="Slack Posts"><span><%= loginBean.translate("Slack Posts") %></span></div></li>
						<li><div><input class="telegramMessages" type="checkbox" name="Telegram Messages" checked><span><%= loginBean.translate("Telegram Messages") %></span></div></li>
						<li><div><input class="telegramPosts" type="checkbox" name="Telegram Posts"><span><%= loginBean.translate("Telegram Posts") %></span></div></li>
						<li><div><input class="email" type="checkbox" name="Emails" checked><span><%= loginBean.translate("Email") %></span></div></li>
						<li><div><input class="emailProcessed" type="checkbox" name="Emails Processed"><span><%= loginBean.translate("Emails Processed") %></span></div></li>
						<li><div><input class="smsSent" type="checkbox" name="SMS Sent" checked><span><%= loginBean.translate("SMS Sent") %></span></div></li>
						<li><div><input class="ivrProcessed" type="checkbox" name="Voice Calls Processed"><span><%= loginBean.translate("Voice Calls Processed") %></span></div></li>
						<li><div><input class="ivrSent" type="checkbox" name="Voice Calls" checked><span><%= loginBean.translate("Voice Calls") %></span></div></li>
						<li><div><input class="smsProcessed" type="checkbox" name="SMS Processed"><span><%= loginBean.translate("SMS Processed") %></span></div></li>
						<li><div><input class="whatsappSent" type="checkbox" name="WhatsApp Sent" checked><span><%= loginBean.translate("WhatsApp Sent") %></span></div></li>
						<li><div><input class="whatsappProcessed" type="checkbox" name="WhatsApp Processed"><span><%= loginBean.translate("WhatsApp Processed") %></span></div></li>
						
						<li><div><input class="alexaMessages" type="checkbox" name="Alexa Messages" checked><span><%= loginBean.translate("Alexa Messages") %></span></div></li>
						<li><div><input class="googleAssistantMessages" type="checkbox" name="Google Assistant Messages" checked><span><%= loginBean.translate("Google Assistant Messages") %></span></div></li>
					</ul>
					<div id="allCharts" class="ct-chart ct-perfect-fourth" style="height:300px;"></div>
					<h2><%= loginBean.translate("All") %></h2>
					<div id="allStatsDiv">
						<table id="allstats" class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("Conversations") %></th>
									<th><%= loginBean.translate("Messages") %></th>
									<th><%= loginBean.translate("Conversation Length") %></th>
									<th><%= loginBean.translate("Engaged Conversations") %></th>
									<th><%= loginBean.translate("Default Responses") %></th>
									<th><%= loginBean.translate("Confidence") %></th>
									<th><%= loginBean.translate("Sentiment") %></th>
									<th><%= loginBean.translate("Response Time") %></th>
									<th><%= loginBean.translate("Connects") %></th>
									<th><%= loginBean.translate("Chats") %></th>
									<th><%= loginBean.translate("Live Chats") %></th>
									<th><%= loginBean.translate("Errors") %></th>
									<th><%= loginBean.translate("Imports") %></th>
									<th><%= loginBean.translate("Tweets") %></th>
									<th><%= loginBean.translate("Retweets") %></th>
									<th><%= loginBean.translate("Tweets Processed") %></th>
									<th><%= loginBean.translate("Direct Messages") %></th>
									<th><%= loginBean.translate("Facebook Posts") %></th>
									<th><%= loginBean.translate("Facebook Likes") %></th>
									<th><%= loginBean.translate("Facebook Processed") %></th>
									<th><%= loginBean.translate("Facebook Messages") %></th>
									<th><%= loginBean.translate("Skype Messages") %></th>
									<th><%= loginBean.translate("WeChat Messages") %></th>
									<th><%= loginBean.translate("Kik Messages") %></th>
									<th><%= loginBean.translate("Slack Posts") %></th>
									<th><%= loginBean.translate("Slack Messages") %></th>
									<th><%= loginBean.translate("Telegram Posts") %></th>
									<th><%= loginBean.translate("Telegram Messages") %></th>
									<th><%= loginBean.translate("Emails") %></th>
									<th><%= loginBean.translate("Emails Processed") %></th>
									<th><%= loginBean.translate("SMS Sent") %></th>
									<th><%= loginBean.translate("SMS Processed") %></th>
									<th><%= loginBean.translate("Voice Calls") %></th>
									<th><%= loginBean.translate("Voice Calls Processed") %></th>
									<th><%= loginBean.translate("WhatsApp Sent") %></th>
									<th><%= loginBean.translate("WhatsApp Processed") %></th>
									<th><%= loginBean.translate("Alexa Messages") %></th>
									<th><%= loginBean.translate("Google Assistant Messages") %></th>
								</tr>
							</thead>
							<tbody>
							<% for (BotStats stat : AdminDatabase.instance().getAllBotStats(String.valueOf(loginBean.getBotBean().getInstanceId()))) { %>
								<tr>
									<td><%= stat.date %></td>
									<td><%= stat.conversations %></td>
									<td><%= stat.messages %></td>
									<% if (stat.conversations != 0) { %>
										<td><%= Math.round((stat.messages / stat.conversations) * 100) / 100 %></td>
									<% } else { %>
										<td>0</td>
									<% } %>
									<td><%= stat.engaged %></td>
									<td><%= stat.defaultResponses %></td>
									<% if (stat.messages != 0) { %>
										<td><%= Math.round((stat.confidence / stat.messages) * 100) / 100 %></td>
									<% } else { %>
										<td>0</td>
									<% } %>
									<td><%= stat.sentiment %></td>
									<% if (stat.messages != 0) { %>
										<td><%= stat.chatTotalResponseTime / stat.messages %></td>
									<% } else { %>
										<td>0</td>
									<% } %>
									<td><%= stat.connects %></td>
									<td><%= stat.chats %></td>
									<td><%= stat.livechats %></td>
									<td><%= stat.errors %></td>
									<td><%= stat.imports %></td>
									<td><%= stat.tweets %></td>
									<td><%= stat.retweets %></td>
									<td><%= stat.tweetsProcessed %></td>
									<td><%= stat.directMessagesProcessed %></td>
									<td><%= stat.facebookPosts %></td>
									<td><%= stat.facebookLikes %></td>
									<td><%= stat.facebookProcessed %></td>
									<td><%= stat.facebookMessagesProcessed %></td>
									<td><%= stat.skypeMessagesProcessed %></td>
									<td><%= stat.wechatMessagesProcessed %></td>
									<td><%= stat.kikMessagesProcessed %></td>
									<td><%= stat.slackPosts %></td>
									<td><%= stat.slackMessagesProcessed %></td>
									<td><%= stat.telegramPosts %></td>
									<td><%= stat.telegramMessagesProcessed %></td>
									<td><%= stat.emails %></td>
									<td><%= stat.emailsProcessed %></td>
									<td><%= stat.smsSent %></td>
									<td><%= stat.smsProcessed %></td>
									<td><%= stat.twilioVoiceCalls %></td>
									<td><%= stat.twilioVoiceProcessed %></td>
									<td><%= stat.whatsappSent %></td>
									<td><%= stat.whatsappProcessed %></td>
									<td><%= stat.alexaMessagesProcessed %></td>
									<td><%= stat.googleAssistantMessagesProcessed %></td>
								</tr>
							<% } %>
							</tbody>
						</table>
					</div>
					<div id="allStatsTodayDiv" style="display:none;">
						<table id="allstats" class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("Conversations") %></th>
									<th><%= loginBean.translate("Messages") %></th>
									<th><%= loginBean.translate("Conversation Length") %></th>
									<th><%= loginBean.translate("Engaged Conversations") %></th>
									<th><%= loginBean.translate("Default Responses") %></th>
									<th><%= loginBean.translate("Confidence") %></th>
									<th><%= loginBean.translate("Sentiment") %></th>
									<th><%= loginBean.translate("Response Time") %></th>
									<th><%= loginBean.translate("Connects") %></th>
									<th><%= loginBean.translate("Chats") %></th>
									<th><%= loginBean.translate("Live Chats") %></th>
									<th><%= loginBean.translate("Errors") %></th>
									<th><%= loginBean.translate("Imports") %></th>
									<th><%= loginBean.translate("Tweets") %></th>
									<th><%= loginBean.translate("Retweets") %></th>
									<th><%= loginBean.translate("Tweets Processed") %></th>
									<th><%= loginBean.translate("Direct Messages") %></th>
									<th><%= loginBean.translate("Facebook Posts") %></th>
									<th><%= loginBean.translate("Facebook Likes") %></th>
									<th><%= loginBean.translate("Facebook Processed") %></th>
									<th><%= loginBean.translate("Facebook Messages") %></th>
									<th><%= loginBean.translate("Skype Messages") %></th>
									<th><%= loginBean.translate("WeChat Messages") %></th>
									<th><%= loginBean.translate("Kik Messages") %></th>
									<th><%= loginBean.translate("Slack Posts") %></th>
									<th><%= loginBean.translate("Slack Messages") %></th>
									<th><%= loginBean.translate("Telegram Posts") %></th>
									<th><%= loginBean.translate("Telegram Messages") %></th>
									<th><%= loginBean.translate("Emails") %></th>
									<th><%= loginBean.translate("Emails Processed") %></th>
									<th><%= loginBean.translate("SMS Sent") %></th>
									<th><%= loginBean.translate("SMS Processed") %></th>
									<th><%= loginBean.translate("Voice Calls") %></th>
									<th><%= loginBean.translate("Voice Calls Processed") %></th>
									<th><%= loginBean.translate("WhatsApp Sent") %></th>
									<th><%= loginBean.translate("WhatsApp Processed") %></th>
									<th><%= loginBean.translate("Alexa Messages") %></th>
									<th><%= loginBean.translate("Google Assistant Messages") %></th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td><%= stats.date %></td>
									<td><%= stats.conversations %></td>
									<td><%= stats.messages %></td>
									<% if (stats.conversations != 0) { %>
										<td><%= Math.round((stats.messages / stats.conversations) * 100) / 100 %></td>
									<% } else { %>
										<td>0</td>
									<% } %>
									<td><%= stats.engaged %></td>
									<td><%= stats.defaultResponses %></td>
									<% if (stats.messages != 0) { %>
										<td><%= Math.round((stats.confidence / stats.messages) * 100) / 100 %></td>
									<% } else { %>
										<td>0</td>
									<% } %>
									<td><%= stats.sentiment %></td>
									<% if (stats.messages != 0) { %>
										<td><%= stats.chatTotalResponseTime / stats.messages %></td>
									<% } else { %>
										<td>0</td>
									<% } %>
									<td><%= stats.connects %></td>
									<td><%= stats.chats %></td>
									<td><%= stats.livechats %></td>
									<td><%= stats.errors %></td>
									<td><%= stats.imports %></td>
									<td><%= stats.tweets %></td>
									<td><%= stats.retweets %></td>
									<td><%= stats.tweetsProcessed %></td>
									<td><%= stats.directMessagesProcessed %></td>
									<td><%= stats.facebookPosts %></td>
									<td><%= stats.facebookLikes %></td>
									<td><%= stats.facebookProcessed %></td>
									<td><%= stats.facebookMessagesProcessed %></td>
									<td><%= stats.skypeMessagesProcessed %></td>
									<td><%= stats.wechatMessagesProcessed %></td>
									<td><%= stats.kikMessagesProcessed %></td>
									<td><%= stats.slackPosts %></td>
									<td><%= stats.slackMessagesProcessed %></td>
									<td><%= stats.telegramPosts %></td>
									<td><%= stats.telegramMessagesProcessed %></td>
									<td><%= stats.emails %></td>
									<td><%= stats.emailsProcessed %></td>
									<td><%= stats.smsSent %></td>
									<td><%= stats.smsProcessed %></td>
									<td><%= stats.twilioVoiceCalls %></td>
									<td><%= stats.twilioVoiceProcessed %></td>
									<td><%= stats.whatsappSent %></td>
									<td><%= stats.whatsappProcessed %></td>
									<td><%= stats.alexaMessagesProcessed %></td>
									<td><%= stats.googleAssistantMessagesProcessed %></td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
			<% } %>
		</div>
	</div>
	</div>
	<script>
		var chatConversationsArray = [];
		var chatMessagesArray = [];
		var averageConvLengthArray = [];
		var engagedConvArray = [];
		var defaultResponseArray = [];
		var confidenceArray = [];
		var sentimentArray = [];
		var responseTimeArray = [];
		var chatConnectsArray = [];
		var chatsArray = [];
		var liveChatArray = [];
		var chatErrorsArray = [];
		var chatImportsArray = [];		
		var tweetsArray = [];
		var retweetsArray = [];
		var tweetsProcessedArray = [];
		var tweetsDirMsgProcessedArray = [];		
		var facebookPostsArray = [];
		var facebookLikesArray = [];
		var facebookProcessedArray = [];
		var facebookMsgProcessedArray = [];	
		var skypeMessageArray = [];
		var weChatMessageArray = [];
		var kikMessageArray = [];		
		var slackPostsArray = [];
		var slackMessageArray = [];
		var telegramPostsArray = [];
		var telegramMessageArray = [];		
		var emailArray = [];
		var emailProcessedArray = [];		
		var smsSentArray = [];
		var smsProcessedArray = [];
		var voiceCallsArray = [];
		var voiceCallsProcessedArray = [];
		var whatsappSentArray = [];
		var whatsappProcessedArray = [];
		var alexaMessageArray = [];
		var googleAssistantMessageArray = [];
		
		function generateData() {
			<% List<BotStats> statsList = new ArrayList<BotStats>(); %>
			<% BotStats stats = BotStats.getStats(loginBean.getBotBean().getInstanceId(), loginBean.getBotBean().getInstanceName()); %>
			<% statsList = AdminDatabase.instance().getAllBotStats(String.valueOf(loginBean.getBotBean().getInstanceId())); %>
			<% statsList.add(0, stats); %>
			
			<% for (int j = statsList.size() - 1; j >= 0; j--) { %>
				<% BotStats stat = statsList.get(j); %>
				chatConversationsArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.conversations %> } );
				chatMessagesArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.messages %> } );
				<% if (stat.conversations != 0) { %>
					averageConvLengthArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= Math.round((stat.messages / stat.conversations) * 100) / 100 %> } );
				<% } else { %>
					averageConvLengthArray.push( { x: new Date(<%= stat.date.getTime() %>), y: 0 } );
				<% } %>
				engagedConvArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.engaged %> } );
				defaultResponseArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.defaultResponses %> } );
				<% if (stat.messages != 0) { %>
					confidenceArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= Math.round((stat.confidence / stat.messages) * 100) / 100 %> } );
				<% } else { %>
					confidenceArray.push( { x: new Date(<%= stat.date.getTime() %>), y: 0 } );
				<% } %>
				sentimentArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.sentiment %> } );
				<% if (stat.messages != 0) { %>
					responseTimeArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.chatTotalResponseTime / stat.messages %> } );
				<% } else { %>
					responseTimeArray.push( { x: new Date(<%= stat.date.getTime() %>), y: 0 } );
				<% } %>
				chatConnectsArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.connects %> } );
				chatsArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.chats %> } );
				liveChatArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.livechats %> } );
				chatErrorsArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.errors %> } );
				chatImportsArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.imports %> } );
							
				tweetsArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.tweets %> } );
				retweetsArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.retweets %> } );
				tweetsProcessedArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.tweetsProcessed %> } );
				tweetsDirMsgProcessedArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.directMessagesProcessed %> } );
							
				facebookPostsArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.facebookPosts %> } );
				facebookLikesArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.facebookLikes %> } );
				facebookProcessedArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.facebookProcessed %> } );
				facebookMsgProcessedArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.facebookMessagesProcessed %> } );
							
				skypeMessageArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.skypeMessagesProcessed %> } );
				weChatMessageArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.wechatMessagesProcessed %> } );
				kikMessageArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.kikMessagesProcessed %> } );
							
				slackPostsArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.slackPosts %> } );
				slackMessageArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.slackMessagesProcessed %> } ); 
					
				telegramPostsArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.telegramPosts %> } );
				telegramMessageArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.telegramMessagesProcessed %> } );
							
				emailArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.emails %> } );
				emailProcessedArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.emailsProcessed %> } );
							
				smsSentArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.smsSent %> } );
				smsProcessedArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.smsProcessed %> } );
				
				voiceCallsArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.twilioVoiceCalls %> } );
				voiceCallsProcessedArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.twilioVoiceProcessed %> } );
				
				whatsappSentArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.whatsappSent %> } );
				whatsappProcessedArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.whatsappProcessed %> } );
				
				alexaMessageArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.alexaMessagesProcessed %> } );
				
				googleAssistantMessageArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.googleAssistantMessagesProcessed %> } );
			<% } %>
		}
		generateData(); /* Java Array List that is called only once initially on page render */
		
		function generateAnalyticsData(dateInterval, groupByValue, listType) {
			var tempChatConversationsArray = chatConversationsArray;
			var tempChatMessagesArray = chatMessagesArray;
			var tempAverageConvLengthArray = averageConvLengthArray;
			var tempEngagedConvArray = engagedConvArray;
			var tempDefaultResponseArray = defaultResponseArray;
			var tempConfidenceArray = confidenceArray;
			var tempSentimentArray = sentimentArray;
			var tempResponseTimeArray = responseTimeArray;
			var tempChatConnectsArray = chatConnectsArray;
			var tempChatsArray = chatsArray;
			var tempLiveChatArray = liveChatArray;
			var tempChatErrorsArray = chatErrorsArray;
			var tempChatImportsArray = chatImportsArray;
			var tempTweetsArray = tweetsArray;
			var tempRetweetsArray = retweetsArray;
			var tempTweetsProcessedArray = tweetsProcessedArray;
			var tempTweetsDirMsgProcessedArray = tweetsDirMsgProcessedArray;
			var tempFacebookPostsArray = facebookPostsArray;
			var tempFacebookLikesArray = facebookLikesArray;
			var tempFacebookProcessedArray = facebookProcessedArray;
			var tempFacebookMsgProcessedArray = facebookMsgProcessedArray;
			var tempSkypeMessageArray = skypeMessageArray;
			var tempWeChatMessageArray = weChatMessageArray;
			var tempKikMessageArray = kikMessageArray;
			var tempSlackPostsArray = slackPostsArray;
			var tempSlackMessageArray = slackMessageArray; 
			var tempTelegramPostsArray = telegramPostsArray;
			var tempTelegramMessageArray = telegramMessageArray;
			var tempEmailArray = emailArray;
			var tempEmailProcessedArray = emailProcessedArray;
			var tempSmsSentArray = smsSentArray;
			var tempSmsProcessedArray = smsProcessedArray;
			var tempVoiceCallsArray = voiceCallsArray;
			var tempVoiceCallsProcessedArray = voiceCallsProcessedArray;
			var tempWhatsAppSentArray = whatsappSentArray;
			var tempWhatsAppProcessedArray = whatsappProcessedArray;
			var tempAlexaMessageArray = alexaMessageArray;
			var tempGoogleAssistantMessageArray = googleAssistantMessageArray;
			var timeInterval = chatConversationsArray.length;
			var endLength = 1;
			if (dateInterval === "weekly" && timeInterval > 7) {
				timeInterval = 8;
			} else if (dateInterval === "monthly" && timeInterval > 30) {
				timeInterval = 31;
			} else if (dateInterval === "today" && timeInterval >= 1) {
				endLength = 0;
				timeInterval = 1;
			}
			
			if (timeInterval == 8 || timeInterval == 31 || timeInterval == 1) {
				tempChatConversationsArray = chatConversationsArray.slice(chatConversationsArray.length - timeInterval, chatConversationsArray.length - endLength);
				tempChatMessagesArray = chatMessagesArray.slice(chatMessagesArray.length - timeInterval, chatMessagesArray.length - endLength);
				tempAverageConvLengthArray = averageConvLengthArray.slice(averageConvLengthArray.length - timeInterval, averageConvLengthArray.length - endLength);
				tempEngagedConvArray = engagedConvArray.slice(engagedConvArray.length - timeInterval, engagedConvArray.length - endLength);
				tempDefaultResponseArray = defaultResponseArray.slice(defaultResponseArray.length - timeInterval, defaultResponseArray.length - endLength);
				tempConfidenceArray = confidenceArray.slice(confidenceArray.length - timeInterval, confidenceArray.length - endLength);
				tempSentimentArray = sentimentArray.slice(sentimentArray.length - timeInterval, sentimentArray.length - endLength);
				tempResponseTimeArray = responseTimeArray.slice(responseTimeArray.length - timeInterval, responseTimeArray.length - endLength);
				tempChatConnectsArray = chatConnectsArray.slice(chatConnectsArray.length - timeInterval, chatConnectsArray.length - endLength);
				tempChatsArray = chatsArray.slice(chatsArray.length - timeInterval, chatsArray.length - endLength);
				tempLiveChatArray = liveChatArray.slice(liveChatArray.length - timeInterval, liveChatArray.length - endLength);
				tempChatErrorsArray = chatErrorsArray.slice(chatErrorsArray.length - timeInterval, chatErrorsArray.length - endLength);
				tempChatImportsArray = chatImportsArray.slice(chatImportsArray.length - timeInterval, chatImportsArray.length - endLength);
				tempTweetsArray = tweetsArray.slice(tweetsArray.length - timeInterval, tweetsArray.length - endLength);
				tempRetweetsArray = retweetsArray.slice(retweetsArray.length - timeInterval, retweetsArray.length - endLength);
				tempTweetsProcessedArray = tweetsProcessedArray.slice(tweetsProcessedArray.length - timeInterval, tweetsProcessedArray.length - endLength);
				tempTweetsDirMsgProcessedArray = tweetsDirMsgProcessedArray.slice(tweetsDirMsgProcessedArray.length - timeInterval, tweetsDirMsgProcessedArray.length - endLength);
				tempFacebookPostsArray = facebookPostsArray.slice(facebookPostsArray.length - timeInterval, facebookPostsArray.length - endLength);
				tempFacebookLikesArray = facebookLikesArray.slice(facebookLikesArray.length - timeInterval, facebookLikesArray.length - endLength);
				tempFacebookProcessedArray = facebookProcessedArray.slice(facebookProcessedArray.length - timeInterval, facebookProcessedArray.length - endLength);
				tempFacebookMsgProcessedArray = facebookMsgProcessedArray.slice(facebookMsgProcessedArray.length - timeInterval, facebookMsgProcessedArray.length - endLength);
				tempSkypeMessageArray = skypeMessageArray.slice(skypeMessageArray.length - timeInterval, skypeMessageArray.length - endLength);
				tempWeChatMessageArray = weChatMessageArray.slice(weChatMessageArray.length - timeInterval, weChatMessageArray.length - endLength);
				tempKikMessageArray = kikMessageArray.slice(kikMessageArray.length - timeInterval, kikMessageArray.length - endLength);
				tempSlackPostsArray = slackPostsArray.slice(slackPostsArray.length - timeInterval, slackPostsArray.length - endLength);
				tempSlackMessageArray = slackMessageArray.slice(slackMessageArray.length - timeInterval, slackMessageArray.length - endLength);
				tempTelegramPostsArray = telegramPostsArray.slice(telegramPostsArray.length - timeInterval, telegramPostsArray.length - endLength);
				tempTelegramMessageArray = telegramMessageArray.slice(telegramMessageArray.length - timeInterval, telegramMessageArray.length - endLength);
				tempEmailArray = emailArray.slice(emailArray.length - timeInterval, emailArray.length - endLength);
				tempEmailProcessedArray = emailProcessedArray.slice(emailProcessedArray.length - timeInterval, emailProcessedArray.length - endLength);
				tempSmsSentArray = smsSentArray.slice(smsSentArray.length - timeInterval, smsSentArray.length - endLength);
				tempSmsProcessedArray = smsProcessedArray.slice(smsProcessedArray.length - timeInterval, smsProcessedArray.length - endLength);
				tempVoiceCallsArray = voiceCallsArray.slice(voiceCallsSentArray.length - timeInterval, voiceCallsSentArray.length - endLength);
				tempVoiceCallsProcessedArray = voiceCallsProcessedArray.slice(voiceCallsProcessedArray.length - timeInterval, voiceCallsProcessedArray.length - endLength);
				tempWhatsAppSentArray = whatsappSentArray.slice(whatsappSentArray.length - timeInterval, whatsappSentArray.length - endLength);
				tempWhatsAppProcessedArray = whatsappProcessedArray.slice(whatsappProcessedArray.length - timeInterval, whatsappProcessedArray.length - endLength);
				tempAlexaMessageArray = alexaMessageArray.slice(alexaMessageArray.length - timeInterval, alexaMessageArray.length - endLength);
				tempGoogleAssistantMessageArray = googleAssistantMessageArray.slice(googleAssistantMessageArray.length - timeInterval, googleAssistantMessageArray.length - endLength);
			}
			var groupByNum = 0
			if (groupByValue === "weekly") {
				groupByNum = 7;
			} else if (groupByValue === "monthly") {
				groupByNum = 30;
			}
			var botStats = {};
			var listTypeStr = "#" + listType + " li div ";
			var chatConv = { 'Chat Conversations': groupBy(tempChatConversationsArray, groupByNum), 'status': $(listTypeStr + '.chatConversations').prop('checked') };
			var chatMsg = { 'Chat Messages': groupBy(tempChatMessagesArray, groupByNum), 'status': $(listTypeStr + '.chatMessages').prop('checked') };
			var avgConvLen = { 'Avg Conv Len': groupBy(tempAverageConvLengthArray, groupByNum), 'status': $(listTypeStr + '.chatConvLength').prop('checked') };
			var engagedConv = { 'Engaged Conversations': groupBy(tempEngagedConvArray, groupByNum), 'status': $(listTypeStr + '.engagedConversations').prop('checked') };
			var defaultRes = { 'Default Responses': groupBy(tempDefaultResponseArray, groupByNum), 'status': $(listTypeStr + '.defaultResponses').prop('checked') };
			var avgConf = { 'Avg Confidence': groupBy(tempConfidenceArray, groupByNum), 'status': $(listTypeStr + '.avgConfidence').prop('checked') };
			var avgSentim = { 'Avg Sentiment': groupBy(tempSentimentArray, groupByNum), 'status': $(listTypeStr + '.avgSentiment').prop('checked') };
			var avgResTime = {'Response Time': groupBy(tempResponseTimeArray, groupByNum), 'status': $(listTypeStr + '.avgResTime').prop('checked') }
			var chatConnects = { 'Chat Connects': groupBy(tempChatConnectsArray, groupByNum), 'status': $(listTypeStr + '.chatConnects').prop('checked') };
			var chats = { 'Chats': groupBy(tempChatsArray, groupByNum), 'status': $(listTypeStr + '.chats').prop('checked') };
			var liveCht = { 'Live Chat': groupBy(tempLiveChatArray, groupByNum), 'status': $(listTypeStr + '.liveChat').prop('checked') };
			var chatErrs = { 'Chat Errors': groupBy(tempChatErrorsArray, groupByNum), 'status': $(listTypeStr + '.chatErrors').prop('checked') };
			var chatImprs = { 'Chat Imports': groupBy(tempChatImportsArray, groupByNum), 'status': $(listTypeStr + '.chatImports').prop('checked') };
						
			var tweets = { 'Tweets': groupBy(tempTweetsArray, groupByNum), 'status': $(listTypeStr + '.tweets').prop('checked') };
			var retweets = { 'Retweets': groupBy(tempRetweetsArray, groupByNum), 'status': $(listTypeStr + '.retweets').prop('checked') };
			var tweetsProcessed = { 'Tweets Processed': groupBy(tempTweetsProcessedArray, groupByNum), 'status': $(listTypeStr + '.tweetsProcessed').prop('checked') };
			var tweetsDirMsg = { 'Direct Messages': groupBy(tempTweetsDirMsgProcessedArray, groupByNum), 'status': $(listTypeStr + '.directMessages').prop('checked') };
						
			var facebookPosts = { 'Facebook Posts': groupBy(tempFacebookPostsArray, groupByNum), 'status': $(listTypeStr + '.facebookPosts').prop('checked') };
			var facebookLikes = { 'Facebook Likes': groupBy(tempFacebookLikesArray, groupByNum), 'status': $(listTypeStr + '.facebookLikes').prop('checked') };
			var facebookProcessed = { 'Facebook Processed': groupBy(tempFacebookProcessedArray, groupByNum), 'status': $(listTypeStr + '.facebookProcessed').prop('checked') };
			var facebookMessages = { 'Facebook Messages Processed': groupBy(tempFacebookMsgProcessedArray, groupByNum), 'status': $(listTypeStr + '.facebookMsgProcessed').prop('checked') };
						
			var skypeMessages = { 'Skype Messages': groupBy(tempSkypeMessageArray, groupByNum), 'status': $(listTypeStr + '.skypeMessages').prop('checked') };
			var weChatMessages = { 'WeChat Messages': groupBy(tempWeChatMessageArray, groupByNum), 'status': $(listTypeStr + '.weChatMessages').prop('checked') };
			var kikMessages = { 'Kik Messages': groupBy(tempKikMessageArray, groupByNum), 'status': $(listTypeStr + '.kikMessages').prop('checked') };
						
			var slackPost = { 'Slack Posts': groupBy(tempSlackPostsArray, groupByNum), 'status': $(listTypeStr + '.slackPosts').prop('checked') };
			var slackMessages = { 'Slack Messages': groupBy(tempSlackMessageArray, groupByNum), 'status': $(listTypeStr + '.slackMessages').prop('checked') };
						
			var telegramPosts = { 'Telegram Posts': groupBy(tempTelegramPostsArray, groupByNum), 'status': $(listTypeStr + '.telegramPosts').prop('checked') };
			var telegramMessages = { 'Telegram Messages': groupBy(tempTelegramMessageArray, groupByNum), 'status': $(listTypeStr + '.telegramMessages').prop('checked') };
						
			var emails = { 'Emails': groupBy(tempEmailArray, groupByNum), 'status': $(listTypeStr + '.email').prop('checked') };
			var emailsProcessed = { 'Emails Processed': groupBy(tempEmailProcessedArray, groupByNum), 'status': $(listTypeStr + '.emailProcessed').prop('checked') };
						
			var smsSent = { 'SMS Sent': groupBy(tempSmsSentArray, groupByNum), 'status': $(listTypeStr + '.smsSent').prop('checked') };
			var smsProcessed = { 'SMS Processed': groupBy(tempSmsProcessedArray, groupByNum), 'status': $(listTypeStr + '.smsProcessed').prop('checked') };
			
			var voiceCalls = { 'Voice Calls': groupBy(tempVoiceCallsArray, groupByNum), 'status': $(listTypeStr + '.voiceCallsSent').prop('checked') };
			var voiceCallsProcessed = { 'Voice Calls Processed': groupBy(tempVoiceCallsProcessedArray, groupByNum), 'status': $(listTypeStr + '.voiceCallsProcessed').prop('checked') };
			
			var whatsappSent = { 'WhatsApp Sent': groupBy(tempWhatsAppSentArray, groupByNum), 'status': $(listTypeStr + '.whatsappSent').prop('checked') };
			var whatsappProcessed = { 'WhatsApp Processed': groupBy(tempWhatsAppProcessedArray, groupByNum), 'status': $(listTypeStr + '.whatsappProcessed').prop('checked') };
			
			
			var alexaMessages = { 'Alexa Messages': groupBy(tempAlexaMessageArray, groupByNum), 'status': $(listTypeStr + '.alexaMessages').prop('checked') };
			
			var googleAssistantMessages = { 'Google Assistant Messages': groupBy(tempGoogleAssistantMessageArray, groupByNum), 'status': $(listTypeStr + '.googleAssistantMessages').prop('checked') };
			
			botStats['chatList'] = { 'Chat Conversations': chatConv, 'Chat Messages': chatMsg, 'Avg Conv Len': avgConvLen, 'Engaged Conversations': engagedConv,
					                 'Default Responses': defaultRes, 'Avg Confidence': avgConf, 'Avg Sentiment': avgSentim, 'Response Time': avgResTime,
					                 'Chat Connects': chatConnects, 'Chats': chats, 'Live Chat': liveCht, 'Chat Errors': chatErrs, 'Chat Imports': chatImprs };
			botStats['twitterList'] = { 'Tweets': tweets, 'Retweets': retweets, 'Tweets Processed': tweetsProcessed, 'Direct Messages': tweetsDirMsg };
			botStats['facebookList'] = { 'Facebook Posts': facebookPosts, 'Facebook Likes': facebookLikes, 'Facebook Processed': facebookProcessed, 'Facebook Messages Processed': facebookMessages };
			botStats['skypeList'] = { 'Skype Messages': skypeMessages };
			botStats['weChatList'] = { 'WeChat Messages': weChatMessages };
			botStats['kikList'] = { 'Kik Messages': kikMessages };
			botStats['slackList'] = { 'Slack Posts': slackPost, 'Slack Messages': slackMessages };
			botStats['telegramList'] = { 'Telegram Posts': telegramPosts, 'Telegram Messages': telegramMessages };
			botStats['emailList'] = { 'Emails': emails, 'Emails Processed': emailsProcessed };
			botStats['smsList'] = { 'SMS Sent': smsSent, 'SMS Processed': smsProcessed };
			botStats['ivrList'] = { 'Voice Calls': voiceCalls, 'Voice Calls Processed': voiceCallsProcessed };
			botStats['whatsappList'] = { 'WhatsApp Sent': whatsappSent, 'WhatsApp Processed': whatsappProcessed };
			botStats['alexaList'] = { 'Alexa Messages': alexaMessages };
			botStats['googleAssistantList'] = { 'Google Assistant Messages': googleAssistantMessages };
			return botStats;
		}
		
		function groupBy(array, group) {
			if (group == 0) return array;
			var i;
			var sum = 0;
			var counter = 0;
			var groupArray = new Array();
			for (i = 0; i < array.length; i++) {
				sum += array[i].y;
				if (counter == group - 1) {
					var date = { x: array[i].x, y: sum };
					groupArray.push(date);
					sum = 0;
					counter = 0;
				} else {
					counter += 1;
				}
			}
			return groupArray;
		}
		
		function generateChart(botJsonData, listType, checkBoxType, chartType, isChecked) {
			var i = 1;
			var seriesArray = [];
			var chartColorArray = [];
			if (listType === "allChartsList") {
				for (var keyOne in botJsonData) {
					if (botJsonData.hasOwnProperty(keyOne)) {
						var socialMediaType = botJsonData[keyOne];
						for (var keyTwo in socialMediaType) {
							if (socialMediaType.hasOwnProperty(keyTwo)) {
								var mediaType = socialMediaType[keyTwo];
								if (keyTwo === checkBoxType) {
									if (isChecked == true) {
										mediaType.status = true;
										seriesArray.push( { name: ('series-' + i), data: mediaType[keyTwo] } );
										chartColorArray.push(keyTwo);
										i++;
									} else {
										mediaType.status = false;
									}
								} else {
									if (mediaType.status == true) {
										seriesArray.push( { name: ('series-' + i), data: mediaType[keyTwo] } );
										chartColorArray.push(keyTwo);
										i++;
									}
								}
							}
						}
					}
				}
			} else {
				var socialMediaType = botJsonData[listType];
				for (var key in socialMediaType) {
					if(socialMediaType.hasOwnProperty(key)) {
						var mediaType = socialMediaType[key];
						if (key === checkBoxType) {
							if (isChecked == true) {
								mediaType.status = true;
								seriesArray.push( { name: ('series-' + i), data: mediaType[key] } );
								chartColorArray.push(key);
								i++;
							} else {
								mediaType.status = false;
							}
						} else {
							if (mediaType.status == true) {
								seriesArray.push( { name: ('series-' + i), data: mediaType[key] } );
								chartColorArray.push(key);
								i++;
							}
						}
					}
				}
			}
			setCheckBoxLabelColor(listType, chartColorArray);
			drawCharts(seriesArray, chartType);
		}
		/* Generate initial chat graph for current week duration with no grouping */
		var botStatsData = generateAnalyticsData("weekly", "none", "chatList");
		generateChart(botStatsData, "chatList", "", "#chatChart", false);
		
		function setCheckBoxLabelColor(listType, checkBoxArray) {
			var listTypeStr = "#" + listType + " li";
			$(listTypeStr).each(function(li) {
			    var checkBoxName = $(this).children(0).children(0).attr("name");
			    var index = checkBoxArray.indexOf(checkBoxName);
			    if (index == 0) {
			    	$(this).children(0).children(1).css("color", "#5f6287");
			    } else if (index == 1) {
			    	$(this).children(0).children(1).css("color", "#ff832c");
			    } else if (index == 2) {
			    	$(this).children(0).children(1).css("color", "#ff5c5c");
			    } else if (index == 3) {
			    	$(this).children(0).children(1).css("color", "#5271ff");
			    } else if (index == 4) {
			    	$(this).children(0).children(1).css("color", "#ffe557");
			    } else if (index == 5) {
			    	$(this).children(0).children(1).css("color", "#7fca58");
			    } else if (index == 6) {
			    	$(this).children(0).children(1).css("color", "#999966");
			    } else if (index == 7) {
			    	$(this).children(0).children(1).css("color", "#996633");
			    } else if (index == 8) {
			    	$(this).children(0).children(1).css("color", "#cc9900");
			    } else if (index == 9) {
			    	$(this).children(0).children(1).css("color", "#339966");
			    } else if (index == 10) {
			    	$(this).children(0).children(1).css("color", "#0099ff");
			    } else if (index == 11) {
			    	$(this).children(0).children(1).css("color", "#ff6666");
			    }else if (index == 12) {
			    	$(this).children(0).children(1).css("color", "#99ccff");
			    } else if (index == 13) {
			    	$(this).children(0).children(1).css("color", "#ffcc99");
			    } else if (index == 14) {
			    	$(this).children(0).children(1).css("color", "#3366cc");
			    } else if (index == 15) {
			    	$(this).children(0).children(1).css("color", "#ff9999");
			    } else if (index == 16) {
			    	$(this).children(0).children(1).css("color", "#00ffff");
			    } else if (index == 17) {
			    	$(this).children(0).children(1).css("color", "#b32d00");
			    } else if (index == 18) {
			    	$(this).children(0).children(1).css("color", "#cc66ff");
			    } else if (index == 19) {
			    	$(this).children(0).children(1).css("color", "#666699");
			    } else if (index == 20) {
			    	$(this).children(0).children(1).css("color", "#6666ff");
			    } else if (index == 21) {
			    	$(this).children(0).children(1).css("color", "#8cff66");
			    } else if (index == 22) {
			    	$(this).children(0).children(1).css("color", "#ffdab3");
			    } else if (index == 23) {
			    	$(this).children(0).children(1).css("color", "#ff8400");
			    } else if (index == 24) {
			    	$(this).children(0).children(1).css("color", "#888844");
			    } else if (index == 25) {
			    	$(this).children(0).children(1).css("color", "#b399ff");
			    } else {
			    	$(this).children(0).children(1).css("color", "inherit");
			    }
			});
		}
		
		function resetCheckBoxes(listType) {
			$(listType).each(function(li) {
			    var checkBoxName = $(this).children(0).children(0).attr("name");
			    if (checkBoxName === "Chat Conversations" || checkBoxName === "Chat Messages" || checkBoxName === "Tweets" ||
			        checkBoxName === "Facebook Messages Processed" || checkBoxName === "Skype Messages" || checkBoxName === "WeChat Messages" ||
			        checkBoxName === "Kik Messages" || checkBoxName === "Slack Messages" || checkBoxName === "Telegram Messages" || 
			        checkBoxName === "Emails" || checkBoxName === "SMS Sent" || checkBoxName === "WhatsApp Sent" || checkBoxName === "Voice Calls" || checkBoxName === "Alexa Messages" || checkBoxName === "Google Assistant Messages") {
			    	$(this).children(0).children(0).prop("checked", true);
			    } else {
			    	$(this).children(0).children(0).prop("checked", false);
			    }
			});
		}
		
		function drawCharts(seriesArray, chartType) {
			var chart = new Chartist.Line(chartType, {
				  series: seriesArray 
				},
				{
					axisX: {
					    type: Chartist.FixedScaleAxis,
					    divisor: 12,
					    labelInterpolationFnc: function(value) {
					      return moment(value).format('MMM D');
					    }
					},
					axisY: {
				         scaleMinSpace: 30
				    }
			});
		}
		/* ####################### Chat ########################### */
		$('.chatConversations').change(function() {
			var chartType = "#chatChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		$('.chatMessages').change(function() {
			var chartType = "#chatChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		$('.chatConvLength').change(function() {
			var chartType = "#chatChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		$('.engagedConversations').change(function() {
			var chartType = "#chatChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		$('.defaultResponses').change(function() {
			var chartType = "#chatChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		$('.avgConfidence').change(function() {
			var chartType = "#chatChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		$('.avgSentiment').change(function() {
			var chartType = "#chatChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		$('.avgResTime').change(function() {
			var chartType = "#chatChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		$('.chatConnects').change(function() {
			var chartType = "#chatChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		$('.chats').change(function() {
			var chartType = "#chatChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		$('.liveChat').change(function() {
			var chartType = "#chatChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		$('.chatErrors').change(function() {
			var chartType = "#chatChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		$('.chatResTime').change(function() {
			var chartType = "#chatChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		$('.chatImports').change(function() {
			var chartType = "#chatChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		/* ####################### Twitter ###########################*/
		$('.tweets').change(function() {
			var chartType = "#twitterChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		$('.retweets').change(function() {
			var chartType = "#twitterChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		$('.tweetsProcessed').change(function() {
			var chartType = "#twitterChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		$('.directMessages').change(function() {
			var chartType = "#twitterChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		/* ####################### Facebook ###########################*/
		$('.facebookPosts').change(function() {
			var chartType = "#facebookChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		$('.facebookLikes').change(function() {
			var chartType = "#facebookChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		$('.facebookProcessed').change(function() {
			var chartType = "#facebookChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		$('.facebookMsgProcessed').change(function() {
			var chartType = "#facebookChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		/* ####################### Skype ###########################*/
		$('.skypeMessages').change(function() {
			var chartType = "#skypeChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		/* ####################### WeChat ###########################*/
		$('.weChatMessages').change(function() {
			var chartType = "#weChatChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		/* ####################### Kik ###########################*/
		$('.kikMessages').change(function() {
			var chartType = "#kikChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		/* ####################### Slack ###########################*/
		$('.slackPosts').change(function() {
			var chartType = "#slackChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		$('.slackMessages').change(function() {
			var chartType = "#slackChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		/* ####################### Telegram ###########################*/
		$('.telegramPosts').change(function() {
			var chartType = "#telegramChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		$('.telegramMessages').change(function() {
			var chartType = "#telegramChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		/* ####################### Email ###########################*/
		$('.email').change(function() {
			var chartType = "#emailChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		$('.emailProcessed').change(function() {
			var chartType = "#emailChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		/* ####################### SMS ########################### */
		$('.smsSent').change(function() {
			var chartType = "#smsChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		$('.smsProcessed').change(function() {
			var chartType = "#smsChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		/* ####################### IVR ########################### */
		$('.voiceCalls').change(function() {
			var chartType = "#voiceCallsChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		$('.voiceCallsProcessed').change(function() {
			var chartType = "#voiceCallsChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		/* ####################### WhatsApp ########################### */
		$('.whatsappSent').change(function() {
			var chartType = "#whatsappChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		$('.whatsappProcessed').change(function() {
			var chartType = "#whatsappChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		/* ####################### Alexa ###########################*/
		$('.alexaMessages').change(function() {
			var chartType = "#alexaChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		/* ####################### Google Assistant ###########################*/
		$('.googleAssistantMessages').change(function() {
			var chartType = "#googleAssistantChart";
			var listType = $(this).parent().parent().parent().attr('id');
			var checkBoxType = $(this).attr('name');
			if (listType === "allChartsList") {
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
			if ($(this).prop("checked")) {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, true);
			} else {
				generateChart(botAnalticsData, listType, checkBoxType, chartType, false);
			}
		});
		/* ################# Date Range Drop Down Select Box ################# */
		$("#datesRangeSelect").on("change", function() {
			var listType = "";
			var chartType = "";
			var dateRangeValue = this.value;
			var mediaTypeSelect = $("#chatType").val();
			if (mediaTypeSelect === "chat") {
				listType = "chatList";
				chartType = "#chatChart";
				if (dateRangeValue === "today") {
					$("#chatStatsDiv").css("display", "none");
					$("#chatStatsTodayDiv").css("display", "inherit");
				} else {
					$("#chatStatsDiv").css("display", "inherit");
					$("#chatStatsTodayDiv").css("display", "none");
				}
			} else if (mediaTypeSelect === "twitter") {
				listType = "twitterList";
				chartType = "#twitterChart";
				if (dateRangeValue === "today") {
					$("#twitterStatsDiv").css("display", "none");
					$("#twitterStatsTodayDiv").css("display", "inherit");
				} else {
					$("#twitterStatsDiv").css("display", "inherit");
					$("#twitterStatsTodayDiv").css("display", "none");
				}
			} else if (mediaTypeSelect === "facebook") {
				listType = "facebookList";
				chartType = "#facebookChart";
				if (dateRangeValue === "today") {
					$("#facebookStatsDiv").css("display", "none");
					$("#facebookStatsTodayDiv").css("display", "inherit");
				} else {
					$("#facebookStatsDiv").css("display", "inherit");
					$("#facebookStatsTodayDiv").css("display", "none");
				}
			} else if (mediaTypeSelect === "skype") {
				listType = "skypeList";
				chartType = "#skypeChart";
				if (dateRangeValue === "today") {
					$("#skypeStatsDiv").css("display", "none");
					$("#skypeStatsTodayDiv").css("display", "inherit");
				} else {
					$("#skypeStatsDiv").css("display", "inherit");
					$("#skypeStatsTodayDiv").css("display", "none");
				}
			} else if (mediaTypeSelect === "wechat") {
				listType = "weChatList";
				chartType = "#weChatChart";
				if (dateRangeValue === "today") {
					$("#wechatStatsDiv").css("display", "none");
					$("#wechatStatsTodayDiv").css("display", "inherit");
				} else {
					$("#wechatStatsDiv").css("display", "inherit");
					$("#wechatStatsTodayDiv").css("display", "none");
				}
			} else if (mediaTypeSelect === "kik") {
				listType = "kikList";
				chartType = "#kikChart";
				if (dateRangeValue === "today") {
					$("#kikStatsDiv").css("display", "none");
					$("#kikStatsTodayDiv").css("display", "inherit");
				} else {
					$("#kikStatsDiv").css("display", "inherit");
					$("#kikStatsTodayDiv").css("display", "none");
				}
			} else if (mediaTypeSelect === "slack") {
				listType = "slackList";
				chartType = "#slackChart";
				if (dateRangeValue === "today") {
					$("#slackStatsDiv").css("display", "none");
					$("#slackStatsTodayDiv").css("display", "inherit");
				} else {
					$("#slackStatsDiv").css("display", "inherit");
					$("#slackStatsTodayDiv").css("display", "none");
				}
			} else if (mediaTypeSelect === "telegram") {
				listType = "telegramList";
				chartType = "#telegramChart";
				if (dateRangeValue === "today") {
					$("#telegramStatsDiv").css("display", "none");
					$("#telegramStatsTodayDiv").css("display", "inherit");
				} else {
					$("#telegramStatsDiv").css("display", "inherit");
					$("#telegramStatsTodayDiv").css("display", "none");
				}
			} else if (mediaTypeSelect === "email") {
				listType = "emailList";
				chartType = "#emailChart";
				if (dateRangeValue === "today") {
					$("#emailStatsDiv").css("display", "none");
					$("#emailStatsTodayDiv").css("display", "inherit");
				} else {
					$("#emailStatsDiv").css("display", "inherit");
					$("#emailStatsTodayDiv").css("display", "none");
				}
			} else if (mediaTypeSelect === "sms") {
				listType = "smsList";
				chartType = "#smsChart";
				if (dateRangeValue === "today") {
					$("#smsStatsDiv").css("display", "none");
					$("#smsStatsTodayDiv").css("display", "inherit");
				} else {
					$("#smsStatsDiv").css("display", "inherit");
					$("#smsStatsTodayDiv").css("display", "none");
				}
			} else if (mediaTypeSelect === "ivr") {
				listType = "voiceCallsList";
				chartType = "#voiceCallsChart";
				if (dateRangeValue === "today") {
					$("#ivrStatsDiv").css("display", "none");
					$("#ivrStatsTodayDiv").css("display", "inherit");
				} else {
					$("#ivrStatsDiv").css("display", "inherit");
					$("#ivrStatsTodayDiv").css("display", "none");
				}
			} else if (mediaTypeSelect === "whatsapp") {
				listType = "whatsappList";
				chartType = "#whatsappChart";
				if (dateRangeValue === "today") {
					$("#whatsappStatsDiv").css("display", "none");
					$("#whatsappStatsTodayDiv").css("display", "inherit");
				} else {
					$("#whatsappStatsDiv").css("display", "inherit");
					$("#whatsappStatsTodayDiv").css("display", "none");
				}
			} else if (mediaTypeSelect === "alexa") {
				listType = "alexaList";
				chartType = "#alexaChart";
				if (dateRangeValue === "today") {
					$("#alexaStatsDiv").css("display", "none");
					$("#alexaStatsTodayDiv").css("display", "inherit");
				} else {
					$("#alexaStatsDiv").css("display", "inherit");
					$("#alexaStatsTodayDiv").css("display", "none");
				}
			} else if (mediaTypeSelect === "googleAssistant") {
				listType = "googleAssistantList";
				chartType = "#googleAssistantChart";
				if (dateRangeValue === "today") {
					$("#googleAssistantStatsDiv").css("display", "none");
					$("#googleAssistantStatsTodayDiv").css("display", "inherit");
				} else {
					$("#googleAssistantStatsDiv").css("display", "inherit");
					$("#googleAssistantStatsTodayDiv").css("display", "none");
				}
			} else if (mediaTypeSelect === "all") {
				listType = "allChartsList";
				chartType = "#allCharts";
				if (dateRangeValue === "today") {
					$("#allStatsDiv").css("display", "none");
					$("#allStatsTodayDiv").css("display", "inherit");
				} else {
					$("#allStatsDiv").css("display", "inherit");
					$("#allStatsTodayDiv").css("display", "none");
				}
			}
			var botAnalticsData = generateAnalyticsData(dateRangeValue, $('#groupBySelect').val(), listType);
			generateChart(botAnalticsData, listType, "", chartType, false);
		});
		/* ################# Group By Drop Down Select Box ################# */
		$("#groupBySelect").on("change", function() {
			var duration = $("#datesRangeSelect").val();
			var listType = "";
			var chartType = "";
			var groupByValue = this.value;
			var mediaTypeSelect = $("#chatType").val();
			if (mediaTypeSelect === "chat") {
				listType = "chatList";
				chartType = "#chatChart";
			} else if (mediaTypeSelect === "twitter") {
				listType = "twitterList";
				chartType = "#twitterChart";
			} else if (mediaTypeSelect === "facebook") {
				listType = "facebookList";
				chartType = "#facebookChart";
			} else if (mediaTypeSelect === "skype") {
				listType = "skypeList";
				chartType = "#skypeChart";
			} else if (mediaTypeSelect === "wechat") {
				listType = "weChatList";
				chartType = "#weChatChart";
			} else if (mediaTypeSelect === "kik") {
				listType = "kikList";
				chartType = "#kikChart";
			} else if (mediaTypeSelect === "slack") {
				listType = "slackList";
				chartType = "#slackChart";
			} else if (mediaTypeSelect === "telegram") {
				listType = "telegramList";
				chartType = "#telegramChart";
			} else if (mediaTypeSelect === "email") {
				listType = "emailList";
				chartType = "#emailChart";
			} else if (mediaTypeSelect === "sms") {
				listType = "smsList";
				chartType = "#smsChart";
			} else if (mediaTypeSelect === "ivr") {
				listType = "voiceCallsList";
				chartType = "#voiceCallsChart";
			} else if (mediaTypeSelect === "whatsapp") {
				listType = "whatsappList";
				chartType = "#whatsappChart";
			} else if (mediaTypeSelect === "alexa") {
				listType = "alexaList";
				chartType = "#alexaChart";
			} else if (mediaTypeSelect === "googleAssistant") {
				listType = "googleAssistantList";
				chartType = "#googleAssistantChart";
			} else if (mediaTypeSelect === "all") {
				listType = "allChartsList";
				chartType = "#allCharts";
			}
			var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), groupByValue, listType);
			generateChart(botAnalticsData, listType, "", chartType, false);
		});
		/* ################# Social Media Type List Drop Down Select Box ################# */
		$("#chatType").on("change", function() {
			var chatValue = this.value;
			var duration = $("#datesRangeSelect").val();
			if (chatValue === "chat") {
				$("#chatDiv").css("display", "block");
				if (duration === "today") {
					$("#chatStatsDiv").css("display", "none");
					$("#chatStatsTodayDiv").css("display", "inherit");
				} else {
					$("#chatStatsDiv").css("display", "inherit");
					$("#chatStatsTodayDiv").css("display", "none");
				}
				$("#twitterDiv").css("display", "none");
				$("#facebookDiv").css("display", "none");
				$("#skypeDiv").css("display", "none");
				$("#kikDiv").css("display", "none");
				$("#weChatDiv").css("display", "none");
				$("#slackDiv").css("display", "none");
				$("#telegramDiv").css("display", "none");
				$("#emailDiv").css("display", "none");
				$("#smsDiv").css("display", "none");
				$("#ivrDiv").css("display", "none");
				$("#whatsappDiv").css("display", "none");
				$("#alexaDiv").css("display", "none");
				$("#googleAssistantDiv").css("display", "none");
				$("#allChartsDiv").css("display", "none");
				resetCheckBoxes("#chatList li");
				var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), "chatList");
				generateChart(botAnalticsData, "chatList", "", "#chatChart", false);
			} else if (chatValue === "twitter") {
				$("#chatDiv").css("display", "none");
				$("#twitterDiv").css("display", "block");
				if (duration === "today") {
					$("#tweetStatsDiv").css("display", "none");
					$("#tweetStatsTodayDiv").css("display", "inherit");
				} else {
					$("#tweetStatsDiv").css("display", "inherit");
					$("#tweetStatsTodayDiv").css("display", "none");
				}
				$("#facebookDiv").css("display", "none");
				$("#skypeDiv").css("display", "none");
				$("#kikDiv").css("display", "none");
				$("#weChatDiv").css("display", "none");
				$("#slackDiv").css("display", "none");
				$("#telegramDiv").css("display", "none");
				$("#emailDiv").css("display", "none");
				$("#smsDiv").css("display", "none");
				$("#ivrDiv").css("display", "none");
				$("#whatsappDiv").css("display", "none");
				$("#alexaDiv").css("display", "none");
				$("#googleAssistantDiv").css("display", "none");
				$("#allChartsDiv").css("display", "none");
				resetCheckBoxes("#twitterList li");
				var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), "twitterList");
				generateChart(botAnalticsData, "twitterList", "", "#twitterChart", false);
			} else if (chatValue === "facebook") {
				$("#chatDiv").css("display", "none");
				$("#twitterDiv").css("display", "none");
				$("#facebookDiv").css("display", "block");
				if (duration === "today") {
					$("#facebookStatsDiv").css("display", "none");
					$("#facebookStatsTodayDiv").css("display", "inherit");
				} else {
					$("#facebookStatsDiv").css("display", "inherit");
					$("#facebookStatsTodayDiv").css("display", "none");
				}
				$("#skypeDiv").css("display", "none");
				$("#kikDiv").css("display", "none");
				$("#weChatDiv").css("display", "none");
				$("#slackDiv").css("display", "none");
				$("#telegramDiv").css("display", "none");
				$("#emailDiv").css("display", "none");
				$("#smsDiv").css("display", "none");
				$("#ivrDiv").css("display", "none");
				$("#whatsappDiv").css("display", "none");
				$("#alexaDiv").css("display", "none");
				$("#googleAssistantDiv").css("display", "none");
				$("#allChartsDiv").css("display", "none");
				resetCheckBoxes("#facebookList li");
				var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), "facebookList");
				generateChart(botAnalticsData, "facebookList", "", "#facebookChart", false);
			} else if (chatValue === "skype") {
				$("#chatDiv").css("display", "none");
				$("#twitterDiv").css("display", "none");
				$("#facebookDiv").css("display", "none");
				$("#skypeDiv").css("display", "block");
				if (duration === "today") {
					$("#skypeStatsDiv").css("display", "none");
					$("#skypeStatsTodayDiv").css("display", "inherit");
				} else {
					$("#skypeStatsDiv").css("display", "inherit");
					$("#skypeStatsTodayDiv").css("display", "none");
				}
				$("#kikDiv").css("display", "none");
				$("#weChatDiv").css("display", "none");
				$("#slackDiv").css("display", "none");
				$("#telegramDiv").css("display", "none");
				$("#emailDiv").css("display", "none");
				$("#smsDiv").css("display", "none");
				$("#ivrDiv").css("display", "none");
				$("#whatsappDiv").css("display", "none");
				$("#alexaDiv").css("display", "none");
				$("#googleAssistantDiv").css("display", "none");
				$("#allChartsDiv").css("display", "none");
				resetCheckBoxes("#skypeList li");
				var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), "skypeList");
				generateChart(botAnalticsData, "skypeList", "", "#skypeChart", false);
			} else if (chatValue === "wechat") {
				$("#chatDiv").css("display", "none");
				$("#twitterDiv").css("display", "none");
				$("#facebookDiv").css("display", "none");
				$("#skypeDiv").css("display", "none");
				$("#kikDiv").css("display", "none");
				$("#weChatDiv").css("display", "block");
				if (duration === "today") {
					$("#wechatStatsDiv").css("display", "none");
					$("#wechatStatsTodayDiv").css("display", "inherit");
				} else {
					$("#wechatStatsDiv").css("display", "inherit");
					$("#wechatStatsTodayDiv").css("display", "none");
				}
				$("#slackDiv").css("display", "none");
				$("#telegramDiv").css("display", "none");
				$("#emailDiv").css("display", "none");
				$("#smsDiv").css("display", "none");
				$("#ivrDiv").css("display", "none");
				$("#whatsappDiv").css("display", "none");
				$("#alexaDiv").css("display", "none");
				$("#googleAssistantDiv").css("display", "none");
				$("#allChartsDiv").css("display", "none");
				resetCheckBoxes("#weChatList li");
				var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), "weChatList");
				generateChart(botAnalticsData, "weChatList", "", "#weChatChart", false);
			} else if (chatValue === "kik") {
				$("#chatDiv").css("display", "none");
				$("#twitterDiv").css("display", "none");
				$("#facebookDiv").css("display", "none");
				$("#skypeDiv").css("display", "none");
				$("#kikDiv").css("display", "block");
				if (duration === "today") {
					$("#kikStatsDiv").css("display", "none");
					$("#kikStatsTodayDiv").css("display", "inherit");
				} else {
					$("#kikStatsDiv").css("display", "inherit");
					$("#kikStatsTodayDiv").css("display", "none");
				}
				$("#weChatDiv").css("display", "none");
				$("#slackDiv").css("display", "none");
				$("#telegramDiv").css("display", "none");
				$("#emailDiv").css("display", "none");
				$("#smsDiv").css("display", "none");
				$("#ivrDiv").css("display", "none");
				$("#whatsappDiv").css("display", "none");
				$("#alexaDiv").css("display", "none");
				$("#googleAssistantDiv").css("display", "none");
				$("#allChartsDiv").css("display", "none");
				resetCheckBoxes("#kikList li");
				var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), "kikList");
				generateChart(botAnalticsData, "kikList", "", "#kikChart", false);
			} else if (chatValue === "slack") {
				$("#chatDiv").css("display", "none");
				$("#twitterDiv").css("display", "none");
				$("#facebookDiv").css("display", "none");
				$("#skypeDiv").css("display", "none");
				$("#kikDiv").css("display", "none");
				$("#weChatDiv").css("display", "none");
				$("#slackDiv").css("display", "block");
				if (duration === "today") {
					$("#slackStatsDiv").css("display", "none");
					$("#slackStatsTodayDiv").css("display", "inherit");
				} else {
					$("#slackStatsDiv").css("display", "inherit");
					$("#slackStatsTodayDiv").css("display", "none");
				}
				$("#telegramDiv").css("display", "none");
				$("#emailDiv").css("display", "none");
				$("#smsDiv").css("display", "none");
				$("#ivrDiv").css("display", "none");
				$("#whatsappDiv").css("display", "none");
				$("#alexaDiv").css("display", "none");
				$("#googleAssistantDiv").css("display", "none");
				$("#allChartsDiv").css("display", "none");
				resetCheckBoxes("#slackList li");
				var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), "slackList");
				generateChart(botAnalticsData, "slackList", "", "#slackChart", false);
			} else if (chatValue === "telegram") {
				$("#chatDiv").css("display", "none");
				$("#twitterDiv").css("display", "none");
				$("#facebookDiv").css("display", "none");
				$("#skypeDiv").css("display", "none");
				$("#kikDiv").css("display", "none");
				$("#weChatDiv").css("display", "none");
				$("#slackDiv").css("display", "none");
				$("#telegramDiv").css("display", "block");
				if (duration === "today") {
					$("#telegramStatsDiv").css("display", "none");
					$("#telegramStatsTodayDiv").css("display", "inherit");
				} else {
					$("#telegramStatsDiv").css("display", "inherit");
					$("#telegramStatsTodayDiv").css("display", "none");
				}
				$("#emailDiv").css("display", "none");
				$("#smsDiv").css("display", "none");
				$("#ivrDiv").css("display", "none");
				$("#whatsappDiv").css("display", "none");
				$("#alexaDiv").css("display", "none");
				$("#googleAssistantDiv").css("display", "none");
				$("#allChartsDiv").css("display", "none");
				resetCheckBoxes("#telegramList li");
				var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), "telegramList");
				generateChart(botAnalticsData, "telegramList", "", "#telegramChart", false);
			} else if (chatValue === "email") {
				$("#chatDiv").css("display", "none");
				$("#twitterDiv").css("display", "none");
				$("#facebookDiv").css("display", "none");
				$("#skypeDiv").css("display", "none");
				$("#kikDiv").css("display", "none");
				$("#weChatDiv").css("display", "none");
				$("#slackDiv").css("display", "none");
				$("#telegramDiv").css("display", "none");
				$("#emailDiv").css("display", "block");
				if (duration === "today") {
					$("#emailStatsDiv").css("display", "none");
					$("#emailStatsTodayDiv").css("display", "inherit");
				} else {
					$("#emailStatsDiv").css("display", "inherit");
					$("#emailStatsTodayDiv").css("display", "none");
				}
				$("#smsDiv").css("display", "none");
				$("#ivrDiv").css("display", "none");
				$("#whatsappDiv").css("display", "none");
				$("#alexaDiv").css("display", "none");
				$("#googleAssistantDiv").css("display", "none");
				$("#allChartsDiv").css("display", "none");
				resetCheckBoxes("#emailList li");
				var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), "emailList");
				generateChart(botAnalticsData, "emailList", "", "#emailChart", false);
			} else if (chatValue === "sms") {
				$("#chatDiv").css("display", "none");
				$("#twitterDiv").css("display", "none");
				$("#facebookDiv").css("display", "none");
				$("#skypeDiv").css("display", "none");
				$("#kikDiv").css("display", "none");
				$("#weChatDiv").css("display", "none");
				$("#slackDiv").css("display", "none");
				$("#telegramDiv").css("display", "none");
				$("#emailDiv").css("display", "none");
				$("#smsDiv").css("display", "block");
				if (duration === "today") {
					$("#smsStatsDiv").css("display", "none");
					$("#smsStatsTodayDiv").css("display", "inherit");
				} else {
					$("#smsStatsDiv").css("display", "inherit");
					$("#smsStatsTodayDiv").css("display", "none");
				}
				$("#ivrDiv").css("display", "none");
				$("#whatsappDiv").css("display", "none");
				$("#alexaDiv").css("display", "none");
				$("#googleAssistantDiv").css("display", "none");
				$("#allChartsDiv").css("display", "none");
				resetCheckBoxes("#smsList li");
				var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), "smsList");
				generateChart(botAnalticsData, "smsList", "", "#smsChart", false);
			} else if (chatValue === "ivr") {
				$("#chatDiv").css("display", "none");
				$("#twitterDiv").css("display", "none");
				$("#facebookDiv").css("display", "none");
				$("#skypeDiv").css("display", "none");
				$("#kikDiv").css("display", "none");
				$("#weChatDiv").css("display", "none");
				$("#slackDiv").css("display", "none");
				$("#telegramDiv").css("display", "none");
				$("#emailDiv").css("display", "none");
				$("#smsDiv").css("display", "none");
				$("#ivrDiv").css("display", "block");
				if (duration === "today") {
					$("#ivrStatsDiv").css("display", "none");
					$("#ivrStatsTodayDiv").css("display", "inherit");
				} else {
					$("#ivrStatsDiv").css("display", "inherit");
					$("#ivrStatsTodayDiv").css("display", "none");
				}
				$("#whatsappDiv").css("display", "none");
				$("#alexaDiv").css("display", "none");
				$("#googleAssistantDiv").css("display", "none");
				$("#allChartsDiv").css("display", "none");
				resetCheckBoxes("#ivrList li");
				var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), "ivrList");
				generateChart(botAnalticsData, "ivrList", "", "#ivrChart", false);
			} else if (chatValue === "whatsapp") {
				$("#chatDiv").css("display", "none");
				$("#twitterDiv").css("display", "none");
				$("#facebookDiv").css("display", "none");
				$("#skypeDiv").css("display", "none");
				$("#kikDiv").css("display", "none");
				$("#weChatDiv").css("display", "none");
				$("#slackDiv").css("display", "none");
				$("#telegramDiv").css("display", "none");
				$("#emailDiv").css("display", "none");
				$("#smsDiv").css("display", "none");
				$("#ivrDiv").css("display", "none");
				$("#whatsappDiv").css("display", "block");
				if (duration === "today") {
					$("#whatsappStatsDiv").css("display", "none");
					$("#whatsappStatsTodayDiv").css("display", "inherit");
				} else {
					$("#whatsappStatsDiv").css("display", "inherit");
					$("#whatsappStatsTodayDiv").css("display", "none");
				}
				$("#alexaDiv").css("display", "none");
				$("#googleAssistantDiv").css("display", "none");
				$("#allChartsDiv").css("display", "none");
				resetCheckBoxes("#whatsappList li");
				var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), "whatsappList");
				generateChart(botAnalticsData, "whatsappList", "", "#whatsappChart", false);
			} else if (chatValue === "alexa") {
				$("#chatDiv").css("display", "none");
				$("#twitterDiv").css("display", "none");
				$("#facebookDiv").css("display", "none");
				$("#skypeDiv").css("display", "none");
				$("#kikDiv").css("display", "none");
				$("#weChatDiv").css("display", "none");
				$("#slackDiv").css("display", "none");
				$("#telegramDiv").css("display", "none");
				$("#emailDiv").css("display", "none");
				$("#smsDiv").css("display", "none");
				$("#ivrDiv").css("display", "none");
				$("#whatsappDiv").css("display", "none");
				$("#alexaDiv").css("display", "block");
				if (duration === "today") {
					$("#alexaStatsDiv").css("display", "none");
					$("#alexaStatsTodayDiv").css("display", "inherit");
				} else {
					$("#alexaStatsDiv").css("display", "inherit");
					$("#alexaStatsTodayDiv").css("display", "none");
				}
				$("#googleAssistantDiv").css("display", "none");
				$("#allChartsDiv").css("display", "none");
				resetCheckBoxes("#alexaList li");
				var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), "alexaList");
				generateChart(botAnalticsData, "alexaList", "", "#alexaChart", false);
			} else if (chatValue === "googleAssistant") {
				$("#chatDiv").css("display", "none");
				$("#twitterDiv").css("display", "none");
				$("#facebookDiv").css("display", "none");
				$("#skypeDiv").css("display", "none");
				$("#kikDiv").css("display", "none");
				$("#weChatDiv").css("display", "none");
				$("#slackDiv").css("display", "none");
				$("#telegramDiv").css("display", "none");
				$("#emailDiv").css("display", "none");
				$("#smsDiv").css("display", "none");
				$("#ivrDiv").css("display", "none");
				$("#whatsappDiv").css("display", "none");
				$("#alexaDiv").css("display", "none");
				$("#googleAssistantDiv").css("display", "block");
				if (duration === "today") {
					$("#googleAssistantStatsDiv").css("display", "none");
					$("#googleAssistantStatsTodayDiv").css("display", "inherit");
				} else {
					$("#googleAssistantStatsDiv").css("display", "inherit");
					$("#googleAssistantStatsTodayDiv").css("display", "none");
				}
				$("#allChartsDiv").css("display", "none");
				resetCheckBoxes("#googleAssistantList li");
				var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), "googleAssistantList");
				generateChart(botAnalticsData, "googleAssistantList", "", "#googleAssistantChart", false);
			} else if(chatValue === "all") {
				$("#chatDiv").css("display", "none");
				$("#twitterDiv").css("display", "none");
				$("#facebookDiv").css("display", "none");
				$("#skypeDiv").css("display", "none");
				$("#kikDiv").css("display", "none");
				$("#weChatDiv").css("display", "none");
				$("#slackDiv").css("display", "none");
				$("#telegramDiv").css("display", "none");
				$("#emailDiv").css("display", "none");
				$("#smsDiv").css("display", "none");
				$("#ivrDiv").css("display", "none");
				$("#whatsappDiv").css("display", "none");
				$("#alexaDiv").css("display", "none");
				$("#googleAssistantDiv").css("display", "none");
				$("#allChartsDiv").css("display", "block");
				if (duration === "today") {
					$("#allStatsDiv").css("display", "none");
					$("#allStatsTodayDiv").css("display", "inherit");
				} else {
					$("#allStatsDiv").css("display", "inherit");
					$("#allStatsTodayDiv").css("display", "none");
				}
				resetCheckBoxes("#allChartsList li");
				var botAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), "allChartsList");
				generateChart(botAnalticsData, "allChartsList", "", "#allCharts", false);
			} 
		});
	</script>
	<jsp:include page="footer.jsp"/>
</body>
</html>
