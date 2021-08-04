/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
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

package org.botlibre.sdk.config;

import java.io.StringWriter;

import org.botlibre.sdk.util.Utils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * DTO for XML content config.
 */
public class UserMessageConfig extends Config {
	public String id;
	public String creationDate;
	public String owner;
	public String creator;
	public String target;
	public String parent;
	public String subject;
	public String message;
	public String avatar;
	public String page;
	public String pageSize;
	public String resultsSize;
	
	public void parseXML(Element element) {
		super.parseXML(element);
		
		this.id = element.getAttribute("id");
		this.creationDate = element.getAttribute("creationDate");
		this.owner = element.getAttribute("owner");
		this.creator = element.getAttribute("creator");
		this.target = element.getAttribute("target");
		this.parent = element.getAttribute("parent");
		this.avatar = element.getAttribute("avatar");
		this.page = element.getAttribute("page");
		this.pageSize = element.getAttribute("pageSize");
		this.resultsSize = element.getAttribute("resultsSize");
		
		Node node = element.getElementsByTagName("subject").item(0);
		if (node != null) {
			this.subject = node.getTextContent();
		}
		node = element.getElementsByTagName("message").item(0);
		if (node != null) {
			this.message = node.getTextContent();
		}
		node = element.getElementsByTagName("avatar").item(0);
		if (node != null) {
			this.avatar = node.getTextContent();
		}
	}

	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<user-message");
		writeCredentials(writer);

		if (this.id != null) {
			writer.write(" id=\"" + this.id + "\"");
		}
		if (this.creationDate != null) {
			writer.write(" creationDate=\"" + this.creationDate + "\"");
		}
		if (this.owner != null) {
			writer.write(" owner=\"" + this.owner + "\"");
		}
		if (this.creator != null) {
			writer.write(" creator=\"" + this.creator + "\"");
		}
		if (this.target != null) {
			writer.write(" target=\"" + this.target + "\"");
		}
		if (this.parent != null) {
			writer.write(" parent=\"" + this.parent + "\"");
		}
		if (this.avatar != null) {
			writer.write(" avatar=\"" + this.avatar + "\"");
		}
		if (this.page != null) {
			writer.write(" page=\"" + this.page + "\"");
		}
		if (this.pageSize != null) {
			writer.write(" pageSize=\"" + this.pageSize + "\"");
		}
		if (this.resultsSize != null) {
			writer.write(" resultsSize=\"" + this.resultsSize + "\"");
		}

		writer.write(">");

		if (this.subject != null) {
			writer.write("<subject>");
			writer.write(Utils.escapeHTML(this.subject));
			writer.write("</subject>");
		}
		if (this.message != null) {
			writer.write("<message>");
			writer.write(Utils.escapeHTML(this.message));
			writer.write("</message>");
		}
		if (this.avatar != null) {
			writer.write("<avatar>");
			writer.write(Utils.escapeHTML(this.message));
			writer.write("</avatar>");
		}
		writer.write("</user-message>");
		return writer.toString();
	}
}