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

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.WebMediumBean;
import org.botlibre.web.rest.Config;
/**
 * DTO for XML analytic label config.
 */
@XmlRootElement(name="label")
@XmlAccessorType(XmlAccessType.FIELD)
public class LabelConfig extends Config {
	@XmlAttribute
	public String label;

	@SuppressWarnings("rawtypes")
	public WebMediumBean validate(LoginBean loginBean, HttpServletRequest requestContext) throws Throwable {
		connect(loginBean, requestContext);
		AnalyticBean bean = loginBean.getBean(AnalyticBean.class);
		bean.validateInstance(this.instance);
		if (loginBean.getError() != null) {
			throw loginBean.getError();
		}
		return bean;
	}
}
