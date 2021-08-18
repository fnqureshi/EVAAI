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

import java.io.FileInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import org.botlibre.sdk.activity.MainActivity.LaunchType;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpChatAction;
import org.botlibre.sdk.activity.actions.HttpCreateBotFileAttachmentAction;
import org.botlibre.sdk.activity.actions.HttpCreateBotImageAttachmentAction;
import org.botlibre.sdk.activity.actions.HttpCreateChannelFileAttachmentAction;
import org.botlibre.sdk.activity.actions.HttpCreateChannelImageAttachmentAction;
import org.botlibre.sdk.activity.actions.HttpFetchChatAvatarAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetInstancesAction;
import org.botlibre.sdk.activity.actions.HttpGetVideoAction;
import org.botlibre.sdk.activity.actions.HttpGetVoiceAction;
import org.botlibre.sdk.activity.avatar.BrowseAvatarActivity;
import org.botlibre.sdk.config.AvatarConfig;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.ChatConfig;
import org.botlibre.sdk.config.ChatResponse;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.MediaConfig;
import org.botlibre.sdk.config.VoiceConfig;
import org.botlibre.sdk.util.Command;
import org.botlibre.sdk.util.CommandProcessor;
import org.botlibre.sdk.util.TextStream;
import org.botlibre.sdk.util.Utils;
import org.json.JSONObject;
import org.w3c.dom.Element;

import com.facebook.ads.AdSize;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import io.evaai.R;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.PopupMenu;

/**
 * Activity for chatting with a bot.
 * To launch this activity from your app you can use the HttpFetchAction passing the bot id or name as a config, and launch=true.
 */
@SuppressWarnings("deprecation")
public class ChatActivity extends AbstractChatActivity implements CommandProcessor {
	protected static final int CAPTURE_IMAGE = 2;
	protected static final int RESULT_SCAN = 3;
	protected static final int CAPTURE_VIDEO = 4;

	private InterstitialAd mInterstitialAd;
	
	private LinearLayout menuBarLayout;
	private LinearLayout chatInputLayout;
	private LinearLayout chatToolBar;
	private LinearLayout chatListLayout;

	protected String childActivity = "";

	protected Menu menu;

	public MediaPlayer backgroundAudioPlayer;

	public void superOnCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	private com.facebook.ads.AdView adView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (MainActivity.current == null) {
			finish();
			return;
		}
		if (MainActivity.instance == null || !(MainActivity.instance instanceof InstanceConfig)) {
			finish();
			return;
		}
		setContentView(R.layout.activity_chat);
		
		webAppInterface = new WebAppInterface(this);
			
		// Remove flag button if a single bot app.
		if (MainActivity.launchType == LaunchType.Bot) {
			//findViewById(R.id.flagButton).setVisibility(View.GONE);
		}
		
		//permission required.
		ActivityCompat.requestPermissions(ChatActivity.this, new String[] { Manifest.permission.RECORD_AUDIO }, 1);
		
		//set/Save the current volume from the device.
		setStreamVolume();
		//Music Volume is Enabled.
		muteMicBeep(false);
		
		//For "scream" issue
		micLastStat = MainActivity.listenInBackground;
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		this.instance = (InstanceConfig)MainActivity.instance;

		setTitle(this.instance.name);
		((TextView) findViewById(R.id.title)).setText(this.instance.name);
		HttpGetImageAction.fetchImage(this, this.instance.avatar, findViewById(R.id.icon));
		ttsInit = false;
		tts = new TextToSpeech(this, this);
		
		if (!MainActivity.handsFreeSpeech){
			setMicIcon(false, false);
		} else if (!MainActivity.listenInBackground){
			setMicIcon(false, false);
		}
		
		//Last time will be saved for the MIC.
		if (MainActivity.listenInBackground && MainActivity.handsFreeSpeech) {
			microphoneThread(thread);
		}
		
		speech = SpeechRecognizer.createSpeechRecognizer(this);
		speech.setRecognitionListener(this);
		chatListLayout = findViewById(R.id.chatListLayout);
		menuBarLayout = (LinearLayout) findViewById(R.id.menuBarLayout);
		chatInputLayout = (LinearLayout) findViewById(R.id.chatInputLayout);
		chatToolBar = (LinearLayout) findViewById(R.id.chatToolBar);
		
		videoView = (VideoView)findViewById(R.id.videoView);
		resetVideoErrorListener();
		videoError = false;
		
		imageView = (ImageView)findViewById(R.id.imageView);
		videoLayout = findViewById(R.id.videoLayout);
		
		textView = (EditText) findViewById(R.id.messageText);
		textView.setOnEditorActionListener(new OnEditorActionListener() {			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				submitChat();
				return false;
			}
		});

		Spinner emoteSpin = (Spinner) findViewById(R.id.emoteSpin);
		emoteSpin.setAdapter(new EmoteSpinAdapter(this, R.layout.emote_list, Arrays.asList(EmotionalState.values())));

		/*Spinner actionSpin = (Spinner) findViewById(R.id.actionSpin);
		actionSpin.setAdapter(new ActionSpinAdapter(this, R.layout.action_list, Arrays.asList(MainActivity.actions)));
		actionSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
				if (position <= 0 || position > MainActivity.actions.length) {
					return;
				}
				String action = MainActivity.actions[position];
				textView.setText(action);
				submitChat();
			}
			@Override
			public void onNothingSelected(AdapterView<?> adapter) {
			}
		});*/

		ListView list = (ListView) findViewById(R.id.chatList);
		list.setAdapter(new ChatListAdapter(this, R.layout.chat_list, this.messages));
		list.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

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
						Toast t = Toast.makeText(getApplicationContext(),
								"Your device doesn't support Speech to Text",
								Toast.LENGTH_SHORT);
						t.show();
					}
				}
			}
		});

		imageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				stateLayouts++;
				if (stateLayouts == 3) {
					stateLayouts = 0;
				}
				switch(stateLayouts) {
					case 0:
						chatInputLayout.setVisibility(View.VISIBLE);
						menuBarLayout.setVisibility(View.VISIBLE);
						textView.setVisibility(View.VISIBLE);
						chatListLayout.setVisibility(View.VISIBLE);
						findViewById(R.id.chatFillLayout).setVisibility(View.GONE);
						chatToolBar.setVisibility(View.VISIBLE);
						break;
					case 1:
						chatListLayout.setVisibility(View.GONE);
						findViewById(R.id.chatFillLayout).setVisibility(View.VISIBLE);
						break;
					case 2:
						menuBarLayout.setVisibility(View.GONE);
						textView.setVisibility(View.INVISIBLE);
						chatToolBar.setVisibility(View.GONE);
						break;
				}
			}
		});
		
		videoLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				stateLayouts++;
				if (stateLayouts == 3) {
					stateLayouts = 0;
				}
				switch(stateLayouts) {
					case 0:
						chatInputLayout.setVisibility(View.VISIBLE);
						menuBarLayout.setVisibility(View.VISIBLE);
						textView.setVisibility(View.VISIBLE);
						chatListLayout.setVisibility(View.VISIBLE);
						chatToolBar.setVisibility(View.VISIBLE);
						break;
					case 1:
						chatListLayout.setVisibility(View.GONE);
						break;
					case 2:
						menuBarLayout.setVisibility(View.GONE);
						textView.setVisibility(View.INVISIBLE);
						chatToolBar.setVisibility(View.GONE);
						break;
				}
			}
		});
		
		GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDoubleTapEvent(MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					boolean isVideo = !MainActivity.disableVideo && !videoError && response != null && response.isVideo();
					View avatarLayout = findViewById(R.id.avatarLayout);
					View imageView = findViewById(R.id.imageView);
					View videoLayout = findViewById(R.id.videoLayout);
					LinearLayout chatListLayout = findViewById(R.id.chatListLayout);
					ViewGroup.LayoutParams params = chatListLayout.getLayoutParams();
					if (avatarLayout.getVisibility() == View.VISIBLE) {
						avatarLayout.setVisibility(View.GONE);
					} else {
						avatarLayout.setVisibility(View.VISIBLE);
					}
					if (imageView.getVisibility() == View.VISIBLE) {
						imageView.setVisibility(View.GONE);
					} else if (!isVideo) {
						imageView.setVisibility(View.VISIBLE);
					}
					if (videoLayout.getVisibility() == View.VISIBLE) {
						videoLayout.setVisibility(View.GONE);
					} else if (isVideo) {
						videoLayout.setVisibility(View.VISIBLE);
					}
					list.postDelayed(new Runnable() {
						@Override
						public void run() {
							list.setSelection(list.getAdapter().getCount() - 1);
						}
					}, 500);
					return true;
				}
				return false;
			}
		};
		final GestureDetector detector = new GestureDetector(this, listener);		
		findViewById(R.id.chatList).setOnTouchListener(new View.OnTouchListener() {			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return detector.onTouchEvent(event);
			}
		});

		String currentBotId = this.instance.id.trim();
		SharedPreferences cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE);
		String previousBotId = cookies.getString("botId", "").trim();
		if (!previousBotId.equals("")) {
			if (!currentBotId.equals(previousBotId)) {
				SharedPreferences.Editor cookieEditor = cookies.edit();
				cookieEditor.remove("voiceConfigXML");
				cookieEditor.remove("customAvatar");
				cookieEditor.remove("avatarID");
				cookieEditor.commit();

				if (MainActivity.customVoice) {
					MainActivity.voice = new VoiceConfig();
					MainActivity.customVoice = false;

					HttpAction action = new HttpGetVoiceAction(this, (InstanceConfig) MainActivity.instance.credentials());
					action.execute();
				}
			}
		}
		SharedPreferences.Editor cookieEditor = cookies.edit();
		cookieEditor.putString("botId", currentBotId);
		cookieEditor.commit();

		customAvatarImage = cookies.getString("customAvatar", "");
		String avatarId = cookies.getString("avatarID", "");
		if (!avatarId.equals("")) {
			this.avatar = new AvatarConfig();
			this.avatar.id = avatarId;
			HttpFetchChatAvatarAction action = new HttpFetchChatAvatarAction(this, this.avatar);
			action.execute();
		} else {
			if (!customAvatarImage.equals("")) {
				MainActivity.disableVideo = true;
				HttpGetImageAction.fetchImage(this, customAvatarImage, imageView);
				instance.avatar = customAvatarImage;
				this.avatar = null;
			}
			HttpGetImageAction.fetchImage(this, instance.avatar, this.imageView);
		}

		final ChatConfig config = new ChatConfig();
		config.instance = instance.id;
		config.avatar = avatarId;

		if (MainActivity.translate && MainActivity.voice != null) {
			config.language = MainActivity.voice.language;
		}
		if (MainActivity.disableVideo) {
			config.avatarFormat = "image";
		} else {
			config.avatarFormat = MainActivity.webm ? "webm" : "mp4";
		}

		config.avatarHD = MainActivity.hd;
		config.speak = !MainActivity.deviceVoice;
		if (MainActivity.customVoice && MainActivity.voice != null) {
			config.voice = MainActivity.voice.voice;
			config.mod = MainActivity.voice.mod;
		}

		// This is required because of a bug in TextToSpeech that prevents onInit being called if an AsynchTask is called...
		Thread thread1 = new Thread() {
			public void run() {
				// Used to add Facebook ads test device.
				//try {
				//	System.out.println(AdvertisingIdClient.getAdvertisingIdInfo(ChatActivity.this));
				//} catch (Exception exception) {
				//	Log.wtf("getAdvertisingIdInfo", exception);
				//}

				for (int count = 0; count < 5; count++) {
					if (ttsInit) {
						break;
					}
					try {
						Thread.sleep(1000);
					} catch (Exception exception) {}
				}
				HttpAction action = new HttpChatAction(ChatActivity.this, config);
				action.execute();
			}
		};
		thread1.start();
	}

	public void resetChat(View view) {
		ChatConfig config = new ChatConfig();
		config.instance = instance.id;
		if (this.avatar != null) {
			config.avatar = this.avatar.id;
		}
		if (MainActivity.translate && MainActivity.voice != null) {
			config.language = MainActivity.voice.language;
		}
		if (MainActivity.disableVideo) {
			config.avatarFormat = "image";
		} else {
			config.avatarFormat = MainActivity.webm ? "webm" : "mp4";
		}
		config.avatarHD = MainActivity.hd;
		config.speak = !MainActivity.deviceVoice;
		HttpAction action = new HttpChatAction(ChatActivity.this, config);
		action.execute();

		EditText v = (EditText) findViewById(R.id.messageText);
		v.setText("");
		this.messages.clear();
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				ListView list = (ListView) findViewById(R.id.chatList);
				((ChatListAdapter)list.getAdapter()).notifyDataSetChanged();
				list.invalidateViews();
			}

		});
	}

	public void uploadFile(View view) {
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
			EditText view = (EditText) findViewById(R.id.messageText);
			view.setText(message);
			this.submitChat();
		} catch (Exception exception) {
			MainActivity.error(exception.getMessage(), exception, this);
			return;
		}
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
				HttpAction action = new HttpCreateBotImageAttachmentAction(this, file, config);
				action.execute().get();
			} else {
				HttpAction action = new HttpCreateBotFileAttachmentAction(this, file, config);
				action.execute().get();
			}
			this.childActivity = "";
		} catch (Exception exception) {
			this.childActivity = "";
			MainActivity.error(exception.getMessage(), exception, this);
			return;
		}
	}

	public void flagResponse() {
        if (MainActivity.user == null) {
        	MainActivity.showMessage("You must sign in to flag a response", this);
        	return;
        }
        final EditText text = new EditText(this);
        MainActivity.prompt("Enter reason for flagging response as offensive", this, text, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            if (instance == null) {
	            	return;
	            }
	    		ChatConfig config = new ChatConfig();
	    		config.instance = instance.id;
	    		config.conversation = MainActivity.conversation;
	    		config.speak = !MainActivity.deviceVoice;
				if (avatar != null) {
					config.avatar = avatar.id;
				}
	    		if (MainActivity.translate && MainActivity.voice != null) {
	    			config.language = MainActivity.voice.language;
	    		}
	    		if (MainActivity.disableVideo) {
	    			config.avatarFormat = "image";
	    		} else {
	    			config.avatarFormat = MainActivity.webm ? "webm" : "mp4";
	    		}
	    		config.avatarHD = MainActivity.hd;
	    		
	    		config.message = text.getText().toString().trim();
	    		if (config.message.equals("")) {
	    			return;
	    		}
	    		messages.add(config);
	    		runOnUiThread(new Runnable(){
	    			@Override
	    			public void run() {
	    				ListView list = (ListView) findViewById(R.id.chatList);
	    				((ChatListAdapter)list.getAdapter()).notifyDataSetChanged();
	    				list.invalidateViews();
	    			}
	    			
	    		});
	    		
	    		config.offensive = true;

	    		Spinner emoteSpin = (Spinner) findViewById(R.id.emoteSpin);
	    		config.emote = emoteSpin.getSelectedItem().toString();
	    		
	    		HttpChatAction action = new HttpChatAction(ChatActivity.this, config);
	    		action.execute();

	    		EditText v = (EditText) findViewById(R.id.messageText);
	    		v.setText("");
	    		emoteSpin.setSelection(0);
	    		resetToolbar();
	        }
        });
	}

	public void submitCorrection() {
        final EditText text = new EditText(this);
        MainActivity.prompt("Enter correction to the bot's response (what it should have said)", this, text, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            if (instance == null) {
	            	return;
	            }
	            
	    		ChatConfig config = new ChatConfig();
	    		config.instance = instance.id;
	    		config.conversation = MainActivity.conversation;
	    		config.speak = !MainActivity.deviceVoice;
				if (avatar != null) {
					config.avatar = avatar.id;
				}
	    		if (MainActivity.disableVideo) {
	    			config.avatarFormat = "image";
	    		} else {
	    			config.avatarFormat = MainActivity.webm ? "webm" : "mp4";
	    		}
	    		config.avatarHD = MainActivity.hd;
	    		
	    		config.message = text.getText().toString().trim();
	    		if (config.message.equals("")) {
	    			return;
	    		}
	    		messages.add(config);
	    		runOnUiThread(new Runnable(){
	    			@Override
	    			public void run() {
	    				ListView list = (ListView) findViewById(R.id.chatList);
	    				((ChatListAdapter)list.getAdapter()).notifyDataSetChanged();
	    				list.invalidateViews();
	    			}
	    			
	    		});
	    		
	    		config.correction = true;

	    		Spinner emoteSpin = (Spinner) findViewById(R.id.emoteSpin);
	    		config.emote = emoteSpin.getSelectedItem().toString();
	    		
	    		HttpChatAction action = new HttpChatAction(ChatActivity.this, config);
	    		action.execute();

	    		EditText v = (EditText) findViewById(R.id.messageText);
	    		v.setText("");
	    		emoteSpin.setSelection(0);
	    		resetToolbar();
	    		
	        }
        });
	}

	public void submitChat() {
		if (lastTime != 0 && (System.currentTimeMillis() - lastTime < 500)) {
			EditText editText = findViewById(R.id.messageText);
			editText.setText("");
			return;
		}
		lastTime = System.currentTimeMillis();
		ChatConfig config = new ChatConfig();
		config.instance = this.instance.id;
		config.conversation = MainActivity.conversation;
		config.speak = !MainActivity.deviceVoice;
		if (this.avatar != null) {
			config.avatar = this.avatar.id;
		} else {
			config.avatar = this.instance.avatar;
		}
		if (MainActivity.customVoice && MainActivity.voice != null) {
			config.voice = MainActivity.voice.voice;
			config.mod = MainActivity.voice.mod;
		}
		config.speak = !MainActivity.deviceVoice;
		if (MainActivity.translate && MainActivity.voice != null) {
			config.language = MainActivity.voice.language;
		}
		if (MainActivity.disableVideo) {
			config.avatarFormat = "image";
		} else {
			config.avatarFormat = MainActivity.webm ? "webm" : "mp4";
		}
		config.avatarHD = MainActivity.hd;

		EditText editText = findViewById(R.id.messageText);
		config.message = editText.getText().toString().trim();
		if (config.message.equals("")) {
			return;
		}
		if (MainActivity.PROFANITY_FILTER) {
			if (Utils.checkProfanity(config.message, Utils.EVERYONE)) {
				MainActivity.error("This app does not allow profanity, offensive, or sexual language", null, this);
				editText.setText("");
				return;
			}
		}
		this.messages.add(config);
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				ListView list = findViewById(R.id.chatList);
				((ChatListAdapter)list.getAdapter()).notifyDataSetChanged();
				list.invalidateViews();
			}
			
		});
		

		Spinner emoteSpin = findViewById(R.id.emoteSpin);
		config.emote = emoteSpin.getSelectedItem().toString();

		HttpChatAction action = new HttpChatAction(ChatActivity.this, config);
		action.execute();

		editText.setText("");
		emoteSpin.setSelection(0);
		resetToolbar();
	
		//Check the volume
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		if (volume <= 1 && volumeChecked) {
			Toast.makeText(this, "Please check 'Media' volume", Toast.LENGTH_LONG).show();
			volumeChecked = false;
		}
		
		//stop letting the mic on.
		stopListening();
		//its Important for "sleep" "scream" ...etc commands.
		//this will turn off the mic
		MainActivity.listenInBackground = false;
	}
	
	public void changeAvatar() {
		MainActivity.changeAvatar = true;
		MainActivity.browsing = true;
		BrowseConfig config = new BrowseConfig();
		config.type = "Avatar";
		config.typeFilter = "Featured";
		config.contentRating = MainActivity.contentRating;
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config);
		action.execute();
	}

	public void changeVoice() {
		this.changingVoice = true;
		Intent intent = new Intent(this, ChangeVoiceActivity.class);		
		startActivity(intent);
	}

	public void toggleDeviceVoice() {
		MainActivity.deviceVoice = !MainActivity.deviceVoice;
		MainActivity.forceDeviceVoice = MainActivity.deviceVoice;

		SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
		cookies.putString("deviceVoice", String.valueOf(MainActivity.deviceVoice));
		cookies.commit();
	}

	public void toggleFlag(View view) {
		flagResponse();
	}

	public void toggleCorrection(View view) {
		submitCorrection();
	}

	public void menu(View view) {
		Intent intent = new Intent(this, ChatMenuActivity.class);
		startActivity(intent);
		/*PopupMenu popup = new PopupMenu(this, view);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.menu_chat, popup.getMenu());
		onPrepareOptionsMenu(popup.getMenu());
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				return onOptionsItemSelected(item);
			}
		});
		popup.show();*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu_chat, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		this.menu = menu;
		resetMenu();
		return true;
	}

	public void resetMenu() {
		if (this.menu == null) {
			return;
		}
		
		this.menu.findItem(R.id.menuSound).setChecked(MainActivity.sound);
		this.menu.findItem(R.id.menuDeviceVoice).setChecked(MainActivity.deviceVoice);
		this.menu.findItem(R.id.menuHandsFreeSpeech).setChecked(MainActivity.handsFreeSpeech);
		this.menu.findItem(R.id.menuDisableVideo).setChecked(MainActivity.disableVideo || this.videoError);
		this.menu.findItem(R.id.menuHD).setChecked(MainActivity.hd);
		this.menu.findItem(R.id.menuWebm).setChecked(MainActivity.webm);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menuChangeLanguage:
			changeLanguage(null);
			return true;
		case R.id.menuChangeVoice:
			changeVoice();
			return true;
		case R.id.menuHandsFreeSpeech:
			toggleHandsFreeSpeech();
			return true;
		case R.id.menuSound:
			toggleSound();
			return true;
		case R.id.menuDeviceVoice:
			toggleDeviceVoice();
			return true;
		case R.id.menuDisableVideo:
			toggleDisableVideo();
			return true;
		case R.id.menuCorrection:
			submitCorrection();
			return true;
		case R.id.menuFlag:
			flagResponse();
			return true;
		case R.id.menuHD:
			MainActivity.hd = !MainActivity.hd;
			return true;
		case R.id.menuWebm:
			MainActivity.webm = !MainActivity.webm;
			return true;
		case R.id.menuChangeAvatar:
			changeAvatar();
			return true;
		case R.id.menuCustomizeAvatar:
			customizeAvatar();
			return true;
		case R.id.menuNoAds:
			noAds();
			return true;
		case R.id.MicConfig:
			MicConfiguration();
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onResume() {
		if (MainActivity.current == null) {
			finish();
			return;
		}
		if (this.imageView == null) {
			finish();
			return;
		}
		if (this.imageView.getVisibility() == View.GONE) {
			this.imageView.setVisibility(View.VISIBLE);
		}
		if (this.videoLayout.getVisibility() == View.VISIBLE) {
			this.videoLayout.setVisibility(View.GONE);
		}
		resetToolbar();
		MainActivity.searching = false;
		MainActivity.searchingPosts = false;
		if (MainActivity.browsing && (MainActivity.instance instanceof AvatarConfig)) {
			if (MainActivity.user == null || MainActivity.user.type == null || MainActivity.user.type.isEmpty() || MainActivity.user.type.equals("Basic")) {
				//MainActivity.showMessage("You must upgrade to get access to this avatar", this);
				//super.onResume();
				//return;
			}
			this.avatar = (AvatarConfig) MainActivity.instance;
			HttpGetImageAction.fetchImage(this, this.avatar.avatar, this.imageView);

			AvatarConfig avatarConfig = (AvatarConfig) this.avatar.credentials();
			HttpFetchChatAvatarAction action = new HttpFetchChatAvatarAction(this, avatarConfig);
			action.execute();
		}
		MainActivity.browsing = false;
		MainActivity.changeAvatar = false;
		if ((MainActivity.instance instanceof InstanceConfig) && MainActivity.instance.id.equals(this.instance.id)) {
			this.instance = (InstanceConfig) MainActivity.instance;
		} else {
			MainActivity.instance = this.instance;
		}
		if (this.changingVoice) {
			if (this.response != null && this.response.avatar != null && this.response.isVideo()) {
				playVideo(this.response.avatar, false);
			} else {
				HttpGetImageAction.fetchImage(this, this.instance.avatar, this.imageView);
			}
			this.changingVoice = false;
			resetTTS();
		}
		SharedPreferences cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE);
		customAvatarImage = cookies.getString("customAvatar", "");
		String avatarId = cookies.getString("avatarID", "");
		if (!avatarId.equals("")) {
			MainActivity.disableVideo = false;
			this.avatar = new AvatarConfig();
			this.avatar.id = avatarId;
			HttpFetchChatAvatarAction action = new HttpFetchChatAvatarAction(this, this.avatar);
			action.execute();
		} else {
			if (!customAvatarImage.equals("")) {
				MainActivity.disableVideo = true;
				HttpGetImageAction.fetchImage(this, customAvatarImage, imageView);
				this.instance.avatar = customAvatarImage;
				this.avatar = null;
			}
		}
		super.onResume();

		chatMenu();
	}

	public void chatMenu() {
		if (ChatMenuActivity.action != null) {
			if (ChatMenuActivity.action.equals("speak")) {
				toggleSound();
			} else if (ChatMenuActivity.action.equals("changeVoice")) {
				changeVoice();
			} else if (ChatMenuActivity.action.equals("deviceVoice")) {
				toggleDeviceVoice();
			} else if (ChatMenuActivity.action.equals("changeLanguage")) {
				changeLanguage(null);
			} else if (ChatMenuActivity.action.equals("handsFree")) {
				toggleHandsFreeSpeech();
			} else if (ChatMenuActivity.action.equals("microphone")) {
				MicConfiguration();
			} else if (ChatMenuActivity.action.equals("disableVideo")) {
				toggleDisableVideo();
			} else if (ChatMenuActivity.action.equals("hdVideo")) {
				MainActivity.hd = !MainActivity.hd;
			} else if (ChatMenuActivity.action.equals("webmVideo")) {
				MainActivity.webm = !MainActivity.webm;
			} else if (ChatMenuActivity.action.equals("changeAvatar")) {
				changeAvatar();
			} else if (ChatMenuActivity.action.equals("customizeAvatar")) {
				customizeAvatar();
			} else if (ChatMenuActivity.action.equals("noAds")) {
				noAds();
			} else if (ChatMenuActivity.action.equals("command")) {
				postback(ChatMenuActivity.command);
			}
			ChatMenuActivity.action = null;

		}
	}

	public void noAds() {

	}

	public void customizeAvatar() {

	}
	
	public String getAvatarIcon(ChatResponse config) {
		if (this.avatar != null) {
			return this.avatar.avatar;
		}
		if (config == null || config.isVideo()) {
			return this.instance.avatar;
		}
		return config.avatar;
	}

	public void resetAvatar(AvatarConfig config) {
		this.avatar = config;
		HttpGetImageAction.fetchImage(this, config.avatar, this.imageView);
	}

	@Override
	public void onDestroy() {
		try {
			
		/*if (MainActivity.showAds && mInterstitialAd.isLoaded()){
			mInterstitialAd.show();
		}*/
		active = false;
		stopListening();
		try {
			thread = null;
		} catch (Exception ex) {
			Log.e("micError", ex.toString());
		}
		if (this.instance != null) {
			ChatConfig config = new ChatConfig();
			config.instance = this.instance.id;
			config.conversation = MainActivity.conversation;
			config.disconnect = true;
			
			HttpChatAction action = new HttpChatAction(this, config);
			action.execute();

			MainActivity.conversation = null;
		}
		muteMicBeep(false);
		if (this.tts != null) {
			try {
				this.tts.stop();
			} catch (Exception ignore) {}
			try {
				this.tts.shutdown();
			} catch (Exception ignore) {}
		}
		if (this.audioPlayer != null) {
			try {
				this.audioPlayer.stop();
			} catch (Exception ignore) {}
			try {
				this.audioPlayer.release();
			} catch (Exception ignore) {}
		}
		if (this.speechPlayer != null) {
			try {
				this.speechPlayer.stop();
			} catch (Exception ignore) {}
			try {
				this.speechPlayer.release();
			} catch (Exception ignore) {}
		}
		if (this.speech != null) {
			try {
				this.speech.destroy();
			} catch (Exception ignore) {}
		}
	
		} catch (Exception exception) {
			Log.e("onDestroy", exception.toString());
		}
		if (adView != null) {
			adView.destroy();
		}
		super.onDestroy();
	}

	public void response(final ChatResponse response) {
		if (speechPlayer != null|| tts != null) {
			try {
				if (tts != null) {
					tts.stop();
				}
				if (speechPlayer != null && speechPlayer.isPlaying()) {
					speechPlayer.pause();
				}
			} catch (Exception exception) {
				Log.e("response", exception.toString());
			}
		}
		//needs when calling "sleep" or the its not going to let the mic off
		//also to stop the mic until the bot finish the sentence
		try {
			stopListening();
			this.response = response;

			String status = "";
			if (response.emote != null && !response.emote.equals("NONE")) {
				status = status + response.emote.toLowerCase();
			}
			if (response.action != null) {
				if (!status.isEmpty()) {
					status = status + " ";
				}
				status = status + response.action;
			}
			if (response.pose != null) {
				if (!status.isEmpty()) {
					status = status + " ";
				}
				status = status + response.pose;
			}

			if (response.command != null) {
				JSONObject jsonObject = response.getCommand();
				Command command = new Command(this, this, jsonObject);
			}

			//TextView statusView = (TextView) findViewById(R.id.statusText);
			//statusView.setText(status);

			final String text = response.message;
			final ListView list = (ListView) findViewById(R.id.chatList);
			if (text == null) {
				list.post(new Runnable() {
					@Override
					public void run() {
						ChatResponse ready = new ChatResponse();
						ready.message = "ready";
						messages.add(ready);
						((ChatListAdapter)list.getAdapter()).notifyDataSetChanged();
						list.invalidateViews();
						if (list.getCount() > 2) {
							list.setSelection(list.getCount() - 2);
						}
						beginListening();
					}
				});
				return;
			}
			list.post(new Runnable() {
				@Override
				public void run() {
					messages.add(response);
					((ChatListAdapter)list.getAdapter()).notifyDataSetChanged();
					list.invalidateViews();
					if (list.getCount() > 2) {
						list.setSelection(list.getCount()-1);
					}
				}
			});
			list.postDelayed(new Runnable() {
				@Override
				public void run() {
					list.setSelection(list.getAdapter().getCount() - 1);
				}
			}, 500);
			list.postDelayed(new Runnable() {
				@Override
				public void run() {
					list.setSelection(list.getAdapter().getCount() - 1);
				}
			}, 1000);
			list.postDelayed(new Runnable() {
				@Override
				public void run() {
					list.setSelection(list.getAdapter().getCount() - 1);
				}
			}, 2000);

			String html = Utils.linkHTML(text);
			if (stateLayouts == 1 || stateLayouts == 2) {
				Toast.makeText(ChatActivity.this, Utils.stripTags(text), Toast.LENGTH_SHORT).show();
			}
			if (html.contains("<") && html.contains(">")) {
				html = linkPostbacks(html);
			}

			boolean talk = (text.trim().length() > 0) && (MainActivity.deviceVoice || (this.response.speech != null && this.response.speech.length() > 0));
			if (MainActivity.sound && talk) {
				if (!MainActivity.disableVideo && !videoError && this.response.isVideo() && this.response.isVideoTalk()) {
					
					videoView.setOnPreparedListener(new OnPreparedListener() {
						@Override
						public void onPrepared(MediaPlayer mp) {
							try {
								mp.setLooping(true);
								if (!MainActivity.deviceVoice) {
									// Voice audio
									speechPlayer = playAudio(response.speech, false, false, false);
									speechPlayer.setOnCompletionListener(new OnCompletionListener() {
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
													if (!music) {
														beginListening();
													}
												}
											});
										}
									});
									System.out.println("***start");
									speechPlayer.start();
									System.out.println("***start2");
								} else {
									HashMap<String, String> params = new HashMap<String, String>();
									params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id");
									
									tts.speak(Utils.stripTags(text), TextToSpeech.QUEUE_FLUSH, params);
								}
							} catch (Exception exception) {
								Log.wtf(exception.getMessage(), exception);
							}
						}
					});
					playVideo(this.response.avatarTalk, false);
				} else if (talk) {
					if (!MainActivity.deviceVoice) {
						// Voice audio
						playAudio(this.response.speech, false, false, true);
					} else {
						HashMap<String, String> params = new HashMap<String, String>();
						params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "id");
						
						this.tts.speak(Utils.stripTags(text), TextToSpeech.QUEUE_FLUSH, params);
					}
				}
			} else if (talk && (!MainActivity.disableVideo && !videoError && this.response.isVideo() && this.response.avatarTalk != null)) {
				videoView.setOnPreparedListener(new OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer mp) {
						mp.setLooping(false);
					}
				});
				videoView.setOnCompletionListener(new OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mp) {
						videoView.setOnCompletionListener(null);
						cycleVideo(response);
					}
				});
				playVideo(this.response.avatarTalk, false);
				runOnUiThread(new Runnable() {
					public void run() {
						beginListening();
					}
				});
			} else {
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

	protected void muteMicBeep(boolean mute) {
		setStreamVolume();

		debug("muteMicBeep:" + mute + ":" + MainActivity.volume);
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		if (mute) {
			//if its true then the Volume will be zero.
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
		} else {
			//if its false, the Volume will put back on
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, MainActivity.volume, 0);
		}
	}

	/**
	 * Play background music from a command.
	 */
	public void playAudio(String src, boolean loop) {
		if (this.backgroundAudioPlayer != null) {
			this.backgroundAudioPlayer.stop();
			this.backgroundAudioPlayer.release();
		}
		this.backgroundAudioPlayer = playAudio(src, loop, true, true);
	}

	/**
	 * Send a message back to the bot from a command.
	 */
	public void postback(String postback) {
		if (postback == null) {
			postback = "postback";
		}
		this.textView.setText(postback);
		submitChat();
	}
}
