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

package org.botlibre.sdk.activity;

import android.os.Bundle;
import android.app.Activity;
import android.widget.BaseAdapter;
import android.widget.ListView;
import io.evaai.R;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpFetchUserAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpUserFriendsAction;
import org.botlibre.sdk.config.UserConfig;
import org.botlibre.sdk.config.UserFriendsConfig;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ViewUserFriendsActivity extends LibreActivity implements FriendsListAdapter.OnItemClickListener, FriendsListAdapter.OnItemTouchListener,
        FollowersListAdapter.OnItemClickListener, FollowersListAdapter.OnItemTouchListener {

    private ListView friendsListView, followersListView;
    public BaseAdapter friendsAdapter, followersAdapter;
    public LinkedHashMap<String, UserConfig> friendsMap;
    public List<UserConfig> followersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_friends);

        if(MainActivity.viewUser != null) {
            HttpGetImageAction.fetchImage(this, MainActivity.viewUser.avatar, findViewById(R.id.icon));
        } else if(MainActivity.user != null){
            HttpGetImageAction.fetchImage(this, MainActivity.user.avatar, findViewById(R.id.icon));
        }
        friendsMap = new LinkedHashMap<>();
        friendsListView = findViewById(R.id.friendsListView);
        friendsAdapter = new FriendsListAdapter(ViewUserFriendsActivity.this, friendsMap, this, this);
        friendsListView.setAdapter(friendsAdapter);

        followersList = new ArrayList<>();
        followersListView = findViewById(R.id.followersListView);
        followersAdapter = new FollowersListAdapter(this, R.layout.followers_list, this.followersList, this, this);
        followersListView.setAdapter(followersAdapter);

        getFriendships();
        getFollowers();
    }

    public void getFriendships() {
        UserFriendsConfig config = new UserFriendsConfig();
        config.action = "getFriends";
        if(MainActivity.user != null && MainActivity.viewUser != null && !MainActivity.user.user.equals(MainActivity.viewUser.user)) {
            config.user = MainActivity.user.user;
            config.token = MainActivity.user.token;
            config.userFriend = MainActivity.viewUser.user;
        }
        HttpUserFriendsAction action = new HttpUserFriendsAction(this, config);
        action.execute();
    }

    public void getFollowers() {
        UserFriendsConfig config = new UserFriendsConfig();
        config.action = "getFollowers";
        if(MainActivity.user != null && MainActivity.viewUser != null && !MainActivity.user.equals(MainActivity.viewUser)) {
            config.user = MainActivity.user.user;
            config.token = MainActivity.user.token;
            config.userFriend = MainActivity.viewUser.user;
        }
        HttpUserFriendsAction action = new HttpUserFriendsAction(this, config);
        action.execute();
    }

    public void viewUserProfile(String userId) {
        if(!userId.isEmpty()) {
            UserConfig config = new UserConfig();
            config.user = userId;
            HttpAction action = new HttpFetchUserAction(this, config);
            action.execute();
            return;
        }
        MainActivity.error("Must select user friend to view profile.", null, this);
    }

    @Override
    public void onItemClick(String userId) {
    }

    @Override
    public void onItemDoubleTap(String userId) {
        viewUserProfile(userId);
    }
}
