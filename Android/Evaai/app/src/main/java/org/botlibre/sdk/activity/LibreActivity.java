/******************************************************************************
 *
 *  Copyright 2014-2021 Paphus Solutions Inc.
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

import android.app.Activity;
import android.content.Intent;
import android.view.View;

/**
 * Generic activity for common behavior.
 */
public abstract class LibreActivity extends Activity {

	public void help(View view) {
        help();
	}

	public void help() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
	}
	public void backButton(View view){
		super.onBackPressed();
		MainActivity.launch = false;
	}
}
