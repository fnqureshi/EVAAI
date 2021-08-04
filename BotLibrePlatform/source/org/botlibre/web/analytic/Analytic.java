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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import org.botlibre.web.admin.MediaFile;
import org.botlibre.web.admin.WebMedium;
import org.botlibre.web.rest.WebMediumConfig;

@Entity
@AssociationOverrides({
	@AssociationOverride(name="admins", joinTable=@JoinTable(name="ANALYTIC_ADMINS")),
	@AssociationOverride(name="users", joinTable=@JoinTable(name="ANALYTIC_USERS")),
	@AssociationOverride(name="tags", joinTable=@JoinTable(name="ANALYTIC_TAGS")),
	@AssociationOverride(name="categories", joinTable=@JoinTable(name="ANALYTIC_CATEGORIES")),
	@AssociationOverride(name="errors", joinTable=@JoinTable(name="ANALYTIC_ERRORS"))
})
public class Analytic extends WebMedium {
	public static final String BotLibre = "BotLibre";
	public static final String Tensorflow = "Tensorflow";
	
	protected String libraryType = "";
	
	protected String labelsFileName = "";
	protected String binaryFileName = "";
	protected String propertiesFileName = "";
	
	// Bot Libre network settings
	protected String analyticType = "";
	protected String activationFunction = "";
	protected int inputs;
	protected int outputs;
	protected int intermediates;
	protected int layers;
	
	// Tensorflow Image Recognition Settings
	protected String analyticFeed = ""; 
	protected String analyticFetch = "";
	protected int imageSize;
	
	protected String processingTestMediaStatus = "ready";
	protected String trainingStatus = "ready";
	protected boolean isTraining = false;
	protected boolean isProcessingTestMedia = false;
	
	// Tensorflow Audio Recognition
	protected String audioInputName;
	protected String audioOutputName;
	
	@Lob
	protected String networkLabels = "";
	@Lob
	protected String propertiesLabels = "";
	@Lob
	protected String testMediaResult = "";
	@OneToOne
	protected MediaFile networkBinary;
	@OneToMany(mappedBy="analytic")
	protected List<AnalyticMedia> media;
	@OneToMany(mappedBy="analytic")
	protected List<AnalyticTestMedia> testMedia;
	@OneToMany
	protected List<AnalyticLabel> label;
	@OneToMany
	protected List<AnalyticTestMediaLabel> testMediaLabel;
	
	public Analytic() {}

	public Analytic(String name) {
		super(name);
	}

	public List<AnalyticMedia> getMedia() {
		if (this.media == null) {
			this.media = new ArrayList<AnalyticMedia>();
		}
		return media;
	}
	
	public List<AnalyticTestMedia> getTestMedia() {
		if (this.testMedia == null) {
			this.testMedia = new ArrayList<AnalyticTestMedia>();
		}
		return testMedia;
	}
	
	public List<AnalyticLabel> getLabels() {
		if (this.label == null) {
			this.label = new ArrayList<AnalyticLabel>();
		}
		return label;
	}
	
	public List<AnalyticTestMediaLabel> getTestMediaLabels() {
		if (this.testMediaLabel == null) {
			this.testMediaLabel = new ArrayList<AnalyticTestMediaLabel>();
		}
		return testMediaLabel;
	}
	
	public WebMediumConfig buildBrowseConfig() {
		AnalyticConfig config = new AnalyticConfig();
		toBrowseConfig(config);
		return config;
	}

	public AnalyticConfig buildConfig() {
		AnalyticConfig config = new AnalyticConfig();
		toConfig(config);
		config.analyticType = this.analyticType;
		config.analyticFeed = this.analyticFeed;
		config.analyticFetch = this.analyticFetch;
		config.audioInputName = this.audioInputName;
		config.audioOutputName = this.audioOutputName;
		config.imageSize = this.imageSize+"";
		config.isTraining = this.isTraining;
		config.trainingStatus = this.trainingStatus;
		config.isProcessingMedia = this.isProcessingTestMedia;
		config.processingTestMediaStatus = this.processingTestMediaStatus;
		return config;
	}
	
	public AnalyticMedia getMedia(long id) {
		for (AnalyticMedia media : this.media) {
			if (media.getMediaId() == id) {
				return media;
			}
		}
		return null;
	}
	
	public AnalyticTestMedia getTestMedia(long id) {
		for (AnalyticTestMedia media : this.testMedia) {
			if (media.getMediaId() == id) {
				return media;
			}
		}
		return null;
	}

	public void setMedia(List<AnalyticMedia> media) {
		this.media = media;
	}
	
	public void setTestMedia(List<AnalyticTestMedia> media) {
		this.testMedia = media;
	}
	
	public String getLibraryType() {
		if (libraryType == null) {
			return "";
		}
		return libraryType;
	}

	public void setLibraryType(String libraryType) {
		this.libraryType = libraryType;
	}
	
	public String getActivationFunction() {
		if (activationFunction == null) {
			return "";
		}
		return activationFunction;
	}

	public void setActivationFunction(String activationFunction) {
		this.activationFunction = activationFunction;
	}
	
	public void setLabel(List<AnalyticLabel> label) {
		this.label = label;
	}
	
	public void setTestMediaLabel(List<AnalyticTestMediaLabel> label) {
		this.testMediaLabel = label;
	}
	
	public void cloneMedia() {
		List<AnalyticMedia> cloneMedia = new ArrayList<AnalyticMedia>();
		for (AnalyticMedia media : this.media){
			cloneMedia.add((AnalyticMedia)media.clone());
		}
		this.media = cloneMedia;
	}
	
	public void setAnalyticType(String analyticType) {
		this.analyticType = analyticType;
	}

	public String getAnalyticType() {
		if (analyticType != null) {
			return analyticType;
		}
		return "None";
	}
	
	public String getTrainingStatus() {
		if (trainingStatus != null) {
			return trainingStatus;
		}
		return "ready";
	}
	
	public void setTrainingStatus(String status, boolean isTraining) {
		this.trainingStatus = status;
		this.isTraining = isTraining;
	}
	
	public void setTestMediaStatus(String status, boolean isProcessing){
		this.processingTestMediaStatus = status;
		this.isProcessingTestMedia = isProcessing;
	}

	public MediaFile getBinaryNetwork() {
		return networkBinary;
	}

	public String getBinaryFileName() {
		if (this.binaryFileName != null) {
			return this.binaryFileName;
		}
		return "";
	}
	
	public String getPropertiesFileName(){
		if(this.propertiesFileName == null || this.propertiesFileName.isEmpty()){
			return "";
		}
		return this.propertiesFileName;
	}
	
	public void setBinaryFileName(String fileName) {
		this.binaryFileName = fileName;
	}

	public void setBinaryNetwork(MediaFile networkBinary) {
		this.networkBinary = networkBinary;
	}

	public String getLabelsNetwork() {
		return networkLabels;
	}
	
	public String getTestMediaResult() {
		if(testMediaResult == null || testMediaResult.isEmpty()){
			return null;
		}
		
		//Text to XML to String
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();  
        DocumentBuilder builder;  
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        String output = "";
        try  
        {  
            builder = factory.newDocumentBuilder();  
            Document doc = builder.parse( new InputSource( new StringReader( testMediaResult ) ) ); 
            transformer = tf.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            output = writer.getBuffer().toString();
        } catch (Exception e) {  
            e.printStackTrace();  
        } 
		
		return output;
	}
	
	public String getPropertiesLabels() {
		if (propertiesLabels == null) {
			return "";
		}
		return propertiesLabels;
	}

	public void setLabelsNetwork(String networkLabels) {
		this.networkLabels = networkLabels;
	}
	
	public void setTestMediaResult(String testMediaResult) {
		this.testMediaResult = testMediaResult;
	}
	
	public void setPropertiesLabels(String propertiesLabels){
		this.propertiesLabels = propertiesLabels;
	}

	public void setLabelsFileName(String fileName) {
		this.labelsFileName = fileName;
	}
	
	public void setPropertiesFileName(String fileName){
		this.propertiesFileName = fileName;
	}

	public String getLabelsFileName() {
		if (this.labelsFileName != null) {
			return this.labelsFileName;
		}
		return "";
	}

	public void setFeed(String feed) {
		this.analyticFeed = feed;
	}

	public void setFetch(String fetch) {
		this.analyticFetch = fetch;
	}

	public void setImageSize(String size) {
		imageSize = Integer.parseInt(size);
	}

	public int getImageSize() {
		return imageSize;
	}

	public void setInputs(int inputs) {
		this.inputs = inputs;
	}

	public int getInputs() {
		return this.inputs;
	}
	
	public void setOutputs(int outputs) {
		this.outputs = outputs;
	}

	public int getOutputs() {
		return this.outputs;
	}
	
	public void setIntermediates(int intermediates) {
		this.intermediates = intermediates;
	}

	public int getIntermediates() {
		return this.intermediates;
	}
	
	public void setLayers(int layers) {
		this.layers = layers;
	}

	public int getLayers() {
		return this.layers;
	}

	public String getFeed() {
		return analyticFeed;
	}

	public String getFetch() {
		return analyticFetch;
	}

	public boolean isTraining() {
		return isTraining;
	}
	
	public boolean isProcessingTestMedia(){
		return isProcessingTestMedia;
	}

	public void setTraining(boolean isTraining) {
		this.isTraining = isTraining;
	}
	
	public String getAudioInputName(){
		return this.audioInputName;
	}
	public void setAudioInputName(String inputName){
		this.audioInputName = inputName;
	}
	public String getAudioOutputName(){
		return this.audioOutputName;
	}
	public void setAudioOutputName(String outputName){
		this.audioOutputName = outputName;
	}

}
