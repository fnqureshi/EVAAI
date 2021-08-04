/******************************************************************************
 *
 *  Copyright 2019 Paphus Solutions Inc.
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

import org.w3c.dom.Element;

import java.io.StringWriter;

public class BrowseUserConfig extends Config {

    public String userFilter;
    public String nameFilter;
    public String sort;
    public String tag;
    public String page;

    public void parseXML(Element element) {
        super.parseXML(element);
        this.userFilter = element.getAttribute("userFilter");
        this.nameFilter = element.getAttribute("nameFilter");
        this.sort = element.getAttribute("sort");
        this.tag = element.getAttribute("tag");
        this.page = element.getAttribute("page");
    }

    public String toXML() {
        StringWriter writer = new StringWriter();
        writer.write("<browse-public-users");
        writeCredentials(writer);
        if (this.userFilter != null) {
            writer.write(" userFilter=\"" + this.userFilter + "\"");
        }
        if (this.nameFilter != null) {
            writer.write(" nameFilter=\"" + this.nameFilter + "\"");
        }
        if (this.sort != null) {
            writer.write(" sort=\"" + this.sort + "\"");
        }
        if (this.tag != null) {
            writer.write(" tag=\"" + this.tag + "\"");
        }
        if ((this.page != null) && !this.page.equals("")) {
            writer.write(" page=\"" + this.page + "\"");
        }
        writer.write(">");
        writer.write("</browse-public-users>");
        return writer.toString();
    }
}
