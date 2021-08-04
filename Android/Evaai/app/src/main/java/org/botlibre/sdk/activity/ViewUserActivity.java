/******************************************************************************
 *
 *  Copyright 2018-2021 Paphus Solutions Inc.
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

import java.io.StringWriter;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpChangeUserIconAction;
import org.botlibre.sdk.activity.actions.HttpCreateAction;
import org.botlibre.sdk.activity.actions.HttpDeleteUserAccountAction;
import org.botlibre.sdk.activity.actions.HttpFetchChannelAction;
import org.botlibre.sdk.activity.actions.HttpFlagUserAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetInstancesAction;
import org.botlibre.sdk.activity.actions.HttpGetPostsAction;
import org.botlibre.sdk.activity.actions.HttpUserFriendsAction;
import org.botlibre.sdk.activity.actions.HttpVerifyEmailAction;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.UserConfig;
import org.botlibre.sdk.config.UserFriendsConfig;
import org.botlibre.sdk.config.WebMediumConfig;
import org.botlibre.sdk.util.Utils;

import io.evaai.R;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity for viewing a user's details.
 */
public class ViewUserActivity extends LibreActivity {

	private UserConfig viewUser;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		if (MainActivity.current == null) {
			finish();
			return;
		}
		viewUser = MainActivity.viewUser;
        setContentView(R.layout.activity_view_user);
        resetView();
		MainActivity.searching = false;
	}
	
	@Override
	public void onResume() {
		if(MainActivity.current == null) {
			finish();
			return;
		}
		MainActivity.searching = false;
		MainActivity.viewUser = this.viewUser;
		resetView();
		super.onResume();
	}

	public void resetView() {
		UserConfig user = MainActivity.viewUser;
        if (user == null) {
        	return;
        }
        
        TextView text = (TextView) findViewById(R.id.title);
        text.setText(user.user);
        
        text = (TextView) findViewById(R.id.userText);
        text.setText(user.user);
        text = (TextView) findViewById(R.id.nameText);
        if (user.showName) {
        	text.setText(user.name);
        } else {
        	text.setVisibility(View.GONE);
        }

		text = (TextView) findViewById(R.id.tagText);
		if(!user.tags.isEmpty()) {
			text.setText(user.tags);
		} else {
			text.setVisibility(View.GONE);
		}

        text = (TextView) findViewById(R.id.websiteText);
        if (user.website == null || user.website.length() == 0) {
        	text.setVisibility(View.GONE);
        } else {
        	text.setText(user.website);
        }
        text = (TextView) findViewById(R.id.joinedText);
        text.setText("Joined " + user.displayJoined());
        text = (TextView) findViewById(R.id.connectsText);
        text.setText(user.connects + " connects");
        text = (TextView) findViewById(R.id.lastConnectText);
        text.setText("Last connected " + user.displayLastConnect());
        
        text = (TextView) findViewById(R.id.contentText);
        StringWriter writer = new StringWriter();
        if (user.bots != null && !"0".equals(user.bots)) {
        	writer.write("" + user.bots + " bots, ");
        }
        if (user.avatars != null && !"0".equals(user.avatars)) {
        	writer.write("" + user.avatars + " avatars, ");
        }
        if (user.channels != null && !"0".equals(user.channels)) {
        	writer.write("" + user.channels + " channels, ");
        }
        if (user.forums != null && !"0".equals(user.forums)) {
        	writer.write("" + user.forums + " forums, ");
        }
        if (user.domains != null && !"0".equals(user.domains)) {
        	writer.write("" + user.domains + " workspaces, ");
        }
        if (user.scripts != null && !"0".equals(user.scripts)) {
        	writer.write("" + user.scripts + " scripts, ");
        }
        if (user.graphics != null && !"0".equals(user.graphics)) {
        	writer.write("" + user.graphics + " graphics");
        }
        text.setText(writer.toString());
        
        text = (TextView) findViewById(R.id.statsText);
        text.setText(user.posts + " posts, " + user.messages + " messages");
        
        text = (TextView) findViewById(R.id.typeText);
        text.setText(user.type + " account");
        
        WebView web = (WebView) findViewById(R.id.bioText);
        web.loadDataWithBaseURL(null, Utils.formatHTMLOutput(user.bio), "text/html", "utf-8", null);        
        web.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
            	try {
            		view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            	} catch (Exception failed) {
            		return false;
            	}
                return true;
            }
        });

        if (!user.isFlagged) {
	        findViewById(R.id.flaggedLabel).setVisibility(View.GONE);
        } else {
	        findViewById(R.id.imageView).setVisibility(View.GONE);        	
        }

        Button messagesButton = findViewById(R.id.messageButton);
        Button friendsButton = findViewById(R.id.friendsButton);
        if(MainActivity.user != null) {
        	messagesButton.setVisibility(View.VISIBLE);
        	friendsButton.setVisibility(View.VISIBLE);
		} else {
			messagesButton.setVisibility(View.GONE);
			friendsButton.setVisibility(View.GONE);
		}

        findViewById(R.id.imageView).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ImageActivity.image = MainActivity.viewUser.avatar;
		        Intent intent = new Intent(ViewUserActivity.this, ImageActivity.class);
		        startActivity(intent);
			}
		});
        
        HttpGetImageAction.fetchImage(this, MainActivity.viewUser.avatar, (ImageView)findViewById(R.id.icon));
        HttpGetImageAction.fetchImage(this, MainActivity.viewUser.avatar, (ImageView)findViewById(R.id.imageView));
	}

	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_view_user, menu);
        return true;
    }
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
        for (int index = 0; index < menu.size(); index++) {
    	    menu.getItem(index).setVisible(true);
        }
        if (MainActivity.user == null || MainActivity.user == MainActivity.viewUser) {
    	    MenuItem item = menu.findItem(R.id.menuFlag);
    	    if (item != null) {
    	    	item.setVisible(false);
    	    }
        }
        if(MainActivity.user == null || !MainActivity.user.user.equals(MainActivity.viewUser.user)) {
    	    menu.findItem(R.id.menuChangeIcon).setVisible(false);
    	    menu.findItem(R.id.menuEditUser).setVisible(false);
			menu.findItem(R.id.menuVerifyEmail).setVisible(false);
			menu.findItem(R.id.menuUserMessages).setVisible(false);
			menu.findItem(R.id.menuUserFriends).setVisible(false);
			menu.findItem(R.id.menuUserAvatar).setVisible(false);
			menu.findItem(R.id.menuUserVoice).setVisible(false);
			menu.findItem(R.id.deleteUserAccount).setVisible(false);
			menu.findItem(R.id.menuUserBot).setVisible(false);
			menu.findItem(R.id.menuUserTags).setVisible(false);
        }
        if (MainActivity.user == null || MainActivity.user.user.equals(MainActivity.viewUser.user)) {
			menu.findItem(R.id.menuSendMessage).setVisible(false);
			menu.findItem(R.id.menuAddFriend).setVisible(false);
		}
	    return true;
	}

	public void showEmailDialog(String title, String message, String userEmail, final Activity activity) {
		AlertDialog dialog = new AlertDialog.Builder(activity).create();
		dialog.setMessage(message);
		dialog.setTitle(title);
		dialog.setCancelable(false);
		final EditText email = new EditText(this);
		email.setText(userEmail);
		TableLayout.LayoutParams params = new TableLayout.LayoutParams();
		email.setLayoutParams(params);
		dialog.setView(email);
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getApplicationContext().getString(R.string.dialogCancelButton), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, getApplicationContext().getString(R.string.dialogOkButton), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String userEmail = email.getText().toString().trim();
				MainActivity.user.email = userEmail;
				hideSoftKeyboard(email);
				HttpVerifyEmailAction action = new HttpVerifyEmailAction(activity, MainActivity.user);
				action.execute();
			}
		});
		dialog.show();
	}

	protected void hideSoftKeyboard(EditText text) {
		InputMethodManager inputMethodManager = (InputMethodManager) ViewUserActivity.this.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputMethodManager .isActive()) {
			inputMethodManager .hideSoftInputFromWindow(text.getWindowToken(), 0);
		}
	}

	public void verifyEmail() {
		if (MainActivity.user == null) {
			return;
		}
		showEmailDialog(getApplicationContext().getString(R.string.emailVerification),
				getApplicationContext().getString(R.string.enterEmailMessage), MainActivity.user.email,this);
	}

	public void showDeleteAccountDialog(String title, String message, final Activity activity) {
		AlertDialog dialog = new AlertDialog.Builder(activity).create();
		dialog.setMessage(message);
		dialog.setTitle(title);
		dialog.setCancelable(false);
		final EditText password = new EditText(this);
		password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		password.setHint(getApplicationContext().getString(R.string.passwordRequiredHint));
		TableLayout.LayoutParams params = new TableLayout.LayoutParams();
		password.setLayoutParams(params);
		dialog.setView(password);
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getApplicationContext().getString(R.string.dialogCancelButton), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, getApplicationContext().getString(R.string.dialogDeleteButton), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String pswd = password.getText().toString().trim();
				if (pswd.equals("")) {
					hideSoftKeyboard(password);
					MainActivity.showMessage(getApplicationContext().getString(R.string.emptyPasswordMessage),ViewUserActivity.this);
					return;
				} else {
					MainActivity.user.password = password.getText().toString().trim();
					hideSoftKeyboard(password);
					HttpDeleteUserAccountAction action = new HttpDeleteUserAccountAction(activity, MainActivity.user);
					action.execute();
				}
			}
		});
		dialog.show();
	}

	public void deleteUserAccount() {
		showDeleteAccountDialog(getApplicationContext().getString(R.string.deleteAccountVerification), getApplicationContext().getString(R.string.dialogPasswordRequired),this);
	}

	public void userFriends(View view) {
		userFriends();
	}

	public void userFriends() {
		if(!MainActivity.user.user.equals(MainActivity.viewUser.user)) {
			Intent intent = new Intent(this, ViewUserFriendsActivity.class);
			startActivity(intent);
			return;
		}
		Intent intent = new Intent(this, UserFriendsActivity.class);
		startActivity(intent);
	}

	public void userMessages(View view) {
		userMessages();
	}

	public void userMessages() {
		if(MainActivity.user != MainActivity.viewUser) {
			Intent intent = new Intent(this, NewMessageActivity.class);
			intent.putExtra("view-user-id", MainActivity.viewUser.user);
			startActivity(intent);
		} else {
			Intent intent = new Intent(this, UserMessagesActivity.class);
			startActivity(intent);
		}
	}

	public void sendMessage() {
		userMessages();
	}

	public void addFriend() {
		UserFriendsConfig config = new UserFriendsConfig();
		config.action = "AddFriendship";
		config.userFriend = MainActivity.viewUser.user;
		HttpUserFriendsAction action = new HttpUserFriendsAction(this, config);
		action.execute();
	}

	public void browseUserTags() {
		Intent intent = new Intent(ViewUserActivity.this, UserTagsActivity.class);
		startActivity(intent);
	}

	public void viewFriends() {
		Intent intent = new Intent(this, UserFriendsActivity.class);
		startActivity(intent);
	}

	public void userAvatar(View view) {
		userAvatar();
	}

	public void userAvatar() {
		Intent intent = new Intent(this, UserAvatarActivity.class);
		startActivity(intent);
	}

	public void userVoice(View view) {
		userVoice();
	}

	public void userVoice() {
		Intent intent = new Intent(this, UserVoiceActivity.class);
		startActivity(intent);
	}

	@SuppressLint("DefaultLocale")
	public void userBot() {
		InstanceConfig instance = new InstanceConfig();
		instance.name = MainActivity.user.user + " Bot";
		instance.type = "instance";
		HttpAction action = new HttpCreateAction(this, instance);
		action.execute();
	}

	@SuppressLint("DefaultLocale")
	public void liveChat() {
		UserConfig userConfig;
		if(MainActivity.user != null) {
			userConfig = MainActivity.viewUser;
			userConfig.channelType = "live-chat";
		} else {
			return;
		}
		HttpFetchChannelAction action = new HttpFetchChannelAction(this, userConfig);
		action.execute();
	}

	@SuppressLint("DefaultLocale")
	public void chatRoom() {
		UserConfig userConfig;
		if(MainActivity.user != null) {
			userConfig = MainActivity.viewUser;
			userConfig.channelType = "chat-room";
		} else {
			return;
		}
		HttpFetchChannelAction action = new HttpFetchChannelAction(this, userConfig);
		action.execute();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menuChangeIcon:
				changeIcon();
				return true;
			case R.id.menuEditUser:
				editUser();
				return true;
			case R.id.menuVerifyEmail:
				verifyEmail();
				return true;
			case R.id.menuUserFriends:
				userFriends();
				return true;
			case R.id.menuUserMessages:
				userMessages();
				return true;
			case R.id.menuSendMessage:
				sendMessage();
				return true;
			case R.id.menuAddFriend:
				addFriend();
				return true;
			case R.id.menuUserTags:
				browseUserTags();
				return true;
			case R.id.menuUserAvatar:
				userAvatar();
				return true;
			case R.id.menuUserVoice:
				userVoice();
				return true;
			case R.id.menuUserBot:
				userBot();
				return true;
			case R.id.menuLiveChat:
				liveChat();
				return true;
			case R.id.menuChatRoom:
				chatRoom();
				return true;
			case R.id.menuFlag:
				flag();
				return true;
			case R.id.deleteUserAccount:
				deleteUserAccount();
				return true;
			case R.id.menuBots:
				browseBots();
				return true;
			case R.id.menuForums:
				browseForums();
				return true;
			case R.id.menuChannels:
				browseChannels();
				return true;
			case R.id.menuAvatars:
				browseAvatars();
				return true;
			case R.id.menuGraphics:
				browseGraphics();
				return true;
			case R.id.menuDomains:
				browseDomains();
				return true;
			case R.id.menuPosts:
				browsePosts();
				return true;
			case R.id.website:
				openWebsite();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	public void openWebsite() {
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.WEBSITE + "/login?view-user=" + MainActivity.viewUser.user));
		startActivity(intent);
	}

	public void browseBots() {
		BrowseConfig config = new BrowseConfig();
		config.typeFilter = "Personal";
		config.type = "Bot";
		config.userFilter = MainActivity.viewUser.user;
		config.contentRating = MainActivity.contentRating;
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config);
		action.execute();
	}

	public void browseForums() {
		BrowseConfig config = new BrowseConfig();
		config.typeFilter = "Personal";
		config.type = "Forum";
		config.userFilter = MainActivity.viewUser.user;
		config.contentRating = MainActivity.contentRating;
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config);
		action.execute();
	}

	public void browseChannels() {
		BrowseConfig config = new BrowseConfig();
		config.typeFilter = "Personal";
		config.type = "Channel";
		config.userFilter = MainActivity.viewUser.user;
		config.contentRating = MainActivity.contentRating;
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config);
		action.execute();
	}

	public void browseAvatars() {
		BrowseConfig config = new BrowseConfig();
		config.typeFilter = "Personal";
		config.type = "Avatar";
		config.userFilter = MainActivity.viewUser.user;
		config.contentRating = MainActivity.contentRating;
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config);
		action.execute();
	}
	
	public void browseGraphics() {
		BrowseConfig config = new BrowseConfig();
		config.typeFilter = "Personal";
		config.type = "Graphic";
		config.userFilter = MainActivity.viewUser.user;
		config.contentRating = MainActivity.contentRating;
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config);
		action.execute();
	}

	public void browseDomains() {
		BrowseConfig config = new BrowseConfig();
		config.typeFilter = "Personal";
		config.type = "Domain";
		config.userFilter = MainActivity.viewUser.user;
		config.contentRating = MainActivity.contentRating;
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config);
		action.execute();
	}

	public void browsePosts() {
		MainActivity.instance = null;
		BrowseConfig config = new BrowseConfig();
		config.type = "Post";
		config.typeFilter = "Personal";
		config.userFilter = MainActivity.viewUser.user;
		config.sort = "date";
		config.contentRating = MainActivity.contentRating;
		HttpGetPostsAction action = new HttpGetPostsAction(this, config);
		action.execute();
	}

	public void editUser() {
        Intent intent = new Intent(this, EditUserActivity.class);
        startActivity(intent);
	}

	public void flag() {
        if (MainActivity.user == null) {
        	MainActivity.showMessage("You must sign in to flag a user", this);
        	return;
        }
        final EditText text = new EditText(this);
        MainActivity.prompt("Enter reason for flagging the user", this, text, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            WebMediumConfig instance = MainActivity.instance.credentials();
	            instance.flaggedReason = text.getText().toString();
	            if (instance.flaggedReason.trim().length() == 0) {
	            	MainActivity.error("You must enter a valid reason for flagging the user", null, ViewUserActivity.this);
	            	return;
	            }
	            
	            HttpAction action = new HttpFlagUserAction(ViewUserActivity.this, MainActivity.viewUser);
	        	action.execute();
	        }
        });
	}

	public void changeIcon() {
		ActivityCompat.requestPermissions(this,
				new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
				1);
	}
	@SuppressLint("Override")
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		switch (requestCode) {
			case 1: {
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Intent upload = new Intent(Intent.ACTION_GET_CONTENT);
					upload.setType("image/*");
					startActivityForResult(upload, 1);
				} else {
					Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
				}
				return;
			}
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			return;
		}
		try {
			String file = MainActivity.getFilePathFromURI(this, data.getData());
			HttpAction action = new HttpChangeUserIconAction(this, file, MainActivity.user);
			action.execute().get();
    		if (action.getException() != null) {
    			throw action.getException();
    		}
		} catch (Exception exception) {
			MainActivity.error(exception.getMessage(), exception, this);
			return;
		}
	}
	
	public void menu(View view) {
		PopupMenu popup = new PopupMenu(this, view);
	    MenuInflater inflater = popup.getMenuInflater();
	    inflater.inflate(R.menu.menu_view_user, popup.getMenu());
	    onPrepareOptionsMenu(popup.getMenu());
	    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
	        @Override
	        public boolean onMenuItemClick(MenuItem item) {
	            return onOptionsItemSelected(item);
	        }
	    });
	    popup.show();
	}
}
