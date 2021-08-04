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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.botlibre.web.bean.BotBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.WeChatBean;
import org.botlibre.web.service.PageStats;

@javax.servlet.annotation.WebServlet("/wechat")
@SuppressWarnings("serial")
public class WeChatServlet extends BeanServlet {
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PageStats.page(request);
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		
		LoginBean loginBean = (LoginBean)request.getSession().getAttribute("loginBean");
		if (loginBean == null) {
			response.sendRedirect("index.jsp");
			return;
		}
		BotBean botBean = loginBean.getBotBean();
		WeChatBean bean = loginBean.getBean(WeChatBean.class);

		try {
			String postToken = (String)request.getParameter("postToken");
			loginBean.verifyPostToken(postToken);
			String instance = (String)request.getParameter("instance");
			if (instance != null) {
				if (botBean.getInstance() == null || !String.valueOf(botBean.getInstanceId()).equals(instance)) {
					botBean.validateInstance(instance);
				}
			}
			if (!botBean.isConnected()) {
				response.sendRedirect("wechat.jsp");
				return;
			}
			botBean.checkAdmin();
			
			String userToken = (String)request.getParameter("userToken");
			String appId = (String)request.getParameter("appId");
			String appPassword = (String)request.getParameter("appPassword");
			String menu = (String)request.getParameter("menu");
			//boolean international = "on".equals((String)request.getParameter("international"));
			boolean international = "international".equals((String)request.getParameter("accountType"));
			
			String submit = (String)request.getParameter("save");
			if (submit != null) {
				bean.save(userToken, appId, appPassword, international, menu);
				bean.connect();
			}
			submit = (String)request.getParameter("check");
			if (submit != null) {
				bean.checkStatus();
			}
		} catch (Exception failed) {
			botBean.error(failed);
		}
		response.sendRedirect("wechat.jsp");
	}
}
