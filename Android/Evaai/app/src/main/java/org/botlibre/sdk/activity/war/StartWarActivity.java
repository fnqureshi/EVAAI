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

package org.botlibre.sdk.activity.war;

import java.util.ArrayList;
import java.util.List;

import org.botlibre.sdk.activity.ImageListAdapter;
import org.botlibre.sdk.activity.LibreActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpFetchWarAction;
import org.botlibre.sdk.activity.actions.HttpGetInstancesAction;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.WebMediumConfig;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import io.evaai.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity for user login.
 */
public class StartWarActivity extends LibreActivity {
	protected int browsing = 0;
	public static InstanceConfig winner;
	public static InstanceConfig looser;
	public static InstanceConfig bot1;
	public static InstanceConfig bot2;
	public static String topic = "Hello";
	public boolean handsFreeSpeechWAS;
	
	private static long SECRET = 4357845875643L;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (MainActivity.current == null) {
			finish();
			return;
		}
		setContentView(R.layout.activity_start_war);
		
		if (MainActivity.handsFreeSpeech) {
			this.handsFreeSpeechWAS = true;
			MainActivity.handsFreeSpeech = false;
		}
		
        ((EditText)findViewById(R.id.topicText)).setText(topic);
        winner = null;
        looser = null;
        
		if (bot1 == null) {
	    	SharedPreferences cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE);
	    	String last = cookies.getString("instance", null);
			if (last != null && !last.isEmpty()) {
				InstanceConfig config = new InstanceConfig();
				config.name = last;
				HttpFetchWarAction action = new HttpFetchWarAction(this, config);
				action.execute();
			}
		}
	}
	
	public void resetView() {
		try{
		List<WebMediumConfig> instances = new ArrayList<WebMediumConfig>();
		if (bot1 != null) {
			instances.add(bot1);
		}
		ListView list = (ListView) findViewById(R.id.bot1List);
		list.setAdapter(new ImageListAdapter(this, R.layout.image_list, instances));
		instances = new ArrayList<WebMediumConfig>();
		if (bot2 != null) {
			instances.add(bot2);
		}
		list = (ListView) findViewById(R.id.bot2List);
		list.setAdapter(new ImageListAdapter(this, R.layout.image_list, instances));
		}catch(Exception e){Log.e("StartWarActivity","Error: " + e.getMessage());}
	}
	
	/**
	 * Start a new war.
	 */
	public void war(final View view) {
		if (bot1 == null || bot2 == null) {
			MainActivity.showMessage("Please select two bots to start a war", this);
			return;
		}
		topic = ((EditText) findViewById(R.id.topicText)).getText().toString();
		if (topic == null || topic.isEmpty()) {
			MainActivity.showMessage("Please enter a topic", this);
			return;
		}
		if (MainActivity.showAds && MainActivity.showPopupAds) {
			//ad before chatActivity
			MainActivity.mInterstitialAd.setAdListener(new AdListener() {
				@Override
				public void onAdLoaded() {
				}

				@Override
				public void onAdClosed() {
					// Load the next interstitial.
					MainActivity.mInterstitialAd.loadAd(new AdRequest.Builder().build());

					war2(view);
				}

				@Override
				public void onAdFailedToLoad(int errorCode) {
					war2(view);
				}
			});
			if (MainActivity.mInterstitialAd.isLoaded()) {
				MainActivity.mInterstitialAd.show();
				return;
			} else {
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (MainActivity.mInterstitialAd.isLoaded()) {
							MainActivity.mInterstitialAd.show();
							return;
						} else {
							war2(view);
						}
					}
				}, 1000);
				Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
				return;
			}
		}
		war2(view);
	}

	/**
	 * Start a new war.
	 */
	public void war2(View view) {
        MainActivity.instance = bot1;
        MainActivity.connection.setUser(null);
		Intent intent = new Intent(this, WarActivity.class);
        startActivity(intent);
    }

	public void browseBot1(View view) {
		BrowseConfig config = new BrowseConfig();
		config.type = "Bot";
		config.sort = "rank";
		config.contentRating = MainActivity.contentRating;
		
		MainActivity.browsing = true;
		this.browsing = 0;
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config);
		action.execute();
	}

	public void browseBot2(View view) {
		BrowseConfig config = new BrowseConfig();
		config.type = "Bot";
		config.sort = "rank";
		config.contentRating = MainActivity.contentRating;

		MainActivity.browsing = true;
		MainActivity.instance = null;
		this.browsing = 1;
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config);
		action.execute();
	}
	
	@Override
	public void onResume() {
		MainActivity.connection.setUser(MainActivity.user);
		if (MainActivity.browsing) {
			if ((MainActivity.instance instanceof InstanceConfig)) {
				if (this.browsing == 0) {
					bot1 = (InstanceConfig)MainActivity.instance;
		        	SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
		        	cookies.putString(bot1.getType(), bot1.alias);
		        	cookies.commit();
				} else {
					bot2 = (InstanceConfig)MainActivity.instance;
				}
			}
		} else if (winner != null) {
			try{
			String text = "Last war " + winner.name + " beat " + looser.name + ".";
			((TextView)findViewById(R.id.winner)).setText(text);
			ChatWarConfig config = new ChatWarConfig();
			config.winner = winner.id;
			config.looser = looser.id;
			config.topic = topic;
			config.secret = String.valueOf(SECRET + MainActivity.user.user.length());
			HttpChatWarAction action = new HttpChatWarAction(this, config);
			action.execute();
			}catch(Exception e){
				MainActivity.showMessage("You must be signed in to vote!", this);
			}
		}
		MainActivity.browsing = false;
		resetView();
		super.onResume();
	}
	@Override
	protected void onDestroy() {
		if(handsFreeSpeechWAS){
			MainActivity.handsFreeSpeech = true;
		}
		super.onDestroy();
	}
}
