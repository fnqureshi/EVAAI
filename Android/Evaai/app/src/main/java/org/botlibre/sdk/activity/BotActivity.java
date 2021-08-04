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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import io.evaai.R;

import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpFetchAction;
import org.botlibre.sdk.activity.actions.HttpFetchChannelAction;
import org.botlibre.sdk.activity.actions.HttpUserFriendsAction;
import org.botlibre.sdk.activity.war.StartWarActivity;
import org.botlibre.sdk.config.ChannelConfig;
import org.botlibre.sdk.config.ForumConfig;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.UserFriendsConfig;
import org.botlibre.sdk.config.WebMediumConfig;

/**
 * Activity for viewing a bot's details.
 * To launch this activity from your app you can use the HttpFetchAction passing the bot id or name as a config.
 */
public class BotActivity extends WebMediumActivity {
	
	public String getType() {
		return "Bot";
	}

	public void resetView() {
        setContentView(R.layout.activity_bot);
		
        InstanceConfig instance = (InstanceConfig)this.instance;

        super.resetView();
        if (instance == null) {
        	return;
        }

        if (instance.isExternal && !instance.hasAPI) {
        	findViewById(R.id.chatButton).setVisibility(View.GONE);
        }
        
    	TextView text = (TextView) findViewById(R.id.sizeLabel);
        if (instance.size != null && instance.size.length() > 0) {
	        text.setText(instance.size + " objects");
        } else {
	        text.setText("");
        }

    	text = (TextView) findViewById(R.id.chatbotwarsLabel);
        text.setText("" + instance.rank + " rank, " + instance.wins + " wins, " + instance.losses + " losses");

		boolean isAdmin = (MainActivity.user != null) && this.instance.isAdmin;
		if (!isAdmin || !"Basic".equals(MainActivity.user.type)) {
			findViewById(R.id.upgradeLabel).setVisibility(View.GONE);
		}
	}

	public void menu(View view) {
		PopupMenu popup = new PopupMenu(this, view);
	    MenuInflater inflater = popup.getMenuInflater();
	    inflater.inflate(R.menu.menu_bot, popup.getMenu());
	    onPrepareOptionsMenu(popup.getMenu());
	    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
	        @Override
	        public boolean onMenuItemClick(MenuItem item) {
	            return onOptionsItemSelected(item);
	        }
	    });
	    popup.show();
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
        WebMediumConfig instance = this.instance;
        
        boolean isAdmin = (MainActivity.user != null) && instance.isAdmin;
        if (!isAdmin || instance.isExternal) {
			menu.findItem(R.id.menuAdmin).setVisible(false);
        }
        if (isAdmin || instance.isFlagged) {
        	menu.findItem(R.id.menuFlag).setVisible(false);
        }
        if (MainActivity.user == null || (!isAdmin && !((InstanceConfig)instance).allowForking)) {
        	menu.findItem(R.id.menuFork).setVisible(false);
        }
        if (instance.isExternal) {
			menu.findItem(R.id.menuLiveChat).setVisible(false);
			menu.findItem(R.id.menuChatRoom).setVisible(false);
		}
		if (MainActivity.user == null) {
			menu.findItem(R.id.menuSendMessage).setVisible(false);
			menu.findItem(R.id.menuAddFriend).setVisible(false);
		}
	    return true;
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_bot, menu);
        return true;
    }
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
		{
			case R.id.menuWar:
				war(null);
				return true;
			case R.id.menuSendMessage:
				sendMessage();
				return true;
			case R.id.menuAddFriend:
				addFriend();
				return true;
			case R.id.menuAdmin:
				admin();
				return true;
			case R.id.menuFlag:
				flag();
				return true;
			case R.id.menuFork:
				fork();
				return true;
			case R.id.menuLiveChat:
				liveChat();
				return true;
			case R.id.menuChatRoom:
				chatRoom();
				return true;
			case R.id.menuStar:
				star();
				return true;
			case R.id.menuThumbsUp:
				thumbsUp();
				return true;
			case R.id.menuThumbsDown:
				thumbsDown();
				return true;
			case R.id.website:
				openWebsite();
				return true;
			default:
				return super.onOptionsItemSelected(item);
        }
    }

	public void sendMessage() {
		Intent intent = new Intent(this, NewMessageActivity.class);
		intent.putExtra("view-user-id", "@" + this.instance.alias);
		startActivity(intent);
	}

	public void addFriend() {
		UserFriendsConfig config = new UserFriendsConfig();
		config.action = "AddFriendship";
		config.userFriend = "@" + this.instance.alias;
		HttpUserFriendsAction action = new HttpUserFriendsAction(this, config);
		action.execute();
	}

	public void admin() {
		Intent intent = new Intent(this, AdminActivity.class);
		startActivity(intent);
	}

	@SuppressLint("DefaultLocale")
	public void fork() {
        if (MainActivity.user == null) {
        	MainActivity.showMessage("You must sign in to fork a " + getType().toLowerCase(), this);
        	return;
        }
		MainActivity.template = this.instance.alias;
        Intent intent = new Intent(this, CreateBotActivity.class);
        startActivity(intent);
        
        finish();
	}

	@SuppressLint("DefaultLocale")
	public void liveChat() {
		InstanceConfig config;
		if(this.instance != null) {
			config = (InstanceConfig) this.instance;
			config.channelType = "live-chat";
			HttpFetchChannelAction action = new HttpFetchChannelAction(this, config);
			action.execute();
		}
	}

	@SuppressLint("DefaultLocale")
	public void chatRoom() {
		InstanceConfig config;
		if(this.instance != null) {
			config = (InstanceConfig) this.instance;
			config.channelType = "chat-room";
			HttpFetchChannelAction action = new HttpFetchChannelAction(this, config);
			action.execute();
		}
	}
	
	/**
	 * Start a chat session with the selected instance and the user.
	 */
	public void openChat(final View view) {
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

					openChat2(view);
				}
				@Override
				public void onAdFailedToLoad(int errorCode) {
					openChat2(view);
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
							openChat2(view);
						}
					}
				}, 1000);
				Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
				return;
			}
		}
		openChat2(view);
	}

	/**
	 * Start a chat session with the selected instance and the user.
	 */
	public void openChat2(View view) {
		Intent intent = new Intent(this, ChatActivity.class);
		startActivity(intent);
	}

	/**
	 * Start a chat bot war.
	 */
	public void war(final View view) {
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
	 * Start a chat bot war.
	 */
	public void war2(View view) {
		StartWarActivity.bot1 = (InstanceConfig)this.instance;
				
        Intent intent = new Intent(this, StartWarActivity.class);
        startActivity(intent);
    }
	
}
