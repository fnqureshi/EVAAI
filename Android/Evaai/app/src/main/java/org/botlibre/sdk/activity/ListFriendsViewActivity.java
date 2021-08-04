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

package org.botlibre.sdk.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.BaseAdapter;
import android.widget.ListView;
import io.evaai.R;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpUserFriendsAction;
import org.botlibre.sdk.config.UserConfig;
import org.botlibre.sdk.config.UserFriendsConfig;
import java.util.LinkedHashMap;

public class ListFriendsViewActivity extends LibreActivity implements FriendsListAdapter.OnItemClickListener, FriendsListAdapter.OnItemTouchListener {

    private Intent data = new Intent();
    private ListView friendsListView;
    public LinkedHashMap<String, UserConfig> friendsMap;
    public BaseAdapter friendsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_friends_view);

        if(MainActivity.viewUser != null) {
            HttpGetImageAction.fetchImage(this, MainActivity.viewUser.avatar, findViewById(R.id.icon));
        } else if(MainActivity.user != null) {
            HttpGetImageAction.fetchImage(this, MainActivity.user.avatar, findViewById(R.id.icon));
        }

        friendsMap = new LinkedHashMap<>();
        friendsListView = findViewById(R.id.friendsListView);
        friendsAdapter = new FriendsListAdapter(ListFriendsViewActivity.this, friendsMap, this, this);
        friendsListView.setAdapter(friendsAdapter);
        getFriends();
    }

    public void getFriends() {
        UserFriendsConfig config = new UserFriendsConfig();
        config.action = "getUserFriends";
        config.user = MainActivity.user.user;
        config.token = MainActivity.user.token;
        if(MainActivity.viewUser != null) {
            config.userFriend = MainActivity.viewUser.user;
        } else {
            config.userFriend = MainActivity.user.user;
        }
        HttpUserFriendsAction action = new HttpUserFriendsAction(this, config);
        action.execute();
    }

    @Override
    public void onItemClick(String userId) {
        data.putExtra("friend-id", userId);
        setResult(RESULT_OK, data);
    }

    @Override
    public void onItemDoubleTap(String userId) {
        data.putExtra("friend-id", userId);
        setResult(RESULT_OK, data);
        finish();
    }
}
