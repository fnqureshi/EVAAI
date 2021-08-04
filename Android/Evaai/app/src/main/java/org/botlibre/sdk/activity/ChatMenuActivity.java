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

import org.botlibre.sdk.config.InstanceConfig;

import io.evaai.R;

/**
 * Activity for viewing chat menu.
 */
public class ChatMenuActivity extends LibreActivity {

	public static String action;

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

	public void selectMenu(View view) {
		findViewById(R.id.menuItems).setVisibility(View.VISIBLE);
		findViewById(R.id.commandItems).setVisibility(View.GONE);

		findViewById(R.id.menuBar).setVisibility(View.VISIBLE);
		findViewById(R.id.commandBar).setVisibility(View.GONE);
	}

	public void selectCommand(View view) {
		findViewById(R.id.menuItems).setVisibility(View.GONE);
		findViewById(R.id.commandItems).setVisibility(View.VISIBLE);

		findViewById(R.id.menuBar).setVisibility(View.GONE);
		findViewById(R.id.commandBar).setVisibility(View.VISIBLE);
	}
}
