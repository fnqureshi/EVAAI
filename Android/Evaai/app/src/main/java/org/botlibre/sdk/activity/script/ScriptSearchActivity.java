/******************************************************************************
 *
 *  Copyright 2016 Paphus Solutions Inc.
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

package org.botlibre.sdk.activity.script;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.SearchActivity;

import io.evaai.R;

import android.os.Bundle;
import android.widget.RadioButton;

/*
 * Browse activity for searching for a script
 */
public class ScriptSearchActivity extends SearchActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (MainActivity.current == null) {
			finish();
			return;
		}
		RadioButton radio = (RadioButton)findViewById(R.id.personalRadio);
		radio.setText("My Scripts");
	}
	
	public String getType() {		
		return "Script";
	}
}
