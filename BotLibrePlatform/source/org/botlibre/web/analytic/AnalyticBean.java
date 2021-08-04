/******************************************************************************
 *
 *  Copyright 2013-2019 Paphus Solutions Inc.
 *
 *  Licensed under the Eclipse Public License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/
package org.botlibre.web.analytic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.commons.lang.StringUtils;
import org.botlibre.BotException;
import org.botlibre.analytics.deeplearning.NeuralNetwork;
import org.botlibre.util.Utils;

import org.botlibre.web.Site;
import org.botlibre.web.admin.AdminDatabase;
import org.botlibre.web.admin.AvatarImage;
import org.botlibre.web.admin.Domain;
import org.botlibre.web.admin.Media;
import org.botlibre.web.admin.MediaFile;
import org.botlibre.web.admin.User;
import org.botlibre.web.bean.SessionProxyBean;
import org.botlibre.web.bean.WebMediumBean;
import org.botlibre.web.service.Stats;

public class AnalyticBean extends WebMediumBean<Analytic> {
	public static boolean trainingInSession = false;
	public static boolean processingTestMediaInSession = false;
	
	protected String typeFilter = "";
	protected boolean selectAll;
	protected String selectedLabel = "";
	protected String selectedTestMediaLabel = "";
	
	/**
	 * based on selection, analytic types
	 */
	public final String INCEP_V3 ="inception_v3", 
			MOBNET50="mobilenet_0.50",
			MOBNET25="mobilenet_0.25",
			MOBNET75="mobilenet_0.75",
			MOBNET100="mobilenet_1.0",
			OBJECT_DETECTION="object_detection",
			CONV="conv",
			LOW_CONV="low_latency_conv",
			LOW_SVDF="low_latency_svdf",
			CUSTOM="custom";
			
	/**
	 * Input Settings
	 * "input" for mobilenet_050
	 * "decodeJepeg/contents" , "Mul:0" for inception_v3
	 */
	public final String INPUT = "input";
	public final String DECODEJEPG_CONTENTS = "DecodeJpeg/contents";
	public final String MUL = "Mul:0";
	
	/**
	 * Output Settings
	 * "final_result" for inception_v3 & mobilenet050
	 * image size only for mobilenet050 and it can be changed.
	 * default Image size : 224
	 */
	public final String SOFTMAX = "softmax";
	public final String FINAL_RESULT = "final_result";
	
	/**
	 * Audio Input Output Settings
	 */
	public final String WAV_DATA="wav_data:0";
	public final String LABELS_SOFTMAX="labels_softmax:0";
	
	
	public void setAnalyticType(String analyticType) {
		getInstance().setAnalyticType(analyticType);
	}

	public String getAnalyticType() {
		if (getInstance().getAnalyticType() != null || getInstance().getAnalyticType() != "") {
			return this.getInstance().getAnalyticType();
		}
		return "";
	}
	public String getTrainingStatus(){
		return getInstance().getTrainingStatus();
	}

	@Override
	public String getEmbeddedBanner() {
		return "analytic-banner.jsp";
	}

	@Override
	public String getPostAction() {
		return "analytic";
	}

	public boolean hasEmptyToolbar(boolean embed) {
		return false;
	}

	
	public String getMediaFileName(AnalyticMedia media) {
		String file = media.getFileName();
		if (file == null) {
			return "images/analytic.png";
		}
		return file;
	}

	public void deleteAnalyticMedia(long id) {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			setInstance(AdminDatabase.instance().deleteAnalyticMedia(id, this.instance));
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public void deleteAnalyticTestMedia(long id) {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			setInstance(AdminDatabase.instance().deleteAnalyticTestMedia(id, this.instance));
		} catch (Exception exception) {
			error(exception);
		}
	}
	
	public AnalyticMedia createAnalyticMedia(byte[] data, String name, String type, String label) {
		try {
			checkLogin();
			checkAdmin();
			checkInstance();
			Media media = new Media();
			media.setMedia(data);
			AnalyticMedia analyticMedia = new AnalyticMedia();
			analyticMedia.setName(Utils.sanitize(name));
			analyticMedia.setType(Utils.sanitize(type));
			analyticMedia.setLabel(Utils.sanitize(label));
			analyticMedia.checkMediaType();
			setInstance(AdminDatabase.instance().addAnalyticMedia(analyticMedia, media, this.instance));
			Stats.stats.analyticImageUpload++;
			return analyticMedia;
		} catch (Exception failed) {
			error(failed);
			return null;
		}
	}
	
	public AnalyticTestMedia createAnalyticTestMedia(byte[] data, String name, String type, String label) {
		try {
			checkLogin();
			checkAdmin();
			checkInstance();
			Media media = new Media();
			media.setMedia(data);
			AnalyticTestMedia analyticTestMedia = new AnalyticTestMedia();
			analyticTestMedia.setName(Utils.sanitize(name));
			analyticTestMedia.setType(Utils.sanitize(type));
			analyticTestMedia.setLabel(Utils.sanitize(label));
			analyticTestMedia.checkMediaType();
			setInstance(AdminDatabase.instance().addAnalyticTestMedia(analyticTestMedia, media, this.instance));
			Stats.stats.analyticTestImageUpload++;
			return analyticTestMedia;
		} catch (Exception failed) {
			error(failed);
			return null;
		}
	}
	
	public AnalyticLabel createAnalyticLabel(String label) {
		try {
			checkLogin();
			checkAdmin();
			checkInstance();
			label = Utils.sanitize(label.trim());
			AnalyticLabel nlabel = new AnalyticLabel();
			nlabel.setLabel(label);
			setInstance(AdminDatabase.instance().addAnalyticLabel(nlabel,this.instance));
			setSelectedLabel(label);
			return nlabel;
		} catch (Exception failed) {
			error(failed);
			return null;
		}
	}
	
	public AnalyticTestMediaLabel createAnalyticTestMediaLabel(String label) {
		try {
			checkLogin();
			checkAdmin();
			checkInstance();
			AnalyticTestMediaLabel nlabel = new AnalyticTestMediaLabel();
			nlabel.setLabel(label);
			setInstance(AdminDatabase.instance().addAnalyticTestMediaLabel(nlabel,this.instance));
			setSelectedTestMediaLabel(label);
			return nlabel;
		} catch (Exception failed) {
			error(failed);
			return null;
		}
	}
	
	public void setSelectedLabel(String label) {
		selectedLabel = Utils.sanitize(label);
	}
	
	public String getSelectedLabel() {
		return selectedLabel;
	}
	
	public void setSelectedTestMediaLabel(String label){
		selectedTestMediaLabel = label;
	}
	
	public String getSelectedTestMediaLabel(){
		return selectedTestMediaLabel;
	}
	
	/**
	 * throws an error and return false
	 * used only once, AnalyticServlet -> deleteInstance
	 * if it catches a database error (when there are some of contents of the Analytic exists on the selected analytic) 
	 * @param error
	 * @return
	 */
	public boolean sendError(String error) {
		BotException e = new BotException(error);
		error(e);
		return false;
	}
	
	public boolean deleteAnalyticLabel(String label) {
		try {
			checkLogin();
			checkAdmin();
			checkInstance();
			setInstance(AdminDatabase.instance().removeAnalyticLabel(label,this.instance));
			if(!getInstance().getLabels().isEmpty()){
				setSelectedLabel(getInstance().getLabels().get(0).getLabel());
			}else{
				setSelectedLabel("");
			}
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	public boolean deleteAnalyticTestMediaLabel(String label) {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			setInstance(AdminDatabase.instance().removeAnalyticTestMediaLabel(label,this.instance));
			if(!getInstance().getTestMediaLabels().isEmpty()){
				setSelectedTestMediaLabel(getInstance().getTestMediaLabels().get(0).getLabel());
			}else{
				setSelectedTestMediaLabel("");
			}
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}
	
	/**
	 * Download the network Binary file.
	 */
	public boolean downloadNetworkBinary(HttpServletResponse response) {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			if (this.instance.getBinaryNetwork() == null) {
				throw new BotException("Network Binary file is missing");
			}

			response.setContentType(this.instance.getBinaryNetwork().getType());
			response.setHeader("Content-disposition",
					"attachment; filename=" + encodeURI(this.instance.getBinaryFileName()));
			OutputStream out = response.getOutputStream();
			Media data = AdminDatabase.instance().findMedia(this.instance.getBinaryNetwork().getMediaId());
			out.write(data.getMedia(), 0, data.getMedia().length);
			//out.flush();
		} catch (Exception exception) {
			error(exception);
			return false;
		}
		return true;
	}

	/**
	 * Download the labels.
	 */
	public boolean downloadNetworkLabels(HttpServletResponse response) {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			if (this.instance.getLabelsNetwork() == null|| this.instance.getLabelsNetwork().isEmpty()) {
				throw new BotException("Network Labels file is missing");
			}
			response.setContentType("text/plain");
			response.setHeader("Content-disposition",
					"attachment; filename=" + encodeURI(this.instance.getLabelsFileName()));
			PrintWriter writer = response.getWriter();
			writer.write(this.instance.getLabelsNetwork());
			// writer.flush();
		} catch (Exception exception) {
			error(exception);
			return false;
		}
		return true;
	}
	
	/**
	 * Download the labels.
	 */
	public boolean downloadPropertiesLabels(HttpServletResponse response) {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			if (this.instance.getPropertiesLabels() == null|| this.instance.getPropertiesLabels().isEmpty()) {
				throw new BotException("Properties Labels file is missing");
			}
			response.setContentType("text/plain");
			response.setHeader("Content-disposition",
					"attachment; filename=" + encodeURI(this.instance.getPropertiesFileName()));
			PrintWriter writer = response.getWriter();
			writer.write(this.instance.getPropertiesLabels());
			// writer.flush();
		} catch (Exception exception) {
			error(exception);
			return false;
		}
		return true;
	}

	public void saveBinaryNetwork(byte[] data, String name, String type) {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			Media media = new Media();
			media.setMedia(data);
			MediaFile mediaFile = new MediaFile();
			mediaFile.setName(Utils.sanitize(name));
			mediaFile.setType(Utils.sanitize(type));
			setInstance(AdminDatabase.instance().updateAnalyticBinaryNetwork(mediaFile, media, this.instance));
			Stats.stats.analyticBinaryUpload++;
		} catch (Exception failed) {
			error(failed);
		}
	}
	
	public void resetNetwork() {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			if (Analytic.BotLibre.equals(getInstance().libraryType)) {
				int nodes[] = new int[getInstance().getLayers() + 2];
				nodes[0] = getInstance().getInputs();
				for (int count = 1; count < (nodes.length - 1); count++) {
					nodes[count] = getInstance().getIntermediates();
				}
				nodes[nodes.length - 1] = getInstance().getOutputs();
				NeuralNetwork network= new NeuralNetwork(nodes);
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				ObjectOutputStream  stream = new ObjectOutputStream(bytes);
				stream.writeObject(network);
				stream.close();
	
				Media media = new Media();
				media.setMedia(bytes.toByteArray());
				MediaFile mediaFile = new MediaFile();
				mediaFile.setName("network.ser");
				mediaFile.setType("application/octet-stream");
				setInstance(AdminDatabase.instance().updateAnalyticBinaryNetwork(mediaFile, media, this.instance));
				Stats.stats.analyticBinaryUpload++;
			}
			else if (Analytic.Tensorflow.equals(getInstance().libraryType) && CUSTOM.equals(getInstance().analyticType)) {
				if (getInstance().getBinaryNetwork() == null) {
					Media media = new Media();
					MediaFile mediaFile = new MediaFile();
					setInstance(AdminDatabase.instance().updateAnalyticBinaryNetwork(mediaFile, media, this.instance));
				}
				
				MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
				multipartEntity.addPart("xml", new StringBody(this.toXML(getInstance()),"text/xml",Charset.defaultCharset()));
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(Site.PYTHONSERVER + "reset-data-analytic");
				httpPost.setEntity(multipartEntity);
				
				HttpResponse response = httpClient.execute(httpPost);
				HttpEntity responseEntity = response.getEntity();
				String result = (responseEntity == null) ? "" : EntityUtils.toString(responseEntity, Charset.defaultCharset());
				
				int status = response.getStatusLine().getStatusCode();
				if (status < 200 || status >= 300) {
					throw new BotException(+ response.getStatusLine().getStatusCode()
							+ " : " + result);
				}
				
				Stats.stats.analyticBinaryUpload++;
			}
			else {
				throw new IllegalArgumentException("Not a BOTlibre analytic or a custom Tensorflow network");
			}
		} catch (Exception failed) {
			error(failed);
		}
	}

	public void deleteBinaryNetwork(HttpServletRequest request) {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			setInstance(AdminDatabase.instance().deleteAnalyticBinary(getInstance()));
		} catch (Exception exception) {
			error(exception);
		}
	}

	/**
	 * Upload text file that contains labels.
	 */
	public void saveLabelsNetwork(InputStream data, String name, String type) {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			String labels = Utils.loadTextFile(data, "", Site.MAX_UPLOAD_SIZE);
			if (labels == null) {
				throw new BotException("Invalid labels file");
			}
			// Cannot sanitize labels as has quotes and other characters.
			// Must sanitize when displayed.
			setInstance(AdminDatabase.instance().updateAnalyticLabelsNetwork(this.instance, labels, Utils.sanitize(name)));
		} catch (Exception failed) {
			error(failed);
		}
	}
	
	/**
	 * Upload text file that contains labels.
	 */
	public void saveLabelsProperties(InputStream data, String name, String type) {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			String properties = Utils.loadTextFile(data, "", Site.MAX_UPLOAD_SIZE);
			if (properties == null) {
				throw new BotException("Invalid properties file");
			}
			// Cannot sanitize labels as has quotes and other characters.
			// Must sanitize when displayed.
			setInstance(AdminDatabase.instance().updateAnalyticLabelsProperties(this.instance, properties, Utils.sanitize(name)));
		} catch (Exception failed) {
			error(failed);
		}
	}

	/**
	 * Only saves the file from the text area after editing. It takes the current labels and upload them again.
	 * @param labels
	 */
	public void saveLabelsNetwork(String labels) {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			// Cannot sanitize labels as has quotes and other characters.
			// Must sanitize when displayed.
			setInstance(AdminDatabase.instance().updateAnalyticLabelsNetwork(this.instance, labels,
					instance.getLabelsFileName()));
		} catch (Exception failed) {
		}

	}
	
	/**
	 * Only saves the file from the text area after editing. It takes the current properties and upload them again.
	 * @param labels
	 */
	public void saveLabelsProperties(String propertiesLabels) {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			// Cannot sanitize labels as has quotes and other characters.
			// Must sanitize when displayed.
			setInstance(AdminDatabase.instance().updateAnalyticLabelsProperties(this.instance, propertiesLabels,
					instance.getPropertiesFileName()));
		} catch (Exception failed) {
		}

	}

	/**
	 * Update Analytic settings.
	 */
	public void updateAnalyticSettings(String libraryType, String inputsString, String outputsString, String intermediatesString, String layersString, String activationFunc, String model, String imageSize, String feed, String fetch,
			String audioInputName, String audioOutputName, String customInputsString, String customOutputsString, String customIntermediatesString, String customLayersString, String customActivationFunc) {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			libraryType = Utils.sanitize(libraryType);
			inputsString = Utils.sanitize(inputsString);
			outputsString = Utils.sanitize(outputsString);
			intermediatesString = Utils.sanitize(intermediatesString);
			layersString = Utils.sanitize(layersString);
			activationFunc = Utils.sanitize(activationFunc);
			model = Utils.sanitize(model); // model is also known as analyticType
			imageSize = Utils.sanitize(imageSize);
			feed = Utils.sanitize(feed);
			fetch = Utils.sanitize(fetch);
			audioInputName = Utils.sanitize(audioInputName);
			audioOutputName = Utils.sanitize(audioOutputName);
			customInputsString = Utils.sanitize(customInputsString);
			customOutputsString = Utils.sanitize(customOutputsString);
			customIntermediatesString = Utils.sanitize(customIntermediatesString);
			customLayersString = Utils.sanitize(customLayersString);
			customActivationFunc = Utils.sanitize(customActivationFunc);
			
			int inputs = 0;
			int outputs = 0;
			int intermediates = 0;
			int layers = 0;
			if (Analytic.BotLibre.contentEquals(libraryType)) {
				// use settings from Botlibre network type
				try {
					inputs = Integer.parseInt(inputsString);
				} catch (Exception Exception) {
					throw new BotException("Invalid Inputs");
				}
				try {
					outputs = Integer.parseInt(outputsString);
				} catch (Exception Exception) {
					throw new BotException("Invalid outputs");
				}
				try {
					layers = Integer.parseInt(layersString);
				} catch (Exception Exception) {
					throw new BotException("Invalid Layers");
				}
				try {
					intermediates = Integer.parseInt(intermediatesString);
				} catch (Exception Exception) {
					throw new BotException("Invalid intermediates");
				}
			}
			else {
				// use settings from custom Tensorflow network type
				activationFunc = customActivationFunc;
				try {
					inputs = Integer.parseInt(customInputsString);
				} catch (Exception Exception) {
					throw new BotException("Invalid Inputs");
				}
				try {
					outputs = Integer.parseInt(customOutputsString);
				} catch (Exception Exception) {
					throw new BotException("Invalid outputs");
				}
				try {
					layers = Integer.parseInt(customLayersString);
				} catch (Exception Exception) {
					throw new BotException("Invalid Layers");
				}
				try {
					intermediates = Integer.parseInt(customIntermediatesString);
				} catch (Exception Exception) {
					throw new BotException("Invalid intermediates");
				}
			}
			if (Analytic.BotLibre.equals(libraryType) || (Analytic.Tensorflow.equals(libraryType) && CUSTOM.equals(model))) {
				if ((inputs <= 0) || (inputs > 1000)) {
					throw new BotException("Input nodes must be between 1-1000");
				}
				if ((outputs <= 0) || (outputs > 1000)) {
					throw new BotException("Output nodes must be between 1-1000");
				}
				if ((intermediates <= 0) || (intermediates > 1000)) {
					throw new BotException("Intermediate nodes must be between 1-1000");
				}
				if ((layers <= 0) || (layers > 10)) {
					throw new BotException("Layers must be between 1-10");
				}
				if (inputs * intermediates * layers > 1000000 ) {
					throw new BotException("Total nodes can not exceed 1000000");
				}
			}
			setInstance(AdminDatabase.instance().updateAnalyticSettings(this.instance, libraryType, inputs, outputs, intermediates, layers, activationFunc, model, imageSize, feed, fetch, audioInputName, audioOutputName));
		} catch (Exception failed) {
			this.loginBean.error(failed);
		}
	}
	
	/***
	 * set Audio Recognition Settings
	 */
	public void setAudioSettings(String audioInputName, String audioOutputName) {
		try {
			checkLogin();
			checkInstance();
			checkAdmin();
			setInstance(AdminDatabase.instance().setAudioSettings(audioInputName, audioOutputName, this.instance));
		} catch (Exception failed){}
	}
	
	public String getAudioInputName() {
		return getInstance().getAudioInputName();
	}
	
	public String getAudioOutputName() {
		return getInstance().getAudioOutputName();
	}

	/**
	 * Copy instance.
	 */
	public void copyInstance() {
		try {
			checkLogin();
			checkInstance();
			Analytic parent = getInstance();
			Analytic newInstance = new Analytic(parent.getName());
			newInstance.setDescription(parent.getDescription());
			newInstance.setDetails(parent.getDetails());
			newInstance.setDisclaimer(parent.getDisclaimer());
			newInstance.setTagsString(parent.getTagsString());
			newInstance.setCategoriesString(parent.getCategoriesString());
			newInstance.setLicense(parent.getLicense());
			newInstance.setContentRating(parent.getContentRating());
			newInstance.setAnalyticType(parent.getAnalyticType());
			setInstance(newInstance);
			setForking(true);
		} catch (Exception failed) {
			error(failed);
		}
	}
	
	@Override
	public void writeMenuPostExtraHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		if (this.instance == null || embed || this.instance.isExternal() || !isAdmin()) {
			return;
		}
		try {
			out.write("<tr class='menuitem'>\n");
			out.write("<td><a class='menuitem' href='analytic?copy=true");
			out.write(proxy.proxyString());
			out.write(instanceString());
			out.write("' class='button' title='Create a new analytic with the same details'><img src='images/copy.svg' class='menu'/> ");
			out.write(this.loginBean.translate("Copy Details"));
			out.write("</a></td>\n");
			out.write("</tr>\n");
			
//			out.write("<tr class='menuitem'>\n");
//			out.write("<td><a class='menuitem' href='analytic?export=true");
//			out.write(proxy.proxyString());
//			out.write(instanceString());
//			out.write("' class='button' title='Export and download the graphic and its metadata'><img src='images/download.svg' class='menu'/> ");
//			out.write(this.loginBean.translate("Export"));
//			out.write("</a></td>\n");
//			out.write("</tr>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}

	@Override
	public void writeInfoTabExtraHTML(SessionProxyBean proxy, boolean embed, Writer out) {
		try {
			out.write("<span>Type: ");
			out.write(getDisplayInstance().getAnalyticType());
			out.write("</span><br/>\n");
		} catch (Exception exception) {
			error(exception);
		}
	}

	public String getTypeCheckedString(String language) {
		if (language != null && language.equals(this.typeFilter)) {
			return "selected=\"selected\"";
		}
		return "";
	}

	public List<Analytic> getAllInstances(Domain domain) {
		try {
			List<Analytic> results = AdminDatabase.instance().getAllAnalytics(this.page, this.pageSize, this.typeFilter,
					this.categoryFilter, this.nameFilter, this.userFilter, this.instanceFilter, this.instanceRestrict,
					this.instanceSort, this.loginBean.contentRating, this.tagFilter, this.startFilter, this.endFilter, getUser(), domain, false);
			if ((this.resultsSize == 0) || (this.page == 0)) {
				if (results.size() < this.pageSize) {
					this.resultsSize = results.size();
				} else {
					this.resultsSize = AdminDatabase.instance().getAllAnalyticsCount(this.typeFilter,
							this.categoryFilter, this.nameFilter, this.userFilter, this.instanceFilter,
							this.instanceRestrict, this.instanceSort, this.loginBean.contentRating, this.tagFilter, this.startFilter, this.endFilter,
							getUser(), domain, false);
				}
			}
			return results;
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Analytic>();
		}
	}

	public List<Analytic> getAllFeaturedInstances() {
		try {
			return AdminDatabase.instance().getAllAnalytics(
					0, 100, "", "", "", "", InstanceFilter.Featured, InstanceRestrict.None, InstanceSort.MonthlyConnects, this.loginBean.contentRating, "", "", "", null, getDomain(), false);
		} catch (Exception failed) {
			error(failed);
			return new ArrayList<Analytic>();
		}
	}

	@Override
	public String getAvatarThumb(AvatarImage avatar) {
		String file = super.getAvatarThumb(avatar);
		if (file.equals("images/bot-thumb.jpg")) {
			return "images/analytic-thumb.jpg";
		}
		return file;
	}

	@Override
	public String getAvatarImage(AvatarImage avatar) {
		String file = super.getAvatarImage(avatar);
		if (file.equals("images/bot.png")) {
			return "images/analytic.png";
		}
		return file;
	}

	public boolean createInstance(AnalyticConfig config) {
		try {
			checkLogin();
			config.sanitize();
			Analytic newInstance = new Analytic(config.name);
			newInstance.setAnalyticType(MOBNET50);
			newInstance.setImageSize("224");
			newInstance.setFeed(INPUT);
			newInstance.setFetch(FINAL_RESULT);
			setInstance(newInstance);
			updateFromConfig(newInstance, config);
			newInstance.setDomain(getDomain());
			checkVerfied(config);
			setSubdomain(config.subdomain, newInstance);
			//AdminDatabase.instance().validateNewAnalytic(newInstance.getAlias(), config.description, config.tags, Site.ADULT, getDomain());
			setInstance(AdminDatabase.instance().createAnalytic(newInstance, getUser(), config.categories, config.tags, this.loginBean));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	/**
	 * Create a directory link.
	 */
	public boolean createLink(AnalyticConfig config) {
		try {
			checkLogin();
			config.sanitize();
			Analytic newInstance = new Analytic(config.name);
			newInstance.setDomain(getDomain());
			newInstance.setAnalyticType(config.analyticType);
			newInstance.setDescription(config.description);
			newInstance.setDetails(config.details);
			newInstance.setDisclaimer(config.disclaimer);
			newInstance.setWebsite(config.website);
			newInstance.setAdult(Site.ADULT);
			newInstance.setExternal(true);
			newInstance.setPaphus(config.website.contains("paphuslivechat") || config.website.contains("botlibre.biz"));
			newInstance.setTagsString(config.tags);
			newInstance.setCategoriesString(config.categories);
			setInstance(newInstance);
			checkVerfied(config);
			if (config.name.equals("")) {
				throw new BotException("Invalid name");
			}
			if (!config.website.contains("http")) {
				throw new BotException("You must enter a valid URL for an external analytic");
			}
			//AdminDatabase.instance().validateNewAnalytic(newInstance.getAlias(), config.description, config.tags, config.isAdult, getDomain());			
			setInstance(AdminDatabase.instance().createAnalytic(newInstance, getUser(), config.categories, config.tags, this.loginBean));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public boolean updateAnalytic(AnalyticConfig config, String newdomain, Boolean featured, Boolean adVerified) {
		try {
			checkLogin();
			checkInstance();
			checkAdminOrSuper();
			config.sanitize();
			Analytic newInstance = (Analytic)this.instance.clone();
			this.editInstance = newInstance;
			updateFromConfig(newInstance, config);
			if (config.creator != null && isSuperUser()) {
				User user = AdminDatabase.instance().validateUser(config.creator);
				newInstance.setCreator(user);
			}
			if (newdomain != null && !newdomain.equals(this.instance.getDomain().getAlias())) {
				Domain domain = AdminDatabase.instance().validateDomain(newdomain);
				newInstance.setDomain(domain);
			}
			if (newInstance.getAdCode() == null || (config.adCode != null && !newInstance.getAdCode().equals(config.adCode))) {
				newInstance.setAdCodeVerified(false);
			}
			if (adVerified != null && isSuper()) {
				newInstance.setAdCodeVerified(adVerified);
			}
			if (isSuper() && featured != null) {
				newInstance.setFeatured(featured);
			}
			setSubdomain(config.subdomain, newInstance);
			setInstance(AdminDatabase.instance().updateAnalytic(newInstance, config.categories, config.tags));
		} catch (Exception failed) {
			error(failed);
			return false;
		}
		return true;
	}

	public void resetSearch() {
		super.resetSearch();
		this.typeFilter = "";
	}

	public String getTypeFiler() {
		return typeFilter;
	}

	public void setTypeFiler(String typeFiler) {
		this.typeFilter = typeFiler;
	}

	@Override
	public Class<Analytic> getType() {
		return Analytic.class;
	}

	@Override
	public String getTypeName() {
		return "Analytic";
	}

	public String isTypeSelected(String type) {
		if (getInstance() == null) {
			return "";
		}
		if (getInstance().getAnalyticType().equals(type)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}
	
	public String isLibraryTypeSelected(String type) {
		if (getInstance() == null) {
			return "";
		}
		if (getInstance().getLibraryType().equals(type)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}
	
	public String isActivationFuncSelected(String type) {
		if (getInstance() == null || getInstance().getActivationFunction() == null) {
			return "";
		}
		if (getInstance().getActivationFunction().equals(type)) {
			return "selected=\"selected\"";
		} else {
			return "";
		}
	}
	
	public String isLabelSelected(String label) {
		if (getInstance() == null) {
			return "";
		}else if(getSelectedLabel() == null || getSelectedLabel().isEmpty()){
			return "";
		}else if(getSelectedLabel().equals(label)){
			return "selected=\"selected\"";
		}else{
			return  "";
		}
	}
	
	public String isLabelTestMediaSelected(String label) {
		if (getInstance() == null) {
			return "";
		}else if(getSelectedTestMediaLabel() == null || getSelectedTestMediaLabel().isEmpty()){
			return "";
		}else if(getSelectedTestMediaLabel().equals(label)){
			return "selected=\"selected\"";
		}else{
			return  "";
		}
	}
	
	public void setEmptyLabel(){
		setSelectedLabel("");
	}
	
	@Override
	public String getCreateURL() {
		return "create-analytic.jsp";
	}

	@Override
	public String getSearchURL() {
		return "analytic-search.jsp";
	}

	@Override
	public String getBrowseURL() {
		return "browse-analytic.jsp";
	}
	
	public String toXML(Analytic analytic) {
		StringWriter writer = new StringWriter();
		if (getAnalyticType().equals(OBJECT_DETECTION)) {
			writer.write("<analytic-object-test");
			writer.write(" instance=\"" + analytic.getId() + "\"");
		} else {
			writer.write("<analytic");
			writer.write(" analyticId=\"" + analytic.getId() + "\"");
		}
		writer.write("/>");
		return writer.toString();
	}
	
	public String testDataAnalytic(AnalyticConfig config, Analytic analytic) {
		checkInstance();
		try {
			if (Analytic.BotLibre.equals(analytic.libraryType)) {
				Stats.stats.analyticTest++;
				String[] inputs = StringUtils.strip(config.input, " ,").split(",");
				double[] values = new double[inputs.length];
				for (int count = 0; count < inputs.length; count++ ) {
					values[count] = Double.parseDouble(inputs[count]);
				}
				Media media = AdminDatabase.instance().findMedia(analytic.getBinaryNetwork().getMediaId());
				ByteArrayInputStream bytes = new ByteArrayInputStream(media.getMedia());
				ObjectInputStream  stream = new ObjectInputStream(bytes);
				NeuralNetwork network = (NeuralNetwork)stream.readObject();
				double[] inputvalues = network.getInputs();
				if (inputvalues.length != values.length) {
					throw new IllegalArgumentException("Length of given inputs does not match current network.");
				}
				for (int count = 0; count < Math.min(inputvalues.length, values.length); count++ ) {
					inputvalues[count] = values[count];
				}
				network.forwardPropagate();
				double[] outputvalues = network.getOutputs();
				StringWriter outputWriter = new StringWriter();
				outputWriter.write("<data-analytic-result><output>");
				for (int count = 0; count < (outputvalues.length); count++ ) {
					// Truncate floats to 3 decimal places.
					outputWriter.write(String.format("%.03f", outputvalues[count]));
					if (count  == outputvalues.length - 1){
						break;
					}
					outputWriter.write(",");
				}
				outputWriter.write("</output></data-analytic-result>");
				return outputWriter.toString();
			}
			else if (Analytic.Tensorflow.equals(analytic.libraryType) && CUSTOM.equals(analytic.analyticType)) {
				Stats.stats.analyticTest++;
				
				StringWriter xmlWriter = new StringWriter();
				xmlWriter.write("<analytic analyticId=\"");
				xmlWriter.write(analytic.getId().toString());
				xmlWriter.write("\">");

				if (!config.input.matches("[\\d\\.\\,\\s\\-]+")) {
					throw new IllegalArgumentException("Input contains illegal characters.");
				}
				
				xmlWriter.write("<input>");
				xmlWriter.write(config.input);
				xmlWriter.write("</input>");
				
				xmlWriter.write("</analytic>");
				
				MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
				multipartEntity.addPart("xml", new StringBody(xmlWriter.toString(), "text/xml", Charset.defaultCharset()));
				
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(Site.PYTHONSERVER + "test-data-analytic");
				httpPost.setEntity(multipartEntity);
				
				HttpResponse response = httpClient.execute(httpPost);
				HttpEntity responseEntity = response.getEntity();
				String result = (responseEntity == null) ? "" : EntityUtils.toString(responseEntity, Charset.defaultCharset());
				
				int status = response.getStatusLine().getStatusCode();
				if (status < 200 || status >= 300) {
					throw new BotException(+ response.getStatusLine().getStatusCode()
							+ " : " + result);
				}
				
				return result;
			}
			else {
				throw new IllegalArgumentException("Not a custom network");
			}
		} catch (Exception exception) {
			AdminDatabase.instance().log(exception);
			throw new BotException("Invalid Network");
		}
	}
	
	public String trainDataAnalytic(AnalyticTrainingData config, Analytic analytic) {
		checkLogin();
		checkAdmin();
		checkInstance();
		if (config.data == null) {
			throw new BotException("Missing data");
		}
		try {
			if (Analytic.BotLibre.equals(analytic.libraryType)) {
				Media media = AdminDatabase.instance().findMedia(analytic.getBinaryNetwork().getMediaId());
				ByteArrayInputStream inBytes = new ByteArrayInputStream(media.getMedia());
				ObjectInputStream  inStream = new ObjectInputStream(inBytes);
				NeuralNetwork network = (NeuralNetwork)inStream.readObject();
				for (AnalyticData data : config.data) {
					/*Media media = AdminDatabase.instance().findMedia(analytic.getBinaryNetwork().getMediaId());
					ByteArrayInputStream inBytes = new ByteArrayInputStream(media.getMedia());
					ObjectInputStream  inStream = new ObjectInputStream(inBytes);
					NeuralNetwork network = (NeuralNetwork)inStream.readObject();*/
					String[] inputs = StringUtils.strip(data.input, " ,").split(",");
					double[] values = new double[inputs.length];
					for (int count = 0; count < inputs.length; count++ ) {
						values[count] = Double.parseDouble(inputs[count]);
					}
					double[] inputvalues = network.getInputs();
					if (inputvalues.length != values.length) {
						throw new IllegalArgumentException("Length of given inputs does not match current network.");
					}
					for (int count = 0; count < Math.min(inputvalues.length, values.length); count++ ) {
						inputvalues[count] = values[count];
					}
					network.forwardPropagate();
					double[] outputvalues = network.getOutputs();
					String[] outputs = StringUtils.strip(data.output, " ,").split(",");
					double[] expectedOutputs = new double[outputs.length];
					for (int count = 0; count < outputs.length; count++ ) {
						expectedOutputs[count] = Double.parseDouble(outputs[count]);
					}
					
					if (outputvalues.length != expectedOutputs.length) {
						throw new IllegalArgumentException("Length of given outputs does not match current network");
					}
					
					double best = -1.0;
					int bestPosition = -1;
					for (int position = 0; position < outputvalues.length; position++) {
						double value = outputvalues[position];
						if (value > best) {
							best = value;
							bestPosition = position;
						}
					}
					double expectedBest = -1.0;
					int expectedBestPosition = -1;
					for (int position = 0; position < expectedOutputs.length; position++) {
						double value = expectedOutputs[position];
						if (value > expectedBest) {
							expectedBest = value;
							expectedBestPosition = position;
						}
					}
					
					// Only learn if the move was different than the network would have made.
					// For some reason this improves learning.
					if (expectedBestPosition != bestPosition) {
						network.backPropagate(expectedOutputs);
					}
					
					
					Stats.stats.analyticTraining++;
					
					/*ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
					ObjectOutputStream  outStream = new ObjectOutputStream(outBytes);
					outStream.writeObject(network);
					outStream.close();
	
					Media outMedia = new Media();
					outMedia.setMedia(outBytes.toByteArray());
					MediaFile mediaFile = new MediaFile();
					mediaFile.setName("network.ser");
					mediaFile.setType("application/octet-stream");
					setInstance(AdminDatabase.instance().updateAnalyticBinaryNetwork(mediaFile, outMedia, this.instance));*/
					
				}
				ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
				ObjectOutputStream  outStream = new ObjectOutputStream(outBytes);
				outStream.writeObject(network);
				outStream.close();
	
				Media outMedia = new Media();
				outMedia.setMedia(outBytes.toByteArray());
				MediaFile mediaFile = new MediaFile();
				mediaFile.setName("network.ser");
				mediaFile.setType("application/octet-stream");
				setInstance(AdminDatabase.instance().updateAnalyticBinaryNetwork(mediaFile, outMedia, this.instance));
				
				return "Success";
			}
			else if (Analytic.Tensorflow.equals(analytic.libraryType) && CUSTOM.equals(analytic.analyticType)) {
				StringWriter xmlWriter = new StringWriter();
				
				xmlWriter.write("<analytic-training-data analyticId=\"");
				xmlWriter.write(analytic.getId().toString());
				xmlWriter.write("\">");

				for (AnalyticData data : config.data) {
					if (!data.input.matches("[\\d\\.\\,\\s\\-]+") || !data.output.matches("[\\d\\.\\,\\s\\-]+")) {
						throw new IllegalArgumentException("Input or output contains illegal characters.");
					}
					
					xmlWriter.write("<data><input>");
					xmlWriter.write(data.input);
					xmlWriter.write("</input><output>");
					xmlWriter.write(data.output);
					xmlWriter.write("</output></data>");
				}
				
				xmlWriter.write("</analytic-training-data>");
				
				MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
				multipartEntity.addPart("xml", new StringBody(xmlWriter.toString(), "text/xml", Charset.defaultCharset()));
				
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(Site.PYTHONSERVER + "train-data-analytic");
				httpPost.setEntity(multipartEntity);
				
				HttpResponse response = httpClient.execute(httpPost);
				HttpEntity responseEntity = response.getEntity();
				String result = (responseEntity == null) ? "" : EntityUtils.toString(responseEntity, Charset.defaultCharset());
				
				int status = response.getStatusLine().getStatusCode();
				if (status < 200 || status >= 300) {
					throw new BotException(+ response.getStatusLine().getStatusCode()
							+ " : " + result);
				}
				
				Stats.stats.analyticTraining += config.data.size();
				return "Success";
			}
			else {
				throw new IllegalArgumentException("Not a custom network");
			}
		} catch (Exception exception) {
			AdminDatabase.instance().log(exception);
			throw new BotException("Invalid Network");
		}
	}

//
//	public AnalyticResponse testAnalytic_Java(byte[] image, Analytic analytic) {
//		AnalyticResponse analyticResponse = new AnalyticResponse();
//		Media binaryData = AdminDatabase.instance().findMedia(analytic.getBinaryNetwork().getMediaId());
//		analyticResponse = predcitImage(image, analyticResponse, binaryData.getMedia());
//		return analyticResponse;
//	}

	/**
	 * predict analytic image. 
	 * @param image
	 * @param analytic
	 * @return result of label and confidence.
	 * @throws Exception
	 */
	public String testAnalytic(byte[] image, Analytic analytic) throws Exception {
		if (analytic.getBinaryNetwork() == null) {
			throw new BotException("Missing network");
		}
		apiConnect();
		
		String result = "";
		ByteArrayBody fileBody = new ByteArrayBody(image, "image.png");
		
		MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		multipartEntity.addPart("file", fileBody);
		multipartEntity.addPart("xml", new StringBody(this.toXML(analytic),"text/xml",Charset.defaultCharset()));
	  

		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = null;
		
		HttpPost httppost = new HttpPost(Site.PYTHONSERVER + "test-analytic");
		
		httppost.setEntity(multipartEntity);
		response = httpclient.execute(httppost);
		
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			result = EntityUtils.toString(entity, HTTP.UTF_8);
		}

		if ((response.getStatusLine().getStatusCode() != 200) && (response.getStatusLine().getStatusCode() != 204)) {
			Exception exception = new BotException(""
					+ response.getStatusLine().getStatusCode()
					+ " : " + result);
 			throw exception;
		}
		Stats.stats.analyticTest++;
		return result;
	}
	
	/**
	 * predict analytic image. 
	 * @param image
	 * @param analytic
	 * @return result of label and confidence.
	 * @throws Exception
	 */
	public String testAudioAnalytic(byte[] audio, Analytic analytic) throws Exception {
		if (analytic.getBinaryNetwork() == null) {
			throw new BotException("Missing network");
		}
		
		String result = "";
		ByteArrayBody fileBody = new ByteArrayBody(audio, "audio.wav");
		
		MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		multipartEntity.addPart("file", fileBody);
		multipartEntity.addPart("xml", new StringBody(this.toXML(analytic),"text/xml",Charset.defaultCharset()));
	  

		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = null;
		
		HttpPost httppost = new HttpPost(Site.PYTHONSERVER + "test-audio-analytic");
		
		httppost.setEntity(multipartEntity);
		response = httpclient.execute(httppost);
		
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			result = EntityUtils.toString(entity, HTTP.UTF_8);
		}

		if ((response.getStatusLine().getStatusCode() != 200) && (response.getStatusLine().getStatusCode() != 204)) {
			Exception exception = new BotException(""
					+ response.getStatusLine().getStatusCode()
					+ " : " + result);
 			throw exception;
		}
		Stats.stats.analyticTest++;
		return result;
	}
	
	/**
	 * predict analytic image. 
	 * @param image
	 * @param analytic
	 * @return result of label and confidence.
	 * @throws Exception
	 */
	public String testObjectDetectionAnalytic(byte[] image, Analytic analytic) throws Exception {
		if (analytic.getBinaryNetwork() == null) {
			throw new BotException("Missing network");
		}
		
		String result = "";
		ByteArrayBody fileBody = new ByteArrayBody(image, "image.png");
		
		MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		multipartEntity.addPart("file", fileBody);
		multipartEntity.addPart("xml", new StringBody(this.toXML(analytic),"text/xml",Charset.defaultCharset()));
	  

		HttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = null;
		
	
		HttpPost httppost = new HttpPost(Site.PYTHONSERVER + "test-analytic-detection");
		
		
		httppost.setEntity(multipartEntity);
		response = httpclient.execute(httppost);
		
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			result = EntityUtils.toString(entity, HTTP.UTF_8);
		}

		if ((response.getStatusLine().getStatusCode() != 200) && (response.getStatusLine().getStatusCode() != 204)) {
			Exception exception = new BotException(""
					+ response.getStatusLine().getStatusCode()
					+ " : " + result);
 			throw exception;
		}
		Stats.stats.analyticTest++;
		return result;
	}
	
	public void refreshEntity(){
		if (getInstance() == null) {
			return;
		}
		checkLogin();
		checkInstance();
		checkAdmin();
		setInstance(AdminDatabase.instance().refreshAnalytic(getInstance()));
		setInstance(AdminDatabase.instance().refreshTestMediaAnalytic(getInstance()));
	}
	
	private void trainingImagesCheck(){
		if(getInstance().getLabels().size() <= 1){
			throw new BotException("A minimum of 2 labels is required");
		}
		for (AnalyticLabel label : getInstance().getLabels()) {
			int count = 0;
			for (AnalyticMedia media : getInstance().getMedia()) {
				if(label.getLabel().equals(media.getLabel())) {
					count = count +1;
				}
			}
			if (count < 19) {
				throw new BotException("A minimum of 20 images is required per label");
			} else {
				count = 0;
			}
		}
	}
	
	/**
	 * Data Test Media.
	 * @param analytic
	 * @throws Exception
	 */
	public String reportMediaAnalytic(String status) throws Exception {
		checkLogin();
		checkInstance();
		checkAdmin();
		if (!Site.COMMERCIAL && getAnalyticType().equals(INCEP_V3)) {
			if (getUser().isBasic() || getUser().isBronze()) {
				throw new BotException("You must upgrade your account in order to use inception_v3");
			}
		}
		if (status.equals("force")) {
			if (!isSuper()){
				throw new BotException("You must be a super user to be admin");
			}
		} else {
			if (getInstance().isProcessingTestMedia()){
				throw new BotException("The server is currently processing test media.");
			}
			if (AnalyticBean.processingTestMediaInSession){
				Stats.stats.analyticTestMediaBusy++;
				throw new BotException("Test Media is busy");
			}
		}
		setInstance(AdminDatabase.instance().updateAnalyticTestMediaStatus(this.instance, "started", true));
		AnalyticBean.processingTestMediaInSession = true;
		Stats.stats.analyticTestMediaProcessing++;
		String result = "";
		HttpResponse response = null;
		try {
			MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			multipartEntity.addPart("xml", new StringBody(this.toXML(getInstance()),"text/xml",Charset.defaultCharset()));
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = null;
			if (getAnalyticType().equals(OBJECT_DETECTION)) {
				httppost = new HttpPost(Site.PYTHONSERVER + "report-media-analytic_object_detection");
			} else {
				httppost = new HttpPost(Site.PYTHONSERVER + "report-media-analytic");
			}
			httppost.setEntity(multipartEntity);
			
			response = httpclient.execute(httppost);
			
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				result = EntityUtils.toString(entity, HTTP.UTF_8);
			}
			if ((response.getStatusLine().getStatusCode() != 200) && (response.getStatusLine().getStatusCode() != 204)) {
				Exception exception = new Exception(""
						+ response.getStatusLine().getStatusCode()
						+ " : " + result);
				throw exception;
			}
		} catch (SocketException exception) {
			throw new BotException(exception.getMessage());
		} finally {
			AnalyticBean.processingTestMediaInSession = false;
			setInstance(AdminDatabase.instance().updateAnalyticTestMediaStatus(this.instance,"ready", false));
		}
		return result;
	}
	
	
	
	/**
	 * Training the set of images saved from the database, sent back to python web server.
	 * @param analytic
	 * @throws Exception
	 */
	public String trainAnalytic(String status) throws Exception {
		checkLogin();
		checkInstance();
		checkAdmin();
		if (getAnalyticType().equals(INCEP_V3)) {
			if (getUser().isBasic() || getUser().isBronze()) {
				throw new BotException("You must upgrade your account in order to use inception_v3");
			}
		}
		trainingImagesCheck();
		if (getInstance().getBinaryNetwork() == null) {
			Media media = new Media();
			MediaFile mediaFile = new MediaFile();
			setInstance(AdminDatabase.instance().updateAnalyticBinaryNetwork(mediaFile, media, this.instance));
		}
		
		if (status.equals("force")) {
			if (!isSuper()){
				throw new BotException("You must be a super user to be admin");
			}
		} else {
			if (getInstance().isTraining()){
				throw new BotException("The server is currently training the selected Analytic");
			}
			if (AnalyticBean.trainingInSession){
				Stats.stats.analyticTrainingBusy++;
				throw new BotException("Training is busy");
			}
		}
		setInstance(AdminDatabase.instance().updateAnalyticTrainingStatus(this.instance, "started", true));
		AnalyticBean.trainingInSession = true;
		Stats.stats.analyticTraining++;
		String result = "";
		HttpResponse response = null;
		try {
			if(getAnalyticType().contains("conv") || getAnalyticType().contains("low")){ //audio training.
				result = trainAnalyticAudio(response);
			}else{
				result = trainAnalyticImage(response);
			}
		} catch (SocketException exception) {
			throw new BotException(exception.getMessage());
		} finally {
			AnalyticBean.trainingInSession = false;
			setInstance(AdminDatabase.instance().updateAnalyticTrainingStatus(this.instance,"ready", false));
		}
		return result;
	}
	
	public String trainAnalyticImage(HttpResponse response) throws Exception{
		String result = "";
		MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		multipartEntity.addPart("xml", new StringBody(this.toXML(getInstance()),"text/xml",Charset.defaultCharset()));

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(Site.PYTHONSERVER+"training-analytic");
		httppost.setEntity(multipartEntity);
		
		response = httpclient.execute(httppost);
		
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			result = EntityUtils.toString(entity, HTTP.UTF_8);
		}
		if ((response.getStatusLine().getStatusCode() != 200) && (response.getStatusLine().getStatusCode() != 204)) {
			Exception exception = new Exception(""
					+ response.getStatusLine().getStatusCode()
					+ " : " + result);
			throw exception;
		}
		return result;
	}
	
	public String trainAnalyticAudio(HttpResponse response) throws Exception {
		String result = "";
		MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
		multipartEntity.addPart("xml", new StringBody(this.toXML(getInstance()),"text/xml",Charset.defaultCharset()));

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(Site.PYTHONSERVER+"training-audio");
		httppost.setEntity(multipartEntity);
		
		response = httpclient.execute(httppost);
		
		HttpEntity entity = response.getEntity();
		if (entity != null) {
			result = EntityUtils.toString(entity, HTTP.UTF_8);
		}
		if ((response.getStatusLine().getStatusCode() != 200) && (response.getStatusLine().getStatusCode() != 204)) {
			Exception exception = new Exception(""
					+ response.getStatusLine().getStatusCode()
					+ " : " + result);
			throw exception;
		}
		return result;
	}
	
	public String getFeed() {
		return getInstance().getFeed();
	}

	public String getFetch() {
		return getInstance().getFetch();
	}

	public int getImageSize() {
		return getInstance().getImageSize();
	}
	
	public int getInputs() {
		return getInstance().getInputs();
	}

	/**
	 * Process of predicting the image. Info:
	 * constructAndExecuteGraphToNormalizeImage(imageBytes) for using mobilenet
	 * - image size 224 Tensor.create(imageBytes) for using inception v3 /
	 * inception h5.
	 * 
	 * @param imageBytes
	 * @param AnalyticConfig
	 * @param graph
	 * @return
	 */
//	public AnalyticResponse predcitImage(byte[] imageBytes, AnalyticResponse response, byte[] graph) {
//		List<String> labels = Arrays.asList(instance.getLabelsNetwork().split("\\n"));
//		if (getAnalyticType().equals(getInstance().MOBNET50)) {
//			try (Tensor image = constructAndExecuteGraphToNormalizeImage(imageBytes)) {
//				float[] labelProbabilities = executeInceptionGraph(graph, image);
//				int bestLabelIdx = maxIndex(labelProbabilities);
//				response.label = String.format("%s", labels.get(bestLabelIdx),
//						labelProbabilities[bestLabelIdx] * 100f);
//				response.confidence = (int) (labelProbabilities[bestLabelIdx] * 100f);
//			}
//		} else if (getAnalyticType().equals(getInstance().INCEP_V3)) {
//			try (Tensor image = Tensor.create(imageBytes)) {
//				float[] labelProbabilities = executeInceptionGraph(graph, image);
//				int bestLabelIdx = maxIndex(labelProbabilities);
//				response.label = String.format("%s", labels.get(bestLabelIdx),
//						labelProbabilities[bestLabelIdx] * 100f);
//				response.confidence = (int) (labelProbabilities[bestLabelIdx] * 100f);
//			}
//		}
//
//		return response;
//	}

	/*private float[] executeInceptionGraph(byte[] graphDef, Tensor<Float> image) {
		try (Graph g = new Graph()) {
			g.importGraphDef(graphDef);
			try (Session s = new Session(g);
				Tensor<Float> result =
					  s.runner().feed(getFeed(), image).fetch(getFetch()).run().get(0).expect(Float.class)) {
				final long[] rshape = result.shape();
				if (result.numDimensions() != 2 || rshape[0] != 1) {
					throw new RuntimeException(
						String.format(
								"Expected model to produce a [1 N] shaped tensor where N is the number of labels, instead it produced one with shape %s",
								Arrays.toString(rshape)));
				}
				int nlabels = (int) rshape[1];
				return result.copyTo(new float[1][nlabels])[0];
			}
		}
	}*/

	/**
	 * Execute graph to normalize image. worked for mobilenet050
	 * image size at 224.
	 * @param imageBytes
	 * @return session
	 */
	/*private Tensor constructAndExecuteGraphToNormalizeImage(byte[] imageBytes) { 
		try (Graph g = new Graph()) {
			GraphBuilder b = new GraphBuilder(g);
			// Some constants specific to the pre-trained model at:
			// https://storage.googleapis.com/download.tensorflow.org/models/inception5h.zip
			//
			// - The model was trained with images scaled to 224x224 pixels.
			// - The colors, represented as R, G, B in 1-byte each were
			// converted to
			// float using (value - Mean)/Scale.
			final int H = getImageSize();
			final int W = getImageSize();
			final float mean = 128f;
			final float scale = 128f;

			// Since the graph is being constructed once per execution here, we
			// can use a constant for the
			// input image. If the graph were to be re-used for multiple input
			// images, a placeholder would
			// have been more appropriate.
			final Output input = b.constant(getFeed(), imageBytes);
			final Output output = b
					.div(b.sub(
							b.resizeBilinear(b.expandDims(b.cast(b.decodeJpeg(input, 3), DataType.FLOAT),
									b.constant("make_batch", 0)), b.constant("size", new int[] { H, W })),
							b.constant("mean", mean)), b.constant("scale", scale));
			try (Session s = new Session(g)) {
				return s.runner().fetch(output.op().name()).run().get(0);
			}
		}catch(Exception e){
			throw new BotException("Error: " + e.getMessage());
		}
	}

	private static int maxIndex(float[] probabilities) {
		int best = 0;
		for (int i = 1; i < probabilities.length; ++i) {
			if (probabilities[i] > probabilities[best]) {
				best = i;
			}
		}
		return best;
	}*/

	/**
	 * In the fullness of time, equivalents of the methods of this class should
	 * be auto-generated from the OpDefs linked into libtensorflow_jni.so. That
	 * would match what is done in other languages like Python, C++ and Go.
	 */
	/*static class GraphBuilder {

		GraphBuilder(Graph g) {
			this.g = g;
		}

		Output div(Output x, Output y) {
			return binaryOp("Div", x, y);
		}

		Output sub(Output x, Output y) {
			return binaryOp("Sub", x, y);
		}

		Output resizeBilinear(Output images, Output size) {
			return binaryOp("ResizeBilinear", images, size);
		}

		Output expandDims(Output input, Output dim) {
			return binaryOp("ExpandDims", input, dim);
		}

		Output cast(Output value, DataType dtype) {
			return g.opBuilder("Cast", "Cast").addInput(value).setAttr("DstT", dtype).build().output(0);
		}

		Output decodeJpeg(Output contents, long channels) {
			return g.opBuilder("DecodeJpeg", "DecodeJpeg").addInput(contents).setAttr("channels", channels).build()
					.output(0);
		}

		Output constant(String name, Object value) {
			try (Tensor t = Tensor.create(value)) {
				return g.opBuilder("Const", name).setAttr("dtype", t.dataType()).setAttr("value", t).build().output(0);
			}
		}

		private Output binaryOp(String type, Output in1, Output in2) {
			return g.opBuilder(type, type).addInput(in1).addInput(in2).build().output(0);
		}

		private Graph g;
	}*/

}
