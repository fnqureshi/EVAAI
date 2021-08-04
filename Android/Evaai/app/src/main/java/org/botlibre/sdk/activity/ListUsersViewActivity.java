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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import io.evaai.R;
import org.botlibre.sdk.activity.actions.HttpGetUsersAction;
import org.botlibre.sdk.activity.actions.HttpUserFriendsAction;
import org.botlibre.sdk.config.BrowseUserConfig;
import org.botlibre.sdk.config.UserConfig;
import org.botlibre.sdk.config.UserFriendsConfig;

import java.util.LinkedHashMap;
import java.util.List;

public class ListUsersViewActivity extends LibreActivity {

    protected int page = 0;
    private ListView publicUsersListView;
    public static List<UserConfig> publicUsersList;
    public LinkedHashMap<String, UserConfig> publicUsersMap;
    private BaseAdapter publicUserAdapter;
    private View next, previous;
    private TextView title;
    public static boolean isUserSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_users_view);
        isUserSelected = false;
        title = findViewById(R.id.theTitle);
        next = findViewById(R.id.userNextPageBtn);
        previous = findViewById(R.id.userPrevPageBtn);
        publicUsersMap = new LinkedHashMap<>();
        publicUsersListView = findViewById(R.id.publicUsersListView);
        publicUserAdapter = new UsersAdapter(ListUsersViewActivity.this, publicUsersMap);
        publicUsersListView.setAdapter(publicUserAdapter);
        publicUsersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                String userId = (String) adapterView.getAdapter().getItem(index);
                UserConfig config = publicUsersMap.get(userId);
                UserFriendsConfig friendConfig = new UserFriendsConfig();
                if (userId != null) {
                    friendConfig.action = "AddFriendship";
                    friendConfig.userFriend = config.user;
                    HttpUserFriendsAction action = new HttpUserFriendsAction(ListUsersViewActivity.this, friendConfig);
                    action.execute();
                }
            }
        });
        if (MainActivity.userFilter) {
            title.setText("Browse Users");
        } else {
            title.setText("Browse Bots");
        }
        if (!UserFriendsActivity.resetView) {
            getUsers();
        } else {
            resetView();
        }
    }

    public void getUsers() {
        MainActivity.userFilter = true;
        BrowseUserConfig config = new BrowseUserConfig();
        config.userFilter = "Public";
        HttpGetUsersAction action = new HttpGetUsersAction(this, config);
        action.execute();
    }

    public void getBots() {
        MainActivity.userFilter = false;
        BrowseUserConfig config = new BrowseUserConfig();
        config.userFilter = "Bot";
        HttpGetUsersAction action = new HttpGetUsersAction(this, config);
        action.execute();
    }

    public void resetView() {
        if(MainActivity.userFilter) {
            title.setText("Browse Users");
        } else {
            title.setText("Browse Bots");
        }
        if(this.publicUsersList.size() >= 56 || this.page > 0) {
            if(this.publicUsersList.size() >= 56) {
                next.setVisibility(View.VISIBLE);
            } else {
                next.setVisibility(View.GONE);
            }
        } else {
            next.setVisibility(View.GONE);
        }
        if(this.page > 0) {
            previous.setVisibility(View.VISIBLE);
        } else {
            previous.setVisibility(View.GONE);
        }
        if(this.publicUsersList != null) {
            publicUsersMap = new LinkedHashMap<>();
            for (UserConfig config : this.publicUsersList) {
                publicUsersMap.put(config.user, config);
            }
            publicUserAdapter = new UsersAdapter(ListUsersViewActivity.this, publicUsersMap);
            publicUsersListView.setAdapter(publicUserAdapter);
        }
    }

    public void browseUsers(View view) {
        getUsers();
    }

    public void browseBots(View view) {
        getBots();
    }

    public void nextPage(View view) {
        this.page++;
        BrowseUserConfig config = new BrowseUserConfig();
        if(MainActivity.userFilter) {
            config.userFilter = "Public";
        } else {
            config.userFilter = "Bot";
        }
        config.page = String.valueOf(this.page);
        HttpGetUsersAction action = new HttpGetUsersAction(this, config);
        action.execute();
    }

    public void previousPage(View View) {
        this.page--;
        BrowseUserConfig config = new BrowseUserConfig();
        if(MainActivity.userFilter) {
            config.userFilter = "Public";
        } else {
            config.userFilter = "Bot";
        }
        config.page = String.valueOf(this.page);
        HttpGetUsersAction action = new HttpGetUsersAction(this, config);
        action.execute();
    }

    public void search() {
        finish();
        if(!MainActivity.searching) {
            Intent intent = new Intent(this, UserSearchActivity.class);
            startActivity(intent);
        }
    }

    public void search(View view) {
        search();
    }

    public void menu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_browse_public_users, popup.getMenu());
        onPrepareOptionsMenu(popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
        popup.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_browse_public_users, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuUsers:
                browseUsers(null);
                return true;
            case R.id.menuBots:
                browseBots(null);
                return true;
            case R.id.menuSearch:
                search(null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UserFriendsActivity.resetView = false;
    }
}