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

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import io.evaai.R;

import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpGetVideoAction;
import org.botlibre.sdk.activity.actions.HttpGetVoiceAction;
import org.botlibre.sdk.activity.actions.HttpSaveVoiceAction;
import org.botlibre.sdk.activity.actions.HttpSpeechAction;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.Speech;
import org.botlibre.sdk.config.VoiceConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public abstract class AbstractVoiceActivity extends LibreActivity implements TextToSpeech.OnInitListener, AdapterView.OnItemSelectedListener {
    protected static final int RESULT_SPEECH = 1;

    private TextToSpeech tts;
    LinearLayout voiceLayout, voiceModLayout;
    LinearLayout languageLayout, speechLayout, pitchLayout;
    SeekBar speechSeekBar, pitchSeekBar;
    public static float speechProgressValue = 1.0f;
    public static float pitchProgressValue = 1.0f;
    private boolean testDeviceVoice;

    public void initWidgets() {
        voiceLayout = (LinearLayout) findViewById(R.id.voiceLayout);
        voiceModLayout = (LinearLayout) findViewById(R.id.voiceModLayout);
        languageLayout = (LinearLayout) findViewById(R.id.languageLayout);
        speechLayout = (LinearLayout) findViewById(R.id.speechLayout);
        pitchLayout = (LinearLayout) findViewById(R.id.pitchLayout);
        speechSeekBar = (SeekBar) findViewById(R.id.speechSeekBar);
        pitchSeekBar = (SeekBar) findViewById(R.id.pitchSeekBar);
        this.tts = new TextToSpeech(this, this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            testDeviceVoice = false;
            voiceLayout.setVisibility(View.VISIBLE);
            voiceModLayout.setVisibility(View.VISIBLE);
            languageLayout.setVisibility(View.GONE);
            speechLayout.setVisibility(View.GONE);
            pitchLayout.setVisibility(View.GONE);
        } else if (position == 1) {
            testDeviceVoice = true;
            voiceLayout.setVisibility(View.GONE);
            voiceModLayout.setVisibility(View.GONE);
            languageLayout.setVisibility(View.VISIBLE);
            speechLayout.setVisibility(View.VISIBLE);
            pitchLayout.setVisibility(View.VISIBLE);
        }
    }

    public void resetSpeech() {
        speechProgressValue = 1.0f;
        speechSeekBar.setProgress((int) (1.0f / 3.0f * 100));
    }

    public void resetPitch() {
        pitchProgressValue = 1.0f;
        pitchSeekBar.setProgress((int) (1.0f / 3.0f * 100));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onDestroy() {
        if (this.tts != null) {
            this.tts.stop();
            this.tts.shutdown();
        }
        super.onDestroy();
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            List<String> locales = new ArrayList<String>();
            if (MainActivity.voice.language != null) {
                locales.add(MainActivity.voice.language);
            }
            locales.add(Locale.US.toString());
            locales.add(Locale.UK.toString());
            locales.add(Locale.FRENCH.toString());
            locales.add(Locale.GERMAN.toString());
            locales.add("ES");
            locales.add("PT");
            locales.add(Locale.ITALIAN.toString());
            locales.add(Locale.CHINESE.toString());
            locales.add(Locale.JAPANESE.toString());
            locales.add(Locale.KOREAN.toString());
            for (Locale locale : Locale.getAvailableLocales()) {
                try {
                    int code = this.tts.isLanguageAvailable(locale);
                    if (code != TextToSpeech.LANG_NOT_SUPPORTED) {
                        locales.add(locale.toString());
                    }
                } catch (Exception ignore) {}
            }
            Spinner spin = (Spinner) findViewById(R.id.languageSpin);
            ArrayAdapter adapter = new ArrayAdapter(this,
                    android.R.layout.simple_spinner_dropdown_item, locales.toArray());
            spin.setAdapter(adapter);
            if (MainActivity.voice.language != null) {
                spin.setSelection(locales.indexOf(MainActivity.voice.language));
            }

            int result = this.tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }
        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }

    public void saveVoiceSettings(VoiceConfig config) {
        try {
            Spinner spinner = (Spinner) findViewById(R.id.voiceTypeSpin);
            String voiceType = spinner.getSelectedItem().toString();
            if (voiceType.equals(getApplicationContext().getString(R.string.serverTypeVoice))) {
                config.nativeVoice = false;
            } else if (voiceType.equals(getApplicationContext().getString(R.string.deviceTypeVoice))) {
                config.nativeVoice = true;
            }
            Spinner spin = (Spinner) findViewById(R.id.voiceSpin);
            config.voice = MainActivity.voices[Arrays.asList(MainActivity.voiceNames).indexOf(spin.getSelectedItem().toString())];
            spin = (Spinner) findViewById(R.id.languageSpin);
            config.language = spin.getSelectedItem().toString();
            spin = (Spinner) findViewById(R.id.voiceModSpin);
            config.mod = spin.getSelectedItem().toString();
            if(this instanceof UserVoiceActivity) {
                config.pitch = String.valueOf(UserVoiceActivity.pitchProgressValue);
                config.speechRate = String.valueOf(UserVoiceActivity.speechProgressValue);
            } else {
                config.pitch = String.valueOf(VoiceActivity.pitchProgressValue);
                config.speechRate = String.valueOf(VoiceActivity.speechProgressValue);
            }
        } catch (Exception exception) {
            Log.wtf("saveVoiceSettings", exception);
        }
    }

    public void save() {
        VoiceConfig config = new VoiceConfig();
        if(MainActivity.instance != null) {
            config.instance = MainActivity.instance.id;
        }
        saveVoiceSettings(config);
        HttpSaveVoiceAction action = new HttpSaveVoiceAction(this, config);
        action.execute();
    }

    public void test() {
        try {
            EditText text = (EditText) findViewById(R.id.testText);
            String test = text.getText().toString();
            if (testDeviceVoice) {
                Spinner spin = (Spinner) findViewById(R.id.languageSpin);

                int result = this.tts.setLanguage(new Locale((String)spin.getSelectedItem()));
                if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    MainActivity.error("This Language is not supported", null, this);
                }
                this.tts.setPitch(pitchProgressValue);
                this.tts.setSpeechRate(speechProgressValue);
                this.tts.speak(test, TextToSpeech.QUEUE_FLUSH, null);
            } else {
                Spinner spin = (Spinner) findViewById(R.id.voiceSpin);
                Speech config = new Speech();
                config.voice = MainActivity.voices[Arrays.asList(MainActivity.voiceNames).indexOf(spin.getSelectedItem().toString())];
                spin = (Spinner) findViewById(R.id.voiceModSpin);
                config.mod = spin.getSelectedItem().toString();
                config.text = test;
                HttpSpeechAction action = new HttpSpeechAction(AbstractVoiceActivity.this, config);
                action.execute();
            }
        } catch (Exception exception) {
            Log.wtf("test", exception);
        }
    }

    public MediaPlayer playAudio(String audio, boolean loop, boolean cache, boolean start) {
        try {
            Uri audioUri = null;
            if (cache) {
                audioUri = HttpGetVideoAction.fetchVideo(this, audio);
            }
            if (audioUri == null) {
                audioUri = Uri.parse(MainActivity.connection.fetchImage(audio).toURI().toString());
            }
            final MediaPlayer audioPlayer = new MediaPlayer();
            audioPlayer.setDataSource(getApplicationContext(), audioUri);
            audioPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    Log.wtf("Audio error", "what:" + what + " extra:" + extra);
                    audioPlayer.stop();
                    audioPlayer.release();
                    return true;
                }
            });
            audioPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    audioPlayer.release();
                }
            });
            audioPlayer.prepare();
            audioPlayer.setLooping(loop);
            if (start) {
                audioPlayer.start();
            }
            return audioPlayer;
        } catch (Exception exception) {
            Log.wtf(exception.toString(), exception);
            return null;
        }
    }
}
