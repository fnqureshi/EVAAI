<%@page import="org.botlibre.util.Utils"%>
<%@page import="org.botlibre.web.analytic.Analytic"%>
<%@page import="org.botlibre.web.bean.LoginBean"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="org.botlibre.web.analytic.AnalyticBean"%>
<%@page import="org.botlibre.web.admin.MediaFile"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.botlibre.web.admin.Category"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page"%>
<%@page import="org.botlibre.web.admin.User"%>
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
	bean.refreshEntity();
	
%>
<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title><%= title %> Analytic - <%= Site.NAME %></title>
	<%-- loginBean.embedCSS(loginBean.getCssURL(), out); %>
	<%= LoginBean.getJQueryHeader() --%>
	<meta name="description" content="Add or update analytic."/>
	<meta name="keywords" content="images, analytic, network, test analytic"/>
	<%= loginBean.getJQueryHeader() %>
	<script>
	$(function() {
		$( "#dialog-resetNetwork" ).dialog({
			autoOpen: false,
			modal: true,
			buttons: {
				"OK": function() {
					document.getElementById("reset-network").click()
				},
				Cancel: function() {
					$( this ).dialog( "close" );
				}
			}
		});
		
		$( "#resetnetwork" ).click(function() {
			$( "#dialog-resetNetwork" ).dialog( "open" );
			return false;
		});
	});
	
	$(function() {
		$( "#dialog-deleteMedia" ).dialog({
			autoOpen: false,
			modal: true,
			buttons: {
				"OK": function() {
					document.getElementById("delete-media").click()
					return false;
				},
				Cancel: function() {
					$( this ).dialog( "close" );
					return false;
				}
			}
		});
		
		$( "#remove-icon" ).click(function() {
			$( "#dialog-deleteMedia" ).dialog( "open" );
			return false;
		});
	});
	</script>
</head>
<style>

#busy {
	display: none;
	padding: 3px
}
#busy-text {
	display: none;
}
.loader {
  border: 4px solid gray;
  border-radius: 50%;
  border-top: 4px solid lightblue;
  width: 25px;
  height: 25px;
  -webkit-animation: spin 0.5s linear infinite; /* Safari */
  animation: spin 0.5s linear infinite;
}

/* Safari */
@-webkit-keyframes spin {
  0% { -webkit-transform: rotate(0deg); }
  100% { -webkit-transform: rotate(360deg); }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
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
	<div id="admin-topper" align="left">
		<div class="clearfix">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("The analytics network page allows you to upload and configure for network binary and labels files.") %><br/>
					<%= loginBean.translate("To upload your own network binary you need to build this graph pb file using Tensorflow.") %><br/>
					<%= loginBean.translate("You can also have the network generated for you by uploading images and training the network from the media and training pages.") %><br/>
				</div>
				<%= loginBean.translate("Help") %>
			</span>
			<% if (!Site.DEDICATED) { %>
			 : <a href="manual-analytic.jsp"><%= loginBean.translate("Docs") %></a>
			 : <a target="_blank" href="https://www.botlibre.com/forum-post?id=22972468"><%= loginBean.translate("How To Guide") %></a>
			<% } %>
		</div>
	</div>
	<div id="mainbody">
	<div id="contents">
	<div class="browse">
<% } %>
	<h1>
		<span class="dropt">
			<img src="images/analytic.png" class="admin-banner-pic">
			<div>
				<p class="help">
					<%= loginBean.translate("Configure and upload your network binary and labels files.") %><br/>
				</p>
			</div>
		</span> <%= loginBean.translate("Analytic Network") %>
	</h1>
	<jsp:include page="error.jsp"/>
	<% if (bean.getInstance() == null) { %>
		<%= loginBean.translate("No analytic selected") %>
	<% } else if (!bean.isAdmin()) { %>
		<p style="color:#E00000;"><%= loginBean.translate("Must be admin") %></p>
	<% } else { %>
	<form action="analytic" method="post" class="message">
		<%= loginBean.postTokenInput() %>
		<span class="dropt-banner">
			<img id="help-mini" src="images/help.svg"/>
			<div>
				<%= loginBean.translate("Select machine learning library to use.") %>
			</div>
		</span>
		<%= loginBean.translate("Library Type: ") %>
		<select id="libraryType" name="library-type" title="Type of Library" onchange="libraryChanged()">
			<option value='<%= Analytic.Tensorflow %>' <%= bean.isLibraryTypeSelected(Analytic.Tensorflow) %>><%= loginBean.translate("Tensorflow") %></option>
			<option value='<%= Analytic.BotLibre %>' <%= bean.isLibraryTypeSelected(Analytic.BotLibre) %>><%= loginBean.translate("Bot Libre") %></option>
		</select>
		<br/>
		<div id="botlibre" style="display:none">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("Number of input nodes") %><br/>
				</div>
			</span>
			<%= loginBean.translate("Input Nodes") %><br/>
			<input id="txtInputs" type="number" name="inputs" value="<%= bean.getInputs() %>"><br/>
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("Number of output nodes") %><br/>
				</div>
			</span>
			<%= loginBean.translate("Output Nodes") %><br/>
			<input id="txtOutputs" type="number" name="outputs" value="<%= bean.getInstance().getOutputs() %>"><br/>
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("Number of intermediate nodes") %><br/>
				</div>
			</span>
			<%= loginBean.translate("Intermediate Nodes") %><br/>
			<input id="txtIntermediates" type="number" name="intermediates" value="<%= bean.getInstance().getIntermediates() %>"><br/>
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("Number of intermediate layers") %><br/>
				</div>
			</span>
			<%= loginBean.translate("Intermediate Layers") %><br/>
			<input id="txtLayers" type="number" name="layers" value="<%= bean.getInstance().getLayers() %>"><br/>
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("Activation Function") %><br/>
				</div>
			</span>
			<%= loginBean.translate("Activation Function: ") %>
			<select id="ActivatonFunc" name="activation-function">
				<option value='Tanh' <%= bean.isActivationFuncSelected("Tanh") %>><%= loginBean.translate("Tanh") %></option>
				<option value='Sigmoid' <%= bean.isActivationFuncSelected("Sigmoid") %>><%= loginBean.translate("Sigmoid") %></option>
			</select>
			<br/>
		</div>
		
		<div id="tensorflow">
		<script type="text/javascript">
			var selectBox, selectedValue, analytic_network_settings, analyticFeed, analyticFetch, analyticSize;
			
			window.onload = function() {
				libraryChanged();
				changeAnalyticType(true);
			};
			
			function libraryChanged() {
				var selectBox = document.getElementById("libraryType");
				var selectedValue = selectBox.options[selectBox.selectedIndex].value;
				if (selectedValue == "BotLibre") {
					document.getElementById("tensorflow").style.display = "none";
					document.getElementById("botlibre").style.display = "inline-block";
				}
				if (selectedValue == "Tensorflow") {
					document.getElementById("botlibre").style.display = "none";
					document.getElementById("tensorflow").style.display = "inline-block";
				}
			}
				
			function changeAnalyticType(onload) {
				selectBox = document.getElementById("analyticType");
				selectedValue = selectBox.options[selectBox.selectedIndex].value;
				analytic_network_settings = document.getElementById("analytic_network_settings");
				audio_network_settings = document.getElementById("audio_network_settings");
				analyticFeed = document.getElementById("txtAnalyticFeed");
				analyticFetch = document.getElementById("txtAnalyticFetch");
				analyticSize = document.getElementById("txtAnalyticSize");
				audioInput = document.getElementById("txt-audio-input");
				audioOutput = document.getElementById("txt-audio-output");
				custom_network_settings = document.getElementById("custom_network_settings");
				
	 			function objectDetectionOption(setting){
					if (setting) {
						audio_network_settings.style.display = "none";
						analytic_network_settings.style.display = "none";
						custom_network_settings.style.display = "none";
					} else {
						audio_network_settings.style.display = "none";
						analytic_network_settings.style.display = "block";
						custom_network_settings.style.display = "none";
					}
				}
	 			
	 			function audioAnalyticOption(setting){
	 				if (setting) {
						analytic_network_settings.style.display = "none";
						audio_network_settings.style.display = "block";
						custom_network_settings.style.display = "none";
					} else {
						analytic_network_settings.style.display = "block";
						audio_network_settings.style.display = "none";
						custom_network_settings.style.display = "none";
					}
	 			}
	 			
	 			function customAnalyticOption(setting) {
	 				if (setting) {
						analytic_network_settings.style.display = "none";
						audio_network_settings.style.display = "none";
						custom_network_settings.style.display = "block";
					} else {
						analytic_network_settings.style.display = "block";
						audio_network_settings.style.display = "none";
						custom_network_settings.style.display = "none";
					}
	 			}
				
	 			if (onload != true) {
					if (selectedValue == "inception_v3") {
						analyticSize.value = "299";
						analyticFeed.value = "Mul";
						analyticFetch.value = "final_result";
					} else if (selectedValue == "object_detection"){
						//clear unused data
						analyticSize.value = "0";
						analyticFeed.value = "none";
						analyticFetch.value = "none";
						audioInput.value = "none";
						audioOutput.value = "none";
					} else if (selectedValue == "conv" || selectedValue == "low_latency_conv" || selectedValue == "low_latency_svdf") {
						//clear unused data
						analyticSize.value = "0";
						analyticFeed.value = "none";
						analyticFetch.value = "none";
						audioInput.value = "wav_data:0";
						audioOutput.value = "labels_softmax:0";
					} else if (selectedValue == "custom") {
						//clear unused data
						analyticSize.value = "0";
						analyticFeed.value = "none";
						analyticFetch.value = "none";
						audioInput.value = "none";
						audioOutput.value = "none";
					} else {
						//mobilenet_0.x
						analyticFeed.value = "input";
						analyticFetch.value = "final_result";
						analyticSize.value = "224";
						audioInput.value = "none";
						audioOutput.value = "none";
					}
	 			}
				if (selectedValue == "inception_v3") {
					objectDetectionOption(false);
				} else if (selectedValue == "object_detection") {
					objectDetectionOption(true);
				} else if (selectedValue == "conv" || selectedValue == "low_latency_conv" || selectedValue == "low_latency_svdf") {
					audioAnalyticOption(true);
				} else if (selectedValue == "custom") {
					customAnalyticOption(true);
				} else {
					objectDetectionOption(false);
				}
		}
				
		</script>
		<span class="dropt-banner">
			<img id="help-mini" src="images/help.svg"/>
			<div>
				<%= loginBean.translate("Select the type of network. For image classification integrated support is provided for Inception v3 and MobileNet.") %><br/>
				<%= loginBean.translate("MobileNet is smaller and faster. Inception is slower but can be more accurate.") %><br/>
			</div>
		</span>
		<%= loginBean.translate("Analytic Type: ") %>
		<select id="analyticType" name="analytic-type" title="Type of Analytic" onchange="changeAnalyticType();">
			<option disabled>Model inceptions - for image recognition</option>
			<option value='inception_v3' <%= bean.isTypeSelected("inception_v3") %>><%= loginBean.translate("inception_v3") %></option>
			<option disabled>Model mobilenet - for image recognition</option>
			<option value='mobilenet_0.25' <%= bean.isTypeSelected("mobilenet_0.25") %>><%= loginBean.translate("mobilenet_0.25") %></option>
			<option value='mobilenet_0.50' <%= bean.isTypeSelected("mobilenet_0.50") %>><%= loginBean.translate("mobilenet_0.50") %></option>
			<option value='mobilenet_0.75' <%= bean.isTypeSelected("mobilenet_0.75") %>><%= loginBean.translate("mobilenet_0.75") %></option>
			<option value='mobilenet_1.0' <%= bean.isTypeSelected("mobilenet_1.0") %>><%= loginBean.translate("mobilenet_1.0") %></option>
			<option disabled>Model object detection api</option>
			<option value='object_detection' <%= bean.isTypeSelected("object_detection") %>><%= loginBean.translate("object_detection") %></option>
			<option disabled>Model convs - for audio recognition</option>
			<option value='conv' <%= bean.isTypeSelected("conv") %>><%= loginBean.translate("conv") %></option>
			<option value='low_latency_conv' <%= bean.isTypeSelected("low_latency_conv") %>><%= loginBean.translate("low_latency_conv") %></option>
			<option value='low_latency_svdf' <%= bean.isTypeSelected("low_latency_svdf") %>><%= loginBean.translate("low_latency_svdf") %></option>
			<option disabled>Model custom - for custom network models</option>
			<option value='custom' <%= bean.isTypeSelected("custom") %>><%= loginBean.translate("custom") %></option>
		</select>
		<br/>
		
		<div id="analytic_network_settings">
		<span class="dropt-banner">
			<img id="help-mini" src="images/help.svg"/>
			<div>
				<%= loginBean.translate("Set the image size of your network. This depends on how you trained your network binary, use 128, 160, 192, 224 for mobilenet_* and use  299 only for inception_v3.") %><br/>
			</div>
		</span>
		<%= loginBean.translate("Analytic Image Size") %><br/>
		<input id="txtAnalyticSize" type="number" name="analytic-size" value="<%= bean.getImageSize() %>"><br/>
		<span class="dropt-banner">
			<img id="help-mini" src="images/help.svg"/>
			<div>
				<%= loginBean.translate("Enter the feed/input name used when training the network binary. This is normally \"input\" for mobilenet_* and \"Mul\" for inception_v3.") %><br/>
			</div>
		</span>
		<%= loginBean.translate("Analytic Feed") %><br/>
		<input id="txtAnalyticFeed" type="text" name="analytic-feed" value="<%= bean.getFeed() %>"><br/>
		<span class="dropt-banner">
			<img id="help-mini" src="images/help.svg"/>
			<div>
				<%= loginBean.translate("Enter the fetch/output name used when training the network binary. This is normally \"final_result\" but may also be \"softmax\" for some networks.") %><br/>
			</div>
		</span>
		<%= loginBean.translate("Analytic Fetch") %><br/>
		<input id="txtAnalyticFetch" type="text" name="analytic-fetch" value="<%= bean.getFetch() %>"><br/>
		</div>
		
		<div id="audio_network_settings">
		<span class="dropt-banner">
			<img id="help-mini" src="images/help.svg"/>
			<div>
				<%= loginBean.translate("Name of WAVE data input node in model. default (wav_data:0)") %><br/>
			</div>
		</span>
		<%= loginBean.translate("Input Name") %><br/>
		<input id="txt-audio-input" type="text" name="audio-input-name" value="<%= bean.getAudioInputName() %>"><br/>
		<span class="dropt-banner">
			<img id="help-mini" src="images/help.svg"/>
			<div>
				<%= loginBean.translate("Name of node outputting a prediction in the model. default (labels_softmax:0)") %><br/>
			</div>
		</span>
		<%= loginBean.translate("Output Name") %><br/>
		<input id="txt-audio-output" type="text" name="audio-output-name" value="<%= bean.getAudioOutputName() %>"><br/>
		</div>
		
		<div id="custom_network_settings">
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("Number of input nodes") %><br/>
				</div>
			</span>
			<%= loginBean.translate("Input Nodes") %><br/>
			<input id="customInputs" type="number" name="custom-inputs" value="<%= bean.getInputs() %>"><br/>
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("Number of output nodes") %><br/>
				</div>
			</span>
			<%= loginBean.translate("Output Nodes") %><br/>
			<input id="customOutputs" type="number" name="custom-outputs" value="<%= bean.getInstance().getOutputs() %>"><br/>
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("Number of intermediate nodes") %><br/>
				</div>
			</span>
			<%= loginBean.translate("Intermediate Nodes") %><br/>
			<input id="customIntermediates" type="number" name="custom-intermediates" value="<%= bean.getInstance().getIntermediates() %>"><br/>
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("Number of intermediate layers") %><br/>
				</div>
			</span>
			<%= loginBean.translate("Intermediate Layers") %><br/>
			<input id="customLayers" type="number" name="custom-layers" value="<%= bean.getInstance().getLayers() %>"><br/>
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("Activation Function") %><br/>
				</div>
			</span>
			<%= loginBean.translate("Activation Function: ") %>
			<select id="customActivatonFunc" name="custom-activation-function">
				<option value='Tanh' <%= bean.isActivationFuncSelected("Tanh") %>><%= loginBean.translate("Tanh") %></option>
				<option value='Sigmoid' <%= bean.isActivationFuncSelected("Sigmoid") %>><%= loginBean.translate("Sigmoid") %></option>
			</select>
			<br/>
		</div>
		
		</div>
		
		<br>
		<input name="save-analytic" type="submit" value="<%= loginBean.translate("Save") %>"/>
		<input id= "resetnetwork" name="resetnetwork" onclick= "return false;" type="submit" value="<%= loginBean.translate("Reset Network") %>"/>
		<br/>
	</form>
	<hr/>
		<form id="upload-form" action="analytic-binary-upload" method="post" enctype="multipart/form-data" class="message">
			<%= loginBean.postTokenInput() %>
			<%= proxy.proxyInput() %>
			<%= bean.instanceInput() %>
			<h2><%= loginBean.translate("Network") %></h2>
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("The network is defined by its binary graph file.") %><br/>
					<%= loginBean.translate("For Tensorflow this is normally a .pb file, which has been trained using Tensorflow in python, or using the Train page in the network Admin Console.") %><br/>
				</div>
			</span>
			<% if (bean.isAdmin()) { %>
				<% if (!bean.getInstance().getBinaryFileName().isEmpty()){ %>
					<%= loginBean.translate("File Name: ") + bean.getInstance().getBinaryFileName()%><br/>
				<% } %>
				<div id="busy" class="loader"></div>
				<p id="busy-text">Uploading File...</p></br>
					<input id="upload-binary" class="hidden" type="file" onchange="this.form.submit(); document.getElementById('busy').style.display = 'inline-block';
					document.getElementById('busy-text').style.display = 'inline-block';" name="file" value="" title="<%= loginBean.translate("Upload a pre-trained network binary file. Do not upload copyright material unless you own the copyright.") %>"/>
					<input id="import-icon" class="icon" type="submit" value="" onclick="document.getElementById('upload-binary').click(); return false;"/>

					<input id="export-icon" name="download-binary" class="icon" type="submit" value="" title="<%= loginBean.translate("Download the network binary file") %>"/>
					<input id="remove-icon" class="icon" type="submit" name="delete-media" value="" title="<%= loginBean.translate("Delete the network binary file") %>"/>
		</form>
			<% } %>
		<hr>
			<h2><%= loginBean.translate("Labels") %></h2>
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("Most Tensorflow networks also have a label files that stores the classification or other information.") %><br/>
					<%= loginBean.translate("This file is generated with the .pb file when training a network in Tensorflow.") %><br/>
				</div>
			</span>
			<% if (!bean.getInstance().getLabelsFileName().isEmpty()){ %>
				<%= loginBean.translate("File Name: ") + bean.getInstance().getLabelsFileName()%><br/>
			<% } %>
			<form id="upload-form" action="analytic-labels-upload" method="post" enctype="multipart/form-data" class="message">
				<%= loginBean.postTokenInput() %>
				<%= proxy.proxyInput() %>
				<%= bean.instanceInput() %>
				<% if (bean.isAdmin()) { %>
					<input id="upload-labels" class="hidden" type="file" onchange="this.form.submit()" name="file" value=""/>
					<input id="download-labels" class="hidden" type="submit" name="download-labels" value="">
				<% } %>
			</form>
			<form id="save-form" action="analytic" method="post" class="message">
				<%= loginBean.postTokenInput() %>
				<%= proxy.proxyInput() %>
				<%= bean.instanceInput() %>
				<textarea name="labels"><%= Utils.sanitize(bean.getInstance().getLabelsNetwork()) %></textarea></br>
				<input id="import-icon" class="icon" type="submit" value="" onclick="document.getElementById('upload-labels').click(); return false;" title="<%= loginBean.translate("Upload the trained labels") %>"/>
				<input id="export-icon" class="icon" type="submit" value="" onclick="document.getElementById('download-labels').click(); return false;" title="<%= loginBean.translate("Download the trained labels") %>">
				<input id="save-icon" class="icon" name="save-labels" type="submit" value="" title="<%= loginBean.translate("Save the trained labels") %>">
			</form>
			
			<!-- Labels Properties -->
			<h2><%= loginBean.translate("Properties") %></h2>
			<span class="dropt-banner">
				<img id="help-mini" src="images/help.svg"/>
				<div>
					<%= loginBean.translate("Bot Libre also allows a properties file that can store properties about the network.") %><br/>
					<%= loginBean.translate("This file is used for object detection label colors.") %><br/>
					<%= loginBean.translate("To set a specific color for a label add the css color after the label name, i.e.") %><br/>
					<pre><%= loginBean.translate("person: red") %>
<%= loginBean.translate("dog: #FFA500") %></pre>
				</div>
			</span>
			<% if (!bean.getInstance().getPropertiesFileName().isEmpty()){ %>
				<%= loginBean.translate("Properties File Name: ") + bean.getInstance().getPropertiesFileName()%><br/>
			<% } %>
			<form id="upload-form" action="analytic-labels-properties-upload" method="post" enctype="multipart/form-data" class="message">
				<%= loginBean.postTokenInput() %>
				<%= proxy.proxyInput() %>
				<%= bean.instanceInput() %>
				<% if (bean.isAdmin()) { %>
				<input id="upload-labels-properties" class="hidden" type="file" onchange="this.form.submit()" name="file" value=""/>
				<input id="download-labels-properties" class="hidden" type="submit" name="download-labels" value="">
			<% } %>
			</form>
			<form id="save-form" action="analytic" method="post" class="message">
				<%= loginBean.postTokenInput() %>
				<%= proxy.proxyInput() %>
				<%= bean.instanceInput() %>
				<textarea name="labels-properties"><%= Utils.sanitize(bean.getInstance().getPropertiesLabels()) %></textarea></br>
				<input id="import-icon" class="icon" type="submit" value="" onclick="document.getElementById('upload-labels-properties').click(); return false;" title="<%= loginBean.translate("Upload the properties labels") %>"/>
				<input id="export-icon" class="icon" type="submit" value="" onclick="document.getElementById('download-labels-properties').click(); return false;" title="<%= loginBean.translate("Download the properties labels") %>">
				<input id="save-icon" class="icon" name="save-labels-properties" type="submit" value="" title="<%= loginBean.translate("Save the properties labels") %>">
			</form>
	<% } %>
		</div>
		</div>
		<% if(!embed){ %>
			</div>
		<% }%>
		
	<div id="dialog-resetNetwork" title="<%= loginBean.translate("Reset Network") %>" class="dialog">
		<form action="analytic" method="post" class="message">
			<%= loginBean.postTokenInput() %>
			<%= bean.instanceInput() %>
			<%= loginBean.translate("This will reset the analytic network") %><br/>
			<input id="reset-network" class="delete" type="submit" name="reset-network" value="<%= loginBean.translate("Reset") %>" style="display:none">
		</form>
	</div>
	
	<div id="dialog-deleteMedia" title="<%= loginBean.translate("Delete Media") %>" class="dialog">
		<form action="analytic-binary-upload" method="post" enctype="multipart/form-data" class="message">
			<%= loginBean.postTokenInput() %>
			<%= proxy.proxyInput() %>
			<%= bean.instanceInput() %>
			<%= loginBean.translate("This will delete the analytic network") %><br/>
			<input id="delete-media" class="delete" type="submit" name="delete-media" value="<%= loginBean.translate("Delete") %>" style="display:none">
		</form>
	</div>
		
	<jsp:include page="footer.jsp"/>
</body>
</html>
