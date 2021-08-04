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

import org.botlibre.sdk.activity.actions.HttpConnectAction;
import org.botlibre.sdk.config.UserConfig;

import io.evaai.R;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Activity for user login.
 */
public class LoginActivity extends LibreActivity {	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (MainActivity.current == null) {
			finish();
			return;
		}
		setContentView(R.layout.activity_login);
		TextView resetPasswordLink = (TextView) findViewById(R.id.resetPassword);
		resetPasswordLink.setText(Html.fromHtml("<a href=\"https://www.botlibre.com/login?request-reset-password\">Reset Password</a>"));
		resetPasswordLink.setMovementMethod(LinkMovementMethod.getInstance());
	}
	
	/**
	 * Start a chat session with the selected instance and the user.
	 */
	public void connect(View view) {
		TextView resetPasswordLink = (TextView) findViewById(R.id.resetPassword);
        EditText text = (EditText) findViewById(R.id.userText);
        String user = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.passwordText);
        String password = text.getText().toString().trim();
		UserConfig config = new UserConfig();
		config.user = user;
		config.password = password;
        HttpConnectAction action = new HttpConnectAction(this, config, true);
    	action.execute();
    }
	
	public void signUp(View view) {
		finish();
		
        Intent intent = new Intent(this, CreateUserActivity.class);
        startActivity(intent);
	}
}
