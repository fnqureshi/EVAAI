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

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;
import io.evaai.R;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpUserMessageAction;
import org.botlibre.sdk.config.UserMessageConfig;
import java.util.LinkedHashMap;

public class UserMessagesActivity extends LibreActivity {

    private Button nextUserBtn, previousUserBtn;
    private String action;
    public static boolean newMessage = false;
    public static boolean newPollMessage = false;
    public ListView messagesListView;
    public static LinkedHashMap usersMap;
    public BaseAdapter messagesAdapter;
    static public LinkedHashMap<String, UserMessageConfig> userMessageMap;
    private int page = 0;
    private int pageSize = 56;
    private int resultSize = 0;

    public void setPage(int page){ this.page = page; }
    public int getPage() { return page; }
    public void setResultSize(int resultSize) { this.resultSize = resultSize; }
    public void setAction(String action) { this.action = action; }
    public String getAction() { return this.action; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_messages);

        nextUserBtn = findViewById(R.id.nextUserButton);
        previousUserBtn = findViewById(R.id.previousUserButton);

        if(MainActivity.viewUser != null) {
            HttpGetImageAction.fetchImage(this, MainActivity.viewUser.avatar, findViewById(R.id.icon));
        } else if(MainActivity.user != null) {
            HttpGetImageAction.fetchImage(this, MainActivity.user.avatar, findViewById(R.id.icon));
        }

        usersMap = new LinkedHashMap<>();
        userMessageMap = new LinkedHashMap<>();
        messagesListView = findViewById(R.id.userMessagesListView);
        messagesAdapter = new UserMessagesAdapter(UserMessagesActivity.this, userMessageMap);
        messagesListView.setAdapter(messagesAdapter);
        messagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UserMessagesAdapter adapter = (UserMessagesAdapter) parent.getAdapter();
                String userId = adapter.getItem(position);
                Bundle userBundle = new Bundle();
                userBundle.putString("curr_user_id", MainActivity.user.user);
                userBundle.putString("target_user_id", userId);
                UserMessageConfig config = userMessageMap.get(userId);
                String avatar = "";
                if(config != null) {
                    avatar = config.avatar;
                }
                userBundle.putString("target_avatar", avatar);
                Intent messagesIntent = new Intent(getApplicationContext(), MessagesActivity.class);
                messagesIntent.putExtras(userBundle);
                startActivity(messagesIntent);
            }
        });
    }


    public void getUserConversation() {
        setAction("get-user-conversations");
        UserMessageConfig config = new UserMessageConfig();
        config.owner = MainActivity.user.user;
        config.subject = "get-user-conversations";
        config.page = Integer.toString(page);
        config.pageSize = Integer.toString(pageSize + 1);
        config.resultsSize = Integer.toString(resultSize);
        HttpUserMessageAction action = new HttpUserMessageAction(this, config, "get-user-conversations");
        action.execute();
    }

    public void nextUserPage(View view) {
        page--;
        getUserConversation();
    }

    public void previousUserPage(View view) {
        page++;
        getUserConversation();
    }

    public void displayPaging() {
        if(page == 0 && resultSize > pageSize) {
            previousUserBtn.setVisibility(View.VISIBLE);
            nextUserBtn.setVisibility(View.GONE);
        } else if(page > 0 && resultSize >  pageSize) {
            previousUserBtn.setVisibility(View.VISIBLE);
            nextUserBtn.setVisibility(View.VISIBLE);
        } else if(page > 0 && resultSize <= pageSize) {
            previousUserBtn.setVisibility(View.GONE);
            nextUserBtn.setVisibility(View.VISIBLE);
        }
    }

    public void newMessage(View view) {
        newMessage();
    }

    public void newMessage() {
        Intent newMessagesIntent = new Intent(getApplicationContext(), NewMessageActivity.class);
        startActivity(newMessagesIntent);
    }

    public String getSubMessageHeader(String message) {
        if(message == null || message.length() == 0) return "";
        String pattern = "\\.|\\?|!";
        String[] array = message.split(pattern);
        if(array[0].length() > 30) {
            message = message.substring(0, 30).trim();
            return message + "...";
        } else {
            if (message.length() >= array[0].length() + 1) {
                return array[0] + message.substring(array[0].length(), array[0].length() + 1);
            }
            return array[0];
        }
    }

    public void menu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_user_messages, popup.getMenu());
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuNewMessage:
                newMessage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        try {
            getUserConversation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
