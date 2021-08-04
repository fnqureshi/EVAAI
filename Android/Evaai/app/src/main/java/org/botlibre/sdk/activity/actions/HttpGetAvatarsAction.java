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
import android.widget.ListView;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.config.AvatarConfig;
import org.botlibre.sdk.config.BrowseConfig;

import java.util.List;

public class HttpGetAvatarsAction extends HttpAction {
	ListView llview;
	List<AvatarConfig> avatars;

	public HttpGetAvatarsAction(Activity activity) {
		super(activity);
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
			BrowseConfig config = new BrowseConfig();
			config.type = "Avatar";
			config.category = "Female";
			config.typeFilter = "Featured";
			config.contentRating = MainActivity.contentRating;
			this.avatars = (List)MainActivity.connection.browse(config);
		} catch (Exception exception) {
			this.exception = exception;
		}
		return "";
	}

	public void postExecute(String xml) {
		if (this.exception != null) {
			return;
		}
		MainActivity.avatars = this.avatars;
	}
}