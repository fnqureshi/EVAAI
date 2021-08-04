/******************************************************************************
 *
 *  Copyright 2018 Paphus Solutions Inc.
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

public class UserFriendsConfig extends Config {

    public String action;
	public String userFriend;
    public String friendship;


    public void parseXML(Element element) {
        super.parseXML(element);
        this.action = element.getAttribute("action");
        this.userFriend = element.getAttribute("userFriend");
        this.friendship = element.getAttribute("friendship");
    }

	public String toXML() {
        StringWriter writer = new StringWriter();
        writer.write("<user-friends");
        writeCredentials(writer);
        if (this.action != null) {
            writer.write(" action=\"" + this.action + "\"");
        }
        if (this.userFriend != null) {
            writer.write(" userFriend=\"" + this.userFriend + "\"");
        }
        if (this.friendship != null) {
            writer.write(" friendship=\"" + this.friendship + "\"");
        }
        writer.write( ">");
        writer.write("</user-friends>");
        return writer.toString();
    }
}
