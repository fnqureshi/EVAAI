<%@page import="org.botlibre.web.admin.Domain.AccountType"%>
<%@page import="org.botlibre.web.bean.DomainBean.WizardState"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page"%>
<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.admin.Domain"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% DomainBean bean = loginBean.getBean(DomainBean.class); %>
<% loginBean.setActiveBean(bean); %>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Create Workspace - <%= Site.NAME %></title>
	<meta name="description" content="Create a new workspace"/>	
	<meta name="keywords" content="create, workspace, domain, directory, website, portal"/>
	<%= loginBean.getJQueryHeader() %>

	<!-- Facebook Pixel Code -->
	<script>
	!function(f,b,e,v,n,t,s){if(f.fbq)return;n=f.fbq=function(){n.callMethod?
	n.callMethod.apply(n,arguments):n.queue.push(arguments)};if(!f._fbq)f._fbq=n;
	n.push=n;n.loaded=!0;n.version='2.0';n.queue=[];t=b.createElement(e);t.async=!0;
	t.src=v;s=b.getElementsByTagName(e)[0];s.parentNode.insertBefore(t,s)}(window,
	document,'script','https://connect.facebook.net/en_US/fbevents.js');
	
	fbq('init', '1124209577628014');
	fbq('track', "PageView");
	fbq('track', 'Lead');
	</script>
	<noscript><img height="1" width="1" style="display:none"
	src="https://www.facebook.com/tr?id=1124209577628014&ev=PageView&noscript=1"
	/></noscript>
	<!-- End Facebook Pixel Code -->
	
</head>
<body>
	<% loginBean.setPageType(Page.Create); %>
	<jsp:include page="banner.jsp"/>
	<div id="mainbody">
	<div id="contents">
		<div class="section">
		<% if (!Site.COMMERCIAL || (Site.DEDICATED && !Site.CLOUD)) { %>
			<h1><%= loginBean.translate("Create new workspace") %></h1>
			<% boolean error = loginBean.getError() != null && bean.getInstance() != null; %>
			<jsp:include page="error.jsp"/>
			<% if (!loginBean.isLoggedIn()) { %>
				<p>
					<%= loginBean.translate("You must first") %> <a href="login?sign-in"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("to create a new workspace") %>.
				</p>
			<% } else { %>
				<form action="domain" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<span class="required"><%= loginBean.translate("Workspace Name") %></span><br/>
					<input class="required" autofocus name="newInstance" type="text" value="<%= (!error) ? "" : bean.getInstance().getName() %>" /><br/>
					
					<% bean.writeCreateCommonHTML(error, false, null, false, out); %>
					<tr>
					<td><%= loginBean.translate("Creation Mode") %></td>
					<td><select name="creationMode" title="<%= loginBean.translate("Define who can create channels, forums, bots, in this workspace") %>">
						<option value="Everyone" <%= (!error) ? "" : bean.isCreationModeSelected("Everyone") %>><%= loginBean.translate("Everyone") %></option>
						<option value="Users" <%= (!error) ? "" : bean.isCreationModeSelected("Users") %>><%= loginBean.translate("Users") %></option>
						<option value="Members" <%= (!error) ? "" : bean.isCreationModeSelected("Members") %>><%= loginBean.translate("Members") %></option>
						<option value="Administrators" <%= (!error) ? "selected" : bean.isCreationModeSelected("Administrators") %>><%= loginBean.translate("Administrators") %></option>
					</select></td>
					</tr>
					</table>
					
					<% bean.writeCreateAdCodeHTML(error, null, false, out); %>
					
					<input id="ok" name="create-instance" type="submit" value="<%= loginBean.translate("Create") %>"/><input id="cancel" name="cancel" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
				</form>
			<% } %>
		<% } else { %>
			<% if ((bean.getWizardDomain() != null) && bean.getWizardDomain().getId() != null) { %>
				<h1><%= loginBean.translate("Make payment") %></h1>
			<% } else { %>
				<h1><%= loginBean.translate("Create new account") %></h1>
			<% } %>
			<% boolean error = loginBean.getError() != null; %>
			<jsp:include page="error.jsp"/>
			<% if (!loginBean.isLoggedIn() || (bean.getWizardState() == DomainBean.WizardState.User)) { %>
				<h3><%= loginBean.translate("Enter user details") %></h3>
				<p><%= loginBean.translate("Choose a user id and password to associate with your new account") %>
				(<%= loginBean.translate("if you already have an user id, please") %> <a href="<%= "login?sign-in=sign-in" %>"><%= loginBean.translate("sign in") %></a> <%= loginBean.translate("first") %>).</p>
	
				<script>
					function statusChangeCallback(response) {
						if (response.status === 'connected') {
							connected();
						} else if (response.status === 'not_authorized') {
							document.getElementById('status').innerHTML = 'Please log '
									+ 'into this app.';
						} else {
							document.getElementById('status').innerHTML = 'Please log '
									+ 'into Facebook.';
						}
						if (response.authResponse != null) {
							document.getElementById("credentials-type").value = "Facebook";
							document.getElementById("credentials-userid").value = response.authResponse.userID;
							document.getElementById("credentials-token").value = response.authResponse.accessToken;
							document.getElementById('status').innerHTML = 'Success, '
								+ 'enter profile info and click Next.';
						}
					}
			
					window.fbAsyncInit = function() {
						FB.init({
							appId : '<%= Site.FACEBOOK_APPID %>',
							cookie : true,
							xfbml : true,
							version : 'v2.2'
						});
					};
			
					// Load the SDK asynchronously
					(function(d, s, id) {
						var js, fjs = d.getElementsByTagName(s)[0];
						if (d.getElementById(id))
							return;
						js = d.createElement(s);
						js.id = id;
						js.src = "//connect.facebook.net/en_US/sdk.js";
						fjs.parentNode.insertBefore(js, fjs);
					}(document, 'script', 'facebook-jssdk'));
			
					function connected() {
						FB.api(
							'/me?fields=id,name,email,first_name',
							function(response) {
								console.log(response);
								document.getElementById("login").style.display = "none";
								document.getElementById("login-fb").style.display = "";
								document.getElementById("name").value = response.name;
								if (response.first_name != null) {
									document.getElementById("user").value = response.first_name.toLowerCase();
								} else {
									document.getElementById("user").value = response.name.toLowerCase().replace(" ", "");						
								}
								if (response.email != null) {
									document.getElementById("email").value = response.email;
								}
								if (response.website != null) {
									document.getElementById("website").value = response.website;
								}
							});
					}
					
					function checkLoginState() {
						FB.getLoginStatus(function(response) {
							statusChangeCallback(response);
						});
					}

					function facebookLogin() {
						FB.login(function(response) {
							statusChangeCallback(response);
						}, {
							scope : 'public_profile,email'
						});
					}
				</script>
				
				<a href="#" class="facebookbutton" onClick="facebookLogin(); return false;"><img style="vertical-align:middle" src="images/facebook2.svg"> <%= loginBean.translate("Sign up using Facebook") %></a>
				<!--fb:login-button scope="public_profile,email,user_about_me,user_website" onlogin="checkLoginState();"></fb:login-button-->

				<br/>
				<div id="status">
				</div>
				<br/>

				<% if (error && (loginBean.getUser() != null)) { %>
					<form action="domain" method="post" class="message">
						<%= loginBean.postTokenInput() %>
						<% if (loginBean.getUser().getCredentialsType() == null) { %>
							<span class="required"><%= loginBean.translate("User ID") %></span><br/>
							<input class="required" autofocus name="user" type="text" value="<%= loginBean.getUser().getUserId() %>" onfocus="this.searchfocus = true;" onmouseup="if(this.searchfocus) {this.select(); this.searchfocus = false;}" title="<%= loginBean.translate("Enter a unique user id (no spaces, alpha numeric, visible to other users, required)") %>"/><br/>
							<span class="required"><%= loginBean.translate("Password") %></span><br/>
							<input class="required" name="password" type="password" value="<%= loginBean.getUser().getPassword() %>" onfocus="this.searchfocus = true;" onmouseup="if(this.searchfocus) {this.select(); this.searchfocus = false;}" title="<%= loginBean.translate("Enter a secure password (required)") %>"/><br/>
							<span class="required"><%= loginBean.translate("Retype Password") %></span><br/>
							<input class="required" name="password2" type="password" value="" onfocus="this.searchfocus = true;" onmouseup="if(this.searchfocus) {this.select(); this.searchfocus = false;}" title="<%= loginBean.translate("Confirm password (required)") %>"/><br/>
							<span class="required"><%= loginBean.translate("Password hint") %></span><br/>
							<input name="hint" type="text" value="<%= loginBean.getUser().getHint() %>" onfocus="this.searchfocus = true;" onmouseup="if(this.searchfocus) {this.select(); this.searchfocus = false;}" title="<%= loginBean.translate("Enter a personal hint in case you forget your password (optional)") %>"/><br/>
						<% } else { %>
							<span class="required"><%= loginBean.translate("User Id") %></span><br/>
							<input class="required" autofocus name="user" type="text" value="<%= loginBean.getUser().getUserId() %>" onfocus="this.searchfocus = true;" onmouseup="if(this.searchfocus) {this.select(); this.searchfocus = false;}" title="<%= loginBean.translate("Enter a unique user id (no spaces, alpha numeric, visible to other users, required)") %>"/><br/>
							<input id="credentials-type" name="credentials-type" value="<%= loginBean.getUser().getCredentialsType() %>" type="hidden" />
							<input id="credentials-userid" name="credentials-userid" value="<%= loginBean.getUser().getCredentialsUserID() %>" type="hidden" />
							<input id="credentials-token" name="credentials-token" value="<%= loginBean.getUser().getCredentialsToken() %>" type="hidden" />
						<% } %>
						<span class="required"><%= loginBean.translate("Name") %></span><br/>
						<input class="required" name="name" type="text" value="<%= loginBean.getUser().getName() %>" onfocus="this.searchfocus = true;" onmouseup="if(this.searchfocus) {this.select(); this.searchfocus = false;}" title="<%= loginBean.translate("Enter your real name") %>"/><br/>
						<span class="required"><%= loginBean.translate("Email") %></span><br/>
						<input class="required" name="email" type="email" value="<%= loginBean.getUser().getEmail() %>" onfocus="this.searchfocus = true;" onmouseup="if(this.searchfocus) {this.select(); this.searchfocus = false;}" title="<%= loginBean.translate("Enter your email address") %>"/><br/>
						<input id="ok" name="next" type="submit" value="Next"/><input id="cancel" name="cancel" type="submit" value="Cancel"/>
					</form>
				<% } else { %>
					<form id="login" action="domain" method="post" class="message">
						<%= loginBean.postTokenInput() %>
						<span class="required"><%= loginBean.translate("User Id") %></span><br/>
						<input class="required" autofocus name="user" type="text" value="" onfocus="this.searchfocus = true;" onmouseup="if(this.searchfocus) {this.select(); this.searchfocus = false;}" title="<%= loginBean.translate("Enter a unique user id (no spaces, alpha numeric, visible to other users, required)")%>"/><br/>
						<span class="required"><%= loginBean.translate("Password") %></span><br/>
						<input class="required" name="password" type="password" value="" onfocus="this.searchfocus = true;" onmouseup="if(this.searchfocus) {this.select(); this.searchfocus = false;}"/><br/>
						<span class="required"><%= loginBean.translate("Retype Password") %></span><br/>
						<input class="required" name="password2" type="password" value="" onfocus="this.searchfocus = true;" onmouseup="if(this.searchfocus) {this.select(); this.searchfocus = false;}"/><br/>
						<span class="required"><%= loginBean.translate("Password hint") %></span><br/>
						<input class="required" name="hint" type="text" value="" onfocus="this.searchfocus = true;" onmouseup="if(this.searchfocus) {this.select(); this.searchfocus = false;}"/><br/>
						<span class="required"><%= loginBean.translate("Name") %></span><br/>
						<input class="required" name="name" type="text" value="" onfocus="this.searchfocus = true;" onmouseup="if(this.searchfocus) {this.select(); this.searchfocus = false;}" title="<%= loginBean.translate("Enter your real name") %>"/><br/>
						<span class="required"><%= loginBean.translate("Email") %></span><br/>
						<input class="required" name="email" type="text" value="" onfocus="this.searchfocus = true;" onmouseup="if(this.searchfocus) {this.select(); this.searchfocus = false;}" title="<%= loginBean.translate("Enter your email address") %>" /><br/>
						<input id="ok" name="next" type="submit" value="Next"/><input id="cancel" name="cancel" type="submit" value="Cancel"/>
					</form>
					<form style="display:none" id="login-fb" action="domain" method="post" class="message">
						<%= loginBean.postTokenInput() %>
						<input id="credentials-type" name="credentials-type" type="hidden" />
						<input id="credentials-userid" name="credentials-userid" type="hidden" />
						<input id="credentials-token" name="credentials-token" type="hidden" />
						<span class="required"><%= loginBean.translate("User Id") %></span><br/>
						<input id="user" class="required" autofocus name="user" type="text" value="" onfocus="this.searchfocus = true;" onmouseup="if(this.searchfocus) {this.select(); this.searchfocus = false;}" title="<%= loginBean.translate("Enter a unique user id (no spaces, alpha numeric, visible to other users, required)") %>"/><br/>
						<span class="required"><%= loginBean.translate("Name") %></span><br/>
						<input class="required" id="name" name="name" type="text" value="" onfocus="this.searchfocus = true;" onmouseup="if(this.searchfocus) {this.select(); this.searchfocus = false;}" title="<%= loginBean.translate("Enter your real name (only visible to other users if 'Show Name' is selected, optional)") %>"/><br/>
						<span class="required"><%= loginBean.translate("Email") %></span><br/>
						<input class="required" id="email" name="email" type="text" value="" onfocus="this.searchfocus = true;" onmouseup="if(this.searchfocus) {this.select(); this.searchfocus = false;}" title="<%= loginBean.translate("Enter your email address (not visible to other users, recommended, required for password reset)") %>"/><br/>
						<input id="ok" name="next" type="submit" value="Next"/><input id="cancel" name="cancel" type="submit" value="Cancel"/>
					</form>
				<% } %>
			<% } else if ((bean.getWizardState() == null) || (bean.getWizardState() == DomainBean.WizardState.Domain)) { %>

				<!-- Google Code for Sign Up Conversion Page -->
				<script type="text/javascript">
				/* <![CDATA[ */
				var google_conversion_id = 981445329;
				var google_conversion_language = "en";
				var google_conversion_format = "3";
				var google_conversion_color = "ffffff";
				var google_conversion_label = "TEJYCI_DtQkQ0dX-0wM";
				var google_remarketing_only = false;
				/* ]]> */
				</script>
				<script type="text/javascript" src="//www.googleadservices.com/pagead/conversion.js">
				</script>
				<noscript>
				<div style="display:inline;">
				<img height="1" width="1" style="border-style:none;" alt="" src="//www.googleadservices.com/pagead/conversion/981445329/?label=TEJYCI_DtQkQ0dX-0wM&amp;guid=ON&amp;script=0"/>
				</div>
				</noscript>
		
				<h3><%= loginBean.translate("Enter workspace details") %></h3>
				<p>
					<%= loginBean.translate("A workspace is your own private or shared space for your account where you can create bots, live chat, chat rooms, forums, and other content.") %>
					<%= loginBean.translate("You can create users for your staff or group members and add them to your workspace.") %>
				</p>
				<form action="domain" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<span class="required"><%= loginBean.translate("Workspace Name") %></span><br/>
					<input class="required" autofocus name="newInstance" type="text" value="<%= (!error || (bean.getInstance() == null)) ? "" : bean.getInstance().getName() %>" onFocus="this.select();" onMouseOut="javascript:return false;"/><br/>
					<%= loginBean.translate("Description") %><br/>
					<textarea name="description" onFocus="this.select();" onMouseOut="javascript:return false;"><%= (!error) ? "a workspace" : bean.getInstance().getDescription() %></textarea><br/>
					<%= loginBean.translate("Website") %><br/>
					<input id="website" name="website" type="text" value="<%= (!error || (bean.getInstance() == null)) ? "" : bean.getInstance().getWebsite() %>" title="<%= loginBean.translate("if this workspace will be embedded on an external website, enter the URL") %>" onFocus="this.select();" onMouseOut="javascript:return false;"/><br/>
					<input name="private" type="checkbox" <% if (!error || (bean.getInstance() == null) || bean.getInstance().isPrivate()) { %>checked<% } %> title="<%= loginBean.translate("A private workspace is not visible to the public, only to the user and users they grant access") %>" onMouseOut="javascript:return false;"><%= loginBean.translate("Is Private") %></input><br/>
					<input name="hidden" type="checkbox" <% if (!error || (bean.getInstance() == null) || bean.getInstance().isHidden()) { %>checked<% } %> title="<%= loginBean.translate("A hidden workspace is not displayed in the browse directory") %>" onMouseOut="javascript:return false;"><%= loginBean.translate("Is Hidden") %></input><br/>
					<table>
					<tr>
					<td><%= loginBean.translate("Access Mode") %></td>
					<td><select name="accessMode" title="<%= loginBean.translate("Define who can access this workspace") %>">
						<option value="Everyone" <%= (!error) ? "" : bean.isAccessModeSelected("Everyone") %>><%= loginBean.translate("Everyone") %></option>
						<option value="Users" <%= (!error) ? "" : bean.isAccessModeSelected("Users") %>><%= loginBean.translate("Users") %></option>
						<option value="Members" <%= (!error) ? "selected" : bean.isAccessModeSelected("Members") %>><%= loginBean.translate("Members") %></option>
						<option value="Administrators" <%= (!error) ? "" : bean.isAccessModeSelected("Administrators") %>><%= loginBean.translate("Administrators") %></option>
					</select></td>
					</tr>
					<tr>
					<td><%= loginBean.translate("Creation Mode") %></td>
					<td><select name="creationMode" title="<%= loginBean.translate("Define who can create channels, forums, bots, in this workspace") %>">
						<option value="Everyone" <%= (!error) ? "" : bean.isCreationModeSelected("Everyone") %>><%= loginBean.translate("Everyone") %></option>
						<option value="Users" <%= (!error) ? "" : bean.isCreationModeSelected("Users") %>><%= loginBean.translate("Users") %></option>
						<option value="Members" <%= (!error) ? "" : bean.isCreationModeSelected("Members") %>><%= loginBean.translate("Members") %></option>
						<option value="Administrators" <%= (!error) ? "selected" : bean.isCreationModeSelected("Administrators") %>><%= loginBean.translate("Administrators") %></option>
					</select></td>
					</tr>
					</table>
					<input id="ok" name="next" type="submit" value="<%= loginBean.translate("Next") %>"/><input id="cancel" name="cancel" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
				</form>
			<% } else if (bean.getWizardState() == DomainBean.WizardState.Payment) { %>
			
				<!-- Google Code for Create Domain Conversion Page -->
				<script type="text/javascript">
				/* <![CDATA[ */
				var google_conversion_id = 981445329;
				var google_conversion_language = "en";
				var google_conversion_format = "3";
				var google_conversion_color = "ffffff";
				var google_conversion_label = "mSc4CN_5zGYQ0dX-0wM";
				var google_remarketing_only = false;
				/* ]]> */
				</script>
				<script type="text/javascript" src="//www.googleadservices.com/pagead/conversion.js">
				</script>
				<noscript>
				<div style="display:inline;">
				<img height="1" width="1" style="border-style:none;" alt="" src="//www.googleadservices.com/pagead/conversion/981445329/?label=mSc4CN_5zGYQ0dX-0wM&amp;guid=ON&amp;script=0"/>
				</div>
				</noscript>
				
				<h3><%= loginBean.translate("Enter payment details") %></h3>
				<form action="domain" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<div class="confirm" style="background-color:#f6f5f1">
					<table>
					<tr>
					<td><%= loginBean.translate("Payment Type") %></td>
					<td><select name="paymentType" id="paymentType" onchange="updatePaymentType()" title="<%= loginBean.translate("Select the number of month you wish to make payment for") %>">
						<option value="onetime" selected><%= loginBean.translate("One-Time Payment") %></option>
						<option value="subscription" ><%= loginBean.translate("Monthly Subscription") %></option>
					</select></td>
					</tr>
					<tr>
					<tr>
					<td><%= loginBean.translate("Account Type") %></td>
					<td><select name="accountType" id="accountType" onchange="updateCost()" title="<%= loginBean.translate("Select the type of account") %>">
						<option value="Trial"><%= loginBean.translate("Free Trial (1 month free)") %></option>
						<option value="Basic" selected><%= loginBean.translate("Basic ($4.99/month)") %></option>
						<option value="Premium"><%= loginBean.translate("Premium ($19.99/month)") %></option>
						<option value="Enterprise"><%= loginBean.translate("Enterprise ($49.99/month)") %></option>
						<option value="EnterprisePlus"><%= loginBean.translate("EnterprisePlus ($99.99/month)") %></option>
					</select>
					</td>
					</tr>
					<tr>
					<td colspan="2"><span class="menu"><%= loginBean.translate("for other options please email") %> <a href="mailto:<%= Site.EMAILSALES %>"><%= Site.EMAILSALES %></a></span></td>
					</tr>
					<tr id="durationOptions">
					<td><%= loginBean.translate("Duration") %></td>
					<td><select name="duration" id="duration" onchange="updateCost()" title="<%= loginBean.translate("Select the number of month you wish to make payment for") %>">
						<option value="1" selected><%= loginBean.translate("1 month") %></option>
						<option value="3"><%= loginBean.translate("3 months") %></option>
						<option value="6"><%= loginBean.translate("6 months") %></option>
						<option value="12"><%= loginBean.translate("12 months (2 month free)") %></option>
						<option value="24"><%= loginBean.translate("24 months (6 months free)") %></option>
					</select></td>
					</tr>
					</table>
					</div>
					<h3><%= loginBean.translate("Please confirm details") %></h3>
					<div class="confirm" style="background-color:#f6f5f1">
					<table cellspacing="2">
					<tr>
						<span class="menu"><%= loginBean.translate("Upgrade") %></span><td>
					</tr>
					</table>
					<p id="upgrade" title="<%= loginBean.translate("Basic") %>"> 
						<%= loginBean.translate("Basic") %>
					</p>
					<table cellspacing="40">
					<tr>
						<td><span class="menu"><%= loginBean.translate("Cost") %></span></td>
					</tr>
					</table>
					<p id="cost" title="4.99"> 
						$4.99
					</p>
					</div>
					<input id="ok" name="next" type="submit" value="<%= loginBean.translate("Next") %>"/><input id="cancel" name="cancel" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
				</form>
			<% } else if (bean.getWizardState() == DomainBean.WizardState.Confirm) { %>
			
				<!-- Google Code for Payment Conversion Page -->
				<script type="text/javascript">
				/* <![CDATA[ */
				var google_conversion_id = 981445329;
				var google_conversion_language = "en";
				var google_conversion_format = "3";
				var google_conversion_color = "ffffff";
				var google_conversion_label = "UTKkCOXG0GYQ0dX-0wM";
				var google_remarketing_only = false;
				/* ]]> */
				</script>
				<script type="text/javascript" src="//www.googleadservices.com/pagead/conversion.js">
				</script>
				<noscript>
				<div style="display:inline;">
				<img height="1" width="1" style="border-style:none;" alt="" src="//www.googleadservices.com/pagead/conversion/981445329/?label=UTKkCOXG0GYQ0dX-0wM&amp;guid=ON&amp;script=0"/>
				</div>
				</noscript>
				
				<h3><%= loginBean.translate("Confirm payment") %></h3>
				<div class="confirm" style="background-color:#f6f5f1">
				<p>
				<span><%= loginBean.translate("Account Type") %>:</span> <%= Site.getPaymentType(bean.getPayment().getAccountType()) %><br/>
				<% if (!bean.getPayment().isSubscription()) { %>
					 <span><%= loginBean.translate("Duration") %>:</span> <%= bean.getPaymentDuration() %><br/>
					 <span><%= loginBean.translate("Payment Option") %>:</span> <%= loginBean.translate("One-Time Payment") %><br/>
				<% } else { %>
					<span><%= loginBean.translate("Payment Option") %>:</span> <%= loginBean.translate("Monthly Subscription") %><br/>					
				<% } %>
				<br/>
				<span><%= loginBean.translate("Total Amount") %>:</span> $<%= bean.getPaymentAmount() %><% if(bean.getPayment().isSubscription()) { %><%= loginBean.translate("/month") %><% } %><br/>
				</p>
				</div>
				<% if (!bean.getUser().isSuperUser() && !bean.getPayment().getAccountType().equals(AccountType.Trial)) { %>
					<p>
						<%= loginBean.translate("Pay securely with PayPal, no PayPal account is required, only a credit card.") %>
						<br/>
						<%= loginBean.translate("Please click the return link after paying to have your payment automatically registered.") %>
					</p>
				<% } %>
				<table>
				<tr>
				<td>
				<% if (bean.getUser().isSuperUser() || bean.getPayment().getAccountType().equals(AccountType.Trial)) { %>
					<form action="upgradepayment" method="post" class="message">
						<%= loginBean.postTokenInput() %>
						<input id="ok" name="next" type="submit" value="<%= loginBean.translate("Complete") %>"/>
					</form>
				<% } else if (!bean.getPayment().isSubscription()) { %>					
					<form action="https://www.<%=  bean.getUser().getUserId().equals("test") ? "sandbox." : "" %>paypal.com/cgi-bin/webscr" method="post" class="message">
	 					<input type="hidden" value="<%= (request.isSecure() ? "https" : "http") + "://" + request.getServerName() + "/upgradepayment" %>" name="return">
	 					<input type="hidden" name="rm" value="2">
						<input type="hidden" name="cmd" value="_xclick">
						<input type="hidden" name="business" value="<%=  bean.getUser().getUserId().equals("test") ? "test2" : "paphus" %>@paphussolutions.com">
						<input type="hidden" name="item_name" value="<%= Site.NAME + " - " + bean.getPayment().getAccountType() + "-" + bean.getPayment().getPaymentDuration() + " month(s) " %>">						
						<input type="hidden" name="amount" value="<%= bean.getPaymentAmount() %>">
						<input type="hidden" name="no_shipping" value="0">
						<input type="hidden" name="no_note" value="1">
						<input type="hidden" name="currency_code" value="USD">
						<input type="hidden" name="lc" value="CA">
						<input type="hidden" name="bn" value="PP-BuyNowBF">
						<input type="hidden" name="invoice" value="<%= bean.getPayment().getToken() %>">
						<input type="hidden" name="custom" value="<%= ((Domain)bean.getInstance()).getId() %>">
						<input type="hidden" name="item_number" value="<%= bean.getPayment().getId() %>">
						<input id="ok" type="submit" name="submit" value="<%= loginBean.translate("Pay with PayPal") %>" alt="<%= loginBean.translate("PayPal - The safer, easier way to pay online") %>" title="<%= loginBean.translate("PayPal - The safer, easier way to pay online") %>">
					</form>			
				<% } else { %>
					<form action="https://www.<%=  bean.getUser().getUserId().equals("test") ? "sandbox." : "" %>paypal.com/cgi-bin/webscr" method="post" class="message">
						<input type="hidden" name="return" value="<%= (request.isSecure() ? "https" : "http") + "://" + request.getServerName() + "/upgradepayment" %>">
						<input type="hidden" name="rm" value="2">
						<input type="hidden" name="cmd" value="_xclick-subscriptions">
						<input type="hidden" name="hosted_button_id" value="854EQ4PN533HS">
						<input type="hidden" name="business" value="<%=  bean.getUser().getUserId().equals("test") ? "test2" : "paphus" %>@paphussolutions.com">
						<input type="hidden" name="item_name" value="<%= Site.NAME + " - " + bean.getPayment().getAccountType() %>">
						<input type="hidden" name="notify_url" value="<%= (request.isSecure() ? "https" : "http") + "://" + request.getServerName() + "/upgradepayment" %>"
						<input type="hidden" name="no_note" value="1">
						<input type="hidden" name="currency_code" value="USD">
						<input type="hidden" name="no_shipping" value="1">
						<input type="hidden" name="a3" value="<%= bean.getPaymentAmount() %>">
						<input type="hidden" name="p3" value="1"> 
						<input type="hidden" name="t3" value="M">
						<input type="hidden" name="src" value="1">
						<input type="hidden" name="sra" value="1">
						<input type="hidden" name="invoice" value="<%= bean.getPayment().getToken() %>">
						<input type="hidden" name="custom" value="<%= ((Domain)bean.getInstance()).getId() %>">
						<input type="hidden" name="item_number" value="<%= bean.getPayment().getId() %>">
						<input id="ok" type="submit" name="submit" value="<%= loginBean.translate("Pay and Subscribe with PayPal") %>" alt="<%= loginBean.translate("PayPal - The safer, easier way to pay online") %>" title="<%= loginBean.translate("PayPal - The safer, easier way to pay online") %>">
					</form>
				<% } %>
				</td>
				<td>
				<form action="domain" method="post" class="message">
					<%= loginBean.postTokenInput() %>
					<input id="cancel" name="cancel" type="submit" value="<%= loginBean.translate("Cancel") %>"/>
				</form>
				</td>
				</tr>
				</table>
			<% } %>
		<% } %>
		</div>
	</div>
	</div>
	<jsp:include page="footer.jsp"/>
	<script>
		function updatePaymentType() {
			var paymentPlan = document.getElementById("paymentType").value;
			var x = document.getElementById('durationOptions');
			if (paymentPlan == "subscription") {
				x.style.display = 'none';
			} else {
				x.style.display = '';
			}
			updateCost();
		}
		
		function updateCost() {
			var dur = document.getElementById("duration").value;
			var plan = document.getElementById("accountType").value;
			var paymentPlan = document.getElementById("paymentType").value;
			var cost = 0;
	
			if (plan == "Trial") {
				cost = 0.00;
			} else if (plan == "Basic") {
				cost = 4.99;
			} else if (plan == "Premium") {
				cost = 19.99;
			} else if (plan == "Enterprise") {
				cost = 49.99;
			} else {
				cost = 99.99;
			}
			if (paymentPlan == "subscription") {
				dur = "1";
			}
			if (dur == "12") {
				dur = "10";
			} else if (dur == "24") {
				dur = "18";
			}
			var updatedCost = dur * cost;
			if (plan == "Trial") {
				document.getElementById("cost").innerHTML = "Free for 1 Month";
			} else {
				document.getElementById("cost").innerHTML = "$" + updatedCost.toFixed(2);
			}
			
			document.getElementById("upgrade").innerHTML = plan;

		}
	</script>
</body>
</html>