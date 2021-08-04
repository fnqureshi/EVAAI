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

import java.util.ArrayList;
import java.util.Arrays;

import org.botlibre.sdk.activity.actions.HttpGetVoiceAction;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.VoiceConfig;
import org.w3c.dom.Element;

import io.evaai.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * Activity for changing a bot's voice.
 */
public class ChangeVoiceActivity extends VoiceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (MainActivity.current == null) {
			finish();
			return;
		}
		SharedPreferences voiceCookie = MainActivity.current.getPreferences(Context.MODE_PRIVATE);
		String voiceXML = voiceCookie.getString("voiceConfigXML", "");
		if (!voiceXML.equals("")) {
			Element root = MainActivity.connection.parse(voiceXML);
			VoiceConfig voiceConfig = new VoiceConfig();
			voiceConfig.parseXML(root);
			MainActivity.voice = voiceConfig;
		}
		this.resetView();
	}

	public void save(View view) {
		VoiceConfig config = new VoiceConfig();
		config.instance = MainActivity.instance.id;
		saveVoiceSettings(config);

		MainActivity.deviceVoice = config.nativeVoice;
		MainActivity.customVoice = true;
		MainActivity.voice = config;
    	String voiceConfigXML = config.toXML();
		SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
		cookies.putString("voiceConfigXML", voiceConfigXML);
		cookies.commit();
    	finish();
	}
}
