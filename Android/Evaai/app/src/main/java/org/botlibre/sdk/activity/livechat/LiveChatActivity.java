/******************************************************************************
 *
 *  Copyright 2019-2021 Paphus Solutions Inc.
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

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import io.evaai.R;
import org.botlibre.sdk.LiveChatConnection;
import org.botlibre.sdk.LiveChatListener;
import org.botlibre.sdk.activity.AbstractChatActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.ViewUserActivity;
import org.botlibre.sdk.activity.WebMediumAdminActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpAvatarMessageAction;
import org.botlibre.sdk.activity.actions.HttpCreateChannelFileAttachmentAction;
import org.botlibre.sdk.activity.actions.HttpCreateChannelImageAttachmentAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetLiveChatUsersAction;
import org.botlibre.sdk.activity.actions.HttpUserAvatarMessageAction;
import org.botlibre.sdk.config.AvatarMessage;
import org.botlibre.sdk.config.ChannelConfig;
import org.botlibre.sdk.config.ChatConfig;
import org.botlibre.sdk.config.ChatResponse;
import org.botlibre.sdk.config.MediaConfig;
import org.botlibre.sdk.config.UserAvatarMessage;
import org.botlibre.sdk.config.UserConfig;
import org.botlibre.sdk.config.VoiceConfig;
import org.botlibre.sdk.config.WebMediumConfig;
import org.botlibre.sdk.util.TextStream;
import org.botlibre.sdk.util.Utils;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.LinearLayoutManager;

/**
 * Activity for live chat and chatrooms.
 * To launch this activity from your app you can use the HttpFetchAction passing the channel id or name as a config, and launch=true.
 */
public class LiveChatActivity extends AbstractChatActivity implements UsersRecyclerViewAdapter.OnItemClickListener, UsersRecyclerViewAdapter.OnItemTouchListener {

	protected LiveChatConnection connection;
	public boolean sound = true;
	protected boolean keepAlive = false;
	protected MediaPlayer chimePlayer;
	protected String childActivity = "";
	protected long startTime;
	protected boolean closing;
	public UserConfig userConfig; //deal with later??
	protected ListView userMessagesListView;
	private LiveChatMessagesAdapter liveChatAdapter;
	private List<ChatConfig> liveChatMessagesList;
	private RecyclerView usersRecyclerView;
	private RecyclerView.LayoutManager usersLayoutManager;
	public UsersRecyclerViewAdapter usersRecyclerViewAdapter;
	private Button toggleAvatarBtn;
	private HashMap<String, UserConfig> usersMap;
	private LinearLayout userMessagesLayout;
	private LinearLayout menuLayout;
	private int prevUserListIndex = -1;
	private boolean isPrivate = false; //need to get rid of this
	private boolean isClosed = false;
	private boolean hasVideoAvatar = false;
	public String html = "";
	private String initialMessage = "";
	private String chatConfigUser;
	private double lastChat = 0;
	private int usersSize;
	public String nick;
	public ArrayList<HttpUserAvatarMessageAction> responseQueue;
	public boolean responding = false;
	public boolean disableVideo = false;

	public HashMap<String, UserConfig> getUsersMap() {
		return this.usersMap;
	}

	public ChannelConfig getInstance() { return (ChannelConfig) this.instance; }

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		if (MainActivity.current == null) {
			finish();
			return;
		}
		this.startTime = System.currentTimeMillis();

		setContentView(R.layout.activity_livechat);

		if(MainActivity.viewUser != null) {
			HttpGetImageAction.fetchImage(this, MainActivity.viewUser.avatar, findViewById(R.id.icon));
		} else if(MainActivity.user != null) {
			HttpGetImageAction.fetchImage(this, MainActivity.user.avatar, findViewById(R.id.icon));
		}

		if(MainActivity.user != null){
			this.nick = MainActivity.user.user;
		}

		this.instance = (ChannelConfig)MainActivity.instance;

		setTitle(Utils.stripTags(this.instance.name));
		((TextView) findViewById(R.id.title)).setText(Utils.stripTags(this.instance.name));

		this.textView = (EditText) findViewById(R.id.messageText);
		this.textView.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				submitChat();
				return false;
			}
		});

		ImageButton	button = (ImageButton) findViewById(R.id.speakButton);
		button.setOnClickListener(new View.OnClickListener() {
			@TargetApi(23)
			@Override
			public void onClick(View v) {
				if (MainActivity.handsFreeSpeech) {
					//set the current volume to the setting.
					setStreamVolume();
					//if its ON Or OFF - Switching back and forth
					MainActivity.listenInBackground = !MainActivity.listenInBackground;
					//saving the boolean data of MainActivity.listeningInBackground
					SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
					cookies.putBoolean("listenInBackground", MainActivity.listenInBackground);
					cookies.commit();
					if (MainActivity.listenInBackground) {
						micLastStat = true;
						try {microphoneThread(thread);} catch (Exception ignore){}
						beginListening();
					} else {
						micLastStat = false;
						microphoneThread(thread);
						stopListening();
					}
				} else {
					Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, MainActivity.voice.language);
					try {
						startActivityForResult(intent, RESULT_SPEECH);
						textView.setText("");
					} catch (ActivityNotFoundException a) {
						Toast t = Toast.makeText(getApplicationContext(),"Your device doesn't support Speech to Text", Toast.LENGTH_SHORT);
						t.show();
					}
				}
			}
		});

		responseQueue = new ArrayList<>();
		usersMap = new HashMap<>();
		liveChatMessagesList = new ArrayList<>();
		messageText = (EditText) findViewById(R.id.messageText);
		menuLayout = (LinearLayout) findViewById(R.id.menuLayout);
		userMessagesLayout = (LinearLayout) findViewById(R.id.userMessagesLayout);
		imageView = (ImageView) findViewById(R.id.imageView);
		videoLayout = findViewById(R.id.videoLayout);
		toggleAvatarBtn = findViewById(R.id.toggleAvatarButton);
		userMessagesListView = (ListView) findViewById(R.id.liveChatUserMessagesListView);
		liveChatAdapter = new LiveChatMessagesAdapter(LiveChatActivity.this, liveChatMessagesList);
		userMessagesListView.setAdapter(liveChatAdapter);
		userMessagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			}
		});

		imageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				stateLayouts++;
				if (stateLayouts > 3) {
					stateLayouts = 0;
				}
				resetLayout(stateLayouts);
			}
		});

		videoLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				stateLayouts++;
				if (stateLayouts > 3) {
					stateLayouts = 0;
				}
				resetLayout(stateLayouts);
			}
		});

		GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {

			@Override
			public boolean onDoubleTapEvent(MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (isClosed) {
						return false;
					}
					//this isnt visible though
					if (!isPrivate && usersRecyclerView.getVisibility() == View.VISIBLE) {
						usersRecyclerView.setVisibility(View.GONE);
					} else if (isPrivate && imageView.getVisibility() == View.VISIBLE && videoLayout.getVisibility() == View.GONE) {
						imageView.setVisibility(View.GONE);
					} else if (isPrivate && imageView.getVisibility() == View.GONE && videoLayout.getVisibility() == View.VISIBLE) {
						videoLayout.setVisibility(View.GONE);
					} else if (!isPrivate && usersRecyclerView.getVisibility() == View.GONE) {
						usersRecyclerView.setVisibility(View.VISIBLE);
					} else if (isPrivate && (imageView.getVisibility() == View.GONE || videoLayout.getVisibility() == View.GONE)) {
						if ((stateLayouts != 3) && (userConfig != null && userConfig.instanceAvatarId != 0)) {
							videoLayout.setVisibility(View.VISIBLE);
							imageView.setVisibility(View.GONE);
							playVideo(response.avatar, true);
						} else {
							imageView.setVisibility(View.VISIBLE);
							videoLayout.setVisibility(View.GONE);
						}
					}
					userMessagesListView.postDelayed(new Runnable() {
						@Override
						public void run() {
							userMessagesListView.setSelection(userMessagesListView.getAdapter().getCount() - 1);
						}
					}, 500);
					return true;
				}
				return false;
			}
		};

		final GestureDetector detector = new GestureDetector(this, listener);
		userMessagesListView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return detector.onTouchEvent(event);
			}
		});

		this.usersRecyclerView = (RecyclerView) findViewById(R.id.usersRecyclerView);

		try {
			this.connection = new LiveChatConnection(MainActivity.connection.getCredentials(),
					new LiveChatListener() {
						@Override
						public void message(String message) {
							response(message);
						}

						@Override
						public void info(String message) {
							response(message);
						}

						@Override
						public void error(String message) {
							response(message);
						}

						public void updateUsers(String csv) {
							HttpAction action = new HttpGetLiveChatUsersAction(LiveChatActivity.this, csv);
							action.execute();
							return;
						}

						@Override
						public void closed() {
							closeChat();
						}
					});
			this.connection.connect((ChannelConfig)this.instance, MainActivity.user);
		} catch (Exception exception) {
			MainActivity.error(exception.getMessage(), exception, this);
		}
		super.instance = (ChannelConfig)MainActivity.instance;
		this.tts = new TextToSpeech(this, this);
		initChat();
	}

	/**
	 * Toggle the avatar layout base on the clicks.
	 */
	public void resetLayout(int index) {
		if (index == 3) {
			this.imageView.setVisibility(View.VISIBLE);
			if (this.userConfig != null) {
				HttpGetImageAction.fetchImage(this, this.userConfig.avatar, imageView);
			}
			this.videoLayout.setVisibility(View.GONE);
			toggleAvatarBtn.setBackgroundResource(0);
			toggleAvatarBtn.setBackgroundResource(R.drawable.avatar_icon_2);
			//Button speechButton = (Button) findViewById(R.id.speechButton);
			//speechButton.setVisibility(View.VISIBLE);
		} else {
			if (this.userConfig != null && this.userConfig.instanceAvatarId != 0) {
				this.imageView.setVisibility(View.GONE);
				this.videoLayout.setVisibility(View.VISIBLE);
			}
			if(this.disableVideo){
				this.imageView.setVisibility(View.VISIBLE);
				this.videoLayout.setVisibility(View.GONE);
				HttpGetImageAction.fetchImage(this, this.userConfig.avatar, imageView);
			}
			//Button speechButton = (Button) findViewById(R.id.speechButton);
			//speechButton.setVisibility(View.GONE);
			toggleAvatarBtn.setBackgroundResource(0);
			toggleAvatarBtn.setBackgroundResource(R.drawable.avatar_icon);
		}
		switch (index) {
			case 1:
				userMessagesLayout.setVisibility(View.GONE);
				this.usersRecyclerView.setVisibility(View.GONE);
				break;
			case 2:
				menuLayout.setVisibility(View.GONE);
				messageText.setVisibility(View.INVISIBLE);
				this.usersRecyclerView.setVisibility(View.GONE);
				break;
			case 3:
				userMessagesLayout.setVisibility(View.VISIBLE);
				menuLayout.setVisibility(View.VISIBLE);
				messageText.setVisibility(View.VISIBLE);
				LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) userMessagesLayout.getLayoutParams();
				params.weight = 5;
				userMessagesLayout.setLayoutParams(params);
				if (this.usersSize > 2) {
					this.usersRecyclerView.setVisibility(View.VISIBLE);
					this.imageView.setVisibility(View.GONE);
				}
				break;
			case 0:
				userMessagesLayout.setVisibility(View.VISIBLE);
				menuLayout.setVisibility(View.VISIBLE);
				messageText.setVisibility(View.VISIBLE);
				params = (LinearLayout.LayoutParams) userMessagesLayout.getLayoutParams();
				params.weight = 1;
				userMessagesLayout.setLayoutParams(params);
				if (this.usersSize > 2) {
					this.usersRecyclerView.setVisibility(View.VISIBLE);
				}
				break;
		}
	}

	@Override
	public void onResume() {
		if (MainActivity.current == null) {
			finish();
			return;
		}
		if ((MainActivity.instance instanceof ChannelConfig) && MainActivity.instance.id.equals(this.instance.id)) {
			this.instance = (ChannelConfig) MainActivity.instance;
		} else {
			MainActivity.instance = this.instance;
		}
		super.onResume();
		if (this.lastResponse != null && (this.videoView != null) && (this.videoLayout.getVisibility() == View.VISIBLE)) {
			cycleVideo(this.lastResponse);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (this.childActivity.equals("")) {
			if (requestCode == RESULT_SPEECH) {
				if (resultCode == RESULT_OK && null != data) {

					ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
					if (text != null) {
						textView.setText(text.get(0));
						submitChat();
					}
				}
				return;
			}
			return;
		}

		if (resultCode != RESULT_OK) {
			return;
		}
		if (data == null || data.getData() == null) {
			this.childActivity = "";
			return;
		}

		try {
			String file = MainActivity.getFilePathFromURI(this, data.getData());
			MediaConfig config = new MediaConfig();
			config.name = MainActivity.getFileNameFromPath(file);
			config.type = MainActivity.getFileTypeFromPath(file);
			config.instance = this.instance.id;
			if (this.childActivity.equals("sendImage")) {
				HttpAction action = new HttpCreateChannelImageAttachmentAction(this, file, config);
				action.execute().get();
			} else {
				HttpAction action = new HttpCreateChannelFileAttachmentAction(this, file, config);
				action.execute().get();
			}
			this.childActivity = "";
		} catch (Exception exception) {
			this.childActivity = "";
			MainActivity.error(exception.getMessage(), exception, this);
			return;
		}
	}

	public void exitChannel(String message) {
		imageView.setVisibility(View.GONE);
		usersRecyclerView.setVisibility(View.VISIBLE);
	}

	public void closeChat() {
		isClosed = true;
		this.userConfig = null;
	}

	public void setText(String text) {
		EditText edit = (EditText) findViewById(R.id.messageText);
		edit.setText(text);
	}

	public void sendMessage(String text) {
		try {
			this.connection.sendMessage(text);
		} catch (Exception exception) {
			MainActivity.error(exception.getMessage(), exception, this);
			return;
		}
	}

	public void sendFile() {
		ActivityCompat.requestPermissions(this,
				new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
	}

	public void sendImage() {
		ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE },3);
	}

	public void sendFile(MediaConfig media) {
		try {
			String message = "file: " + media.name + " : " + media.type + " : http://"
					+ MainActivity.connection.getCredentials().host + MainActivity.connection.getCredentials().app + "/" + media.file;
			setText(message);
			this.connection.sendMessage(message);
			setText("");
		} catch (Exception exception) {
			MainActivity.error(exception.getMessage(), exception, this);
			return;
		}
	}

	public void whisper(UserConfig user) {
		if (user == null) {
			setText("whisper: ");
		} else {
			setText("whisper: " + user.user + ": ");
		}
	}

	public void boot(UserConfig user) {
		if (user == null) {
			setText("boot: ");
		} else {
			setText("boot: " + user.user);
			this.connection.boot(user.user);
		}
		setText("");
	}

	public void pvt(UserConfig user) {
		if (user == null) {
			setText("private: ");
		} else if(MainActivity.user != null && MainActivity.user.user.equals(user.user)) {
			setText("private: ");
		} else {
			isPrivate = true;
			setText("private: " + user.user);
			this.connection.pvt(user.user);
		}
		setText("");
	}

	/**
	 * Clear the log.
	public void clear(View view) {
		liveChatMessagesList = new ArrayList<>();
		liveChatAdapter.notifyDataSetChanged();
	}*/

	public void menu(View view) {
		PopupMenu popup = new PopupMenu(this, view);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.menu_livechat, popup.getMenu());
		onPrepareOptionsMenu(popup.getMenu());
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				return onOptionsItemSelected(item);
			}
		});
		popup.show();
	}

	public void sendFile(View view) {
		sendFile();
	}

	public void sendImage(View view) {
		sendImage();
	}

	public void exit(View view) {
		this.isPrivate = false;
		//this.hasVideoAvatar = false;

		if (this.stateLayouts != 3) {
			this.stateLayouts = 0;
		}

		resetLayout(stateLayouts);
		this.connection.exit();
		super.exit();
	}

	public void accept(View view) {
		this.isPrivate = true;
		this.connection.accept();
	}

	public void toggleSound() {
		this.sound = !this.sound;
		Button button = (Button) findViewById(R.id.soundButton);
		if (this.sound) {
			button.setBackgroundResource(R.drawable.sound);
		} else {
			button.setBackgroundResource(R.drawable.mute);
		}
	}

	public void toggleSound(View view){toggleSound();}

	public void toggleAvatar() {
		if (stateLayouts != 3) {
			stateLayouts = 3;
			resetLayout(stateLayouts);
		} else {
			stateLayouts = 0;
			resetLayout(stateLayouts);
		}
	}

	public void toggleAvatar(View view) {
		toggleAvatar();
	}

	@Override
	public void onDestroy() {
		this.closing = true;
		if (this.connection != null) {
			try {
				this.connection.disconnect();
			} catch (Exception exception) { }
		}

		if (this.tts != null) {
			this.tts.stop();
			this.tts.shutdown();
		}
		if (this.chimePlayer != null) {
			this.chimePlayer.stop();
			this.chimePlayer.release();
		}

		if (this.speechPlayer != null) {
			try {
				this.speechPlayer.stop();
			} catch (Exception ignore) {}
			try {
				this.speechPlayer.release();
			} catch (Exception ignore) {}
		}
		super.onDestroy();
	}

	public void chime() {
		if (this.chimePlayer == null) {
			this.chimePlayer = MediaPlayer.create(this, R.raw.chime);
		}
		this.chimePlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				beginListening();
			}
		});
		stopListening();
		this.chimePlayer.start();
	}

	public void viewUser(UserConfig user) {
		if (user == null) {
			MainActivity.showMessage("Select user", this);
			return;
		}
		MainActivity.viewUser = user;
		Intent intent = new Intent(this, ViewUserActivity.class);
		startActivity(intent);
	}

	public void toggleKeepAlive() {
		this.connection.setKeepAlive(!this.connection.isKeepAlive());
	}

	public void clearLog() {
		this.html = "";
		liveChatMessagesList = null;
		liveChatMessagesList = new ArrayList<>();
		liveChatAdapter = new LiveChatMessagesAdapter(LiveChatActivity.this, liveChatMessagesList);
		userMessagesListView.setAdapter(liveChatAdapter);
	}

	public void submitChat() {
		if (lastTime != 0 && (System.currentTimeMillis() - lastTime < 500)) {
			EditText editText = findViewById(R.id.messageText);
			editText.setText("");
			return;
		}
		lastTime = System.currentTimeMillis();
		try {
			EditText v = findViewById(R.id.messageText);
			String input = v.getText().toString().trim();
			if (input.equals("")) {
				return;
			}
			this.connection.sendMessage(input);
			v.setText("");

			//Check the volume
			AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			if (volume <= 1 && volumeChecked) {
				Toast.makeText(this, "Please check 'Media' volume", Toast.LENGTH_LONG).show();
				volumeChecked = false;
			}

			//stop letting the mic on.
			//stopListening();
			//its Important for "sleep" "scream" ...etc commands.
			//this will turn off the mic
			//MainActivity.listenInBackground = false;
		} catch (Exception exception) {
			MainActivity.error(exception.getMessage(), exception, this);
			return;
		}
	}

	@Override
	public void response(final ChatResponse response) {

		if (speechPlayer != null || tts != null) {
			try {
				tts.stop();
				speechPlayer.pause();
			} catch(Exception ignore) {Log.e("RESPONSE","Error: " + ignore.getMessage());}
		}
		//needs when calling "sleep" or the its not going to let the mic off
		//also to stop the mic until the bot finish the sentence
		try {
			stopListening();
			this.response = response;
			final String text = response.message;
			boolean talk = (text.trim().length() > 0) && (MainActivity.deviceVoice || (this.response.speech != null && this.response.speech.length() > 0));
			if (this.sound && talk) {
				if (!this.disableVideo && !videoError && this.response.isVideo() && this.response.isVideoTalk()) {
					videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
						@Override
						public void onPrepared(MediaPlayer mp) {
							try {
								mp.setLooping(true);
								if (!MainActivity.deviceVoice) {
									// Voice audio
									speechPlayer = playAudio(response.speech, false, false, false);
									speechPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
										@Override
										public void onCompletion(MediaPlayer mp) {
											mp.release();
											videoView.post(new Runnable() {
												public void run() {
													cycleVideo(response);
												}
											});
											runOnUiThread(new Runnable() {
												public void run() {
													if(!music){
														beginListening();
													}
												}
											});
											synchronized (this) {
												if (responseQueue.size() > 0) {
													HttpUserAvatarMessageAction next = responseQueue.remove(0);
													next.execute();
												}else{
													responding = false;
												}
											}
										}
									});
									speechPlayer.start();
								} else {
									HashMap<String, String> params = new HashMap<String, String>();
									params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id");
									tts.speak(Utils.stripTags(text), TextToSpeech.QUEUE_FLUSH, params);
									responding = false;
								}
							} catch (Exception exception) {
								Log.wtf(exception.getMessage(), exception);
							}
						}
					});
					this.lastVideoUrl = this.response.avatarTalk;
					playVideo(this.response.avatarTalk, false);
				} else if (talk) {
					if (!MainActivity.deviceVoice) {
						// Voice audio
						speechPlayer = playAudio(this.response.speech, false, false, false);
						speechPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
							@Override
							public void onCompletion(MediaPlayer mp) {
								mp.release();
								runOnUiThread(new Runnable() {
									public void run() {
										if(!music){
											beginListening();
										}
									}
								});
								synchronized (this) {
									if (responseQueue.size() > 0) {
										HttpUserAvatarMessageAction next = responseQueue.remove(0);
										next.execute();
									}else{
										responding = false;
									}
								}
							}
						});
						speechPlayer.start();
					} else {
						HashMap<String, String> params = new HashMap<String, String>();
						params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id");

						this.tts.speak(Utils.stripTags(text), TextToSpeech.QUEUE_FLUSH, params);
						responding = false;
					}
				}
			} else if (talk && (!this.disableVideo && !videoError && this.response.isVideo() && this.response.avatarTalk != null)) {
				beginListening();
				videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer mp) {
						mp.setLooping(false);
					}
				});
				videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						videoView.setOnCompletionListener(null);
						cycleVideo(response);
						synchronized (this) {
							if (responseQueue.size() > 0) {
								HttpUserAvatarMessageAction next = responseQueue.remove(0);
								next.execute();
							}else{
								responding = false;
							}
						}
					}
				});
				playVideo(this.response.avatarTalk, false);
				runOnUiThread(new Runnable() {
					public void run() {
						beginListening();
					}
				});
			} else {
				responding = false;
				runOnUiThread(new Runnable() {
					public void run() {
						beginListening();
					}
				});
			}
		} catch (Exception exception) {
			Log.wtf(exception.getMessage(), exception);
		}
		if (micLastStat) {
			MainActivity.listenInBackground = true;
		}
	}

	public void response(String text) {
		if (this.closing) {
			return;
		}
		String user = "";
		String message = text;

		int index = text.indexOf(':');
		if (index != -1) {
			user = text.substring(0, index);
			message = text.substring(index + 2, text.length());
		}
		if (user.equals("Online")) {
			HttpAction action = new HttpGetLiveChatUsersAction(this, message);
			action.execute();
			return;
		}

		if (user.equals("Media")) {
			return;
		}
		if (user.equals("Channel")) {
			return;
		}
		message = Utils.linkHTML(message);
		if (message.contains("<") && message.contains(">")) {
			message = linkPostbacks(message);
		}
		this.html = this.html + "<b>" + user + "</b> <span style='font-size:10px;color:gray'>"
				+ Utils.displayTime(new Date()) + "</span><br/>"
				+ Utils.linkHTML(message) + "<br/>";

		Set<String> set =  getUsersMap().keySet();
		if (set.isEmpty() && "Nick".equals(user)) {
			this.nick = message;
			return;
		}

		final ChatConfig chatConfig = new ChatConfig();
		chatConfig.user = user;
		chatConfig.message = message;
		this.chatConfigUser = chatConfig.user;

		if (this.userConfig != null && message.equals(this.userConfig.user + " has disconnected.")) {
			isPrivate = false;
		}
		userMessagesListView.post(new Runnable() {
			@Override
			public void run() {
				liveChatMessagesList.add(chatConfig);
				((LiveChatMessagesAdapter)userMessagesListView.getAdapter()).notifyDataSetChanged();
				userMessagesListView.invalidateViews();
				if (userMessagesListView.getCount() > 2) {
					userMessagesListView.setSelection(userMessagesListView.getCount() - 2);
				}
			}
		});
		userMessagesListView.postDelayed(new Runnable() {
			@Override
			public void run() {
				userMessagesListView.setSelection(userMessagesListView.getAdapter().getCount() - 1);
			}
		}, 500);
		userMessagesListView.postDelayed(new Runnable() {
			@Override
			public void run() {
				userMessagesListView.setSelection(userMessagesListView.getAdapter().getCount() - 1);
			}
		}, 1000);

		if (!"Info".equals(user) && !"Error".equals(user) && !message.equals("")) {

			initialMessage = message;
		} else {
			initialMessage = "";
			return;
		}

//		if ((System.currentTimeMillis() - startTime) < (1000 * 5)) {
//			return;
//		}

		boolean speak = this.sound;
		boolean chime = this.sound;
		if (user.equals("Error") || user.equals("Info")) {
			speak = false;
		} else {
			if (user.equals(this.nick)) {
				speak = false;
				chime = false;
				beginListening();
			}
			else{
				if (this.usersMap.get(user) != null) {
					this.userConfig = this.usersMap.get(user);
				} else if (this.usersMap.get(user.substring(1))!= null) {
					this.userConfig = this.usersMap.get(user.substring(1));
				}
			}
			if (this.userConfig != null) {

				if (!user.equals(this.nick)) {
					String myAvatar = this.userConfig.avatar;
					if ((this.stateLayouts == 3)) {
						HttpGetImageAction.fetchImage(this, myAvatar, imageView);
					} else {
						/*if ((System.currentTimeMillis() - this.lastChat) < 1000) {
								return;
						}*/
						String trimmedMessage = message;
						index = message.indexOf(':');
						if (index != -1) {
							String name = message.substring(0, index);
							if(set.contains(name)){
								trimmedMessage = trimmedMessage.substring(index + 2);
							}

						}

						UserAvatarMessage config = new UserAvatarMessage();
						config.message = Utils.stripTags(trimmedMessage);

						if (this.userConfig.instanceAvatarId != 0) {
							config.userAvatar = this.chatConfigUser;//String.valueOf(this.userConfig.instanceAvatarId);
						} else {
							config.userAvatar = this.userConfig.avatar;
						}
						if(this.disableVideo){
							config.userAvatar = myAvatar;
						}

						config.format = MainActivity.webm ? "webm" : "mp4";
						config.speak = !MainActivity.deviceVoice && this.sound;
						config.hd = MainActivity.hd;
						//config.voice = this.userConfig.voice;

						HttpUserAvatarMessageAction action = new HttpUserAvatarMessageAction(LiveChatActivity.this, config);
						synchronized (this) {
							if(this.responseQueue.size() < 4) {
								if (this.responding) {
									this.responseQueue.add(action);
								} else {
									this.responding = true;
									action.execute();
								}
							}
						}
						this.lastChat = System.currentTimeMillis();
						speak = false;
						chime = false;
						final String toast = message;
						if (this.stateLayouts == 1 || this.stateLayouts == 2) {
							userMessagesListView.postDelayed(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(LiveChatActivity.this, Utils.stripTags(toast), Toast.LENGTH_SHORT).show();
								}
							}, 100);
						}
					}
				}
			}
		}
		//if (speak) {
		//	this.tts.speak(message, TextToSpeech.QUEUE_FLUSH, null);
		//}
		if (chime) {
			if (this.chimePlayer != null && this.chimePlayer.isPlaying()) {
				return;
			}
			chime();
		}
	}
    
    public void setUsers(List<UserConfig> users) {
		this.usersSize = users.size();
		if (users.size() == 2) {
    		this.isPrivate = true;
		} else {
			this.isPrivate = false;
		}
		UserConfig userConfig = null;

		for (UserConfig user : users) {
			this.usersMap.put(user.user, user);

			if (this.isPrivate && MainActivity.user != null && !user.user.equals(MainActivity.user.user)) {
				userConfig = user;
			}
			if (this.instance != null && "OneOnOne".equals(this.instance.type) && MainActivity.user == null) {
				if (users.size() >= 2) {
					this.userConfig = users.get(1);
				}
			}
		}
		if (userConfig != null) {
			this.userConfig = userConfig;
		} else {
			this.userConfig = users.get(1);
		}
		this.usersRecyclerView.setVisibility(View.GONE);
		this.hasVideoAvatar = true;
		this.toggleAvatarBtn.setVisibility(View.VISIBLE);
		UserAvatarMessage config = new UserAvatarMessage();
		if (!this.initialMessage.equals("")) {
			config.message = initialMessage;
			this.initialMessage = "";
		} else {
			config.message = "";
		}
		if (this.userConfig != null) {
			if (this.stateLayouts != 3) {
				if (this.userConfig.instanceAvatarId != 0 && !this.chatConfigUser.equals("Info")) {
					videoLayout.setVisibility(View.VISIBLE);
					config.userAvatar = this.chatConfigUser;
				} else {
					config.userAvatar = this.userConfig.avatar;
				}
				//config.voice = this.userConfig.voice;
				config.format = MainActivity.webm ? "webm" : "mp4";
				config.speak = !MainActivity.deviceVoice && this.sound;
				config.hd = MainActivity.hd;
				HttpUserAvatarMessageAction action = new HttpUserAvatarMessageAction(LiveChatActivity.this, config);
				action.execute();
			}
		}
		if (this.userConfig != null && this.userConfig.avatar != null) {
			imageView.setVisibility(View.VISIBLE);
			HttpGetImageAction.fetchImage(this, this.userConfig.avatar, imageView);
		}
		resetLayout(this.stateLayouts);

		if (!this.isPrivate) {
			this.userConfig = null;

			//missing instance type?
			if (MainActivity.instance != null && !MainActivity.instance.isAdmin && this.instance.type != null && this.instance.type.equals("OneOnOne")){
				usersRecyclerView.setVisibility(View.GONE);
			} else {
				usersRecyclerView.setVisibility(View.VISIBLE);
			}
		}

		usersRecyclerView.setHasFixedSize(true);
		usersLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
		usersRecyclerViewAdapter = new UsersRecyclerViewAdapter(this, users);
		usersRecyclerView.setLayoutManager(usersLayoutManager);
		usersRecyclerView.setAdapter(usersRecyclerViewAdapter);
		liveChatAdapter.notifyDataSetChanged();
		usersRecyclerViewAdapter.setOnItemClickListener(LiveChatActivity.this);
		usersRecyclerViewAdapter.setOnItemTouchListener(LiveChatActivity.this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu_livechat, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		WebMediumConfig instance = this.instance;
		boolean isAdmin = (MainActivity.user != null) && instance.isAdmin;
		if (!isAdmin) {
			menu.findItem(R.id.menuBoot).setVisible(false);
		}
		if (this.sound) {
			menu.findItem(R.id.menuSound).setChecked(true);
		}
		else{
			menu.findItem(R.id.menuSound).setChecked(false);
		}
		if (this.hasVideoAvatar) {
			menu.findItem(R.id.menuAvatar).setVisible(true);
			if (this.stateLayouts != 3) {
				menu.findItem(R.id.menuAvatar).setChecked(true);
			} else {
				menu.findItem(R.id.menuAvatar).setChecked(false);
			}
		} else {
			menu.findItem(R.id.menuAvatar).setVisible(false);
		}
		if (this.keepAlive) {
			menu.findItem(R.id.menuKeepAlive).setChecked(true);
		}
		if(this.disableVideo){
			menu.findItem(R.id.disableVideo).setChecked(true);
		} else {
			menu.findItem(R.id.disableVideo).setChecked(false);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		UserConfig user = this.userConfig;
		switch (item.getItemId())
		{
			case R.id.menuSendImage:
				sendImage();
				return true;
			case R.id.menuSendFile:
				sendFile();
				return true;
			case R.id.menuPing:
				this.connection.ping();
				return true;
			case R.id.menuWhisper:
				whisper(user);
				return true;
			case R.id.menuPrivate:
				pvt(user);
				return true;
			case R.id.menuAccept:
				isPrivate = true;
				this.connection.accept();
				return true;
			case R.id.menuBoot:
				boot(user);
				return true;
			case R.id.menuClear:
				clearLog();
				return true;
			case R.id.menuExit:
				isPrivate = false;
				this.connection.exit();
				return true;
			case R.id.menuViewUser:
				viewUser(user);
				return true;
			case R.id.menuAvatar:
				toggleAvatar();
				return true;
			case R.id.menuSound:
				toggleSound();
				return true;
			case R.id.menuKeepAlive:
				this.keepAlive = !keepAlive;
				toggleKeepAlive();
				return true;
			case R.id.disableVideo:
				this.disableVideo = !disableVideo;
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onItemClick(int position) {
		UserConfig userConfig = usersRecyclerViewAdapter.getItem(position);
		this.userConfig = userConfig;
		if(prevUserListIndex >= 0) {
			UsersRecyclerViewAdapter.UsersViewHolder usersViewHolder = (UsersRecyclerViewAdapter.UsersViewHolder) usersRecyclerView.findViewHolderForAdapterPosition(prevUserListIndex);
			if(usersViewHolder != null) {
				usersViewHolder.userCardView.setBackgroundResource(R.drawable.user_card_view);
			}
		}
		UsersRecyclerViewAdapter.UsersViewHolder usersViewHolder = (UsersRecyclerViewAdapter.UsersViewHolder) usersRecyclerView.findViewHolderForAdapterPosition(position);
		if(usersViewHolder != null) {
			usersViewHolder.userCardView.setBackgroundResource(R.drawable.users_recycler_list_select);
		}
		prevUserListIndex = position;
	}

	@Override
	public void onItemDoubleTap(int position) {
		UserConfig userConfig = usersRecyclerViewAdapter.getItem(position);
		this.userConfig = userConfig;
		if(MainActivity.user != null && userConfig != null && MainActivity.user.user.equals(userConfig.user)) {
			return;
		}
		if(userConfig != null && userConfig.user.contains("anonymous")) {
			return;
		}
		pvt(userConfig);
	}

	@SuppressLint("Override")
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		switch (requestCode) {
			case 2: {
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Intent upload = new Intent(Intent.ACTION_GET_CONTENT);
					upload.setType("*/*");
					this.childActivity = "sendFile";
					try {
						startActivityForResult(upload, 2);
					} catch (Exception notFound) {
						this.childActivity = "sendFile";
						upload = new Intent(Intent.ACTION_GET_CONTENT);
						upload.setType("file/*");
						startActivityForResult(upload, 2);
					}
				} else {
					Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
				}
				return;
			}

			case 3:{
				if (grantResults.length > 0
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					try {
						Intent upload = new Intent(Intent.ACTION_GET_CONTENT);
						upload.setType("image/*");
						this.childActivity = "sendImage";
						startActivityForResult(upload, 3);
					} catch (Exception exception) {
						MainActivity.error(exception.getMessage(), exception, this);
						return;
					}
				} else {
					Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
				}
				return;
			}

		}
	}

}
