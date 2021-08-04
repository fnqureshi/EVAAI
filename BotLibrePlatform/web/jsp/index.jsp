<%@page import="org.botlibre.web.admin.Domain"%>
<%@page import="org.botlibre.web.admin.Payment"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.bean.DomainBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.bean.BotBean"%>
<%@page import="org.eclipse.persistence.internal.helper.Helper" %>
<%@page import = "org.botlibre.web.bean.LoginBean.Page" %>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
try {
	boolean databaseFailure = false;
	DomainBean domainBean = null;
	try {
		AdminDatabase.instance().getFactory();
		loginBean = proxy.checkLoginBean(loginBean);
		if (loginBean.checkForward(request, response)) {
			return;
		}
		domainBean = loginBean.getBean(DomainBean.class);
		loginBean.initialize(getServletContext(), request, response); // Need to init in root page for cookie.
	} catch (Exception exception) {
		// Detect the first start failure and prompt for the database password.
		if (Site.BOOTSTRAP && AdminDatabase.DATABASEFAILURE) {
			loginBean.setError(exception);
			databaseFailure = true;
			loginBean.setBootstrap(true);
		}
	}
	boolean embed = loginBean.isEmbedded();
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= Site.NAME %> - AI powered digital therapy platform</title>
	<meta name="description" content="AI powered digital therapy platform"/>	
	<meta name="keywords" content="chat bot, open source, create chat bot, create your own chat bot, chat, bot, commercial, hosting, embed, artificial intelligence, ai, create, twitterbot, twitter, facebook, ircbot, irc, chatterbot, chatbot"/>

	<link href="css/simpletextrotator.css" rel="stylesheet" media="screen">
	<link href="css/overwrite.css" rel="stylesheet" media="screen">
	<link href="css/animate.css" rel="stylesheet" media="screen">
	


</head>

<% if (databaseFailure) { %>

	<body style="background-color: #fff;">
		<div class="center">
			<h1><%= loginBean.translate("Welcome to") %> <%= Site.NAME %></h1>
			<jsp:include page="error.jsp"/>
			<p>
				<%= loginBean.translate("There was a password failure trying to access your database.") %><br/>
				<%= loginBean.translate("Please enter your database postgres user password below.") %>
			</p>
			<p>
				<%= loginBean.translate("If this is your first time starting this platform you will next need to sign in as:") %><br/>
				<%= loginBean.translate("user") %>: admin, <%= loginBean.translate("password") %>: password<br/>
				<%= loginBean.translate("After signing in click on 'Admin Console' to configure your platform settings.") %><br/>
			</p>
			<form action="super" method="post" class="message">
				<%= loginBean.postTokenInput() %>
				<p>
				<%= loginBean.translate("Database Password") %><br/>
				<input type="password" name="database-password"/>
				<br/>
				<input type="submit" id="ok" name="bootstrap" value="<%= loginBean.translate("Connect") %>"/>
				</p>
			</form>
		</div>

<% } else if (embed) { %>

	<body style="background-color: #fff;">
	<jsp:include page="<%= loginBean.getActiveBean().getEmbeddedBanner() %>"/>

<% } else { %>

<body class="mainpage">
	<% loginBean.setPageType(Page.Home); %>
	<jsp:include page="banner.jsp"/>
	<style>.nav1 { display:none; } .navbar1 { height: 42px; }</style>
	<jsp:include page="error.jsp"/>

	<!-- HOME -->
	<div id="intro" style="background-color:black; overflow: hidden;">

<video muted="" autoplay="" loop="" src="images/splash.mp4" style="
    position: absolute;
    min-height: 100%;
    min-width: 100%;
    left: 50%;
    top: 50%;
    transform: translate(-50%, -50%);
"></video>

		<div class="overlay" style="background-image: unset;">
                        <div class="intro-text" >
			<!--div class="intro-text" style="justify-content: unset;"-->
			 
				<div class="container" style="padding-bottom:150px">
					<% if (domainBean.hasValidInstance()) { %>
						<div class="col-md-12">
							<div id="rotator">
								<a href="domain?details=true&id=<%= loginBean.getDomain().getId() %>"><h1><span style="color: #fff"><%= loginBean.getDomain().getName() %></span></h1></a>
								<div class="line-spacer"></div>
								<p><span><%= loginBean.getDomain().getDescriptionText() %></span></p>
								<% if (domainBean.isAdmin() && Site.COMMERCIAL && !Site.DEDICATED) { %>
									<p>
										<a href="domain?details=true" style="text-decoration: none;">
											<span class="menu"><%= loginBean.translate("View details or make a payment") %></span>
										</a>
									</p>
								<% } %>
							</div>
							<br>
							<% if (!loginBean.isLoggedIn() && Site.ALLOW_SIGNUP) { %>
								<span> <a href="login?sign-up=true" class="btn btn-2 wow fadeInUp"><%= loginBean.translate("Sign Up") %>&rarr;</a></span>
							<% } %>
							<span> <a href="browse?browse-type=Bot&browse=true<%= domainBean.domainURL() %>" class="btn btn-3 wow fadeInUp"><%= loginBean.translate("Browse") %>&rarr;</a></span>
							<% if (loginBean.isLoggedIn() && loginBean.getDomain().isCreationAllowed(loginBean.getUser())) { %>
								<span> <a href="browse?browse-type=Bot&create=true" class="btn btn-4 wow fadeInUp"><%= loginBean.translate("Create") %>&rarr;</a></span>
							<% } %>
						</div>
					<% } else if (Site.DEDICATED) { %>
						<p class="text-intro wow fadeInUp">THE AI POWERED DIGITAL THERAPY PLATFORM</p>
						<div class="col-md-12">
							<div id="rotator">
								<% if (domainBean.isAdmin()) { %>
									<a href="domain?details=true&id=<%= loginBean.getDomain().getId() %>"><h1><span class="1strotate" style="color: #fff"><%= Site.NAME %></span></h1></a>
								<% } else { %>
									<h1><span style="color: #fff"><%= Site.NAME %></span></h1>
								<% } %>
								<div class="line-spacer"></div>
								<p>
									<span id="2ndrotate" class="2ndrotate" style="display:none">
Built by psychologists and psychiatrists,

Conduct novel research,

Reach out to more clients,

Connect with specialised AI assistants for therapy,

Find funding for your next-gen AI assistant,

Network with other professionals,

Find a support system
									</span>
								</p>
							</div>
							<br>
							<% if (!loginBean.isLoggedIn()) { %>
								<span> <a href="login?sign-in=true" class="btn btn-2 wow fadeInUp"><%= loginBean.translate("Sign In") %>&rarr;</a></span>
							<% } %>
							<% if (!loginBean.isLoggedIn() && Site.ALLOW_SIGNUP) { %>
								<% if (Site.COMMERCIAL && (!Site.DEDICATED || Site.CLOUD)) { %>
									<span> <a href="create-domain.jsp" class="btn btn-2 wow fadeInUp"><%= loginBean.translate("Sign Up") %>&rarr;</a></span>
								<% } else { %>
									<span> <a href="login?sign-up=true" class="btn btn-2 wow fadeInUp"><%= loginBean.translate("Sign Up") %>&rarr;</a></span>
								<% } %>
							<% } %>
							<% if (loginBean.isLoggedIn()) { %>
								<span> <a href="browse?browse-type=Bot&browse=true<%= domainBean.domainURL() %>" class="btn btn-3 wow fadeInUp"><%= loginBean.translate("Browse") %>&rarr;</a></span>
							<% } %>
							<% if (loginBean.isLoggedIn() && loginBean.getDomain().isCreationAllowed(loginBean.getUser())) { %>
								<span> <a href="browse?browse-type=Bot&create=true" class="btn btn-4 wow fadeInUp"><%= loginBean.translate("Create") %>&rarr;</a></span>
							<% } %>
						</div>
					<% } else { %>
						
					<% } %>
				</div>
			</div>
		</div>
	</div>
	
	<% if (!domainBean.hasValidInstance()) { %>

		<!-- Experiences -->
		<section id="experience" class="home-section bg-white">
			<div class="container wow fadeInUp">
				<div class="row ">
					<div class="col-md-offset-2 col-md-8">
						<div class="section-heading">
							<h2 style="color:#00A3FF"><%= loginBean.translate("Welcome to") %> <%= Site.NAME %></h2>
							<div class="heading-line"></div>
							<p>An online community trying to solve the mental health crisis by bringing together mental health professionals, users, and AI assistants.</p>
						</div>
					</div>
				</div> <br>
				<div class="row">
					
					<div class="col-md-6 content">
						<p>
							We are living in a mental health crisis. According to the WHO, there are 450 million people around the world who suffer from some sort of mental health disorder. In most third world countries, there is one psychiatrist for 100,000 people. By 2030, depression is expected to become the leading disease. Furthermore, 76% and 85% of people in low- and middle-income countries receive no treatment for their disorder. Barriers to effective care include a lack of resources, lack of trained health-care providers and social stigma associated with mental disorders.
						</p><p>
							On the other hand, Mental health professionals are often overworked, with Medspace, reporting psychiatrists 45% more likely to suffer from some sort of burnout. Burnout often leads to fatigue and fatigue leads to depression.
						</p>
					</div>
					<div class="col-md-6 experience-img" style="padding-right:0px;padding-left:0px;">
						<img style="border-radius: 20px;" src="images/0.jpg" alt="Social media">
					</div>
				</div>
				<div style="margin-top:50px" class="content">
					<p style="color: #00A3FF;font-weight:bold;font-size:24px;">To solve this problem the concept of EVAAI was conceived.</p>
					<p>
					EVAAI is an online platform that allows mental health professionals to create AI assistants that can provide basic counseling and consultation to their clients and also reach out to people not fortunate enough to afford or receive therapy. EVAAI connects people seeking professional guidance to these specialized AI assistants that can provide them with basic counseling, consultation, following a routine, follow-ups, self-help toolkits and access for setting appointments with the mental health professionals for online therapy. According to MentalHealth.gov, only 44% of adults with diagnosable illnesses require intensive treatment, sometimes having the right tools and better know-how of self-care is enough to keep people mentally healthy. EVAAI helps in reducing the burden on the mental health professionals, while also helping them reach out to new clients by making them more self-sufficient. This helps in reducing the cost of therapy for the end-users.
					</p><p>
						EVAAI's architecture is based on a concept of predictive processing, modeled after the human brain. Predictive processing is a theory of brain function in which the brain is constantly generating and updating a mental model of the environment. The model is used to generate predictions of sensory input that are compared to actual sensory input. The neural architecture is composed of four main parts Mind, Memory, Awareness and Mood.�
					</p><p>
						EVAAI is also a community for mental and medical health experts to join together for discussing ideas, tackling problems, networking and conducting novel research.
					</p>
				</div>
			</div>
		</section>

	<% } %>

	<!-- CARDS -->
	<style>
	#card .content {
		margin-top: 20px;
		margin-bottom: 20px;
	}
	#card .card-img img {
		border-radius: 20px;
	}
	</style>
	<section id="card" class="home-section bg-gray">
		<div class="container">
			
			<div class="row wow bounceInLeft">
				
				<div class="col-md-6 content ">
					<h2>Create intelligent, engaging and computationally powerful AI assistants</h2>
					<h3>EVAAI lets you build AI assistants that can train to understand emotions, user sentiment and conversational context.</h3>
					<p>
						You can use these assistants to automate your practice. From providing answers to frequently asked questions that your clients ask, to primary consultation, helping them follow a routine, meditate, daily follow-ups and providing them with self-help toolkits. You can train these assistants using pre-built scripts and templates. You can also train them yourselves. No coding is required. It's as simple as using MS Word.
					</p>
				</div>
				
				<div class="col-md-6 card-img">
					<img src="images/1.jpg" alt="Card-image">
				</div>
				
			</div>
			
			<div class="row wow bounceInLeft">
				
				<div class="col-md-6 content ">
					<h2>Join our research network to connect with peers from all over the world</h2>
					<h3>EVAAI is a community connecting doctors and mental health professionals from all around the world to discuss ideas, research, and build research networks.</h3>
<p>You can ask each other for help, network and emotionally support one another.
					</p>
				</div>
				
				<div class="col-md-6 card-img">
					<img src="images/2.jpg" alt="Card-image">
				</div>
				
			</div>
			
			<div class="row wow bounceInRight">
				
				<div class="col-md-6 content">
				<h2>Reach Out to More Clients</h2>
					<h3>Teletherapy allows you to become location independent. AI assistants combined with teletherapy helps you reach out to more people even when you're not physically or virtually available.</h3>
					<p>
						You can be available 24/7 for your existing patients and new ones whenever they need your support.
					</p>
				</div>
				
				<div class="col-md-6 card-img">
					<img src="images/3.jpg" alt="Card-image">
				</div>
				
			</div>

			<div class="row wow bounceInRight">
				
				<div class="col-md-6 content">
					<h2>Conduct Novel Research And Find New Ways of Therapy</h2>
					<h3>
						AI assistants are currently being tested for trying new therapy techniques for mental health settings focusing on populations with or at high risk of developing depression, anxiety, schizophrenia, bipolar, and substance abuse disorders. Furthermore, they are also being used to deliver information about prostate cancer to patients (epidemiology, risk factors, treatment options, and their side-effects). The overall, potential for conversational agents in the cases was reported to be high across all studies (Vaidyam, Wisniewski, Halama, 2019; Bibault et al. 2019; Kretzschmar, Tyroll, Pavarini, 2019).
</h3>
				</div>
				
				<div class="col-md-6 card-img">
					<img src="images/4.jpg" alt="Card-image">
				</div>
				
			</div>

			<div class="row wow bounceInRight">
				
				<div class="col-md-6 content">
					<h2>Store and Analyze Patients Data Easily</h2>
					<h3>With EVAAI's dashboard, you can easily store and manage your clients' data and information.</h3>
					<p>
						You can review their interactions, responses and leave them notes for providing feedback. Our backend sentiment analysis helps you understand your patients' moods. And analytics helps you keep track of daily usage and check-ins.  
					</p>
				</div>
				
				<div class="col-md-6 card-img">
					<img src="images/5.jpg" alt="Card-image">
				</div>
				
			</div>
			
			<div class="row wow bounceInLeft">
				
				<div class="col-md-6 content ">
					<h2>Deep Learning Analytics (Beta)</h2>
					<h3>We provide out of the box image, sound and object detection algorithms for students and researchers to easily deploy deep-learning algorithms for research purposes. </h3>
					<p>Experiment and run your own experiments with EVAAI's deep learning analytics.</p>
				</div>
				
				<div class="col-md-6 card-img">
					<img src="images/6.jpg" alt="Card-image">
				</div>
				
			</div>
			
			<div class="row wow bounceInLeft">
				
				<div class="col-md-6 content ">
					<h2>Marketplace for AI assistants and online therapy through social networks and IoT devices.</h2>
					<h3>Users can find AI assistants from the marketplace dealing with different specialties from depression, anxiety, paranoia, mindfulness, and many other mental health categories. </h3>
					<p>As the user, you can choose AI assistants from the many different specialties for basic counseling, consultation, toolkits, setting appointments and connecting for online therapy with the mental health professionals directly from the AI assistants interface. You can also connect them to your favorite social networks or IoT devices such as Google Home and Alexa. We recommend staying within the EVAAI platform as your data is anonymized, encrypted and much safer with EVAAI.</p>
				</div>
				
				<div class="col-md-6 card-img">
					<img src="images/7.jpg" alt="Card-image">
				</div>
				
			</div>

		</div>
	</section>
	
	<jsp:include page="footer.jsp"/>
	
	<% if (!domainBean.hasValidInstance() && loginBean.getGreet() && !loginBean.isLoggedIn() && !loginBean.isMobile()) { %>
		<% loginBean.setGreet(false); %>
	<% } %>
	
	<!-- js -->
	<script src="js/bootstrap.min.js"></script>
	<script src="js/wow.min.js"></script>
	<!--script src="js/mb.bgndGallery.js"></script>
	<script src="js/mb.bgndGallery.effects.js"></script-->
	<script src="js/jquery.simple-text-rotator.min.js"></script>
	<script src="js/jquery.scrollTo.min.js"></script>
	<script src="js/jquery.nav.js"></script>
	<script src="js/modernizr.custom.js"></script>
	<script src="js/grid.js"></script>
	<script src="js/stellar.js"></script>

	<!- Custom Javascript -->
	<script src="js/custom.js"></script>
	<script src="js/videoplay.js"></script>
	<script>
		var ndrotate = document.getElementById("2ndrotate");
		if (ndrotate != null) {
			ndrotate.style.display = "inline-block";
		}
	</script>

<% } %>
<% proxy.clear(); %>
<% } catch (Exception exception) { AdminDatabase.instance().log(exception); }%>

</body>
</html>
