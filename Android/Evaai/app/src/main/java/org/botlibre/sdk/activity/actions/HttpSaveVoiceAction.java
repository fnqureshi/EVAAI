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

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.UserVoiceActivity;
import org.botlibre.sdk.activity.VoiceActivity;
import org.botlibre.sdk.config.VoiceConfig;

import android.app.Activity;

public class HttpSaveVoiceAction extends HttpUIAction {

	VoiceConfig config;
	
	public HttpSaveVoiceAction(Activity activity, VoiceConfig config) {
		super(activity);
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
			if(this.activity instanceof UserVoiceActivity) {
				MainActivity.connection.saveUserVoice(this.config);
			} else {
				MainActivity.connection.saveVoice(this.config);
			}
		} catch (Exception exception) {
			this.exception = exception;
		}
		return "";
	}

	@Override
	public void onPostExecute(String xml) {
		super.onPostExecute(xml);
		if (this.exception != null) {
			return;
		}
		MainActivity.voice = this.config;
		
		this.activity.finish();
    }
}