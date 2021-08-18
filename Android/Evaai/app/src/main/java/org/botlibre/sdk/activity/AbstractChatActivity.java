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

package org.botlibre.sdk.activity;

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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import io.evaai.R;

import org.botlibre.sdk.activity.MainActivity.LaunchType;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpChatAction;
import org.botlibre.sdk.activity.actions.HttpCreateChannelFileAttachmentAction;
import org.botlibre.sdk.activity.actions.HttpCreateChannelImageAttachmentAction;
import org.botlibre.sdk.activity.actions.HttpFetchChatAvatarAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetInstancesAction;
import org.botlibre.sdk.activity.actions.HttpGetVideoAction;
import org.botlibre.sdk.config.AvatarConfig;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.ChannelConfig;
import org.botlibre.sdk.config.ChatConfig;
import org.botlibre.sdk.config.ChatResponse;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.MediaConfig;
import org.botlibre.sdk.config.VoiceConfig;
import org.botlibre.sdk.config.WebMediumConfig;
import org.botlibre.sdk.util.Command;
import org.botlibre.sdk.util.TextStream;
import org.botlibre.sdk.util.Utils;
import org.json.JSONObject;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Activity for chatting with a bot.
 * To launch this activity from your app you can use the HttpFetchAction passing the bot id or name as a config, and launch=true.
 */
@SuppressWarnings("deprecation")
public abstract class AbstractChatActivity extends LibreActivity implements TextToSpeech.OnInitListener, TextToSpeech.OnUtteranceCompletedListener, RecognitionListener {
	protected static boolean DEBUG;
	protected static final int RESULT_SPEECH = 1;

	protected boolean isRecording;
	public static boolean isListening;

	public class WebAppInterface {
		Context context;
		public WebAppInterface(Context context) {
			this.context = context;
		}

		@JavascriptInterface
		public void postback(final String message) {
			try {
				final EditText messageText = (EditText) findViewById(R.id.messageText);
				messageText.post(new Runnable() {
					@Override
					public void run() {
						try {
							messageText.setText(message);
							submitChat();
						} catch (Throwable error) {
							error.printStackTrace();
						}
					}
				});
			} catch (Throwable error) {
				error.printStackTrace();
			}
		}
	}

	protected TextToSpeech tts;
	protected boolean ttsInit = false;
	protected VideoView videoView;
	protected EditText textView;
	protected EditText messageText;
	protected boolean volumeChecked = true;
	protected Thread thread;
	protected SpeechRecognizer speech;
	protected int stateLayouts = 0;
	public boolean music = false;
	protected double lastReply =  System.currentTimeMillis();
	public List<Object> messages = new ArrayList<Object>();
	public ChatResponse response;
	public MediaPlayer audioPlayer;
	public String currentAudio;
	public boolean videoError;
	protected volatile boolean wasSpeaking;
	protected WebMediumConfig instance;
	protected boolean active = true;
	protected String lastVideoUrl;
	protected ChatResponse lastResponse;
	protected AvatarConfig avatar;
	public static String customAvatarImage;
	protected boolean changingVoice;
	protected MediaPlayer speechPlayer;
	protected Random random = new Random();
	protected Bitmap icon;
	protected ImageView imageView;
	protected View videoLayout;
	protected long lastTime;
	
	//flag will check if the mic is ON or OFF
	public static boolean micLastStat;

	protected boolean failedOfflineLanguage = false;
	protected boolean threadIsOn = false;
	public WebAppInterface webAppInterface;

	public void setThreadIsOn(boolean threadIsOn) {
		this.threadIsOn = threadIsOn;
	}
	
	public boolean getThreadIsOn() {
		return this.threadIsOn;
	}
	
	public ImageView getImageView() {
		return this.imageView;
	}
	
	public View getVideoLayout() {
		return this.videoLayout;
	}
	
	public void superOnCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	public void initChat() {
		webAppInterface = new WebAppInterface(this);
		ActivityCompat.requestPermissions(AbstractChatActivity.this, new String[] { Manifest.permission.RECORD_AUDIO }, 1);
		setStreamVolume();
		muteMicBeep(false);
		micLastStat = MainActivity.listenInBackground;
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		if(this.instance instanceof InstanceConfig) {
			this.instance = (InstanceConfig) MainActivity.instance;
		} else if(this.instance instanceof ChannelConfig) {
			this.instance = (ChannelConfig) MainActivity.instance;
		}

		if (this.instance == null) {
			return;
		}
		
		if(!MainActivity.handsFreeSpeech){
			setMicIcon(false, false);
		} else if(!MainActivity.listenInBackground){
			setMicIcon(false, false);
		}

		if (MainActivity.listenInBackground && MainActivity.handsFreeSpeech) {
			microphoneThread(thread);
		}
		
		speech = SpeechRecognizer.createSpeechRecognizer(this);
		speech.setRecognitionListener(this);

		messageText = (EditText) findViewById(R.id.messageText);
		videoView = (VideoView)findViewById(R.id.videoView);
		resetVideoErrorListener();
		videoError = false;
		textView = (EditText) findViewById(R.id.messageText);

		this.tts = new TextToSpeech(this, this);

		/*if (MainActivity.translate) {
			findViewById(R.id.yandex).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.yandex).setVisibility(View.GONE);
		}*/
		
		GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onDoubleTapEvent(MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					boolean isVideo = !MainActivity.disableVideo && !videoError && response != null && response.isVideo();
					LinearLayout chatListLayout = findViewById(R.id.chatListLayout);
					ViewGroup.LayoutParams params = chatListLayout.getLayoutParams();
					if (imageView.getVisibility() == View.VISIBLE) {
						imageView.setVisibility(View.GONE);
						params.height = LinearLayout.LayoutParams.MATCH_PARENT;
						chatListLayout.setLayoutParams(params);
					} else if (!isVideo) {
						params.height = 0;
						chatListLayout.setLayoutParams(params);
						imageView.setVisibility(View.VISIBLE);
					}
					if (videoLayout.getVisibility() == View.VISIBLE) {
						params.height = LinearLayout.LayoutParams.MATCH_PARENT;
						videoLayout.setVisibility(View.GONE);
						chatListLayout.setLayoutParams(params);
					} else if (isVideo) {
						params.height = 0;
						chatListLayout.setLayoutParams(params);
						videoLayout.setVisibility(View.VISIBLE);
					}
					return true;
				}
				return false;
			}
		};
	}
	
	//thread for the Microphone
	public Thread microphoneThread(Thread thread) {
		//make sure its on if it didn't turn off by the user. if 'sleep' is called it will turn the mic off.
		if(MainActivity.listenInBackground && threadIsOn){
			return thread;
		}
		//if the user clicked on the Mic while its ON it will turn off and turn the thread off as well.
		if(threadIsOn){
			threadIsOn = false;
			active = false;
			try{
				thread.stop();
				Log.e("Thread","STOP CURRENT RUNNING MIC THREAD");
			}catch(Exception ignore){}
			return thread;
		}
		//if the user clicked on the Mic while its off it will turn ON the thread.
		if (!threadIsOn) {
			threadIsOn = true;
			active = true;
			thread = new Thread() {
				@Override
				public void run() {
					Log.e("Thread","RUNNING");
					while (active) {
						Log.e("Thread","ACTIVE");
						if (!isRecording && isListening && (System.currentTimeMillis() - lastReply) > 5000) {
							lastReply = System.currentTimeMillis();
							debug("speech death restart");
							restartListening();
						}
						try {
							Thread.sleep(1500);
						} catch (Exception exception) {
						}
					}
				}
			};
			thread.start();
		}
		
		return thread;
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
		HttpAction action = new HttpChatAction(AbstractChatActivity.this, config);
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
	
	public void resetVideoErrorListener() {
		videoView.setOnErrorListener(new OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				Log.wtf("Video error", "what:" + what + " extra:" + extra);
				videoError = true;
				return true;
			}
		});
	}

	public void exit() {
		//this.threadIsOn = false;
		//this.active = false;
	}

	public abstract void submitChat();

	public void toggleSound(View view) {
		toggleSound();
	}

	public void toggleSound() {
		MainActivity.sound = !MainActivity.sound;
		resetToolbar();
	}

	public void toggleHandsFreeSpeech() {
		MainActivity.handsFreeSpeech = !MainActivity.handsFreeSpeech;
		if (!MainActivity.handsFreeSpeech) {
			stopListening();
		} else if (MainActivity.handsFreeSpeech) {
			beginListening();
		}
		SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
    	cookies.putBoolean("handsfreespeech", MainActivity.handsFreeSpeech);
    	cookies.commit();
	}

	public void toggleDisableVideo() {
		if (this.videoError) {
			this.videoError = false;
			MainActivity.disableVideo = false;
		} else {
			MainActivity.disableVideo = !MainActivity.disableVideo;
		}
	}

	public void changeLanguage(View view) {
		MainActivity.changeLanguage(this, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				resetTTS();
			}
		});
	}

	/**
	 * Disconnect from the conversation.
	 */
	public void disconnect(View view) {		
		finish();
	}

	public void resetToolbar() {
		if (MainActivity.sound) {
			findViewById(R.id.soundButton).setBackgroundResource(R.drawable.sound);
		} else {
			findViewById(R.id.soundButton).setBackgroundResource(R.drawable.mute);
		}
	}

	/**
	 * Clear the log.
	 */
	public void clear(View view) {
	}

	public void resetTTS() {
		try {
			this.tts.stop();
		} catch (Exception exception) {}
		try {
			this.tts.shutdown();
		} catch (Exception exception) {}
		this.tts = new TextToSpeech(this, this);
	}

	public void noAds() {

	}
	
	
	public void MicConfiguration(){
		Intent i = new Intent(this, MicConfiguration.class);
		startActivity(i);
		finish();
	}

	@Override
	public void onStop() {
		super.onStop();
		SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
		cookies.putString("deviceVoice", String.valueOf(MainActivity.deviceVoice));
		cookies.commit();
	}

	@Override
	public void onInit(int status) {

		if (status == TextToSpeech.SUCCESS) {

			Locale locale = null;
			VoiceConfig voice = MainActivity.voice;
			if (voice != null && voice.language != null && voice.language.length() > 0) {
				locale = new Locale(voice.language);
			} else {
				locale = Locale.US;
			}
			int result = this.tts.setLanguage(locale);

			float pitch = 1;
			if (voice != null && voice.pitch != null && voice.pitch.length() > 0) {
				try {
					pitch = Float.valueOf(voice.pitch);
				} catch (Exception exception) {}
			}
			float speechRate = 1;
			if (voice != null && voice.speechRate != null && voice.speechRate.length() > 0) {
				try {
					speechRate = Float.valueOf(voice.speechRate);
				} catch (Exception exception) {}
			}
			this.tts.setPitch(pitch);
			this.tts.setSpeechRate(speechRate);

			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "This Language is not supported");
			}

			this.tts.setOnUtteranceCompletedListener(this);

		} else {
			Log.e("TTS", "Initilization Failed!");
		}

	}

	/**
	 * Add JavaScript to the HTML to raise postback events to send messages to the bot.
	 */
	public String linkPostbacks(String html) {
		if (html.contains("button")) {
			TextStream stream = new TextStream(html);
			StringWriter writer = new StringWriter();
			while (!stream.atEnd()) {
				writer.write(stream.upToAll("<button", true));
				if (!stream.atEnd()) {
					String element = stream.upTo('>', true);
					String button = stream.upTo('<', false);
					writer.write(" onclick=\"Android.postback('" + button + "')\" ");
					writer.write(element);
					writer.write(button);
				}
			}
			html = writer.toString();
		}
		if (html.contains("chat:")) {
			TextStream stream = new TextStream(html);
			StringWriter writer = new StringWriter();
			while (!stream.atEnd()) {
				writer.write(stream.upToAll("href=\"", true));
				if (stream.atEnd()) {
					break;
				}
				String protocol = stream.upTo(':', true);
				if (!protocol.equals("chat:")) {
					writer.write(protocol);
					continue;
				}
				String chat = stream.upTo('"', false);
				writer.write("#\"");
				writer.write(" onclick=\"Android.postback('" + chat + "')\" ");
			}
			html = writer.toString();
		}
		if (html.contains("select")) {
			TextStream stream = new TextStream(html);
			StringWriter writer = new StringWriter();
			while (!stream.atEnd()) {
				writer.write(stream.upToAll("<select", true));
				if (!stream.atEnd()) {
					writer.write(" onchange=\"Android.postback(this.value)\" ");
				}
			}
			html = writer.toString();
		}
		return html;
	}

	public abstract void response(final ChatResponse response);

	@Override
	 public void onPause() {
		stopListening();
		muteMicBeep(false);
		super.onPause();
	 }
	
	public void playVideo(String video, boolean loop) {
		if (loop) {
			videoView.setOnPreparedListener(new OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mp) {
					mp.setLooping(true);
				}
			});
		}
		try {
			Uri videoUri = HttpGetVideoAction.fetchVideo(this, video);
			if (videoUri == null) {
				videoUri = Uri.parse(MainActivity.connection.fetchImage(video).toURI().toString());
			}
			videoView.setVideoURI(videoUri);
			videoView.start();
		} catch (Exception exception) {
			Log.wtf(exception.toString(), exception);
		}
	}
	
	public void cycleVideo(final ChatResponse response) {
		lastResponse = response;
		if ((response.avatar2 == null || response.avatar3 == null || response.avatar4 == null || response.avatar5 == null)
				|| (response.avatar2.isEmpty() || response.avatar3.isEmpty() || response.avatar4.isEmpty() || response.avatar5.isEmpty())
				|| (response.avatar.equals(response.avatar2) && response.avatar2.equals(response.avatar3)
						&& response.avatar3.equals(response.avatar4) && response.avatar4.equals(response.avatar5))) {
			playVideo(response.avatar, true);
			return;
		}
		videoView.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.setLooping(false);
			}
		});
		videoView.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				cycleVideo(response);
			}
		});
		int value = random.nextInt(5);
		String avatar = response.avatar;
		switch (value) {
			case 1 :
				avatar = response.avatar2;
				break;
			case 2 :
				avatar = response.avatar3;
				break;
			case 3 :
				avatar = response.avatar5;
				break;
			case 14 :
				avatar = response.avatar4;
				break;
		}
		
		try {
			Uri videoUri = HttpGetVideoAction.fetchVideo(this, avatar);
			if (videoUri == null) {
				videoUri = Uri.parse(MainActivity.connection.fetchImage(avatar).toURI().toString());
			}
			videoView.setVideoURI(videoUri);
			videoView.start();
		} catch (Exception exception) {
			Log.wtf(exception.toString(), exception);
		}
	}
	
	public MediaPlayer playAudio(String audio, boolean loop, boolean cache, boolean start) {
		try {
			System.out.println("***playAudio");
			Uri audioUri = null;
			if (cache) {
				System.out.println("***fetchVideo");
				audioUri = HttpGetVideoAction.fetchVideo(this, audio);
			} 
			if (audioUri == null) {
				System.out.println("***parse");
				audioUri = Uri.parse(MainActivity.connection.fetchImage(audio).toURI().toString());
			}
			final MediaPlayer audioPlayer = new MediaPlayer();
			System.out.println("***setDataSource");
			audioPlayer.setDataSource(getApplicationContext(), audioUri);
			audioPlayer.setOnErrorListener(new OnErrorListener() {
			
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					Log.wtf("Audio error", "what:" + what + " extra:" + extra);
					audioPlayer.stop();
					audioPlayer.release();
					return true;
				}
			});
			audioPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					audioPlayer.release();
					runOnUiThread(new Runnable() {
						public void run() {
							try {
								beginListening();
							} catch (Exception e) {
								Log.e("ChatActivity", "MediaPlayer: " + e.getMessage());
							}
						}
					});
				}
			});
			System.out.println("***prepare");
			audioPlayer.prepare();
			audioPlayer.setLooping(loop);
			if (start) {
				System.out.println("***start");
				audioPlayer.start();
			}
			return audioPlayer;
		} catch (Exception exception) {
			Log.wtf(exception.toString(), exception);
			return null;
		}
	}
	

	@Override
	public void onUtteranceCompleted(String utteranceId) {
		debug("onUtteranceCompleted");
		runOnUiThread(new Runnable() {
			public void run() {
				try {
				beginListening();
				} catch (Exception exception) {
					Log.wtf(exception.toString(), exception);
				}
			}
		});
		try {
			if (!MainActivity.disableVideo && !videoError && this.response.isVideo()) {
				this.videoView.post(new Runnable() {
					public void run() {
						cycleVideo(response);
					}
				});
			}
		} catch (Exception exception) {
			Log.wtf(exception.toString(), exception);
		}
	}

	public TextToSpeech getTts() {
		return tts;
	}

	public void setTts(TextToSpeech tts) {
		this.tts = tts;
	}

	public VideoView getVideoView() {
		return videoView;
	}

	public void setVideoView(VideoView videoView) {
		this.videoView = videoView;
	}

	public List<Object> getMessages() {
		return messages;
	}

	public void setMessages(List<Object> messages) {
		this.messages = messages;
	}

	public ChatResponse getResponse() {
		return response;
	}

	public void setResponse(ChatResponse response) {
		this.response = response;
	}

	public MediaPlayer getAudioPlayer() {
		return audioPlayer;
	}

	public void setAudioPlayer(MediaPlayer audioPlayer) {
		this.audioPlayer = audioPlayer;
	}

	public String getCurrentAudio() {
		return currentAudio;
	}

	public void setCurrentAudio(String currentAudio) {
		this.currentAudio = currentAudio;
	}

	public boolean getWasSpeaking() {
		return wasSpeaking;
	}

	public void setWasSpeaking(boolean wasSpeaking) {
		this.wasSpeaking = wasSpeaking;
	}
	
	protected void stopListening() {
		debug("stopListening");
		try {
			muteMicBeep(false);
			isListening = false;
			if(this.speech != null) {
				this.speech.stopListening();
			}
			setMicIcon(false, false);
		} catch (Exception ignore) {
			Log.e("StopListening", "Error" + ignore.getMessage());
			Log.e("StopListeningTrace", Log.getStackTraceString(ignore));
		}
	}
	
	protected void restartListening() {
		lastReply = System.currentTimeMillis();
		debug("restartListening");
		if (!MainActivity.listenInBackground) {
			return;
		}
		if (!isListening) {
			return;
		}
		this.runOnUiThread(new Runnable() {
			public void run() {
				try{
					Log.e("ChatActivity","Start Listening from Restart");
					beginListening();
				}catch(Exception e){Log.e("ErrorChatActivity","Error: " + e.getMessage()); }
			}
		});
	}
	
	public void scanBarcode(View v) {
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.initiateScan();
	}

	protected void setMicIcon(boolean on, boolean recording) {
		try{
		View micButton = findViewById(R.id.speakButton);
		
		if (!on) {
			((ImageButton) micButton).setImageResource(R.drawable.micoff);
		} else if (on && recording) {
			((ImageButton) micButton).setImageResource(R.drawable.micrecording);
		} else {
			((ImageButton) micButton).setImageResource(R.drawable.mic);
		}
		}catch(Exception e){Log.e("ChatActivity.setMicIcon","" + e.getMessage());}
	}
	

	@Override
	public void onBeginningOfSpeech() {
		debug("onBeginningOfSpeech");
		Log.e("onBeginningOfSpeech","");
		setMicIcon(true, true);
		isRecording = true;
	}

	@Override
	public void onBufferReceived(byte[] arg0) {
		Log.e("onBufferReceived","");
		}

	@Override
	public void onEndOfSpeech() {
		debug("onEndOfSpeech:");
		isRecording = false;
		lastReply = System.currentTimeMillis();
		Log.e("onEndOfSpeech","");
		setMicIcon(false, false);
	}

	public void debug(final String text) {
		if (!DEBUG) {
			return;
		}
		final ListView list = (ListView) findViewById(R.id.chatList);
		list.post(new Runnable() {
			@Override
			public void run() {
				ChatResponse ready = new ChatResponse();
				ready.message = text;
				messages.add(ready);
				((ChatListAdapter)list.getAdapter()).notifyDataSetChanged();
				list.invalidateViews();
				if (list.getCount() > 2) {
					list.setSelection(list.getCount() - 2);
				}
			}
		});
		return;
	}

	@Override
	public void onError(int error) {
		debug("onError:" + error);
		Log.d("onError Info", "ChatActivity on error executes here!");
		try{
		isRecording = false;

		lastReply = System.currentTimeMillis();
		this.speech.destroy();
		this.speech = SpeechRecognizer.createSpeechRecognizer(this);
		this.speech.setRecognitionListener(this);
		
		setMicIcon(false, false);

		muteMicBeep(false);

		setStreamVolume();
		
		if (error == SpeechRecognizer.ERROR_AUDIO) {
			Log.d("System.out", "Error: Audio Recording Error");
		} else if (error == SpeechRecognizer.ERROR_CLIENT) {
			Log.d("System.out", "Error: Other client side error");
			restartListening();
		} else if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
			Log.d("System.out", "Error: INsufficient permissions");
		} else if (error == SpeechRecognizer.ERROR_NETWORK) {
			Log.d("System.out", "Error: Other network Error");
		} else if (error == SpeechRecognizer.ERROR_NETWORK_TIMEOUT) {
			Log.d("System.out", "Error: Network operation timed out");
		} else if (error == SpeechRecognizer.ERROR_NO_MATCH) {
			Log.d("System.out", "Error: No recognition result matched");
			restartListening();
		} else if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
			Log.d("System.out", "Error: Recognition service busy");
			restartListening();
		} else if (error == SpeechRecognizer.ERROR_SERVER) {
			Log.d("System.out", "Error: Server Error");
			failedOfflineLanguage = true;
			restartListening();
		} else if (error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
			Log.d("System.out", "Error: NO speech input");
			//isListening = true; - This error is triggered when stopListening() is called, so must not restart (isListening will be false).
			restartListening();
		}
		}catch(Exception e){Log.e("micError",e.getMessage());}
	}

	@Override
	public void onEvent(int arg0, Bundle arg1) {
		Log.e("OnEvent","Listening OnEvent");
		debug("onEvent:" + arg0);
	}

	@Override
	public void onPartialResults(Bundle arg0) {
		debug("onPartialResults:");
	}

	@Override
	public void onReadyForSpeech(Bundle arg0) {
		debug("onReadyForSpeech:");
		Log.e("onReadyForSpeech","");
		setMicIcon(true, false);
	}

	@Override
	public void onResults(Bundle results) {
		debug("onResults:");
		muteMicBeep(false);
		List<String> text = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		this.textView.setText(text.get(0));
		submitChat();
	}

	@Override
	public void onRmsChanged(float arg0) {}
	
	protected void muteMicBeep(boolean mute) {
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

	protected void setStreamVolume() {
		AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		if (volume != 0) {
			debug("setStreamVolume:" + volume);
			Log.d("ChatActivity","The volume changed and saved to : " + volume);
			MainActivity.volume = volume;
		}
	}

	@TargetApi(23)
	protected void beginListening() {
		lastReply = System.currentTimeMillis();
		setStreamVolume();
		//debug("beginListening:");
		try {
			if(!MainActivity.handsFreeSpeech){return;}
			if (MainActivity.handsFreeSpeech) {
				muteMicBeep(true);
				isListening = true;
			}

			if (!MainActivity.listenInBackground) {
				muteMicBeep(false);
				return;
			}

		} catch (Exception ignore) {
			Log.e("Error", "BeginListening");
		}
		
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		if (MainActivity.offlineSpeech) {
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, MainActivity.voice.language);
			if (!this.failedOfflineLanguage) {
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
				// intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
			}
			intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
		} else {
			if (MainActivity.voice != null && MainActivity.voice.language != null && !MainActivity.voice.language.isEmpty()) {
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, MainActivity.voice.language);
				if (!this.failedOfflineLanguage) {
					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, MainActivity.voice.language);
				}
			} else {
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en");
				if(!this.failedOfflineLanguage) {
					intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en");
				}
			}
		}

		try {
			Log.d("BeginListening","StartListening");
			this.speech.startListening(intent);
			setMicIcon(true, false);
		} catch (Exception exception) {
			Log.d("BeginListening","CatchError: " + exception.getMessage());
			Toast t = Toast.makeText(getApplicationContext(),"Your device doesn't support Speech to Text", Toast.LENGTH_SHORT);
			t.show();
		}		
	}
}
