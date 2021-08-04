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

package org.botlibre.sdk.activity;

import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpFetchAction;
import org.botlibre.sdk.activity.actions.HttpGetInstancesAction;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.ChannelConfig;
import org.botlibre.sdk.config.ForumConfig;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.WebMediumConfig;

import io.evaai.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

/**
 * Help page.
 */
public class HelpActivity extends LibreActivity {
	protected WebMediumConfig instance;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		if (MainActivity.current == null) {
			finish();
			return;
		}
        setContentView(R.layout.activity_help);
        this.instance = MainActivity.instance;
		MainActivity.searching = false;
		MainActivity.searchingPosts = false;
	}
	
	@Override
	public void onResume() {
		if (MainActivity.current == null) {
			finish();
			return;
		}
		MainActivity.searching = false;
		MainActivity.searchingPosts = false;
		MainActivity.instance = this.instance;
		super.onResume();
	}

	public void faq(View view) {
		ForumConfig instance = new ForumConfig();
		instance.domain = "";
		instance.name = "FAQ";
		
		HttpAction action = new HttpFetchAction(this, instance, true);
		action.execute();
	}

	public void forums(View view) {
		BrowseConfig config = new BrowseConfig();
		config.domain = "";
		config.type = "Forum";
		config.contentRating = MainActivity.contentRating;
		
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config);
		action.execute();
	}

	public void liveSupport(View view) {
		ChannelConfig instance = new ChannelConfig();
		instance.domain = "";
		instance.name = "Help";
		
		HttpAction action = new HttpFetchAction(this, instance, true);
		action.execute();
	}

	public void helpBot(View view) {
		InstanceConfig instance = new InstanceConfig();
		instance.domain = "";
		instance.name = "Help Bot";
		
		HttpAction action = new HttpFetchAction(this, instance, true);
		action.execute();
	}

	public void website(View view) {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.WEBSITEHTTPS));
			startActivity(intent);
		} catch (Exception exception) {
			MainActivity.error("Unable to open website", exception, this);
		}
	}

	public void email(View view) {
		try {
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("plain/text");
			intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "support@botlibre.com" });
			startActivity(Intent.createChooser(intent, ""));
		} catch (Exception exception) {
			MainActivity.error("Unable to open email", exception, this);
		}
	}
	
}
