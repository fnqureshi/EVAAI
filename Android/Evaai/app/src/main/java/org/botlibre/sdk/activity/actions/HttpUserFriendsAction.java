/******************************************************************************
 *
 *  Copyright 2019-2021 Paphus Solutions Inc.
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

import android.app.Activity;;
import android.widget.EditText;
import io.evaai.R;
import org.botlibre.sdk.activity.ListFriendsViewActivity;
import org.botlibre.sdk.activity.ListUsersViewActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.UserFriendsActivity;
import org.botlibre.sdk.activity.ViewUserActivity;
import org.botlibre.sdk.activity.ViewUserFriendsActivity;
import org.botlibre.sdk.config.UserConfig;
import org.botlibre.sdk.config.UserFriendsConfig;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class HttpUserFriendsAction extends HttpUIAction {

    private Activity activity;
    private UserFriendsConfig config;
    private List<UserConfig> friends;
    private UserConfig userConfig;

    public HttpUserFriendsAction(Activity activity, UserFriendsConfig config) {
        super(activity);
        this.activity = activity;
        this.config = config;
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            if(this.config.action.equals("getFriends") || this.config.action.equals("getUserFriends")) {
                friends = MainActivity.connection.getFriends(this.config);
            } else if(this.config.action.equals("getFollowers")) {
                friends = MainActivity.connection.getFollowers(this.config);
            } else {
                userConfig = MainActivity.connection.userFriendship(this.config);
            }
        } catch (Exception exception) {
            this.exception = exception;
        }
        return "";
    }

    @Override
    public void onPostExecute(String xml) {
        super.onPostExecute(xml);
        if(this.exception != null) {
            return;
        }
        if(activity instanceof ListUsersViewActivity) {
            ListUsersViewActivity.isUserSelected = true;
            activity.finish();
        } else if("AddFriendship".equals(this.config.action)) {
            if(MainActivity.user != MainActivity.viewUser) {
                if(this.activity instanceof ViewUserActivity) {
                    ((ViewUserActivity) this.activity).viewFriends();
                    return;
                }
            }
            if(userConfig != null) {
                if(this.activity instanceof UserFriendsActivity) {
                    EditText friendView = this.activity.findViewById(R.id.friendIdView);
                    friendView.setText("");
                    ((UserFriendsActivity)this.activity).friendsMap.put(userConfig.user, userConfig);
                    ((UserFriendsActivity)this.activity).friendsAdapter.notifyDataSetChanged();
                }
            }
        } else if ("RemoveFriendship".equals(this.config.action)) {
            Iterator<String> it = ((UserFriendsActivity)this.activity).friendshipSet.iterator();
            while(it.hasNext()) {
                ((UserFriendsActivity)this.activity).friendsMap.remove(it.next());
            }
            ((UserFriendsActivity)this.activity).friendshipSet = new HashSet<>();
            ((UserFriendsActivity)this.activity).friendsAdapter.notifyDataSetChanged();
        } else if ("getFriends".equals(this.config.action)) {
            if(this.friends != null) {
                for(UserConfig config : friends) {
                    if(this.activity instanceof ViewUserFriendsActivity) {
                        ((ViewUserFriendsActivity)this.activity).friendsMap.put(config.user, config);
                    } else {
                        ((UserFriendsActivity)this.activity).friendsMap.put(config.user, config);
                    }
                }
            }
            if(this.activity instanceof ViewUserFriendsActivity) {
                ((ViewUserFriendsActivity)this.activity).friendsAdapter.notifyDataSetChanged();
            } else {
                EditText friendView = this.activity.findViewById(R.id.friendIdView);
                friendView.setText("");
                ((UserFriendsActivity)this.activity).friendsAdapter.notifyDataSetChanged();
            }
        } else if("getFollowers".equals(this.config.action)) {
            if (this.friends != null) {
                for(UserConfig config : friends) {
                    if(this.activity instanceof ViewUserFriendsActivity) {
                        ((ViewUserFriendsActivity)this.activity).followersList.add(config);
                    } else {
                        ((UserFriendsActivity)this.activity).followersList.add(config);
                    }
                }
            }
            if(this.activity instanceof ViewUserFriendsActivity) {
                ((ViewUserFriendsActivity) this.activity).followersAdapter.notifyDataSetChanged();
            } else {
                ((UserFriendsActivity) this.activity).followersAdapter.notifyDataSetChanged();
            }
        } else if("getUserFriends".equals(this.config.action)) {
            if(this.friends != null) {
                for(UserConfig config : friends) {
                    ((ListFriendsViewActivity)this.activity).friendsMap.put(config.user, config);
                }
                ((ListFriendsViewActivity)this.activity).friendsAdapter.notifyDataSetChanged();
            }
        }
    }
}
