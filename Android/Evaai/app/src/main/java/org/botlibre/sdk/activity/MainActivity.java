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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.android.billingclient.api.BillingClient;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import org.botlibre.sdk.BOTlibreCredentials;
import org.botlibre.sdk.Credentials;
import org.botlibre.sdk.SDKConnection;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpConnectAction;
import org.botlibre.sdk.activity.actions.HttpFetchAction;
import org.botlibre.sdk.activity.actions.HttpFetchOrCreateAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetInstancesAction;
import org.botlibre.sdk.activity.actions.HttpGetTemplatesAction;
import org.botlibre.sdk.activity.actions.HttpVerifyEmailAction;
import org.botlibre.sdk.activity.avatar.AvatarActivity;
import org.botlibre.sdk.activity.avatar.AvatarSearchActivity;
import org.botlibre.sdk.activity.avatar.CreateAvatarActivity;
import org.botlibre.sdk.activity.forum.CreateForumActivity;
import org.botlibre.sdk.activity.forum.ForumActivity;
import org.botlibre.sdk.activity.forum.ForumSearchActivity;
import org.botlibre.sdk.activity.graphic.CreateGraphicActivity;
import org.botlibre.sdk.activity.graphic.GraphicActivity;
import org.botlibre.sdk.activity.graphic.GraphicSearchActivity;
import org.botlibre.sdk.activity.livechat.ChannelActivity;
import org.botlibre.sdk.activity.livechat.ChannelSearchActivity;
import org.botlibre.sdk.activity.livechat.CreateChannelActivity;
import org.botlibre.sdk.activity.script.CreateScriptActivity;
import org.botlibre.sdk.activity.script.ScriptActivity;
import org.botlibre.sdk.activity.script.ScriptSearchActivity;
import org.botlibre.sdk.activity.war.StartWarActivity;
import org.botlibre.sdk.config.AvatarConfig;
import org.botlibre.sdk.config.AvatarMedia;
import org.botlibre.sdk.config.BotModeConfig;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.ChannelConfig;
import org.botlibre.sdk.config.DomainConfig;
import org.botlibre.sdk.config.ForumConfig;
import org.botlibre.sdk.config.ForumPostConfig;
import org.botlibre.sdk.config.GraphicConfig;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.IssueConfig;
import org.botlibre.sdk.config.LearningConfig;
import org.botlibre.sdk.config.ResponseConfig;
import org.botlibre.sdk.config.ScriptConfig;
import org.botlibre.sdk.config.ScriptSourceConfig;
import org.botlibre.sdk.config.UserConfig;
import org.botlibre.sdk.config.VoiceConfig;
import org.botlibre.sdk.config.WebMediumConfig;
import org.botlibre.sdk.util.Utils;
import org.w3c.dom.Element;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AudienceNetworkAds;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import io.evaai.R;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.Toast;
import android.widget.VideoView;

/**
 * Main view, allows connect, browse and content creation.
 * Also stores a lot of shared data, such as the connection, current user/instance/conversation.
 * You do not need to use the MainActivity in your app, but it needs to be there so the other activities can access the shared data.
 * You can reuse any of the activities in your own app, such as just the Chat, Forum, LiveChat, or user management activities.
 * You can customize the code and layouts any way you wish for your own app, or just use the SDKConnection, or LiveChatConnection API.
 * <p>
 * You can create an app to access a single bot, forum, or channel instance using this MainActivity class.
 * You will need to create your bot, forum, or channel using your service provider website, or mobile app (Bot Libre, Bot Libre for Business, Forums Libre, Live Chat Libre).
 * <p>
 * You only need to set the applicationId, launchType, and launchInstanceId or launchInstanceName.
 * You will also want to replace the logo.png in res/drawable and update the application name and version in the AndroidManifest,
 * then you can package the app into your own apk file and upload it to Google Play or any other site.
 * The app is yours, you can charge for it, or give it away for free, just ensure you keep the above license in the code.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
@SuppressLint("NewApi")
public class MainActivity extends LibreActivity {
	public static final boolean DEBUG = true; // to remove adds for testing set DEBUG true, to view adds DEBUG false;
	public static final boolean COMMERCIAL = false;
	public static boolean PROFANITY_FILTER = true;
	public static boolean BIRTHDATE_FAILED = false;
	/**
	 * Enter your application ID here.
	 * You can get an application ID from any of the services websites (Bot Libre, Bot Libre for Business, Forums Libre, Live Chat Libre)
	 */
	public static String applicationId = "4835101306057882783";

	/**
	 * Configure your connection credentials here.
	 * Choose which service provider you wish to connect to.
	 */
	public static SDKConnection connection;

	/**
	 * To launch into a specific domain, enter the domain id here.
	 */
	public static String domainId = null;
	public static DomainConfig domain;
	public static String defaultType = "Bots";
	public static boolean showAds = false;
	public static boolean showBannerAds = false;
	public static boolean showPopupAds = false;
	public static boolean showVideoAds = false;
	public static String popupAd = "";
	public static String videoAd = "";
	public static InterstitialAd mInterstitialAd;
	public static RewardedVideoAd mRewardedVideoAd;

	public static boolean showFacebookBanner = false;
	public static boolean showFacebookPopupAds = false;
	public static String facebookBannerAd = "";
	public static String facebookPopupAd = "";
	public static com.facebook.ads.InterstitialAd facebookInterstitialAd;

	/**
	 * Choose your service provider using the correct credentials.
	 */
	static {
		Credentials credentials = new Credentials();
		credentials.applicationId = applicationId;
		credentials.host = "evaai.io";
		credentials.app = "";
		credentials.url = "https://evaai.io/rest/api";
		connection = new SDKConnection(credentials);
		if (domainId != null) {
			domain = new DomainConfig();
			domain.id = domainId;
			connection.setDomain(domain);
		}
		if (DEBUG) {
			showAds = false;
			connection.setDebug(true);
		}
	}
	public static String WEBSITE = "https://evaai.io";
	public static final String WEBSITEHTTPS = "https://evaai.io";
	public static String SERVER = "evaai.io";

	/**
	 * If you are building a single instance app, then you can set the instance id or name here,
	 * and use this activity to launch it.
	 */
	public static String launchInstanceId = "54800"; // i.e. "171"
	public static String launchInstanceName = "INSIGHT"; // i.e. "Help Bot"
	public static String website = "https://evaai.io";
	public static String prefix = "Bot";

	/**
	 * If you are building a single instance app, then you can set the launchType to
	 * have this activity launch the bot, forum, or channel.
	 */
	public static LaunchType launchType = LaunchType.Bot;
	public enum LaunchType {Browse, Bot, Forum, Channel}

	public static boolean launch = true;

	public static boolean handsFreeSpeech = true;
	public static boolean listenInBackground = false;

	public static boolean hasRequestAvatar;
	public static boolean sound = true;
	public static boolean disableVideo;
	public static boolean webm = true;
	public static boolean hd;
	public static boolean deviceVoice;
	public static boolean forceDeviceVoice;
	public static boolean customVoice;
	public static boolean translate;
	public static boolean changeAvatar;

	public static boolean offlineSpeech = false;
	public static boolean accountDeleted = false;
	
	public static GraphicConfig gInstance;
	public static WebMediumConfig instance;
	public static ForumPostConfig post;
	public static IssueConfig issue;
	public static UserConfig user;
	public static UserConfig viewUser;
	public static String type = "Bots";
	public static BotModeConfig botMode = new BotModeConfig();
	public static VoiceConfig voice = new VoiceConfig();
	public static LearningConfig learning = new LearningConfig();
	public static AvatarMedia avatarMedia;
	public static ResponseConfig response;
	public static ScriptSourceConfig script;
	public static String conversation;
	public static String template = "";
	public static String currentPhotoPath;
	public static List<InstanceConfig> templates;
	public static List<AvatarConfig> avatars;
	public static Object[] tags;
	public static Object[] categories;
	public static Object[] userTags;
	public static Object[] forumTags;
	public static Object[] forumPostTags;
	public static Object[] forumCategories;
	public static Object[] channelTags;
	public static Object[] channelCategories;
	public static Object[] avatarTags;
	public static Object[] scriptTags;
	public static Object[] scriptCategories;
	public static Object[] avatarCategories;
	public static int volume;
	
	public static String contentRating = "Teen";
	public static Object[] domainTags;
	public static Object[] domainCategories;

	public static Object[] graphicTags;
	public static Object[] graphicCategories;
	public static boolean showImages = true;
	public static BrowseConfig browse = null;
	public static BrowseConfig browsePosts = null;
	public static BrowseConfig browseIssues = null;
	public static List<WebMediumConfig> instances = new ArrayList<WebMediumConfig>();
	public static List<ForumPostConfig> posts = new ArrayList<ForumPostConfig>();
	public static List<IssueConfig> issues = new ArrayList<IssueConfig>();
	public static List<AvatarMedia> avatarMedias = new ArrayList<AvatarMedia>();
	public static MainActivity current;
	public static boolean browsing;
	public static boolean searching;
	public static boolean userFilter = true;
	public static boolean searchingPosts;
	public static boolean searchingIssues;
	public static boolean wasDelete;
	public static boolean importingBotScript = false;
	public static boolean importingBotLog = false;
	public static String[] actions = new String[]{"actions", "smile", "laugh", "kiss", "dance", "walk", "jump", "sleep", "stop", "spin", "flirt", "cry", "happy", "angry", "scream", "sneeze", "fart", "games", "I Spy"};
	public static String[] languages = new String[]{
			"Default",
			"af - Afrikaans", "sq - Albanian", "ar - Arabic", "hy - Armenian", "az - Azerbaijani",
			"ba - Bashkir", "eu - Basque", "be - Belarusian", "bn - Bengali", "bs - Bosnian", "bg - Bulgarian",
			"ca - Catalan", "zh - Chinese", "hr - Croatian", "cs - Czech", "da - Danish", "nl - Dutch", "en - English", "et - Estonian", "fi - Finnish", "fr - French",
			"gl - Galician", "ka - Georgian", "de - German", "el - Greek", "gu - Gujarati", "ht - Haitian", "he - Hebrew", "hi - Hindi", "hu - Hungarian",
			"is - Icelandic", "id - Indonesian", "ga - Irish", "it - Italian", "ja - Japanese", "kn - Kannada", "kk - Kazakh", "ky - Kirghiz", "ko - Korean",
			"la - Latin", "lv - Latvian", "lt - Lithuanian", "mk - Macedonian", "mg - Malagasy", "ms - Malay", "mt - Maltese", "mn - Mongolian", "no - Norwegian",
			"fa - Persian", "pl - Polish", "pt - Portuguese", "pa - Punjabi", "ro - Romanian", "ru - Russian",
			"sr - Serbian", "si - Sinhalese", "sk - Slovak", "sl - Slovenian", "es - Spanish", "sw - Swahili", "sv - Swedish",
			"tl - Tagalog", "tg - Tajik", "ta - Tamil", "tt - Tatar", "th - Thai", "tr - Turkish",
			"udm - Udmurt", "uk - Ukrainian", "ur - Urdu", "uz - Uzbek", "vi - Vietnamese", "cy - Welsh"
	};
	public static String[] servers = new String[]{"www.botlibre.com", "twitter.botlibre.com", "www.botlibre.biz", "www.livechatlibre.com", "www.forumslibre.com"};
	public static String[] types = new String[]{"Bots", "Avatars", "Scripts", "Forums","Live Chat","Graphics", "Workspaces", "Chat Bot Wars"};
	public static String[] channelTypes = new String[]{"ChatRoom", "OneOnOne"};
	public static String[] priorities = new String[]{"Low", "Medium", "High", "Sever"};
	public static String[] accessModes = new String[]{"Everyone", "Users", "Members", "Administrators"};
	public static String[] userAccessModes = new String[]{"Friends", "Private", "Everyone"};
	public static String[] contentRatings = new String[]{"Everyone","Teen"};
	public static String[] forkAccMode = new String[]{"Administrators","Members","Users","Disabled"};
	public static String[] mediaAccessModes = new String[]{"Administrators", "Everyone", "Members", "Users", "Disabled"};
	public static String[] learningModes = new String[]{"Disabled", "Administrators", "Users", "Everyone"};
	public static String[] correctionModes = new String[]{"Disabled", "Administrators", "Users", "Everyone"};
	public static String[] scriptLanguages = new String[]{"Self", "AIML", "Response List", "Chat Log", "CVS"};
	public static String[] botModes = new String[]{"ListenOnly", "AnswerOnly", "AnswerAndListen"};
	public static String[] responseTypes = new String[]{"conversations", "responses", "greetings", "default", "flagged"};
	public static String[] durations = new String[]{"all", "day", "week", "month"};
	public static String[] inputTypes = new String[]{"all", "chat", "tweet", "post", "directmessage", "email"};
	public static String[] responseRestrictions = new String[]{"", "exact", "keyword", "required", "topic", "label", "previous",
			"repeat", "missing-keyword", "missing-required", "missing-topic", "patterns", "templates", "flagged", "corrections",
			"emotions", "actions", "poses"};
	public static String[] voices = new String[]{
			"cmu-slt",
			"cmu-slt-hsmm",
			"cmu-bdl-hsmm",
			"cmu-rms-hsmm",
			"dfki-prudence",
			"dfki-prudence-hsmm",
			"dfki-spike",
			"dfki-spike-hsmm",
			"dfki-obadiah",
			"dfki-obadiah-hsmm",
			"dfki-poppy",
			"dfki-poppy-hsmm",
			"bits1-hsmm",
			"bits3",
			"bits3-hsmm",
			"dfki-pavoque-neutral-hsmm",
			"camille",
			"camille-hsmm-hsmm",
			"jessica_voice-hsmm",
			"pierre-voice-hsmm",
			"enst-dennys-hsmm",
			"istc-lucia-hsmm",
			"voxforge-ru-nsh",
			"dfki-ot",
			"dfki-ot-hsmm",
			"cmu-nk",
			"cmu-nk-hsmm"
	};
	public static String[] voiceMods = new String[]{
			"default","child","whisper","echo","robot"
	};
	public static String[] voiceNames = new String[]{
			"English : US : Female : SLT",
			"English : US : Female : SLT (hsmm)",
			"English : US : Male : BDL (hsmm)",
			"English : US : Male : RMS (hsmm)",
			"English : GB : Female : Prudence",
			"English : GB : Female : Prudence (hsmm)",
			"English : GB : Male : Spike",
			"English : GB : Male : Spike (hsmm)",
			"English : GB : Male : Obadiah",
			"English : GB : Male : Obadiah (hsmm)",
			"English : GB : Female : Poppy",
			"English : GB : Female : Poppy (hsmm)",
			"German : DE : Female : Bits1 (hsmm)",
			"German : DE : Male : Bits3",
			"German : DE : Male : Bits3 (hsmm)",
			"German : DE : Male : Pavoque (hsmm)",
			"French : FR : Female : Camille",
			"French : FR : Female : Camille (hsmm)",
			"French : FR : Female : Jessica (hsmm)",
			"French : FR : Male : Pierre (hsmm)",
			"French : FR : Male : Dennys (hsmm)",
			"Italian : IT : Male : Lucia (hsmm)",
			"Russian : RU : Male : NSH (hsmm)",
			"Turkish : TR : Male : OT",
			"Turkish : TR : Male : OT (hsmm)",
			"Telugu : TE : Female : NK",
			"Telugu : TE : Female : NK (hsmm)"
	};

	Menu menu;

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@SuppressLint("NewApi")
	public static String getFilePathFromURI(Context context, Uri uri) {
		final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];
				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}
				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {
				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];
				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}
				final String selection = "_id=?";
				final String[] selectionArgs = new String[] { split[1] };
				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {
			// Return the remote address
			if (isGooglePhotosUri(uri)) {
				return uri.getLastPathSegment();
			}
			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}
		return null;
	}

	public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };
		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	public static Boolean getFileSize(String q, Context a) {
		if (q == null || q.equals("")) {
			return true;
		}
		File file = new File(q);
		long fileSizeInBytes = file.length();
		long fileSizeInKB = fileSizeInBytes / 1024;
		long fileSizeInMB = fileSizeInKB / 1024;
		if (fileSizeInMB >= 5) {
			showMessage("The file exceeded the maximum size limit!", (Activity) a);
			return false;
		} else {
			return true;
		}
	}

	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */

	//com.google.android.apps.drive.DRIVE_OPEN
	//com.google.android.apps.photos.content
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}

	public static String getFileNameFromPath(String path) {
		int index = path.lastIndexOf("/");
		return path.substring(index + 1, path.length());
	}

	public static String getFileTypeFromPath(String path) {
		int index = path.lastIndexOf(".");
		String ext = path.substring(index + 1, path.length());
		if (ext.equalsIgnoreCase("webm")) {
			return "video/webm";
		}
		return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
	}

	public static boolean isTablet(Context context) {
		return (context.getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	@SuppressWarnings("rawtypes")
	public static Class getActivity(WebMediumConfig config) {
		if (config instanceof ChannelConfig) {
			return ChannelActivity.class;
		} else if (config instanceof ForumConfig) {
			return ForumActivity.class;
		} else if (config instanceof InstanceConfig) {
			return BotActivity.class;
		} else if (config instanceof DomainConfig) {
			return DomainActivity.class;
		} else if (config instanceof AvatarConfig) {
			return AvatarActivity.class;
		} else if (config instanceof ScriptConfig) {
			return ScriptActivity.class;
		} else if (config instanceof GraphicConfig) {
			return GraphicActivity.class;
		}
		return null;
	}

	public static void error(String message, Exception exception, Activity activity) {
		try {
			if (DEBUG) {
				System.out.println(String.valueOf(message));
				if (exception != null) {
					exception.printStackTrace();
				}
			}
			if (message == null) {
				message = "";
			}
			if (message.contains("<html>")) {
				message = "Server Error, ensure you are connected to the Internet";
			}
			MainActivity.showMessage(message, activity);
		} catch (Throwable error) {
			error.printStackTrace();
		}
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
				HttpVerifyEmailAction action = new HttpVerifyEmailAction(activity, MainActivity.user);
				action.execute();
			}
		});
		dialog.show();
	}

	public static void showMessage(String title, String message, Activity activity) {
		try {
			AlertDialog dialog = new AlertDialog.Builder(activity).create();
			dialog.setMessage(message);
			if (title != null) {
				dialog.setTitle(title);
			}
			dialog.setCancelable(false);
			dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {}
			});
			dialog.show();
		} catch (Exception exception) {
			Log.wtf("showMessage", exception);
		}
	}

	public static void showMessage(String message, Activity activity) {
		showMessage(null, message, activity);
	}

	public static void prompt(String message, Activity activity, EditText text, DialogInterface.OnClickListener listener) {
		AlertDialog dialog =  new AlertDialog.Builder(activity).create();
		dialog.setMessage(message);
		dialog.setView(text);
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", listener);
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Do nothing.
			}
		});
		dialog.show();
	}

	public void contentRatingList() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Content Rating");
		final Spinner spin = new Spinner(this);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,contentRatings);
		spin.setAdapter(adapter);
	
		if (contentRating != null){
			for (int i = 0; i < adapter.getCount(); i++) {
				if (spin.getItemAtPosition(i).equals(contentRating)) {
					spin.setSelection(i);
				}
			}
		} else {
			spin.setSelection(1);
		}
		builder.setView(spin);
		builder.setMessage("Select Content Rating: ")
				.setPositiveButton("Select", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// save data
						SharedPreferences.Editor editor = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
						editor.putString("contentRating", (String) spin.getSelectedItem());
						contentRating = (String) spin.getSelectedItem();
						editor.commit();
					}
				}).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

					}
				});
		builder.create().show();
	}

	public static void confirm(String message, Activity activity, DialogInterface.OnClickListener listener) {
		AlertDialog dialog =  new AlertDialog.Builder(activity).create();
		dialog.setMessage(message);
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", listener);
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Do nothing.
			}
		});
		dialog.show();
	}

	public static List<InstanceConfig> getAllTemplates(Activity activity) {
		if (templates == null) {
			try {
				HttpGetTemplatesAction action = new HttpGetTemplatesAction(activity);
				action.postExecute(action.execute().get());
				if (action.getException() != null) {
					templates = new ArrayList<InstanceConfig>();
				}
			} catch (Exception ignore) {}
		}
		return templates;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (MainActivity.showAds && (MainActivity.showFacebookBanner || MainActivity.showFacebookPopupAds)) {
			AudienceNetworkAds.initialize(this);
		}
		if (MainActivity.showAds && MainActivity.showPopupAds) {
			mInterstitialAd = new InterstitialAd(this);
			mInterstitialAd.setAdUnitId(popupAd);
			mInterstitialAd.loadAd(new AdRequest.Builder().build());
		}
		if (MainActivity.showAds && MainActivity.showVideoAds) {
			MobileAds.initialize(this, videoAd);
			mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
			mRewardedVideoAd.loadAd(videoAd, new AdRequest.Builder().build());
		}
		if (MainActivity.showAds && MainActivity.showFacebookPopupAds) {
			facebookInterstitialAd = new com.facebook.ads.InterstitialAd(this, facebookPopupAd);
			facebookInterstitialAd.loadAd();
		}

		current = this;

		try {
			SharedPreferences cookies = current.getPreferences(Context.MODE_PRIVATE);

			// saved handsFreeSpeech toggle - load cookies.
			handsFreeSpeech = cookies.getBoolean("handsfreespeech", handsFreeSpeech);
			listenInBackground = cookies.getBoolean("listenInBackground", listenInBackground);
			offlineSpeech = cookies.getBoolean("offlineSpeech", offlineSpeech);
			contentRating = cookies.getString("contentRating", contentRating);
			ChatActivity.DEBUG = cookies.getBoolean("debug", ChatActivity.DEBUG);

			if (user == null) {
				if (MainActivity.accountDeleted) {
					MainActivity.accountDeleted = false;
				}
				String user = cookies.getString("user", null);
				String token = cookies.getString("token", null);
				boolean terms = cookies.getBoolean("terms", false);
				//Check Volume and make sure its above 5%
				AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
				int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
				if (volume == 0 || volume < 2) {
					Toast.makeText(this, "Please check 'Media' volume", Toast.LENGTH_LONG).show();
				}

				if ((user != null) && (token != null)) {
					UserConfig config = new UserConfig();
					config.user = user;
					config.token = token;
					config.acceptTerms = terms;
					HttpConnectAction action = new HttpConnectAction(this, config, false);
					action.execute();
				}

				hasRequestAvatar = cookies.getBoolean("hasrequestavatar", false);

				String language = cookies.getString("language", null);
				boolean translate = cookies.getString("translate", "").equals("true");
				boolean deviceVoice = cookies.getString("deviceVoice", "").equals("true");
				String voiceXML = cookies.getString("voiceConfigXML", "");

				if (translate) {
					MainActivity.translate = true;
				}
				if (deviceVoice) {
					MainActivity.forceDeviceVoice = true;
				}
				if (!voiceXML.equals("")) {
					try {
						Element root = MainActivity.connection.parse(voiceXML);
						VoiceConfig voiceConfig = new VoiceConfig();
						voiceConfig.parseXML(root);
						MainActivity.customVoice = true;
						MainActivity.voice = voiceConfig;
						MainActivity.deviceVoice = voiceConfig.nativeVoice;
					} catch (Exception exception) {
						Log.wtf("voiceXML", exception);
					}
				} else {
					if (translate) {
						MainActivity.voice = new VoiceConfig();
						MainActivity.voice.language = language;
						MainActivity.voice.nativeVoice = true;
						MainActivity.deviceVoice = true;
					} else if (deviceVoice) {
						MainActivity.deviceVoice = true;
					}
				}
			}
		} catch (Exception exception) {
			Log.wtf("main", exception);
		}

		setContentView(R.layout.activity_main_chat);

		resetView();
		searching = false;

		hd = isTablet(this);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void resetView() {

		VideoView videoView = findViewById(R.id.main_backgroundVideo);
		videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.avatar_blink));
		videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.setLooping(true);
			}
		});
		videoView.start();

		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			ArrayAdapter adapter = new ArrayAdapter(this,
					android.R.layout.simple_spinner_dropdown_item, MainActivity.types);
			spin.setAdapter(adapter);
			spin.setSelection(Arrays.asList(MainActivity.types).indexOf(MainActivity.type));
			spin.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
					resetLast();
				}

				@Override
				public void onNothingSelected(AdapterView<?> parentView) {
				}
			});
		}
		if (domain != null) {
			setTitle(Utils.stripTags(domain.name));
			HttpGetImageAction.fetchImage(this, domain.avatar, (ImageView) findViewById(R.id.splash));
		}

		/*if (MainActivity.user == null) {
			findViewById(R.id.viewUserButton).setVisibility(View.GONE);
			findViewById(R.id.logoutButton).setVisibility(View.GONE);
			findViewById(R.id.loginButton).setVisibility(View.VISIBLE);
			findViewById(R.id.newMessageButton).setVisibility(View.GONE);
		} else {
			findViewById(R.id.logoutButton).setVisibility(View.VISIBLE);
			findViewById(R.id.loginButton).setVisibility(View.GONE);
			findViewById(R.id.viewUserButton).setVisibility(View.VISIBLE);
			findViewById(R.id.newMessageButton).setVisibility(View.VISIBLE);
			HttpGetImageAction.fetchImage(this, MainActivity.user.avatar, findViewById(R.id.viewUserButton));
			if (MainActivity.user.newMessage) {
				findViewById(R.id.newMessageButton).setBackground(getResources().getDrawable(R.drawable.messages_black));
			} else {
				findViewById(R.id.newMessageButton).setBackground(getResources().getDrawable(R.drawable.messages_white));
			}
		}*/
		MessagesActivity.setMostRecentMessage(null);
		//resetMenu(this.menu);
		//resetLast();

		System.out.println("resetview");
		if (user == null) {
			findViewById(R.id.startButton).setVisibility(View.VISIBLE);
			findViewById(R.id.logoutButton).setVisibility(View.GONE);
		} else {
			findViewById(R.id.startButton).setVisibility(View.GONE);
			findViewById(R.id.logoutButton).setVisibility(View.VISIBLE);
		}
		if (launch == true && user != null) {
			launch(null);
		} else {
			launch = true;
		}
	}

	public void resetLast() {
		String type = MainActivity.defaultType;
		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			type = (String) spin.getSelectedItem();
			if (type.equals("Chat Bot Wars")) {
				spin.setSelection(0);
				war(null);
				return;
			}
		}
		if (type == null) {
			type = MainActivity.defaultType;
		}
		Button button = (Button) findViewById(R.id.lastButton);
		if (button != null) {
			SharedPreferences cookies = getPreferences(Context.MODE_PRIVATE);
			String last = null;
			if (type.equals("Forums")) {
				last = cookies.getString("forum", null);
			} else if (type.equals("Live Chat")) {
				last = cookies.getString("channel", null);
			} else if (type.equals("Workspaces")) {
				last = cookies.getString("domain", null);
			} else if (type.equals("Avatars")) {
				last = cookies.getString("avatar", null);
			} else if (type.equals("Scripts")) {
				last = cookies.getString("script", null);
			} else if (type.equals("Graphics")) {
				last = cookies.getString("graphic", null);
			} else {
				last = cookies.getString("instance", null);
			}

			if (last != null) {
				button.setText(last);
				button.setVisibility(View.VISIBLE);
			} else {
				button.setVisibility(View.GONE);
			}
		}

	}

	public void openLast(View view) {

		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			type = (String) spin.getSelectedItem();
		}
		if (type == null) {
			type = MainActivity.defaultType;
		}
		SharedPreferences cookies = getPreferences(Context.MODE_PRIVATE);
		String last = null;
		if (type.equals("Forums")) {
			last = cookies.getString("forum", null);
		} else if (type.equals("Live Chat")) {
			last = cookies.getString("channel", null);
		} else if (type.equals("Workspaces")) {
			last = cookies.getString("domain", null);
		} else if (type.equals("Avatars")) {
			last = cookies.getString("avatar", null);
		} else if (type.equals("Scripts")) {
			last = cookies.getString("script", null);
		} else if (type.equals("Graphics")) {
			last = cookies.getString("graphic", null);
		} else {
			last = cookies.getString("instance", null);
		}

		if (last == null) {
			MainActivity.showMessage("Invalid cookie", this);
			return;
		}

		WebMediumConfig config = null;
		if (type.equals("Forums")) {
			config = new ForumConfig();
		} else if (type.equals("Live Chat")) {
			config = new ChannelConfig();
		} else if (type.equals("Workspaces")) {
			config = new DomainConfig();
		} else if (type.equals("Avatars")) {
			config = new AvatarConfig();
		} else if (type.equals("Scripts")) {
			config = new ScriptConfig();
		} else if (type.equals("Graphics")) {
			config = new GraphicConfig();
		} else {
			config = new InstanceConfig();
		}
		config.name = last;

		HttpAction action = new HttpFetchAction(this, config);
		action.execute();
	}

	@Override
	public void onResume() {
		/*if (MainActivity.showAds && MainActivity.showPopupAds) {
			//loadAd
			mInterstitialAd = new InterstitialAd(this);
			mInterstitialAd.setAdUnitId("ca-app-pub-8254066784819048/8769990160");
			mInterstitialAd.loadAd(new AdRequest.Builder().build());
		}*/
		
		searching = false;
		browsing = false;
		searchingPosts = false;
		importingBotLog = false;
		importingBotScript = false;
		resetView();
		if (user != null) {
			SharedPreferences.Editor cookies = getPreferences(Context.MODE_PRIVATE).edit();
			cookies.putString("user", MainActivity.user.user);
			cookies.putString("token", MainActivity.user.token);
			cookies.commit();
		}
		if (BIRTHDATE_FAILED) {
			BIRTHDATE_FAILED = false;
			finish();
		}
		if (accountDeleted) {
			logout();
		}

		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu_main, menu);
		this.menu = menu;
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		resetMenu(menu);
		return true;
	}

	public void resetMenu(Menu menu) {
		if (menu == null) {
			return;
		}
		for (int index = 0; index < menu.size(); index++) {
			menu.getItem(index).setVisible(true);
		}
		if (MainActivity.user == null) {
			menu.findItem(R.id.menuMyBots).setVisible(false);
			menu.findItem(R.id.menuSignOut).setVisible(false);
			menu.findItem(R.id.menuViewUser).setVisible(false);
			menu.findItem(R.id.menuEditUser).setVisible(false);
			menu.findItem(R.id.menuVerifyEmail).setVisible(false);
			menu.findItem(R.id.menuMessages).setVisible(false);
		} else {
			menu.findItem(R.id.menuSignIn).setVisible(false);
			menu.findItem(R.id.menuSignUp).setVisible(false);
		}

		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			type = (String) spin.getSelectedItem();
		}
		if (type == null) {
			type = MainActivity.defaultType;
		}
		MenuItem item = menu.findItem(R.id.menuMyBots);
		if (type.equals("Bots")) {
			item.setTitle("My Bots");
		} else if (type.equals("Forums")) {
			item.setTitle("My Forums");
		} else if (type.equals("Live Chat")) {
			item.setTitle("My Channels");
		} else if (type.equals("Workspaces")) {
			item.setTitle("My Workspaces");
		} else if (type.equals("Avatars")) {
			item.setTitle("My Avatars");
		} else if (type.equals("Scripts")){
			item.setTitle("My Scripts");
		}else if (type.equals("Graphics")){
			item.setTitle("My Graphics");
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.menuMyBots:
				browseMyBots();
				return true;
			case R.id.menuWar:
				war(null);
				return true;
			case R.id.menuChangeLanguage:
				changeLanguage(this, null);
				return true;
			case R.id.menuSearch:
				search();
				return true;
			case R.id.menuMessages:
				userMessages();
				return true;
			case R.id.menuSignIn:
				login();
				return true;
			case R.id.menuSignUp:
				createUser();
				return true;
			case R.id.menuSignOut:
				logout();
				return true;
			case R.id.menuEditUser:
				editUser();
				return true;
			case R.id.menuVerifyEmail:
				verifyEmail();
				return true;
			case R.id.menuViewUser:
				viewUser();
				return true;
			case R.id.menuWebsite:
				openWebsite();
				return true;
			//case R.id.menuChangeServer:
			//	changeServer();
			//	return true;
			case R.id.menuHelp:
				help(null);
				return true;
			case R.id.menuContentRating:
				contentRatingList();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void verifyEmail() {
		if (MainActivity.user == null) {
			return;
		}
		showEmailDialog(getApplicationContext().getString(R.string.emailVerification),
				getApplicationContext().getString(R.string.enterEmailMessage), MainActivity.user.email, this);
	}

	public void war(View view) {
		Intent intent = new Intent(this, StartWarActivity.class);
		startActivity(intent);
	}

	public void login(View view) {
		login();
	}

	public void logout(View view) {
		logout();
	}

	public void login() {
		System.out.println("login:" + user);
		if (user != null) {
			launch(null);
			return;
		}
		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			type = (String)spin.getSelectedItem();
		}

		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
	}

	public void signUp(View view) {
		signUp();
	}

	public void signUp() {
		Intent intent = new Intent(this, CreateUserActivity.class);
		startActivity(intent);
	}

	public void logout() {
		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			type = (String)spin.getSelectedItem();
		}
		connection.disconnect();
		user = null;
		instance = null;
		conversation = null;
		post = null;
		posts = new ArrayList<ForumPostConfig>();
		instances = new ArrayList<WebMediumConfig>();
		domain = null;
		tags = null;
		categories = null;
		learning = null;
		voice = null;
		customVoice = false;
		translate = false;

		SharedPreferences.Editor editor = getPreferences(Context.MODE_PRIVATE).edit();
		editor.remove("user");
		editor.remove("token");
		editor.remove("instance");
		editor.remove("forum");
		editor.remove("channel");
		editor.remove("domain");
		editor.remove("avatar");
		editor.remove("graphic");
		editor.remove("voice");
		editor.remove("language");
		editor.remove("nativeVoice");
		editor.remove("translate");
		editor.remove("voiceConfigXML");
		editor.remove("customAvatar");
		editor.remove("avatarID");
		editor.remove("botId");
		editor.remove("deviceVoice");
		editor.commit();

		HttpGetImageAction.clearFileCache(this);

		Intent intent = getIntent();
		finish();
		startActivity(intent);
	}

	public void menu(View view) {
		PopupMenu popup = new PopupMenu(this, view);
		MenuInflater inflater = popup.getMenuInflater();
		inflater.inflate(R.menu.menu_main, popup.getMenu());
		onPrepareOptionsMenu(popup.getMenu());
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				return onOptionsItemSelected(item);
			}
		});
		popup.show();
	}

	public void createUser() {
		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			type = (String)spin.getSelectedItem();
		}

		Intent intent = new Intent(this, CreateUserActivity.class);
		startActivity(intent);
	}

	public void editUser() {
		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			type = (String)spin.getSelectedItem();
		}

		Intent intent = new Intent(this, EditUserActivity.class);
		startActivity(intent);
	}

	public void help(View view) {
		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			type = (String)spin.getSelectedItem();
		}
		super.help(view);
	}

	public void upgrade(View view) {

	}

	public void viewUser(View view) {
		viewUser();
	}

	public void viewUser() {
		viewUser = user;
		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			type = (String)spin.getSelectedItem();
		}

		Intent intent = new Intent(this, ViewUserActivity.class);
		startActivity(intent);
	}

	public void openWebsite() {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
			startActivity(intent);
		} catch (Exception exception) {
			error("Unable to open website", exception, this);
		}
	}

	public void launchCreate(View view) {
		createInstance(view);
	}

	public void createInstance(final View view) {
		if (user == null) {
			AlertDialog dialog = new AlertDialog.Builder(this).create();
			dialog.setMessage(getApplicationContext().getString(R.string.signInFirst));
			dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					login();
				}
			});
			dialog.show();
			return;
		}
		if ((user.type == null || user.type.isEmpty() || user.type.equals("Basic")) && showAds && showVideoAds) {
			AlertDialog dialog = new AlertDialog.Builder(this).create();
			dialog.setMessage(getApplicationContext().getString(R.string.upgradeOrAd));
			dialog.setButton(AlertDialog.BUTTON_POSITIVE, getApplicationContext().getString(R.string.upgrade), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					upgrade(view);
				}
			});
			dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getApplicationContext().getString(R.string.viewAd), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					createInstance2(view);
				}
			});
			dialog.show();
			return;
		}
		createInstance2(view);
	}

	public void createInstance2(final View view) {
		if ((user.type == null || user.type.isEmpty() || user.type.equals("Basic")) && showAds && showVideoAds) {
			//ad before create
			mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
				boolean reward = false;

				@Override
				public void onRewarded(RewardItem reward) {
					this.reward = true;
				}

				@Override
				public void onRewardedVideoAdLeftApplication() {
				}

				@Override
				public void onRewardedVideoAdClosed() {
					// Load the next interstitial.
					mRewardedVideoAd.loadAd(videoAd, new AdRequest.Builder().build());
					if (this.reward) {
						createInstance3(view);
					}
				}

				@Override
				public void onRewardedVideoAdFailedToLoad(int errorCode) {
					// Load the next interstitial.
					mRewardedVideoAd.loadAd(videoAd, new AdRequest.Builder().build());
					createInstance3(view);
				}

				@Override
				public void onRewardedVideoAdLoaded() {
				}

				@Override
				public void onRewardedVideoAdOpened() {
				}

				@Override
				public void onRewardedVideoStarted() {
				}
			});
			if (mRewardedVideoAd.isLoaded()) {
				mRewardedVideoAd.show();
				return;
			} else {
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (mRewardedVideoAd.isLoaded()) {
							mRewardedVideoAd.show();
							return;
						} else {
							createInstance3(view);
						}
					}
				}, 4000);
				Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
				return;
			}
		} else if (showAds && showPopupAds) {
			//ad before create
			mInterstitialAd.setAdListener(new AdListener() {
				@Override
				public void onAdLoaded() {
				}
				@Override
				public void onAdClosed() {
					// Load the next interstitial.
					mInterstitialAd.loadAd(new AdRequest.Builder().build());
					createInstance3(view);
				}
				@Override
				public void onAdFailedToLoad(int errorCode) {
					// Load the next interstitial.
					mInterstitialAd.loadAd(new AdRequest.Builder().build());
					createInstance3(view);
				}
			});
			if (mInterstitialAd.isLoaded()) {
				mInterstitialAd.show();
				return;
			} else {
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (mInterstitialAd.isLoaded()) {
							mInterstitialAd.show();
							return;
						} else {
							createInstance3(view);
						}
					}
				}, 2000);
				Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
				return;
			}
		} else {
			createInstance3(view);
		}

		if (showAds && showFacebookPopupAds) {
			//ad before chatActivity
			facebookInterstitialAd.setAdListener(new com.facebook.ads.InterstitialAdListener() {
				@Override
				public void onInterstitialDisplayed(Ad ad) {
				}

				@Override
				public void onInterstitialDismissed(Ad ad) {
					// Load the next interstitial.
					facebookInterstitialAd.loadAd();
					createInstance3(view);
				}

				@Override
				public void onError(Ad ad, AdError adError) {
					// Load the next interstitial.
					facebookInterstitialAd.loadAd();
					createInstance3(view);
				}

				@Override
				public void onAdLoaded(Ad ad) {
				}

				@Override
				public void onAdClicked(Ad ad) {
				}

				@Override
				public void onLoggingImpression(Ad ad) {
				}
			});
			if (facebookInterstitialAd.isAdLoaded()) {
				facebookInterstitialAd.show();
				return;
			} else {
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (facebookInterstitialAd.isAdLoaded()) {
							facebookInterstitialAd.show();
							return;
						} else {
							createInstance3(view);
						}
					}
				}, 1000);
				Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
				return;
			}
		}
	}

	public void createInstance3(View view) {
		Spinner spin = (Spinner) findViewById(getResources().getIdentifier("typeSpin", "id", getPackageName()));
		if (spin != null) {
			type = (String)spin.getSelectedItem();
		}
		if (type == null) {
			type = MainActivity.defaultType;
		}
		if (type.equals("Bots")) {
			MainActivity.template = "";
			Intent intent = new Intent(this, CreateBotActivity.class);
			startActivity(intent);
		} else if (type.equals("Forums")) {
			Intent intent = new Intent(this, CreateForumActivity.class);
			startActivity(intent);
		} else if (type.equals("Live Chat")) {
			Intent intent = new Intent(this, CreateChannelActivity.class);
			startActivity(intent);
		} else if (type.equals("Workspaces")) {
			Intent intent = new Intent(this, CreateDomainActivity.class);
			startActivity(intent);
		} else if (type.equals("Avatars")) {
			Intent intent = new Intent(this, CreateAvatarActivity.class);
			startActivity(intent);
		} else if (type.equals("Scripts")) {
			Intent intent = new Intent(this, CreateScriptActivity.class);
			startActivity(intent);
		} else if (type.equals("Graphics")) {
			Intent intent = new Intent(this, CreateGraphicActivity.class);
			startActivity(intent);
		}
	}

	public void search() {
		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			type = (String)spin.getSelectedItem();
		}
		if (type == null) {
			type = MainActivity.defaultType;
		}
		SharedPreferences cookie = current.getPreferences(Context.MODE_PRIVATE);
		boolean terms = cookie.getBoolean("terms", false);
		if (!terms) {
			Intent termsIntent = new Intent(this, TermsActivity.class);
			startActivity(termsIntent);
			TermsActivity.searchType = "Search";
			return;
		}
		if (type.equals("Bots")) {
			Intent intent = new Intent(this, BotSearchActivity.class);
			startActivity(intent);
		} else if (type.equals("Forums")) {
			Intent intent = new Intent(this, ForumSearchActivity.class);
			startActivity(intent);
		} else if (type.equals("Live Chat")) {
			Intent intent = new Intent(this, ChannelSearchActivity.class);
			startActivity(intent);
		} else if (type.equals("Workspaces")) {
			Intent intent = new Intent(this, DomainSearchActivity.class);
			startActivity(intent);
		} else if (type.equals("Avatars")) {
			Intent intent = new Intent(this, AvatarSearchActivity.class);
			startActivity(intent);
		} else if (type.equals("Scripts")) {
			Intent intent = new Intent(this, ScriptSearchActivity.class);
			startActivity(intent);
		} else if (type.equals("Graphics")) {
			Intent intent = new Intent(this, GraphicSearchActivity.class);
			startActivity(intent);
		}
	}

	public void userMessages(View view) {
		userMessages();
	}

	public void userMessages() {
		if (MainActivity.user.newMessage) {
			MainActivity.user.newMessage = false;
			findViewById(R.id.newMessageButton).setBackground(getResources().getDrawable(R.drawable.messages_black));
		} else {
			findViewById(R.id.newMessageButton).setBackground(getResources().getDrawable(R.drawable.messages_white));
		}
		Intent intent = new Intent(this, UserMessagesActivity.class);
		startActivity(intent);
	}

	public void browseMyBots() {
		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			type = (String)spin.getSelectedItem();
		}
		if (type == null) {
			type = MainActivity.defaultType;
		}
		BrowseConfig config = new BrowseConfig();
		config.typeFilter = "Personal";
		if (type.equals("Bots")) {
			config.type = "Bot";
		} else if (type.equals("Forums")) {
			config.type = "Forum";
		} else if (type.equals("Live Chat")) {
			config.type = "Channel";
		} else if (type.equals("Workspaces")) {
			config.type = "Domain";
		} else if (type.equals("Avatars")) {
			config.type = "Avatar";
		} else if (type.equals("Scripts")) {
			config.type = "Script";
		} else if (type.equals("Graphics")) {
			config.type = "Graphic";
		}
		config.contentRating = "Mature";
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config);
		action.execute();
	}

	public void browse(View view) {
		Spinner spin = (Spinner) findViewById(R.id.typeSpin);
		if (spin != null) {
			type = (String)spin.getSelectedItem();
		}
		if (type == null) {
			type = MainActivity.defaultType;
		}

		SharedPreferences cookie = current.getPreferences(Context.MODE_PRIVATE);
		boolean terms = cookie.getBoolean("terms", false);
		if (!terms) {
			Intent termsIntent = new Intent(this, TermsActivity.class);
			startActivity(termsIntent);
			TermsActivity.searchType = "Browse";
			return;
		}

		BrowseConfig config = new BrowseConfig();
		if (type.equals("Bots")) {
			config.type = "Bot";
		} else if (type.equals("Forums")) {
			config.type = "Forum";
		} else if (type.equals("Live Chat")) {
			config.type = "Channel";
		} else if (type.equals("Workspaces")) {
			config.type = "Domain";
		} else if (type.equals("Avatars")) {
			config.type = "Avatar";
		} else if (type.equals("Scripts")) {
			importingBotScript = false;
			config.type = "Script";
		} else if (type.equals("Graphics")) {
			config.type = "Graphic";
		}
		config.contentRating = MainActivity.contentRating;
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config);
		action.execute();
	}

	public void launchBrowse(View view) {
		BrowseConfig config = new BrowseConfig();
		config.type = "Bot";
		config.contentRating = MainActivity.contentRating;

		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config);
		action.execute();
	}

	/**
	 * Open the details screen.
	 */
	public void about() {
		WebMediumConfig config = null;
		if (launchType == LaunchType.Bot) {
			config = new InstanceConfig();
		} else if (launchType == LaunchType.Forum) {
			config = new ForumConfig();
		} else if (launchType == LaunchType.Channel) {
			config = new ChannelConfig();
		}
		config.id = launchInstanceId;
		config.name = launchInstanceName;

		HttpAction action = new HttpFetchAction(this, config, false);
		action.execute();
	}

	/**
	 * Start a chat session with the hard coded instance.
	 */
	public void launch(final View view) {
		if (showAds && showPopupAds) {
			//ad before chatActivity
			mInterstitialAd.setAdListener(new AdListener() {
				@Override
				public void onAdLoaded() {
				}
				@Override
				public void onAdClosed() {
					// Load the next interstitial.
					mInterstitialAd.loadAd(new AdRequest.Builder().build());

					launch2(view);
				}
				@Override
				public void onAdFailedToLoad(int errorCode) {
					// Load the next interstitial.
					mInterstitialAd.loadAd(new AdRequest.Builder().build());
					launch2(view);
				}
			});
			if (mInterstitialAd.isLoaded()) {
				mInterstitialAd.show();
				return;
			} else {
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (mInterstitialAd.isLoaded()) {
							mInterstitialAd.show();
							return;
						} else {
							launch2(view);
						}
					}
				}, 1000);
				Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
				return;
			}
		}
		if (showAds && showFacebookPopupAds) {
			//ad before chatActivity
			facebookInterstitialAd.setAdListener(new com.facebook.ads.InterstitialAdListener() {
				@Override
				public void onInterstitialDisplayed(Ad ad) {
				}

				@Override
				public void onInterstitialDismissed(Ad ad) {
					// Load the next interstitial.
					facebookInterstitialAd.loadAd();
					launch2(view);
				}

				@Override
				public void onError(Ad ad, AdError adError) {
					// Load the next interstitial.
					facebookInterstitialAd.loadAd();
					launch2(view);
				}

				@Override
				public void onAdLoaded(Ad ad) {
				}

				@Override
				public void onAdClicked(Ad ad) {
				}

				@Override
				public void onLoggingImpression(Ad ad) {
				}
			});
			if (facebookInterstitialAd.isAdLoaded()) {
				facebookInterstitialAd.show();
				return;
			} else {
				Handler handler = new Handler();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (facebookInterstitialAd.isAdLoaded()) {
							facebookInterstitialAd.show();
							return;
						} else {
							launch2(view);
						}
					}
				}, 1000);
				Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
				return;
			}
		}

		launch2(view);
	}

	/**
	 * Start a chat session with the hard coded instance.
	 */
	public void launch2(View view) {

		WebMediumConfig config = null;
		if (launchType == LaunchType.Bot) {
			config = new InstanceConfig();
		} else if (launchType == LaunchType.Forum) {
			config = new ForumConfig();
		} else if (launchType == LaunchType.Channel) {
			config = new ChannelConfig();
		}
		config.id = launchInstanceId;
		config.name = launchInstanceName;

		HttpAction action = new HttpFetchAction(this, config, true);
		action.execute();
	}

	/**
	 * View the user's personal instance.
	 */
	public void browseMyBot() {
		if (user == null) {
			AlertDialog dialog = new AlertDialog.Builder(this).create();
			dialog.setMessage("You must sign in first");
			dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					login();
				}
			});
			dialog.show();
			return;
		}
		if (user.type == null || user.type.isEmpty() || user.type.equals("Basic")) {
			AlertDialog dialog = new AlertDialog.Builder(this).create();
			dialog.setMessage("You must upgrade your account");
			dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					upgrade(null);
				}
			});
			dialog.show();
			return;
		}
		WebMediumConfig config = new InstanceConfig();
		config.name = prefix + " " + MainActivity.user.user;

		HttpAction action = new HttpFetchOrCreateAction(this, config, false);
		action.execute();
	}

	/**
	 * Start a chat session with the user's personal instance.
	 */
	public void launchMyBot(View view) {
		if (user == null) {
			AlertDialog dialog = new AlertDialog.Builder(this).create();
			dialog.setMessage("You must sign in first");
			dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					login();
				}
			});
			dialog.show();
			return;
		}
		if (user.type == null || user.type.isEmpty() || user.type.equals("Basic")) {
			AlertDialog dialog = new AlertDialog.Builder(this).create();
			dialog.setMessage("You must upgrade your account");
			dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					upgrade(null);
				}
			});
			dialog.show();
			return;
		}
		WebMediumConfig config = new InstanceConfig();
		config.name = prefix + " " + MainActivity.user.user;

		HttpAction action = new HttpFetchOrCreateAction(this, config, true);
		action.execute();
	}

	/**
	 * Browse the user's bots.
	 */
	public void launchMyBots(View view) {
		if (user == null) {
			AlertDialog dialog = new AlertDialog.Builder(this).create();
			dialog.setMessage(getApplicationContext().getString(R.string.signInFirst));
			dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					login();
				}
			});
			dialog.show();
			return;
		}
		browseMyBots();
	}
	
	/*@SuppressWarnings({ "rawtypes", "unchecked" })
	public void changeServer() {
		AlertDialog dialog =  new AlertDialog.Builder(this).create();
		dialog.setMessage("Enter the domain name of the new server to switch to");
		final AutoCompleteTextView text = new AutoCompleteTextView(this);
		text.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		ArrayAdapter adapter = new ArrayAdapter(this,
				android.R.layout.select_dialog_item, MainActivity.servers);
		text.setThreshold(0);
		text.setAdapter(adapter);
		text.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event){
				text.showDropDown();
				return false;
			}
		});
		dialog.setView(text);
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String server = text.getText().toString();
				WEBSITE = "http://" + server;
				SERVER = server;
				Credentials credentials = new Credentials();
				credentials.url = "http://" + server + "/rest/api";
				credentials.host = server;
				if (server.equals("www.botlibre.com")) {
					credentials.applicationId = applicationId;
				} else if (server.equals("twitter.botlibre.com")) {
					credentials.applicationId = twitterAppId;
				} else if (server.equals("twitter.botlibre.com")) {
					credentials.applicationId = twitterAppId;
				} else if (server.equals("www.botlibre.biz")) {
					credentials.applicationId = paphusAppId;
				} else if (server.equals("www.paphussolutions.com")) {
					credentials.applicationId = paphusAppId;
				} else if (server.equals("www.forumslibre.com")) {
					credentials.applicationId = forumsAppId;
				} else if (server.equals("www.livechatlibre.com")) {
					credentials.applicationId = livechatAppId;
				}
				connection.setCredentials(credentials);
				logout();
			}
		});
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Do nothing.
			}
		});
		dialog.show();
	}*/

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void changeLanguage(Activity activty, final DialogInterface.OnClickListener listener) {
		AlertDialog dialog = new AlertDialog.Builder(activty).create();
		dialog.setTitle("Languages");
		dialog.setMessage("Select your language");
		final Spinner spin = new Spinner(activty);
		ArrayAdapter adapter = new ArrayAdapter(activty,
				android.R.layout.simple_spinner_dropdown_item, MainActivity.languages);
		spin.setAdapter(adapter);
		int index = -1;

		if (MainActivity.voice != null && MainActivity.voice.language != null) {
			index = Arrays.asList(MainActivity.languages).indexOf(MainActivity.voice.language);
		}
		spin.setSelection(index);
		
		if (MainActivity.voice != null && MainActivity.voice.language != null){
			for (int i = 0; i < adapter.getCount(); i++) {
				if(MainActivity.languages[i].contains(MainActivity.voice.language + " - ")){
					spin.setSelection(i);
				}
			}
		}

		dialog.setView(spin);
		dialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String lang = (String)spin.getSelectedItem();
				if (lang.equals("Default")) {
					MainActivity.translate = false;
					MainActivity.customVoice = false;
					MainActivity.voice = new VoiceConfig();
					MainActivity.deviceVoice = false;

					SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
					cookies.remove("translate");
					cookies.remove("language");
					cookies.commit();
				} else {
					MainActivity.translate = true;
					if (MainActivity.voice == null) {
						MainActivity.voice = new VoiceConfig();
					}
					MainActivity.voice.language = lang.substring(0, 2);
					MainActivity.voice.nativeVoice = true;
					MainActivity.deviceVoice = true;

					SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
					cookies.putString("translate", "true");
					cookies.putString("language", MainActivity.voice.language);
					cookies.commit();
				}
				if (listener != null) {
					listener.onClick(dialog, whichButton);
				}
			}
		});
		dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Do nothing.
			}
		});
		dialog.show();
	}

}
