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

import android.os.Bundle;
import android.view.View;

import org.botlibre.sdk.activity.actions.HttpGetInstancesAction;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.InstanceConfig;

import io.evaai.R;

/**
 * Activity for viewing chat menu.
 */
public class ChatMenuActivity extends LibreActivity {

	public static String action;
	public static String command;

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
		setContentView(R.layout.activity_menu);
		action = null;
	}

	public void speak(View view) {
		action = "speak";
		finish();
	}

	public void changeVoice(View view) {
		action = "changeVoice";
		finish();
	}

	public void deviceVoice(View view) {
		action = "deviceVoice";
		finish();
	}

	public void handsFree(View view) {
		action = "handsFree";
		finish();
	}

	public void microphone(View view) {
		action = "microphone";
		finish();
	}

	public void changeLanguage(View view) {
		action = "changeLanguage";
		finish();
	}

	public void changeAvatar(View view) {
		action = "changeAvatar";
		finish();
	}

	public void customizeAvatar(View view) {
		action = "customizeAvatar";
		finish();
	}

	public void noAds(View view) {
		action = "noAds";
		finish();
	}

	public void hdVideo(View view) {
		action = "hdVideo";
		finish();
	}

	public void webmVideo(View view) {
		action = "webmVideo";
		finish();
	}

	public void disableVideo(View view) {
		action = "disableVideo";
		finish();
	}

	public void observeThoughts(View view) {
		action = "command";
		command = "observe my thoughts";
		finish();
	}

	public void soundScapes(View view) {
		action = "command";
		command = "create soundscapes";
		finish();
	}

	public void mindfulWorkout(View view) {
		action = "command";
		command = "mindful workout";
		finish();
	}

	public void whatMyFaceSaysAboutMe(View view) {
		action = "command";
		command = "what my face says about me";
		finish();
	}

	public void interpretMyDreams(View view) {
		action = "command";
		command = "interpret my dreams";
		finish();
	}

	public void findPeacefulPlace(View view) {
		action = "command";
		command = "find peaceful place";
		finish();
	}

	public void pomodoro(View view) {
		action = "command";
		command = "pomodoro";
		finish();
	}

	public void ambientMusic(View view) {
		action = "command";
		command = "create ambient music";
		finish();
	}

	public void socialSituation(View view) {
		action = "command";
		command = "simulate social situation";
		finish();
	}

	public void startMeditation(View view) {
		action = "command";
		command = "start meditation";
		finish();
	}

	public void asmr(View view) {
		action = "command";
		command = "asmr";
		finish();
	}

	public void helpMeSleep(View view) {
		action = "command";
		command = "help me sleep";
		finish();
	}

	public void shadowWork(View view) {
		action = "command";
		command = "shadow work";
		finish();
	}

	public void journal(View view) {
		MainActivity.browsing = true;
		BrowseConfig config = new BrowseConfig();
		config.type = "Forum";
		config.typeFilter = "Personal";
		config.contentRating = MainActivity.contentRating;
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config, true);
		action.execute();
	}

	public void privateChatroom(View view) {
		MainActivity.browsing = true;
		BrowseConfig config = new BrowseConfig();
		config.type = "Channel";
		config.typeFilter = "Personal";
		config.contentRating = MainActivity.contentRating;
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config, true);
		action.execute();
	}

	public void selectMenu(View view) {
		findViewById(R.id.menuScroll).setVisibility(View.VISIBLE);
		findViewById(R.id.commandScroll).setVisibility(View.GONE);

		findViewById(R.id.menuBar).setVisibility(View.VISIBLE);
		findViewById(R.id.commandBar).setVisibility(View.GONE);
	}

	public void selectCommand(View view) {
		findViewById(R.id.menuScroll).setVisibility(View.GONE);
		findViewById(R.id.commandScroll).setVisibility(View.VISIBLE);

		findViewById(R.id.menuBar).setVisibility(View.GONE);
		findViewById(R.id.commandBar).setVisibility(View.VISIBLE);
	}
}
