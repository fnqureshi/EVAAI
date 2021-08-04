/******************************************************************************
 *
 *  Copyright 2019 Paphus Solutions Inc.
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
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetVoiceAction;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.VoiceConfig;
import io.evaai.R;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * Activity for administering a bot's voice.
 */
public class VoiceActivity extends AbstractVoiceActivity implements TextToSpeech.OnInitListener, AdapterView.OnItemSelectedListener {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		if (MainActivity.current == null || MainActivity.instance == null || !(MainActivity.instance instanceof InstanceConfig)) {
			finish();
			return;
		}
        setContentView(R.layout.activity_voice);
		initWidgets();
        HttpGetImageAction.fetchImage(this, MainActivity.instance.avatar, findViewById(R.id.icon));
		if (!(this instanceof ChangeVoiceActivity)) {
			HttpGetVoiceAction action = new HttpGetVoiceAction(this, (InstanceConfig) MainActivity.instance.credentials());
			action.execute();
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void resetView(){
		try {
	        if (MainActivity.voice == null) {
	        	MainActivity.voice = new VoiceConfig();
	        }
			ArrayList<String> spinnerList = new ArrayList<>();
	        spinnerList.add(getApplicationContext().getString(R.string.serverTypeVoice));
	        spinnerList.add(getApplicationContext().getString(R.string.deviceTypeVoice));
	        Spinner voiceTypeSpin = (Spinner) findViewById(R.id.voiceTypeSpin);
			ArrayAdapter voiceTypeAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerList);
			voiceTypeSpin.setAdapter(voiceTypeAdapter);
			voiceTypeSpin.setOnItemSelectedListener(this);
			if (MainActivity.voice.nativeVoice) {
				voiceTypeSpin.setSelection(1);
				voiceLayout.setVisibility(View.GONE);
				voiceModLayout.setVisibility(View.GONE);
				languageLayout.setVisibility(View.VISIBLE);
				speechLayout.setVisibility(View.VISIBLE);
				pitchLayout.setVisibility(View.VISIBLE);
			} else {
				voiceTypeSpin.setSelection(0);
				voiceLayout.setVisibility(View.VISIBLE);
				voiceModLayout.setVisibility(View.VISIBLE);
				languageLayout.setVisibility(View.GONE);
				speechLayout.setVisibility(View.GONE);
				pitchLayout.setVisibility(View.GONE);
			}
			speechSeekBar.setOnSeekBarChangeListener(
					new SeekBar.OnSeekBarChangeListener() {
						int currProgress = 0;
						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
							currProgress = progress;
						}
						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
						}
						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
							speechProgressValue = (float) currProgress / seekBar.getMax();
							speechProgressValue = speechProgressValue * 3.0f;
							// Make it easier to set 1.0
							if (speechProgressValue > 0.9f && speechProgressValue < 1.1f) {
								speechProgressValue = 1.0f;
							}
							Toast.makeText(VoiceActivity.this, getApplicationContext().getString(R.string.speechRate) + " " + String.valueOf(speechProgressValue), Toast.LENGTH_SHORT).show();
						}
					}
			);
			pitchSeekBar.setOnSeekBarChangeListener(
					new SeekBar.OnSeekBarChangeListener() {
						int currProgress = 0;
						@Override
						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
							currProgress = progress;
						}
						@Override
						public void onStartTrackingTouch(SeekBar seekBar) {
						}
						@Override
						public void onStopTrackingTouch(SeekBar seekBar) {
							pitchProgressValue = (float) currProgress / seekBar.getMax();
							pitchProgressValue = pitchProgressValue * 3.0f;
							// Make it easier to set 1.0
							if (pitchProgressValue > 0.9f && pitchProgressValue < 1.1f) {
								pitchProgressValue = 1.0f;
							}
							Toast.makeText(VoiceActivity.this, getApplicationContext().getString(R.string.pitch) + " " + String.valueOf(pitchProgressValue), Toast.LENGTH_SHORT).show();
						}
					}
			);

			Spinner voiceSpin = (Spinner) findViewById(R.id.voiceSpin);
			Spinner voiceSpinMod = (Spinner) findViewById(R.id.voiceModSpin);

			ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, MainActivity.voiceNames);
			voiceSpin.setAdapter(adapter);
			int index = Arrays.asList(MainActivity.voices).indexOf(MainActivity.voice.voice);
			if (index != -1) {
				voiceSpin.setSelection(index);
			}
			
			ArrayAdapter adapter1 = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, MainActivity.voiceMods);
			voiceSpinMod.setAdapter(adapter1);
			index = Arrays.asList(MainActivity.voiceMods).indexOf(MainActivity.voice.mod);
			if (index != -1) {
				voiceSpinMod.setSelection(index);
			}

			if (MainActivity.voice.speechRate != null && !MainActivity.voice.speechRate.equals("")) {
				speechProgressValue = Float.valueOf(MainActivity.voice.speechRate);
			} else {
				speechProgressValue = 1.0f;
			}
			float speechVal = speechProgressValue / 3.0f;
			speechSeekBar.setProgress((int) (speechVal * 100));

			if (MainActivity.voice.pitch != null && !MainActivity.voice.pitch.equals("")) {
				pitchProgressValue = Float.valueOf(MainActivity.voice.pitch);
			} else {
				pitchProgressValue = 1.0f;
			}
			float pitchVal = pitchProgressValue / 3.0f;
			pitchSeekBar.setProgress((int) (pitchVal * 100));
		} catch (Exception exception) {
			MainActivity.error(exception.getMessage(), exception, this);
		}
	}

	public void resetSpeech(View view) {
		super.resetSpeech();
	}

	public void resetPitch(View view) {
		super.resetPitch();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void save(View view) {
		super.save();
	}

	public void test(View view) {
		super.test();
	}
}
