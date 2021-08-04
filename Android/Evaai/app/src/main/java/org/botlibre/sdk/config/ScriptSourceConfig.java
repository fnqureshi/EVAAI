/*  Copyright 2016 Paphus Solutions Inc.
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
import android.util.Log;

public class ScriptSourceConfig extends Config{
	public String id;
	public String creationDate;
	public String updateDate;
	public boolean version;
	public String versionName;
	public String creator;
	public String source;

	public ScriptSourceConfig credentials() {
		ScriptSourceConfig config = new ScriptSourceConfig();
		config.creator = this.creator;
		return config;
	}

	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<script-source");
		writeCredentials(writer);
		if (this.id != null) {
			writer.write(" id=\"" + this.id + "\"");
		}
		if (this.creationDate != null) {
			writer.write(" creationDate=\"" + this.creationDate + "\"");
		}
		if (this.updateDate != null) {
			writer.write(" updateDate=\"" + this.updateDate + "\"");
		}
		if (this.version) {
			writer.write(" version=\"true\"");
		}
		if (this.versionName != null && !this.versionName.equals("")) {
			writer.write(" versionName=\"" + this.versionName + "\"");
		}
		if (this.creator != null && !this.creator.equals("")) {
			writer.write(" creator=\"" + this.creator + "\"");
		}

		writer.write(">");
		
		if (this.source != null) {
			writer.write("<source>");
			writer.write(Utils.escapeHTML(this.source));
			writer.write("</source>");
		}
		writer.write("</script-source>"); 
		return writer.toString();
	}

	public void parseXML(Element element){
		this.id = element.getAttribute("id");
		this.creationDate = element.getAttribute("creationDate");
		this.updateDate = element.getAttribute("updateDate");
		this.version = Boolean.valueOf(element.getAttribute("version"));
		this.versionName = element.getAttribute("versionName");
		this.creator = element.getAttribute("creator");
		
		Node node = element.getElementsByTagName("source").item(0);
		if (node != null) {
			this.source = node.getTextContent();
		}
	}

	public String getNextVersion() {
		if (this.source == null) {
			return "0.1";
		}

		String version = this.source;
		int index = version.lastIndexOf('.');
		if (index != -1) {
			String major = version.substring(0, index);
			String minor = version.substring(index + 1, version.length());
			try {
				int value = Integer.valueOf(minor);
				version = major + "." + (value + 1); 
			}
			catch (NumberFormatException ignore) {
			}
		}
		return version;
	}

}
