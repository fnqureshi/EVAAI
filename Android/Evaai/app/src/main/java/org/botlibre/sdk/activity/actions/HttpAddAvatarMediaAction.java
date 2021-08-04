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
import org.botlibre.sdk.config.AvatarConfig;
import org.botlibre.sdk.config.AvatarMedia;

import android.app.Activity;
import android.app.ProgressDialog;

public class HttpAddAvatarMediaAction extends HttpUIAction {

	AvatarMedia config;
	String file;
	
	public HttpAddAvatarMediaAction(Activity activity, String file, AvatarMedia config) {
		super(activity);
		this.config = config;
		this.file = file;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
			MainActivity.connection.createAvatarMedia(this.file, this.config);
		} catch (Exception exception) {
			this.exception = exception;
		}
		return "";
	}
	
	@Override
	protected void onPreExecute() {
		this.dialog = new ProgressDialog(this.activity);
		this.dialog.setCancelable(false);
		this.dialog.setMessage("Uploading..."); 
		this.dialog.show();
	}

	@Override
	protected void onPostExecute(String xml) {
		super.onPostExecute(xml);
		if (this.exception != null) {
			MainActivity.error(this.exception.getMessage(), this.exception, this.activity);
			return;
		}
		HttpAction action = new HttpGetAvatarMediaAction(this.activity, (AvatarConfig)MainActivity.instance.credentials(), true);
		action.execute();
	}
}