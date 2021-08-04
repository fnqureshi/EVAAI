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

import javax.persistence.Entity;

import org.botlibre.web.admin.AbstractMedia;

/**
 * This class connects between an Analytic and its images and media.
 * One analytic to many medias.
 */
@Entity
public class AnalyticTestMedia extends AbstractMedia {
	protected Analytic analytic;
	
	protected String label;

	public AnalyticTestMedia() { }

	public Analytic getAnalytic() {
		return analytic;
	}

	public void setAnalytic(Analytic analytic) {
		this.analytic = analytic;
	}
	
	public String getLabel(){
		return label;
	}
	public void setLabel(String label){
		this.label = label;
	}
	
	public AnalyticMediaConfig toConfig() {
		AnalyticMediaConfig config = new AnalyticMediaConfig();
		config.mediaId = String.valueOf(this.mediaId);
		config.label = this.label;
		config.media = getFileName();
		config.name = getName();
		config.mediaType = getType();

		return config;
	}
	
	public String toString() {
		return "AnalyticMedia(" + this.name + ":" + this.type + ":" + this.label + ")";
	}
}
