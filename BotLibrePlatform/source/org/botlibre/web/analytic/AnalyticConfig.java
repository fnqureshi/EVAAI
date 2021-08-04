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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.botlibre.util.Utils;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.rest.WebMediumConfig;

/**
 * DTO for XML analytic config.
 */
@XmlRootElement(name="analytic")
@XmlAccessorType(XmlAccessType.FIELD)
public class AnalyticConfig extends WebMediumConfig {
	@XmlAttribute
	public String analyticType;
	@XmlAttribute
	public String analyticFeed; 
	@XmlAttribute
	public String analyticFetch;
	@XmlAttribute
	public String imageSize;
	@XmlAttribute
	public String trainingStatus;
	@XmlAttribute
	public String processingTestMediaStatus;
	@XmlAttribute 
	public boolean isTraining;
	@XmlAttribute 
	public boolean isProcessingMedia;
	@XmlAttribute 
	public String numberOfClasses;
	@XmlAttribute 
	public String audioInputName;
	@XmlAttribute 
	public String audioOutputName;
	
	public String input;
	public String output;

	public AnalyticBean getBean(LoginBean loginBean) {
		return loginBean.getBean(AnalyticBean.class);
	}
	
	public void sanitize() {
		super.sanitize();
		analyticType = Utils.sanitize(analyticType);
		analyticFeed = Utils.sanitize(analyticFeed);
		analyticFetch = Utils.sanitize(analyticFetch);
		imageSize = Utils.sanitize(imageSize);
		processingTestMediaStatus = Utils.sanitize(processingTestMediaStatus);
		numberOfClasses = Utils.sanitize(numberOfClasses);
		audioInputName = Utils.sanitize(audioInputName);
		audioOutputName = Utils.sanitize(audioOutputName);
	}
}
