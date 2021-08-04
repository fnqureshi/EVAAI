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
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.botlibre.BotException;

import org.botlibre.web.Site;
import org.botlibre.web.analytic.AnalyticBean;
import org.botlibre.web.bean.BotBean;
import org.botlibre.web.bean.LoginBean;

@javax.servlet.annotation.WebServlet("/analytic-binary-upload")
@SuppressWarnings("serial")
@MultipartConfig
public class AnalyticBinaryUploadServlet extends BeanServlet {
	private final int MAX_SIZE = 100000000;

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		LoginBean loginBean = getLoginBean(request, response);
		if (loginBean == null) {
			httpSessionTimeout(request, response);
			return;
		}

		AnalyticBean bean = loginBean.getBean(AnalyticBean.class);
		try {
			String postToken = (String)request.getParameter("postToken");
			loginBean.verifyPostToken(postToken);
			String instance = (String)request.getParameter("instance");
			if (instance != null) {
				if (bean.getInstance() == null || !String.valueOf(bean.getInstanceId()).equals(instance)) {
					bean.validateInstance(instance);
				}
			}
			bean.checkLogin();
			bean.checkInstance();
			bean.checkAdmin();
			if (!Site.COMMERCIAL && bean.getUser().isBasic() || bean.getUser().isBronze()) {
				throw new BotException("You must upgrade your account to Gold to upload a network binary");
			}
			String download = (String)request.getParameter("download-binary");
			if (download != null) {
				if (!bean.downloadNetworkBinary(response)) {
					response.sendRedirect("analytic-network.jsp");
				}
				return;
			}
			String deleteMedia = (String)request.getParameter("delete-media");
			if (deleteMedia != null) {
				bean.deleteBinaryNetwork(request);
				response.sendRedirect("analytic-network.jsp");
				return;
			}
			Part filePart = request.getPart("file");
			String fileName = getFileName(filePart);
			if (filePart != null && filePart.getContentType().equals("application/octet-stream")) {
				InputStream stream = filePart.getInputStream();
				bean.saveBinaryNetwork(BotBean.loadImageFile(stream, true, MAX_SIZE), fileName, filePart.getContentType());
			} else {
				throw new BotException("Please select the analytic binary file to upload");
			}
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		response.sendRedirect("analytic-network.jsp");
	}

}
