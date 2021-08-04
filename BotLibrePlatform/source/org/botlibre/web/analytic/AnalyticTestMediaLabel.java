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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * One analytic has many labels.
 */
@Entity
public class AnalyticTestMediaLabel {
	@Id
	@GeneratedValue
	protected long id;
	protected String testMediaLabel;
	
	@ManyToOne
	protected Analytic analytic;
	
	
	public AnalyticTestMediaLabel() {}
	
	public AnalyticTestMediaLabel(String label) {
		this.testMediaLabel = label;
	}
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	public void setLabel(String label){
		this.testMediaLabel = label;
	}
	public String getLabel(){
		return testMediaLabel;
	}
	public void setAnalytic(Analytic analytic){
		this.analytic = analytic;
	}
	public Analytic getAnalytic(){
		return analytic;
	}
}
