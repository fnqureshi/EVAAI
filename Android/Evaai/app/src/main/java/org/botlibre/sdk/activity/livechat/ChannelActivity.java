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

package org.botlibre.sdk.activity.livechat;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import io.evaai.R;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.WebMediumActivity;
import org.botlibre.sdk.config.ChannelConfig;

/**
 * Activity for viewing a channels details.
 * To launch this activity from your app you can use the HttpFetchAction passing the channel id or name as a config.
 */
public class ChannelActivity extends WebMediumActivity {

	public void admin() {
        Intent intent = new Intent(this, ChannelAdminActivity.class);		
        startActivity(intent);
	}

	public void resetView() {
        setContentView(R.layout.activity_channel);
		
        ChannelConfig instance = (ChannelConfig)MainActivity.instance;

        super.resetView();

        if (instance.isExternal) {
        	findViewById(R.id.chatButton).setVisibility(View.GONE);
        }

    	TextView text = (TextView) findViewById(R.id.messagesLabel);
        if (instance.messages != null && instance.messages.length() > 0) {
	        text.setText(instance.messages + " messages");
        } else {
	        text.setText("");
        }

    	text = (TextView) findViewById(R.id.onlineLabel);
        if (instance.usersOnline != null && instance.messages.length() > 0) {
	        text.setText(instance.usersOnline + " users online, " + instance.adminsOnline + " admins");
        } else {
	        text.setText("");
        }

    	text = (TextView) findViewById(R.id.typeLabel);
        text.setText(instance.type);
	}
	
	public void openWebsite() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.WEBSITE + "/livechat?id=" + MainActivity.instance.id));
		startActivity(intent);
	}
	
	public String getType() {
		return "Channel";
	}
	
	/**
	 * Start a chat session with the selected instance and the user.
	 */
	public void chat(final View view) {
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
					chat2(view);
				}

				@Override
				public void onAdFailedToLoad(int errorCode) {
					chat2(view);
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
							chat2(view);
						}
					}
				}, 1000);
				Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
				return;
			}
		}
		chat2(view);
	}

	/**
	 * Start a chat session with the selected instance and the user.
	 */
	public void chat2(View view) {
        Intent intent = new Intent(this, LiveChatActivity.class);
        startActivity(intent);
    }
	
}
