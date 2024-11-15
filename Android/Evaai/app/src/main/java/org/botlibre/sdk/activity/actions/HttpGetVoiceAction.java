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

package org.botlibre.sdk.activity.actions;

import android.app.Activity;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.UserVoiceActivity;
import org.botlibre.sdk.activity.VoiceActivity;
import org.botlibre.sdk.config.Config;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.UserConfig;
import org.botlibre.sdk.config.VoiceConfig;

public class HttpGetVoiceAction extends HttpAction {
	Config config;
	VoiceConfig voice;

	public HttpGetVoiceAction(Activity activity, Config config) {
		super(activity);
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
			if(activity instanceof UserVoiceActivity) {
				this.voice = MainActivity.connection.getUserVoice((UserConfig) this.config);
			} else {
				this.voice = MainActivity.connection.getVoice((InstanceConfig) this.config);
			}
		} catch (Exception exception) {
			this.exception = exception;
		}
		return "";
	}

	@Override
	public void onPostExecute(String xml) {
		if (this.exception != null) {
			return;
		}
		try {
			if (!MainActivity.customVoice && !MainActivity.translate) {
				MainActivity.voice = this.voice;
				if (!MainActivity.forceDeviceVoice) {
					MainActivity.deviceVoice = this.voice.nativeVoice;
				}
			}
			if (this.activity instanceof VoiceActivity) {
				((VoiceActivity) this.activity).resetView();
			} else if (this.activity instanceof UserVoiceActivity) {
				MainActivity.user.voice = this.voice.voice;
				MainActivity.user.nativeVoice = this.voice.nativeVoice;
				MainActivity.user.language = this.voice.language;
				MainActivity.user.voiceMod = this.voice.mod;
				MainActivity.user.speechRate = this.voice.speechRate;
				MainActivity.user.pitch = this.voice.pitch;
				((UserVoiceActivity) this.activity).resetView();
			}
		} catch (Exception error) {
			this.exception = error;
			return;
		}
	}
	
}