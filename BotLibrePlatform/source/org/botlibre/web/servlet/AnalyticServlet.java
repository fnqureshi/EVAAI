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

import org.botlibre.util.Utils;

import org.botlibre.web.analytic.Analytic;
import org.botlibre.web.analytic.AnalyticBean;
import org.botlibre.web.analytic.AnalyticConfig;
import org.botlibre.web.bean.BrowseBean.InstanceFilter;
import org.botlibre.web.bean.DomainBean;
import org.botlibre.web.bean.LoginBean;
import org.botlibre.web.bean.LoginBean.Page;
import org.botlibre.web.bean.SessionProxyBean;

@javax.servlet.annotation.WebServlet("/analytic")
@SuppressWarnings("serial")
public class AnalyticServlet extends BeanServlet {
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		LoginBean loginBean = getEmbeddedLoginBean(request, response);
		SessionProxyBean proxy = (SessionProxyBean)request.getSession().getAttribute("proxy");
		AnalyticBean bean = loginBean.getBean(AnalyticBean.class);
		loginBean.setActiveBean(bean);
		if (!loginBean.checkDomain(request, response)) {
			return;
		}

		String domain = (String)request.getParameter("domain");
		if (domain != null) {
			DomainBean domainBean = loginBean.getBean(DomainBean.class);
			if (domainBean.getInstance() == null || !String.valueOf(domainBean.getInstanceId()).equals(domain)) {
				domainBean.validateInstance(domain);
			}
		}
		
		String browse = (String)request.getParameter("id");

		if (browse != null) {
			if (proxy != null) {
				proxy.setInstanceId(browse);
			}
			bean.validateInstance(browse);

			request.getRequestDispatcher("analytic.jsp").forward(request, response);
			return;
		}
		doPost(request, response);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");

		SessionProxyBean proxy = (SessionProxyBean)request.getSession().getAttribute("proxy");
		LoginBean loginBean = getLoginBean(request, response);
		if (loginBean == null) {
			httpSessionTimeout(request, response);
			return;
		}
		AnalyticBean bean = loginBean.getBean(AnalyticBean.class);
		loginBean.setActiveBean(bean);
		
		try {
			String postToken = (String)request.getParameter("postToken");
			if (!loginBean.checkDomain(request, response)) {
				return;
			}
			String domain = (String)request.getParameter("domain");
			if (domain != null) {
				DomainBean domainBean = loginBean.getBean(DomainBean.class);
				if (domainBean.getInstance() == null || !String.valueOf(domainBean.getInstanceId()).equals(domain)) {
					domainBean.validateInstance(domain);
				}
			}
			String cancel = (String)request.getParameter("cancel");
			if (cancel != null) {
				response.sendRedirect("browse-analytic.jsp");
				return;
			}
			String instance = (String)request.getParameter("instance");
			if (instance != null) {
				if (bean.getInstance() == null || !String.valueOf(bean.getInstanceId()).equals(instance)) {
					bean.validateInstance(instance);
				}
			}
			String showDetails = (String)request.getParameter("show-details");
			if (showDetails != null) {
				request.getRequestDispatcher("analytic.jsp").forward(request, response);
				return;
			}
			String cancelInstance = (String)request.getParameter("cancel-instance");
			if (cancelInstance != null) {
				response.sendRedirect("analytic?id=" + bean.getInstanceId() + proxy.proxyString());
				return;
			}
			String copy = (String)request.getParameter("copy");
			if (copy != null) {
				bean.copyInstance();
				response.sendRedirect("create-analytic.jsp");
				return;
			}
			String createAnalytic = (String)request.getParameter("create-analytic");
			String createAnalyticLink = (String)request.getParameter("create-analytic-link");
			String createInstance = (String)request.getParameter("create-instance");
			String createLink = (String)request.getParameter("create-link");
			String editInstance = (String)request.getParameter("edit-instance");
			String saveInstance = (String)request.getParameter("save-instance");
			String deleteInstance = (String)request.getParameter("delete-instance");
			String saveAnalytic = (String) request.getParameter("save-analytic");

			if (createAnalytic != null) {
				response.sendRedirect("create-analytic.jsp");
				return;
			}
			if (createAnalyticLink != null) {
				response.sendRedirect("create-analytic-link.jsp");
				return;
			}
			if (editInstance != null) {
				if (!bean.editInstance(bean.getInstanceId())) {
					response.sendRedirect("analytic?details=true&id=" + bean.getInstanceId() + proxy.proxyString());
					return;
				}
				request.getRequestDispatcher("edit-analytic.jsp").forward(request, response);
				return;
			}
			
			if (checkCommon(bean, "analytic?id=" + bean.getInstanceId(), request, response)) {
				return;
			}
			if (checkUserAdmin(bean, "analytic-users.jsp", request, response)) {
				return;
			}
			
			String analyticLabels = (String)request.getParameter("labels");
			String analyticTextSave = (String)request.getParameter("save-labels");
			if (analyticTextSave != null) {
				if (analyticLabels != null) {
					loginBean.verifyPostToken(postToken);
					bean.saveLabelsNetwork(analyticLabels);
					response.sendRedirect("analytic-network.jsp");
					return;
				}
			}
			
			String analyticLabelsProperties = (String) request.getParameter("labels-properties");
			String analyticSaveLabelsProperties = (String) request.getParameter("save-labels-properties");
			if (analyticSaveLabelsProperties != null) {
				if (analyticLabelsProperties != null) {
					loginBean.verifyPostToken(postToken);
					bean.saveLabelsProperties(analyticLabelsProperties);
					response.sendRedirect("analytic-network.jsp");
					return;
				}
			}
			
			String search = (String)request.getParameter("search-analytic");
			if (search != null) {
				bean.resetSearch();
				bean.setCategoryFilter(bean.getCategoryString());
				request.getRequestDispatcher("analytic-search.jsp").forward(request, response);
				return;
			}
			String category = (String)request.getParameter("category");
			if (category != null) {
				bean.browseCategory(category);
				request.getRequestDispatcher("browse-analytic.jsp").forward(request, response);
				return;
			}
			String create = (String)request.getParameter("create-analytic");
			if (create != null) {
				request.getRequestDispatcher("create-analytic.jsp").forward(request, response);
				return;
			}
			String createCategory = (String)request.getParameter("create-category");
			if (createCategory != null) {
				loginBean.setCategoryType("Analytic");
				loginBean.setActiveBean(bean);
				request.getRequestDispatcher("create-category.jsp").forward(request, response);
				return;
			}
			String editCategory = (String)request.getParameter("edit-category");
			if (editCategory != null) {
				loginBean.setCategoryType("Analytic");
				loginBean.setActiveBean(bean);
				loginBean.setCategory(bean.getCategory());
				request.getRequestDispatcher("edit-category.jsp").forward(request, response);
				return;
			}
			String deleteCategory = (String)request.getParameter("delete-category");
			if (deleteCategory != null) {
				loginBean.verifyPostToken(postToken);
				loginBean.setCategoryType("Analytic");
				loginBean.setActiveBean(bean);
				loginBean.setCategory(bean.getCategory());
				loginBean.deleteCategory();
				response.sendRedirect("browse-analytic.jsp");
				return;
			}
			String analyticTest = (String)request.getParameter("test-analytic");
			if (analyticTest != null) {
				if (bean.getInstance() != null) {
					response.sendRedirect("test-analytic.jsp");
					return;
				}
				request.getRequestDispatcher("test-analytic.jsp").forward(request, response);
				return;
			}
			AnalyticConfig config = new AnalyticConfig();
			updateParameters(config, request);
			String newdomain = (String)request.getParameter("newdomain");
			config.subdomain = (String)request.getParameter("subdomain");
			config.analyticType = bean.MOBNET50;
			config.analyticFeed = bean.INPUT;
			config.analyticFetch = bean.FINAL_RESULT;
			config.imageSize = "224";
			boolean isFeatured = "on".equals((String)request.getParameter("isFeatured"));
			String delete = (String)request.getParameter("delete");
			
			String adVerified = (String)request.getParameter("adVerified");
			if (createInstance != null) {
				loginBean.verifyPostToken(postToken);
				config.name = (String)request.getParameter("newInstance");
				if (!bean.createInstance(config)) {
					response.sendRedirect("create-analytic.jsp");
				} else {
					loginBean.setPageType(Page.Browse);
					response.sendRedirect("analytic?id=" + bean.getInstanceId() + proxy.proxyString());
				}
				return;
			}
			
			if (createLink != null) {
				loginBean.verifyPostToken(postToken);
				config.name = (String)request.getParameter("newInstance");
				if (!bean.createLink(config)) {
					response.sendRedirect("create-analytic-link.jsp");
				} else {
					loginBean.setPageType(Page.Browse);
					response.sendRedirect("analytic?id=" + bean.getInstanceId() + proxy.proxyString());
				}
				return;
			}
			
			String resetNetwork = (String) request.getParameter("reset-network");
			if (resetNetwork != null) {
				loginBean.verifyPostToken(postToken);
				bean.resetNetwork();
				response.sendRedirect("analytic-network.jsp");
				return;
			}
			
			String libraryType=(String)request.getParameter("library-type");
			String inputs =(String) request.getParameter("inputs");
			String outputs =(String) request.getParameter("outputs");
			String intermediates =(String) request.getParameter("intermediates");
			String layers =(String) request.getParameter("layers");
			String activationFunc =(String) request.getParameter("activation-function");
			String analyticType=(String)request.getParameter("analytic-type");
			String imageSize =(String) request.getParameter("analytic-size");
			String analyticFeed = (String) request.getParameter("analytic-feed");
			String analyticFetch = (String) request.getParameter("analytic-fetch");
			String audioInputName = (String) request.getParameter("audio-input-name");
			String audioOutputName = (String) request.getParameter("audio-output-name");
			String customInputs =(String) request.getParameter("custom-inputs");
			String customOutputs =(String) request.getParameter("custom-outputs");
			String customIntermediates =(String) request.getParameter("custom-intermediates");
			String customLayers =(String) request.getParameter("custom-layers");
			String customActivationFunc =(String) request.getParameter("custom-activation-function");
			
			if (saveAnalytic != null) {
				loginBean.verifyPostToken(postToken);
				if (analyticType != null) {
					bean.updateAnalyticSettings(libraryType, inputs, outputs, intermediates, layers, activationFunc,analyticType, imageSize, analyticFeed, analyticFetch,
							audioInputName, audioOutputName, customInputs, customOutputs, customIntermediates, customLayers, customActivationFunc);
					response.sendRedirect("analytic-network.jsp");
					return;
				}
			}
			if (saveInstance != null) {
				try {
					loginBean.verifyPostToken(postToken);
					if (!bean.updateAnalytic(config, newdomain, isFeatured, "on".equals(adVerified))) {
						request.getRequestDispatcher("edit-analytic.jsp").forward(request, response);
					} else {
						response.sendRedirect("analytic?id=" + bean.getInstanceId() + proxy.proxyString());
					}
				} catch (Exception failed) {
					bean.error(failed);
					request.getRequestDispatcher("edit-analytic.jsp").forward(request, response);
				}
				return;
			}
			if (deleteInstance != null) {
				loginBean.verifyPostToken(postToken);
				Analytic analytic = bean.getInstance();
				if (!analytic.getLabels().isEmpty() || !analytic.getMedia().isEmpty() || !analytic.getTestMediaLabels().isEmpty() || !analytic.getTestMedia().isEmpty()) {
					if (!bean.sendError("Please delete the analytic's contents first")) {
						response.sendRedirect("analytic?id=" + bean.getInstanceId() + proxy.proxyString());
						return;
					}
				}
				if (!bean.deleteInstance("on".equals(delete))) {
					response.sendRedirect("analytic?id=" + bean.getInstanceId() + proxy.proxyString());
				} else {
					if (loginBean.getPageType() == Page.Search) {
						request.getRequestDispatcher("analytic-search.jsp").forward(request, response);
					} else {
						request.getRequestDispatcher("browse-analytic.jsp").forward(request, response);
					}
				}
				return;
			}
			String adminInstance = (String)request.getParameter("admin");
			if (adminInstance != null) {
				if (!bean.adminInstance(bean.getInstanceId())) {
					response.sendRedirect("analytic?id=" + bean.getInstanceId() + proxy.proxyString());
					return;
				}
				request.getRequestDispatcher("admin-analytic.jsp").forward(request, response);
				return;
			}
			
			if (checkSearchCommon(bean, "analytic-search.jsp", request, response)) {
				return;
			}

			setSearchFields(request, bean);
			String typeFilter = (String)request.getParameter("type-filter");
			if (typeFilter != null) {
				bean.setTypeFiler(Utils.sanitize(typeFilter));
			}
		} catch (Throwable failed) {
			loginBean.error(failed);
		}
		request.getRequestDispatcher("analytic-search.jsp").forward(request, response);
	}
}
