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

import org.botlibre.web.Site;
import org.botlibre.web.analytic.AnalyticBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.BotBean;

/**
 * This class is handled to upload images from analytic-media.jsp, it uploads one or multiple images to one selected label.
 */
@javax.servlet.annotation.WebServlet("/analytic-test-media-upload")
@SuppressWarnings("serial")
@MultipartConfig
public class AnalyticTestMediaUploadServlet extends BeanServlet {
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
			
			String selectedLabel = (String) request.getParameter("label-selected");
			if(selectedLabel !=null){
				if(selectedLabel.equals("") || selectedLabel.isEmpty()){
					throw new BotException("Please add a label");
				}
				bean.setSelectedTestMediaLabel(selectedLabel);
			}
			
			// check the allowed amount of images to be uploaded
			int allowedImages = 0;
			int availableSlots = 0;
			if (!bean.isSuper() && !bean.getUser().isPartnerUser()) {
				if (Site.COMMERCIAL) {
					allowedImages = 10000;
				} else {
					switch (loginBean.getUser().getType()) {
					case Bronze:
						allowedImages = 100;
						break;
					case Gold:
						allowedImages = 500;
						break;
					case Platinum:
						allowedImages = 5000;
						break;
					case Diamond:
						allowedImages = 10000;
						break;
					case Basic:
						allowedImages = 50;
					default:
						break;
					}
				}
				availableSlots = allowedImages - bean.getInstance().getTestMedia().size();

				// if there is not enough space.
				if (availableSlots <= 0) {
					throw new BotException("You must upgrade your account to increase the limit of images");
				}
			}
			
			byte[] image = null;
			Collection<Part> files = request.getParts();
			int count = 0;
			for (Part filePart : files) {
				if (!bean.isSuper() && !bean.getUser().isPartnerUser()) {
					if(availableSlots <= 0){
						throw new BotException("You have reached your limit " + allowedImages + "\nYou must upgrade your account to upload more images");
					}
					availableSlots--;
				}
				if (!filePart.getName().equals("file")) {
					continue;
				}
				String type = filePart.getContentType();
				if (filePart != null && type.equals("image/jpeg") || type.equals("audio/wav")) {
					InputStream stream = filePart.getInputStream();
					image = BotBean.loadImageFile(stream,true,3000000);
				}else{
					throw new BotException("Please select the media file (image/jpeg)");
				}
				if ((image == null) || (image.length == 0)) {
					continue;
				}
				String fileName = getFileName(filePart);
				bean.createAnalyticTestMedia(image, fileName, filePart.getContentType(), selectedLabel);
				
				count++;
			}
			if (count == 0) {
				throw new BotException("Please select the media file");
			}
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		response.sendRedirect("analytic-test-media.jsp");
	}
}
