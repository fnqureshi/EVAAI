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
package org.botlibre.web.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.botlibre.BotException;

import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.ScriptBean;
import org.botlibre.web.bean.SessionProxyBean;

@javax.servlet.annotation.WebServlet("/script-source-upload")
@SuppressWarnings("serial")
@MultipartConfig
public class ScriptSourceUploadServlet extends BeanServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		SessionProxyBean proxy = (SessionProxyBean)request.getSession().getAttribute("proxy");
		LoginBean loginBean = getLoginBean(request, response);
		if (loginBean == null) {
			httpSessionTimeout(request, response);
			return;
		}
		ScriptBean bean = loginBean.getBean(ScriptBean.class);
		try {
			String postToken = (String)request.getParameter("postToken");
			loginBean.verifyPostToken(postToken);
			String instance = (String)request.getParameter("instance");
			if (instance != null) {
				if (bean.getInstance() == null || !String.valueOf(bean.getInstanceId()).equals(instance)) {
					bean.validateInstance(instance);
				}
			}
			String version = (String)request.getParameter("version");
			String versionName = (String)request.getParameter("versionName");
			String encoding = (String)request.getParameter("import-encoding");
			Collection<Part> files = request.getParts();
			int count = 0;
			for (Part filePart : files) {
				if (!filePart.getName().equals("file")) {
					continue;
				}
				if (filePart != null) {
					InputStream stream = filePart.getInputStream();
					bean.updateScriptSource(stream, encoding, "on".equals(version), versionName);
					count++;
				}
			}
			if (count == 0) {
				throw new BotException("Please select the source file to upload");
			}
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		response.sendRedirect("script?source=true&id=" + bean.getInstanceId() + proxy.proxyString());
	}
}
