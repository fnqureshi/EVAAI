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

import java.util.LinkedHashMap;
import java.util.List;
import android.app.Activity;
import android.content.Intent;
import org.botlibre.sdk.activity.ListUsersViewActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.UserFriendsActivity;
import org.botlibre.sdk.activity.UserSearchActivity;
import org.botlibre.sdk.activity.WebMediumUsersActivity;
import org.botlibre.sdk.config.BrowseUserConfig;
import org.botlibre.sdk.config.Config;
import org.botlibre.sdk.config.UserConfig;
import org.botlibre.sdk.config.WebMediumConfig;

public class HttpGetUsersAction extends HttpUIAction {

	private boolean finish = false;
	Config config;
	List<String> users;
	List<UserConfig> publicUsersList;

	public HttpGetUsersAction(Activity activity, Config config) {
		super(activity);
		this.config = config;
	}

	public HttpGetUsersAction(Activity activity, Config config, boolean finish) {
		super(activity);
		this.finish = finish;
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
			if(this.activity instanceof ListUsersViewActivity || this.activity instanceof UserSearchActivity) {
				this.publicUsersList = MainActivity.connection.getAllUsers((BrowseUserConfig) this.config);
			} else {
				this.users = MainActivity.connection.getUsers((WebMediumConfig) this.config);
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
		try {
			if(this.finish) {
				this.activity.finish();
			}
			if(this.activity instanceof ListUsersViewActivity) {
				ListUsersViewActivity.publicUsersList = this.publicUsersList;

				if(this.publicUsersList != null) {
					((ListUsersViewActivity) this.activity).publicUsersMap = new LinkedHashMap<>();
					for (UserConfig config : this.publicUsersList) {
						((ListUsersViewActivity) this.activity).publicUsersMap.put(config.user, config);
					}
				}
				((ListUsersViewActivity) this.activity).resetView();
			} else if(this.activity instanceof UserSearchActivity) {
				UserFriendsActivity.resetView = true;
				ListUsersViewActivity.publicUsersList = this.publicUsersList;
				Intent intent = new Intent(this.activity, ListUsersViewActivity.class);
				this.activity.startActivity(intent);
			} else {
				((WebMediumUsersActivity)this.activity).users = this.users;
				((WebMediumUsersActivity)this.activity).resetView();
			}
		} catch (Exception error) {
			this.exception = error;
			MainActivity.error(this.exception.getMessage(), this.exception, this.activity);
			return;			
		}
	}
}