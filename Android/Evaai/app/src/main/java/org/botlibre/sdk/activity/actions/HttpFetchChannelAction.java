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
import android.content.Intent;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.livechat.LiveChatActivity;
import org.botlibre.sdk.config.ChannelConfig;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.UserConfig;

public class HttpFetchChannelAction extends HttpUIAction {

    private Activity activity;
    private UserConfig userConfig;
    private InstanceConfig instanceConfig;
    private ChannelConfig channelConfig;

    public HttpFetchChannelAction(Activity activity, UserConfig userConfig) {
        super(activity);
        this.activity = activity;
        this.userConfig = userConfig;
    }

    public HttpFetchChannelAction(Activity activity, InstanceConfig instanceConfig) {
        super(activity);
        this.activity = activity;
        this.instanceConfig = instanceConfig;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            if(instanceConfig != null) {
                this.channelConfig = MainActivity.connection.fetchChannel(instanceConfig);
            }
            if(userConfig != null) {
                this.channelConfig = MainActivity.connection.fetchChannel(userConfig);
            }
        } catch (Exception exception) {
            this.exception = exception;
        }
        return "";
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void onPostExecute(String xml) {
        super.onPostExecute(xml);
        if(channelConfig != null) {
            MainActivity.instance = channelConfig;
            Intent intent = new Intent(activity, LiveChatActivity.class);
            activity.startActivity(intent);
        }
    }
}
