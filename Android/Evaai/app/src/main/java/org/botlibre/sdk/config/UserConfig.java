/******************************************************************************
 *
 *  Copyright 2014-2020 Paphus Solutions Inc.
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

import android.util.Log;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.botlibre.sdk.SDKConnection;
import org.botlibre.sdk.util.Utils;

/**
 * DTO for XML user creation config.
 */
public class UserConfig extends Config {
	public String password;
	public String newPassword;
	public String hint;
	public String name;
	public boolean showName;
	public String gender;
	public String properties;
	public String source;
	public String affiliate;
	public String userAccess;
	public String email;
	public Boolean emailMessages;
	public Boolean emailNotices;
	public Boolean emailSummary;
	public boolean isSubscribed;
	public String website;
	public boolean over18;
	public String dateOfBirth;

	public boolean acceptTerms;
	
	public String connects;
	public String bots;
	public String forums;
	public String channels;
	public String posts;
	public String avatars;
	public String scripts;
	public String analytics;
	public String graphics;
	public String issues;
	public String issueTrackers;
	public String domains;

	public String friends;
	public String followers;
	public String affiliates;

	public String messages;
	
	public String joined;
	public String lastConnect;
	public String upgradeDate;
	public String expiryDate;
	public String type;
	public boolean isFlagged;
	public String credentialsType;
	public String credentialsUserID;
	public String credentialsToken;
	public String applicationId;
	public Boolean newMessage;
	public String tags;

	public String bio;
	public String avatar;
	public String avatarThumb;
	public String flaggedReason;

	public boolean isBot;
	public String voice;
	public String voiceMod;
	public boolean nativeVoice;
	public String nativeVoiceName;
	public String nativeVoiceProvider;
	public String nativeVoiceApiKey;
	public String nativeVoiceAppId;
	public String language;
	public String speechRate;
	public String pitch;
	public long instanceAvatarId;
	public String channelType;
	
	public String displayJoined() {
		try {
			SimpleDateFormat formater = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
			Date date = formater.parse(joined);
			return Utils.displayDate(date);
		} catch (Exception exception) {
			return joined;
		}
	}
	
	public String displayLastConnect() {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
			Date date = formatter.parse(lastConnect);
			return Utils.displayTimestamp(date);
		} catch (Exception exception) {
			return lastConnect;
		}
	}
	
	public void addCredentials(SDKConnection connection) {
		this.application = connection.getCredentials().getApplicationId();
		if (connection.getDomain() != null) {
			this.domain = connection.getDomain().id;
		}
	}
	
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof UserConfig)) {
			return false;
		}
		return this.user != null && this.user.equals(((UserConfig)object).user);
	}

	public String getProperty(String property) {
		return getProperties().optString(property);
	}

	public void setProperty(String property, String value) {
		try {
			JSONObject json = getProperties();
			json.put(property, value);
			this.properties = json.toString();
		} catch (Exception exception) {
			Log.wtf("json", exception);
		}
	}

	public JSONObject getProperties() {
		if (this.properties == null || this.properties.isEmpty()) {
			this.properties = "{}";
		}
		try {
			return new JSONObject(this.properties);
		} catch (Exception exception) {
			Log.wtf("json", exception);
			return null;
		}
	}

	public void parseXML(Element element) {
		this.user = element.getAttribute("user");
		this.name = element.getAttribute("name");
		this.showName = Boolean.valueOf(element.getAttribute("showName"));
		this.token = element.getAttribute("token");
		this.email = element.getAttribute("email");
		this.emailMessages = Boolean.valueOf(element.getAttribute("emailMessages"));
		this.emailNotices = Boolean.valueOf(element.getAttribute("emailNotices"));
		this.emailSummary = Boolean.valueOf(element.getAttribute("emailSummary"));
		this.isSubscribed = Boolean.valueOf(element.getAttribute("isSubscribed"));
		this.hint = element.getAttribute("hint");
		this.website = element.getAttribute("website");
		this.over18 = Boolean.valueOf(element.getAttribute("over18"));

		this.gender = element.getAttribute("gender");
		this.dateOfBirth = element.getAttribute("dateOfBirth");
		this.properties = element.getAttribute("properties");

		this.connects = element.getAttribute("connects");
		this.bots = element.getAttribute("bots");
		this.forums = element.getAttribute("forums");
		this.channels = element.getAttribute("channels");
		this.posts = element.getAttribute("posts");
		this.avatars = element.getAttribute("avatars");
		this.scripts = element.getAttribute("scripts");
		this.analytics = element.getAttribute("analytics");
		this.graphics = element.getAttribute("graphics");
		this.issues = element.getAttribute("issues");
		this.issueTrackers = element.getAttribute("issueTrackers");
		this.domains = element.getAttribute("domains");

		this.friends = element.getAttribute("friends");
		this.followers = element.getAttribute("followers");
		this.affiliates = element.getAttribute("affiliates");

		this.messages = element.getAttribute("messages");

		this.joined = element.getAttribute("joined");
		this.lastConnect = element.getAttribute("lastConnect");
		this.upgradeDate = element.getAttribute("upgradeDate");
		this.expiryDate = element.getAttribute("expiryDate");
		this.type = element.getAttribute("type");
		this.isFlagged = Boolean.valueOf(element.getAttribute("isFlagged"));
		this.credentialsType = element.getAttribute("credentialsType");
		this.applicationId = element.getAttribute("applicationId");
		this.newMessage = Boolean.valueOf(element.getAttribute("newMessage"));
		this.tags = element.getAttribute("tags");

		this.isBot = Boolean.valueOf(element.getAttribute("isBot"));
		this.voice = element.getAttribute("voice");
		this.voiceMod = element.getAttribute("voiceMod");
		this.nativeVoice = Boolean.valueOf(element.getAttribute("nativeVoice"));
		this.nativeVoiceName= element.getAttribute("nativeVoiceName");
		this.nativeVoiceProvider = element.getAttribute("nativeVoiceProvider");
		this.nativeVoiceApiKey = element.getAttribute("nativeVoiceApiKey");
		this.nativeVoiceAppId = element.getAttribute("nativeVoiceAppId");
		this.language = element.getAttribute("language");
		this.speechRate = element.getAttribute("speechRate");
		this.pitch = element.getAttribute("pitch");
		this.instanceAvatarId = Long.valueOf(element.getAttribute("instanceAvatarId"));
		this.channelType = element.getAttribute("channelType");
		
		Node node = element.getElementsByTagName("bio").item(0);
		if (node != null) {
			this.bio = node.getTextContent();
		}
		node = element.getElementsByTagName("avatar").item(0);
		if (node != null) {
			this.avatar = node.getTextContent();
		}
		node = element.getElementsByTagName("flaggedReason").item(0);
		if (node != null) {
			this.flaggedReason = node.getTextContent();
		}
	}
	
	public String toXML() {
		StringWriter writer = new StringWriter();
		writer.write("<user");
		writeCredentials(writer);
		if (this.password != null) {
			writer.write(" password=\"" + this.password + "\"");
		}
		if (this.newPassword != null) {
			writer.write(" newPassword=\"" + this.newPassword + "\"");
		}
		if (this.hint != null) {
			writer.write(" hint=\"" + this.hint + "\"");
		}
		if (this.name != null) {
			writer.write(" name=\"" + this.name + "\"");
		}
		if (this.showName) {
			writer.write(" showName=\"" + this.showName + "\"");
		}
		if (this.gender != null) {
			writer.write(" gender=\"" + this.gender + "\"");
		}
		if (this.properties != null) {
			writer.write(" properties=\"" + Utils.escapeHTML(this.properties) + "\"");
		}
		if (this.source != null) {
			writer.write(" source=\"" + this.source + "\"");
		}
		if (this.affiliate != null) {
			writer.write(" affiliate=\"" + this.affiliate + "\"");
		}
		if (this.userAccess != null) {
			writer.write(" userAccess=\"" + this.userAccess + "\"");
		}
		if (this.dateOfBirth != null) {
			writer.write(" dateOfBirth=\"" + this.dateOfBirth + "\"");
		}
		if (this.email != null) {
			writer.write(" email=\"" + this.email + "\"");
		}
		if (this.emailMessages != null) {
			writer.write(" emailMessages=\"" + this.emailMessages + "\"");
		}
		if (this.emailNotices != null) {
			writer.write(" emailNotices=\"" + this.emailNotices + "\"");
		}
		if (this.emailSummary != null) {
			writer.write(" emailSummary=\"" + this.emailSummary + "\"");
		}
		if (this.website != null) {
			writer.write(" website=\"" + this.website + "\"");
		}
		if (this.over18) {
			writer.write(" over18=\"" + this.over18 + "\"");
		}
		if ((this.lastConnect != null) && !this.lastConnect.equals("") && !"null".equals(this.lastConnect)) {
			writer.write(" lastConnect=\"" + this.lastConnect + "\"");
		}
		if (this.channelType != null) {
			writer.write(" channelType=\"" + this.channelType + "\"");
		}
		if (this.tags != null) {
			writer.write(" tags=\"" + this.tags + "\"");
		}
		writer.write(">");
		
		if (this.bio != null) {
			writer.write("<bio>");
			writer.write(Utils.escapeHTML(this.bio));
			writer.write("</bio>");
		}
		writer.write("</user>");
		return writer.toString();
	}
		
}