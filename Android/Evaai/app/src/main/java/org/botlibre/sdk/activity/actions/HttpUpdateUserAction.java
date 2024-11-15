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

package org.botlibre.sdk.activity.actions;

import android.app.Activity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.UserTagsActivity;
import org.botlibre.sdk.activity.UserTagsAdapter;
import org.botlibre.sdk.config.UserConfig;

public class HttpUpdateUserAction extends HttpUIAction {

	private Activity activity;
	private UserConfig config;

	public HttpUpdateUserAction(Activity activity, UserConfig config) {
		super(activity);
		this.activity = activity;
		this.config = config;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
			this.config = MainActivity.connection.update(this.config);
		} catch (Exception exception) {
			this.exception = exception;
		}
		return "";
	}

	@Override
	protected void onPostExecute(String xml) {
		super.onPostExecute(xml);
		if (this.exception != null) {
			return;
		}
		try {
			if(this.activity instanceof UserTagsActivity) {
				if(this.config != null) {
					String[] tagArray = this.config.tags.split(",");
					for(String tag : tagArray) {
						tag = tag.trim();
						if(!tag.isEmpty()) {
							((UserTagsAdapter) ((UserTagsActivity) this.activity).userTagsAdapter).addTagToSet(tag);
						}
					}
					MainActivity.user = this.config;
					((UserTagsActivity) this.activity).clearEditTextView();
					((UserTagsAdapter) ((UserTagsActivity) this.activity).userTagsAdapter).setTagList();
					((UserTagsActivity) this.activity).userTagsAdapter.notifyDataSetChanged();
				}
			} else {
				MainActivity.user = this.config;
				this.activity.finish();
			}
		} catch (Exception error) {
			this.exception = error;
			MainActivity.error(this.exception.getMessage(), this.exception, this.activity);
			return;			
		}
	}
	
}