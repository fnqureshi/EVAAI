<%@page import="org.botlibre.web.analytic.AnalyticTestMedia"%>
<%@page import="org.botlibre.web.analytic.AnalyticTestMediaLabel"%>
<%@page import="org.botlibre.web.admin.AdminDatabase"%>
<%@page import="org.botlibre.web.analytic.AnalyticBean"%>
<%@page import="org.botlibre.web.bean.LoginBean.Page"%>
<%@page import="org.botlibre.web.Site"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<%@page import="org.botlibre.emotion.EmotionalState"%>

<%@page contentType="text/html; charset=UTF-8" %>

<jsp:useBean id="proxy" class="org.botlibre.web.bean.SessionProxyBean" scope="session"/>
<jsp:useBean id="loginBean" class="org.botlibre.web.bean.LoginBean" scope="session"/>
<% 
	loginBean = proxy.checkLoginBean(loginBean);
	boolean embed = loginBean.isEmbedded();
	AnalyticBean bean = loginBean.getBean(AnalyticBean.class);
	bean.refreshEntity();
%>

<!DOCTYPE HTML>
<html>
<head>
	<jsp:include page="head.jsp"/>
	<title>Media Repository<%= embed ? "" : " - " + Site.NAME %></title>
	<meta name="description" content="Add and edit the analytic's media files, and metadata labels"/>	
	<meta name="keywords" content="analytic, media"/>
	<%= loginBean.getJQueryHeader() %>
</head>
<style>
	.imgDiv {
		border-style: solid;
		border-color: grey;
		border-width: 1px;
		width: 180px;
		height: 150px;
	}
	.imgDiv img {
		width: 180px;
		max-height: 150px;
	}
	
	#busy {
		display: none;
		padding: 4px
	}
	
	.loader {
		border: 2px solid #f3f3f3;
		border-radius: 50%;
		border-top: 2px solid #3498db;
		width: 30px;
		height: 30px;
		-webkit-animation: spin 0.4s linear infinite; /* Safari */
		animation: spin 0.4s linear infinite;
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
	<body style="background-color: <%= loginBean.getBackgroundColor() %>;">
	<jsp:include page="avatar-banner.jsp"/>
	<div id="embedbody" style="background-color: <%= loginBean.getBackgroundColor() %>;">
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
					<%= loginBean.translate("The analytics test media page allows you to upload test images and media.") %><br/>
					<%= loginBean.translate("You must label the images with the expect test label result.") %><br/>
					<%= loginBean.translate("This data can then be used to test a network for image classification.") %><br/>
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
	<jsp:include page="error.jsp"/>
	<% if (bean.getInstance() == null) { %>
		No analytic network training selected
	<% } else if (!bean.isAdmin()) { %>
		<p style="color:#E00000;"><%= loginBean.translate("Must be admin") %></p>
	<% } else { %>
		<h1>
			<span class="dropt">
				<img src="images/analytic-media.svg" class="admin-banner-pic">
				<div>
					<p class="help">
						<%= loginBean.translate("Upload and label your images and media.") %><br/>
					</p>
				</div>
			</span> <%= loginBean.translate("Test Media Repository") %>
		</h1>
		<form id="analyticForm" action="analytic" method="post" class="message">
			<%= loginBean.postTokenInput() %>
			<%= bean.instanceInput() %>
			<h2><%= loginBean.translate("Labels") %></h2>
			<script type="text/javascript">
				SDK.applicationId = "<%= AdminDatabase.getTemporaryApplicationId() %>";
				var connectSDK = new SDKConnection();
				var currentTotalImages = 0; //to get the current total images of the selected label and update when deleting.
				var euser = new UserConfig();
				<% if (loginBean.isLoggedIn()) { %>
					euser.user = "<%= loginBean.getUser().getUserId() %>";
					euser.token = "<%= loginBean.getUser().getToken() %>";
					connectSDK.user = euser;
				<% } %>
				connectSDK.error = function(error) {
					console.log(error);
					SDK.showError(error, "<%= loginBean.translate("Server Error") %>");
					return;;
				}
					
				function selectAll(){
					var check = document.getElementsByClassName("check");
					for(var i = 0; i < check.length; i++){
						var checkbox = check[i];
						checkbox.checked = true;
					}
				}
				function addLabelDialog() {
					document.getElementById("label-name").value = "";
					return $("#dialog-add-label").dialog({
						modal: true,
						buttons: [
							{
								text: "<%= loginBean.translate("Add") %>",
								click: function() {
									createAnalyticTestMediaLabel();
									$( this ).dialog( "close" );
								},
								class: "okbutton"
							},
							{
								text: "<%= loginBean.translate("Cancel") %>",
								click: function() {
									$( this ).dialog( "close" );
								}
							}
						]
					});
				}
				function deleteSelectedMedia() {
					var divs = document.getElementById("label-images").getElementsByTagName("div");
					var totalDivs = divs.length;
					for (var i = 0; i < divs.length; i++) {
						var checkbox = divs[i].querySelector("#check");
						var label = divs[i].querySelector("#label");
						if (checkbox != null && checkbox.checked) {
							var analyticMediaConfig = new AnalyticMediaConfig();
							analyticMediaConfig.instance = "<%= bean.getInstanceId()%>";
							analyticMediaConfig.mediaId = checkbox.name;
							analyticMediaConfig.label = label.textContent;
							connectSDK.deleteAnalyticTestMedia(analyticMediaConfig, function(){
								divs[i].parentNode.removeChild(divs[i]);
								i--;
								currentTotalImages --;
							});
							document.getElementById("total-label-medias").innerHTML = "<span>" + label.textContent + " <%= loginBean.translate("images")%>:</span> " + currentTotalImages + "<br><br>";
						}

					}
					if (totalDivs == divs.length) {
						SDK.showError("<%= loginBean.translate("Select a media to delete.")%>", "<%= loginBean.translate("Delete Media")%>");
						return;
					}
				}
				function createAnalyticTestMediaLabel(){
					var divs = document.getElementById("label-images").getElementsByTagName("div");
					var label = document.getElementsByClassName("label-name")[0].value;
					if (label == '') {
						SDK.showError("<%= loginBean.translate("Missing label name.")%>", "<%= loginBean.translate("Label")%>");
						return;
					}
					label = label.trim();
					//check if label exists.
					var labels = document.getElementById("labels");
					for (i=0; i<labels.options.length;i++) {
						if (label == labels.options[i].value) {
							SDK.showError("<%= loginBean.translate("Label already exists.")%>", "<%=loginBean.translate("Label")%>");
							$('input[type=text].label').val('');
							return;
						}
					}
					var labelConfig = new LabelConfig();
					labelConfig.instance = "<%= bean.getInstanceId()%>";
					labelConfig.label = label;
					connectSDK.createAnalyticTestMediaLabel(labelConfig);
					var select = document.getElementById("labels");
					var option = document.createElement("option")
					option.text = label;
					option.value = label;
					select.add(option);
					select.value = option.value;
					$('input[type=text].label').val('');
					for(var i = 0; i < divs.length; i++){
						divs[i].parentNode.removeChild(divs[i]);
						i--;
					}
					getAnalyticTestMedia();
				}
				
				function deleteAnalyticLabel() {
					var select = document.getElementById("labels");
					if (select.value == "") {
						SDK.showError("<%= loginBean.translate("Please select a label to delete.")%>", "<%= loginBean.translate("Selection")%>");
						return;
					}
					var divs = document.getElementById("label-images").getElementsByTagName("div");
					if (divs.length > 0) {
						SDK.showError("<%= loginBean.translate("Please delete all contents of the label.")%>", "<%= loginBean.translate("Delete Label")%>");
						return;
					}
					var labelConfig = new LabelConfig();
					labelConfig.instance = "<%= bean.getInstanceId()%>";
					labelConfig.label = select.value;
					console.log(labelConfig.label);
					connectSDK.deleteAnalyticTestMediaLabel(labelConfig);
					select.remove(select.selectedIndex);
					//updating media
					getAnalyticTestMedia();
				}
				
				function getAnalyticTestMedia() {
					document.getElementById("busy").style.display = "inline-block";
					var analyticConfig = new AnalyticConfig();
					analyticConfig.id = <%= bean.getInstanceId()%>
					//passing AnalyticId
					connectSDK.getAnalyticTestMedia(analyticConfig, function(medias) {
						try {
							document.getElementById("label-images").innerHTML = "";
							var selectedLabel = document.getElementById("labels");
							var label = selectedLabel.options[selectedLabel.selectedIndex].text
							
							//status of how many images are displayed
							var totalSelectedMedias = document.createElement("span");
							totalSelectedMedias.id = 'total-label-medias';
							var totalMedias = document.createElement("span");
							totalMedias.id = 'total-medias';
							totalMedias.innerHTML = "<span><%= loginBean.translate("Total images")%>:</span> " + medias.length + "<br>";
							document.getElementById("label-images").appendChild(totalMedias); 
							document.getElementById("label-images").appendChild(totalSelectedMedias); 
							
							var countDisplayedImages = 0;
							for (var i = 0; i < medias.length; i++) {
								if (label == medias[i].label) {
									countDisplayedImages++;
									var divMedia = document.createElement("div");
									divMedia.className = "browse-div";
									divMedia.style.margin = "1px";
									//divMedia.style.height = "200px";
									//creating checkbox
									var checkboxMedia = document.createElement("input");
									checkboxMedia.type = 'checkbox';
									checkboxMedia.id = 'check';
									checkboxMedia.className = 'check';
									checkboxMedia.name = medias[i].mediaId;
									//creating img
									var imgDiv = document.createElement("div");
									imgDiv.className = "imgDiv";
									var imgMedia = document.createElement("img");
									if(medias[i].type == "image/jpeg"){
										imgMedia.src = medias[i].media;
										imgMedia.alt = medias[i].id;
									}else {
										imgMedia.src = "images/audio.png";
										imgMedia.alt = "audio";
									}
									//creating label
									var labelMedia = document.createElement("span");
									labelMedia.innerHTML = " " + medias[i].label;
									labelMedia.id = 'label';
									//creating span
									var spanMedia = document.createElement("span");
									spanMedia.className = 'menu';
									spanMedia.id = 'span';
									spanMedia.innerHTML = medias[i].name + " <br> " + medias[i].type;
									// After creating the media - append all into divMedia.
									imgDiv.appendChild(imgMedia);
									divMedia.appendChild(imgDiv);
									//divMedia.appendChild(document.createElement("br"));
									divMedia.appendChild(checkboxMedia);
									divMedia.appendChild(labelMedia);
									divMedia.appendChild(document.createElement("br"));
									divMedia.appendChild(spanMedia);
									
									document.getElementById("label-images").appendChild(divMedia);
								}
							}
							currentTotalImages = countDisplayedImages;
							document.getElementById("total-label-medias").innerHTML = "<span>" + label + " <%= loginBean.translate("images")%>:</span> " + countDisplayedImages + "<br><br>";
							document.getElementById("busy").style.display = "none";
						} catch(ignore) {
							//it means there is no labels.
							document.getElementById("busy").style.display = "none";
						}
					});
				}
				function uploadFile(){
					var selectedLabel = document.getElementById("labels");
					var label = selectedLabel.options[selectedLabel.selectedIndex].text
					if (label=='') {
						SDK.showError("<%= loginBean.translate("Select a label to upload media.")%>", "<%= loginBean.translate("Label")%>");
					}
					var slabel = document.getElementById("slabel").value;
					slabel = label;
					if (slabel != "") {
						<%if(bean.getAnalyticType().contains(bean.CONV)||bean.getAnalyticType().contains("low")){%> //audio file
							uploadAudioMedia(label, function(){
								location.reload();
								document.getElementById("busy").style.display = "none";
							});
						<%} else { %>
							uploadAnalyticMedia(label); //image file.
						<%}%>
					} else {
						SDK.showError("<%= loginBean.translate("Select a label.")%>", "<%= loginBean.translate("Empty Label")%>");
						return;
					}
				}
				function uploadAnalyticMedia(label){
					fileInput = document.getElementById('upload-analytic-media');
					fileInput.click();
					fileInput.addEventListener("change",function(event){
						document.getElementById("busy").style.display = "inline-block";
						var properties = {"label-selected":label};
						SDK.uploadImage(
							document.getElementById('upload-analytic-media'),
							'analytic-test-media-upload',
							'600',
							null,
							properties,
							function() {
								location.reload();
								document.getElementById("busy").style.display = "none";
						});
					});
				}
				function uploadAudioMedia(label){
					fileInput = document.getElementById('upload-analytic-media')
					fileInput.click();
					fileInput.addEventListener("change",function(event){
						document.getElementById("slabel").value = label;
						document.getElementById("busy").style.display = "inline-block";
						var formData = new FormData(document.getElementById("upload-form"));
						var request = new XMLHttpRequest();
						request.onreadystatechange = function() {
							if (request.readyState != 4) {
								return;
							}
							location.reload();
							document.getElementById("busy").style.display = "none";
						}
						request.open("POST", "analytic-media-upload");
						request.send(formData);
					});
				}

				//list AnalyticMedias when loading the page.
				window.onload = function () { getAnalyticTestMedia(); }
				
			</script>
			<input id="remove-icon" class="icon" type="button" onClick="deleteAnalyticLabel();" value="" title="Delete the selected label">
			<input id="add-icon" class="icon" type="button" onClick="addLabelDialog();" value="" title="Add Label"><br/>
			<%= loginBean.translate("Labels") %>: 
			<select id="labels" name="selected-label" title="Labels" onChange="getAnalyticTestMedia();">
				<% List<AnalyticTestMediaLabel> labels = bean.getInstance().getTestMediaLabels(); %>
					<% if (!labels.isEmpty()) { %>
						<% for(AnalyticTestMediaLabel label : labels) { %>
							<option value="<%= label.getLabel() %>" <%= bean.isLabelTestMediaSelected(label.getLabel()) %>><%= label.getLabel() %></option>
						<% } %>
					<% } %>	
			</select>
			<hr/>
			<br/>
			<h2><%= loginBean.translate("Media") %></h2>
			<input id="select-icon" class="icon" type="button" onClick="selectAll();" value="" title="<%= loginBean.translate("Select all media.") %>">
			<input id="remove-icon" onclick="deleteSelectedMedia();" class="icon" type="button" value="" title="<%= loginBean.translate("Delete the selected media.") %>">
			<input id="import-icon" onclick="uploadFile()" class="icon" type="button" value="" title="<%= loginBean.translate("Upload images files for the analytic") %>"/>
			<br><div id="busy" class="loader"></div>
		</form>
		<form id="upload-form" action="analytic-media-upload" method="post" enctype="multipart/form-data" class="message">
			<%= loginBean.postTokenInput() %>
			<input id="slabel" type="text" class="hidden" name="label-selected"/>
			<input id="upload-analytic-media" class="hidden" type="file" name="file" multiple>
		</form>
		<br/>
		<div id="label-images">
			<!-- Place holder for label images fetched through JavaScript. -->
		</div>
		
		<!-- Add label dialog. -->
		<div id="dialog-add-label" class='dialog' title='<%= loginBean.translate("New Label") %>'>
			<form action="#" method="post" class="message">
				<%= loginBean.postTokenInput() %>
				<%= loginBean.translate("Label Name")%><br/>
				<input id="label-name" class="label-name" name="label-name" type="text">
				<script>
				$('#label-name').keypress(function(event) {
					if (event.keyCode == $.ui.keyCode.ENTER) {
						createAnalyticTestMediaLabel();
						$("#dialog-add-label").dialog( "close" );
						return false;
					}
				});
				</script>
			</form>
		</div>
			
	<% } %>
	</div>
	<% if (!embed) { %>
		</div>
		</div>
		<jsp:include page="footer.jsp"/>
	<% } %>
	<% proxy.clear(); %>
</body>
</html>
