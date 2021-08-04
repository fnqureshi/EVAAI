<%@page import="org.botlibre.web.chat.ChannelAttachment"%>
<%@page import="java.util.Calendar"%>
<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.botlibre.web.chat.ChatMessage"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Collections"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page"%>
<%@page import="org.botlibre.web.bean.LiveChatBean"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.service.LiveChatStats"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	try {

	boolean embed = loginBean.isEmbedded();
	LiveChatBean liveChatBean = loginBean.getBean(LiveChatBean.class);
	String title = "Live Chat";
	if (liveChatBean.getInstance() != null) {
		title = liveChatBean.getInstance().getName();
	}
%>

<!DOCTYPE HTML>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<jsp:include page="head.jsp"/>
		<title><%= title %> Logs - <%= Site.NAME %></title>
		<meta name="description" content="View stats for the channel"/>	
		<meta name="keywords" content="stats, live chat, channel"/>
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
			#datesRangeSelect, #groupBySelect {
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
				stroke: #5271ff; /* navy blue */
			}
			.ct-series-a .ct-point {
				stroke: #5271ff; /* navy blue */
			}
			.ct-series-b .ct-line {
				stroke: #ff832c; /* orange pink */
			}
			.ct-series-b .ct-point {
				stroke: #ff832c; /* orange pink */
			}
			.ct-series-c .ct-line {
				stroke: #ff5c5c; /* pink */
			}
			.ct-series-c .ct-point {
				stroke: #ff5c5c; /* pink */
			}
			.ct-series-d .ct-line {
				stroke: #339966; /* dull green */
			}
			.ct-series-d .ct-point {
				stroke: #339966; /* dull green */
			}
			.ct-series-e .ct-line {
				stroke: #99ccff; /* light blue */
			}
			.ct-series-e .ct-point {
				stroke: #99ccff; /* light blue */
			}
		</style>
	</head>
	<% if (embed) { %>
		<body style="background-color: #fff;">
		<jsp:include page="channel-banner.jsp"/>
		<jsp:include page="admin-channel-banner.jsp"/>
		<div id="mainbody">
		<div class="about">
	<% } else { %>
		<body>
			<script>
				$(document).ready(function() {
					$("#liveChatStats").tablesorter({widgets: ['zebra']});
				});
			</script>
			<% loginBean.setPageType(Page.Admin); %>
			<jsp:include page="banner.jsp"/>
			<jsp:include page="admin-channel-banner.jsp"/>
			<div id="admin-topper" align="left">
				<div class="clearfix">
					<span class="dropt-banner">
						<img id="help-mini" src="images/help.svg"/>
						<div>
							<%= loginBean.translate("The analytics tab allows you to view your channel's statistics.") %><br/>
						</div>
						<%= loginBean.translate("Help") %>
					</span>
					<% if (!Site.DEDICATED) { %>
					 : <a href="manual-livechat.jsp"><%= loginBean.translate("Docs") %></a>
					<% } %>
				</div>
			</div>
			<div id="mainbody">
			<div id="contents">
			<div class="browse">
	<% } %>
				<h1>
					<span class="dropt-banner">
						<img src="images/stats.svg" class="admin-banner-pic">
						<div>
							<p class="help">
								<%= loginBean.translate("The analytics tab allows you to view your channel's statistics.") %><br/>
							</p>
						</div>
					</span> <%= loginBean.translate("Analytics") %>
				</h1>
				<jsp:include page="error.jsp"/>
				<% if (!liveChatBean.isAdmin()) { %>
					<p style="color:#E00000;"><%= loginBean.translate("Must be admin") %></p>
				<% } else { %>
					<div class="select-div">
						<span id="dateRangeSpan" class="selectTitle">Duration</span>
						<select id="datesRangeSelect">
							<option value="weekly"><%= loginBean.translate("current week") %></option>
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
					<div id="liveChatStatsDiv" style="display:block;">
						<ul id="liveChatList" class="selectiveCharts">
							<li><div><input class="liveChatConnects" type="checkbox" name="Connects" checked><span><%= loginBean.translate("Connects") %></span></div></li>
							<li><div><input class="liveChatMessages" type="checkbox" name="Messages"><span><%= loginBean.translate("Messages") %></span></div></li>
						</ul>
						<div id="liveChatStatsChart" class="ct-chart ct-perfect-fourth" style="height:300px;"></div>
						<h2><%= loginBean.translate("Live Chat Stats") %></h2>
						<table id="liveChatStats" class="tablesorter">
							<thead>
								<tr>
									<th><%= loginBean.translate("Date") %></th>
									<th><%= loginBean.translate("Connects") %></th>
									<th><%= loginBean.translate("Messages") %></th>
								</tr>
							</thead>
							<tbody>
								<% for (LiveChatStats stat : AdminDatabase.instance().getAllLiveChatStats(String.valueOf(liveChatBean.getInstanceId()))) { %>
									<tr>
										<td><%= stat.date %></a></td>
										<td><%= stat.connects %></td>
										<td><%= stat.messages %></td>
									</tr>
								<% } %>
							</tbody>
						</table>
					</div>
				<% } %>
			</div>
			</div>
		<% if (!embed) { %>
			</div>
			<jsp:include page="footer.jsp"/>
		<% } %>
		<script>
			var connectsArray = [];
			var messagesArray = [];
			function generateData() {
				<% List<LiveChatStats> statsList = AdminDatabase.instance().getAllLiveChatStats(String.valueOf(liveChatBean.getInstanceId())); %>
				<% for (int i = statsList.size() - 1; i >= 0; i--) { %>
					<% LiveChatStats stat = statsList.get(i); %>
					connectsArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.connects %> } );
					messagesArray.push( { x: new Date(<%= stat.date.getTime() %>), y: <%= stat.messages %> } );
				<% } %>
			}
			generateData();
			
			function generateAnalyticsData(dateInterval, groupByValue, listType) {
				var tempConnectsArray = connectsArray;
				var tempMessagesArray = messagesArray;
				var timeInterval = connectsArray.length;
				if (dateInterval === "weekly" && timeInterval >= 7) {
					timeInterval = 7;
				} else if (dateInterval === "monthly" && timeInterval >= 30) {
					timeInterval = 30;
				}
				if (timeInterval == 7 || timeInterval == 30) {
					tempConnectsArray = tempConnectsArray.slice(connectsArray.length - timeInterval, connectsArray.length);
					tempMessagesArray = tempMessagesArray.slice(messagesArray.length - timeInterval, messagesArray.length);
				}
				var groupByNum = 0
				if (groupByValue === "weekly") {
					groupByNum = 7;
				} else if (groupByValue === "monthly") {
					groupByNum = 30;
				}
				var liveChatStats = {};
				var listTypeStr = "#" + listType + " li div ";
				var messages = { 'Connects': groupBy(tempConnectsArray, groupByNum), 'status': $(listTypeStr + '.liveChatConnects').prop('checked') };
				var connects = { 'Messages': groupBy(tempMessagesArray, groupByNum), 'status': $(listTypeStr + '.liveChatMessages').prop('checked') };
				liveChatStats['liveChatList'] = { 'Connects': messages, 'Messages': connects };
				return liveChatStats;
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
				setCheckBoxLabelColor(listType, chartColorArray);
				drawCharts(seriesArray, chartType);
			}
			/* Generate initial chat graph for current week duration with no grouping */
			var liveChatStatsData = generateAnalyticsData("weekly", "none", "liveChatList");
			generateChart(liveChatStatsData, "liveChatList", "", "#liveChatStatsChart", false);
			
			function setCheckBoxLabelColor(listType, checkBoxArray) {
				var listTypeStr = "#" + listType + " li";
				$(listTypeStr).each(function(li) {
				    var checkBoxName = $(this).children(0).children(0).attr("name");
				    var index = checkBoxArray.indexOf(checkBoxName);
				    if (index == 0) {
				    	$(this).children(0).children(1).css("color", "#5271ff");
				    } else if (index == 1) {
				    	$(this).children(0).children(1).css("color", "#ff832c");
				    } else if (index == 2) {
				    	$(this).children(0).children(1).css("color", "#ff5c5c");
				    } else if (index == 3) {
				    	$(this).children(0).children(1).css("color", "#339966");
				    } else if (index == 4) {
				    	$(this).children(0).children(1).css("color", "#99ccff");
				    } else {
				    	$(this).children(0).children(1).css("color", "inherit");
				    }
				});
			}
			
			function resetCheckBoxes(listType) {
				$(listType).each(function(li) {
				    var checkBoxName = $(this).children(0).children(0).attr("name");
				    if (checkBoxName === "Connects" || checkBoxName === "Messages") {
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
			
			$('.liveChatConnects').change(function() {
				var checkBoxType = $(this).attr('name');
				var listType = $(this).parent().parent().parent().attr('id');
				var liveChatAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
				if ($(this).prop("checked")) {
					generateChart(liveChatAnalticsData, listType, checkBoxType, "#liveChatStatsChart", true);
				} else {
					generateChart(liveChatAnalticsData, listType, checkBoxType, "#liveChatStatsChart", false);
				}
			});
			$('.liveChatMessages').change(function() {
				var checkBoxType = $(this).attr('name');
				var listType = $(this).parent().parent().parent().attr('id');
				var liveChatAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), $('#groupBySelect').val(), listType);
				if ($(this).prop("checked")) {
					generateChart(liveChatAnalticsData, listType, checkBoxType, "#liveChatStatsChart", true);
				} else {
					generateChart(liveChatAnalticsData, listType, checkBoxType, "#liveChatStatsChart", false);
				}
			});
			/* ################# Date Range Drop Down Select Box ################# */
			$("#datesRangeSelect").on("change", function() {
				var dateRangeValue = this.value;
				var liveChatAnalticsData = generateAnalyticsData(dateRangeValue, $('#groupBySelect').val(), "liveChatList");
				generateChart(liveChatAnalticsData, "liveChatList", "", "#liveChatStatsChart", false);
			});
			/* ################# Group By Drop Down Select Box ################# */
			$("#groupBySelect").on("change", function() {
				var groupByValue = this.value;
				var liveChatAnalticsData = generateAnalyticsData($('#datesRangeSelect').val(), groupByValue, "liveChatList");
				generateChart(liveChatAnalticsData, "liveChatList", "", "#liveChatStatsChart", false);
			});
		</script>
		<% proxy.clear(); %>
		<% } catch (Exception error) { loginBean.error(error); }%>
	</body>
</html>