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

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import io.evaai.R;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpFetchUserAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpUserFriendsAction;
import org.botlibre.sdk.config.UserConfig;
import org.botlibre.sdk.config.UserFriendsConfig;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Stack;

public class UserFriendsActivity extends LibreActivity implements FriendsListAdapter.OnItemClickListener,
        FriendsListAdapter.OnItemTouchListener, FollowersListAdapter.OnItemClickListener, FollowersListAdapter.OnItemTouchListener {

    private String friendId = "";
    private boolean isSelected;
    public static boolean resetView = false;
    private ListView friendsListView, followersListView;
    protected int page = 0;
    public HashSet<String> friendshipSet;
    public List<UserConfig> followersList;
    private EditText friendView;
    public LinkedHashMap<String, UserConfig> friendsMap;
    public BaseAdapter friendsAdapter, followersAdapter;
    public void setIsAllSelected(boolean selected) { this.isSelected = selected; }
    public boolean getIsAllUsersSelected() { return isSelected; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_friends);
        if(MainActivity.user != null) {
            HttpGetImageAction.fetchImage(this, MainActivity.user.avatar, findViewById(R.id.icon));
        }
        setIsAllSelected(false);
        friendshipSet = new HashSet<>();
        friendsMap = new LinkedHashMap<>();
        followersList = new ArrayList<>();
        friendView = findViewById(R.id.friendIdView);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        friendView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    addFriendship();
                }
                return false;
            }
        });

        friendsListView = findViewById(R.id.friendsListView);
        friendsAdapter = new FriendsListAdapter(UserFriendsActivity.this, friendsMap, this, this);
        friendsListView.setAdapter(friendsAdapter);
        followersListView = findViewById(R.id.followersListView);
        followersAdapter = new FollowersListAdapter(this, R.layout.followers_list, this.followersList, this, this);
        followersListView.setAdapter(followersAdapter);
        getFriendships();
        getFollowers();

        ((RadioButton) findViewById(R.id.friendsTab)).setTextColor(Color.parseColor("#FFFFFF"));
        RadioGroup tabs = (RadioGroup) findViewById(R.id.toggleTabs);
        tabs.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup tabs, int checkedId) {
                TextView followersLabel = findViewById(R.id.userFollowersLabel);
                ImageButton imageButton = findViewById(R.id.viewPublicUsers);
                Button selectFriends = findViewById(R.id.selectFriends);
                Button deleteFriends = findViewById(R.id.deleteFriends);
                ImageButton viewUserProfile = findViewById(R.id.viewUserProfile);
                RadioButton friendsTab = findViewById(R.id.friendsTab);
                RadioButton followersTab = findViewById(R.id.followersTab);
                if (checkedId == R.id.followersTab) {
                    // Followers is enabled
                    friendsListView.setVisibility(View.GONE);
                    followersListView.setVisibility(View.VISIBLE);
                    followersLabel.setVisibility(View.VISIBLE);
                    friendView.setVisibility(View.GONE);
                    imageButton.setVisibility(View.GONE);
                    selectFriends.setVisibility(View.GONE);
                    deleteFriends.setVisibility(View.GONE);
                    viewUserProfile.setVisibility(View.GONE);
                    friendsTab.setTextColor(Color.parseColor("#000000"));
                    followersTab.setTextColor(Color.parseColor("#ffffff"));
                } else if (checkedId == R.id.friendsTab){
                    // Friends is enabled
                    friendsListView.setVisibility(View.VISIBLE);
                    followersListView.setVisibility(View.GONE);
                    followersLabel.setVisibility(View.GONE);
                    friendView.setVisibility(View.VISIBLE);
                    imageButton.setVisibility(View.VISIBLE);
                    selectFriends.setVisibility(View.VISIBLE);
                    deleteFriends.setVisibility(View.VISIBLE);
                    viewUserProfile.setVisibility(View.VISIBLE);
                    friendsTab.setTextColor(Color.parseColor("#ffffff"));
                    followersTab.setTextColor(Color.parseColor("#000000"));
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.userFilter = true;
        UserFriendsActivity.resetView = false;
        if (ListUsersViewActivity.isUserSelected) {
            getFriendships();
        }
        ListUsersViewActivity.isUserSelected = false;
    }

    public void viewUserProfile(String userId) {
        if(!userId.isEmpty()) {
            friendId = userId;
            UserConfig config = new UserConfig();
            config.user = friendId;
            HttpAction action = new HttpFetchUserAction(this, config);
            action.execute();
            return;
        }
        MainActivity.error("Must select user friend to view profile.", null, this);
    }

    public void viewUserProfile(View view) {
        viewUserProfile(friendId);
    }

    public void getFriendships() {
        UserFriendsConfig config = new UserFriendsConfig();
        config.action = "getFriends";
        config.userFriend = MainActivity.user.user;
        HttpUserFriendsAction action = new HttpUserFriendsAction(this, config);
        action.execute();
    }

    public void getFollowers() {
        UserFriendsConfig config = new UserFriendsConfig();
        config.action = "getFollowers";
        if(MainActivity.user != null) {
            config.userFriend = MainActivity.user.user;
        } else if(MainActivity.viewUser != null) {
            config.userFriend = MainActivity.viewUser.user;
        }
        HttpUserFriendsAction action = new HttpUserFriendsAction(this, config);
        action.execute();
    }

    public void viewUsers() {
        Intent intent = new Intent(this, ListUsersViewActivity.class);
        startActivityForResult(intent, 1);
    }

    public void viewUsers(View view) {
        viewUsers();
    }

    public void addFriendship() {
        UserFriendsConfig config = new UserFriendsConfig();
        config.action = "AddFriendship";
        config.userFriend = friendView.getText().toString().trim();
        if (config.userFriend.length() == 0) {
            MainActivity.error("Friend name cannot be empty", null, this);
            return;
        }
        HttpUserFriendsAction action = new HttpUserFriendsAction(this, config);
        action.execute();
    }

    public void removeFriends(View view) {
        if(friendshipSet.isEmpty()) {
            MainActivity.error("Must select friend(s) to remove.", null, this);
            return;
        }
        String friendships = "";
        Iterator<String> it = friendshipSet.iterator();
        while(it.hasNext()) {
            friendships = friendships + it.next() + ",";
        }
        if(!friendships.isEmpty()) {
            friendships = friendships.substring(0, friendships.length() - 1);
        }
        UserFriendsConfig config = new UserFriendsConfig();
        config.action = "RemoveFriendship";
        config.userFriend = friendships;
        HttpUserFriendsAction action = new HttpUserFriendsAction(this, config);
        action.execute();
    }

    public void selectAllFriends(View view) {
        setIsAllSelected(!isSelected);
        friendsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(String userId) {
        friendId = userId;
    }

    @Override
    public void onItemDoubleTap(String userId) {
        friendId = userId;
        viewUserProfile(friendId);
    }
}
