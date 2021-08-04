/******************************************************************************
 *
 *  Copyright 2020 Paphus Solutions Inc.
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
package org.botlibre.web.admin;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.botlibre.web.Site;

@Entity
@Table(name="botplatform")
public class BotPlatform {
	@Id
	private int db_row_id = 1;
	protected String URL_PREFIX;
	protected String URL_SUFFIX;
	protected String SERVER_NAME;
	protected String SERVER_NAME2;
	protected String URL;
	protected String URLLINK;
	protected String SECUREURLLINK;
	protected String SANDBOXURLLINK;
	protected String REDIRECT;
	protected boolean HTTPS;
	protected String PYTHONSERVER;
	protected boolean BOOTSTRAP;
	protected boolean LOCK;
	protected boolean READONLY;
	protected boolean ADULT;
	protected boolean REVIEW_CONTENT;
	protected String CONTENT_RATING;
	protected String NAME;
	protected String DOMAIN;
	protected String ID;
	protected String PREFIX;
	protected String HASHTAG;
	protected String TYPE;
	protected boolean TWITTER;
	protected boolean FACEBOOK;
	protected boolean TELEGRAM;
	protected boolean SLACK;
	protected boolean SKYPE;
	protected boolean WECHAT;
	protected boolean KIK;
	protected boolean EMAIL;
	protected boolean TIMERS;
	protected boolean FORGET;
	protected boolean ADMIN;
	protected boolean VERIFYUSERS;
	protected boolean DEDICATED;
	protected boolean CLOUD;
	protected boolean COMMERCIAL;
	protected boolean ALLOW_SIGNUP;
	protected boolean VERIFY_EMAIL;
	protected boolean ANONYMOUS_CHAT;
	protected boolean REQUIRE_TERMS;
	protected boolean AGE_RESTRICT;
	protected Boolean DISABLE_SUPERGROUP;
	protected String BLOCK_AGENT;
	protected boolean BACKLINK;
	protected boolean WEEKLYEMAIL;
	protected boolean WEEKLYEMAILBOTS;
	protected boolean WEEKLYEMAILCHANNELS;
	protected boolean WEEKLYEMAILFORUMS;
	protected String EMAILHOST;
	protected String EMAILSALES;
	protected String EMAILPAYPAL;
	protected String SIGNATURE;
	protected String EMAILBOT;
	protected String EMAILSMTPHost;
	protected int EMAILSMTPPORT;
	protected String EMAILUSER;
	protected String EMAILPASSWORD;
	protected boolean EMAILSSL;
	protected int MEMORYLIMIT;
	protected int MAX_PROCCESS_TIME;
	protected int CONTENT_LIMIT;
	protected int MAX_CREATES_PER_IP;
	protected int MAX_USER_MESSAGES;
	protected int MAX_UPLOAD_SIZE;
	protected int MAX_LIVECHAT_MESSAGES;
	protected int MAX_ATTACHMENTS;
	protected int MAX_TRANSLATIONS;
	protected int URL_TIMEOUT;
	protected int MAX_API;
	protected int MAX_BRONZE;
	protected int MAX_GOLD;
	protected int MAX_PLATINUM;
	protected int MAX_BOT_CACHE_SIZE;
	protected int MAX_BOT_POOL_SIZE;
	protected int MAXTWEETIMPORT;
	protected String TWITTER_OAUTHKEY;
	protected String TWITTER_OAUTHSECRET;
	protected String FACEBOOK_APPID;
	protected String FACEBOOK_APPSECRET;
	protected String KEY;
	protected String UPGRADE_SECRET;
	protected String GOOGLEKEY;
	protected String GOOGLECLIENTID;
	protected String GOOGLECLIENTSECRET;
	protected String MICROSOFT_SPEECH_KEY;
	protected String RESPONSIVEVOICE_KEY;
	protected String YANDEX_KEY;
	protected String MICROSOFT_TRANSLATION_KEY;
	
	public void init() {
		// Set defaults for upgrade.
		if (DISABLE_SUPERGROUP == null) {
			DISABLE_SUPERGROUP = Site.DISABLE_SUPERGROUP;
		}
		if (BLOCK_AGENT == null) {
			BLOCK_AGENT = Site.BLOCK_AGENT;
		}
	}
}
