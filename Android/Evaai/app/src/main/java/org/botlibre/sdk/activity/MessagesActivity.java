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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import io.evaai.R;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpUserMessageAction;
import org.botlibre.sdk.config.UserMessageConfig;
import java.util.LinkedHashMap;
import static java.lang.Thread.sleep;

public class MessagesActivity extends LibreActivity {

    private static String action = "";
    private String targetUserId = "";
    private Thread messageThread;
    public ListView messagesListView;
    public LinkedHashMap<String, UserMessageConfig> messagesMap;
    public BaseAdapter messagesAdapter;
    private static UserMessageConfig lastMessage;
    public EditText userMessageText;
    private boolean poolMessages;
    private static boolean messagePool;
    private TextView userIdView;
    private int page = 0;
    private int pageSize = 56;
    private int resultSize = 0;
    private String targetAvatar = "";
    private Button nextConversationButton, previousConversationButton;
    public static void setAction(String act) { action = act; }
    public static String getAction() { return action; }
    public void setTargetUserId(String id) { this.targetUserId = id; }
    public String getTargetUserId() { return this.targetUserId; }
    public static void setMostRecentMessage(UserMessageConfig message) { lastMessage = message; }
    public static UserMessageConfig getMostRecentMessage() { return lastMessage; }
    public void setResultSize(int resultSize) { this.resultSize = resultSize; }
    public void setPoolMessages(boolean poolMessages) { this.poolMessages = poolMessages; }
    public boolean getPoolMessages() { return poolMessages; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        messagePool = true;
        nextConversationButton = findViewById(R.id.nextConversationButton);
        previousConversationButton = findViewById(R.id.previousConversationButton);
        userIdView = findViewById(R.id.userId);
        userMessageText = findViewById(R.id.userMessageText);
        userMessageText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    sendMessage();
                }
                return false;
            }
        });
        Bundle userBundle = getIntent().getExtras();
        if (userBundle != null) {
            setTargetUserId(userBundle.getString("target_user_id"));
            targetAvatar = userBundle.getString("target_avatar");
            HttpGetImageAction.fetchImage(this, targetAvatar, findViewById(R.id.userAvatarIcon));
        }
        userIdView.setText(getTargetUserId());
        messagesMap = new LinkedHashMap<>();
        if(targetUserId.length() > 0) {
            setPoolMessages(true);
            getUserToUserMessages(getTargetUserId());
        }
        messagesListView = findViewById(R.id.messagesListView);
        messagesAdapter = new MessageListAdapter(MessagesActivity.this, messagesMap);
        messagesListView.setAdapter(messagesAdapter);
        messagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });
    }

    public void startMessagePool() {
        setAction("check-user-new-messages");
        UserMessageConfig config = new UserMessageConfig();
        config.token = MainActivity.user.token;
        UserMessageConfig message = getMostRecentMessage();
        if(message != null) {
            config.creationDate = message.creationDate;
        }
        config.user = MainActivity.user.user;
        config.creator = getTargetUserId();
        config.owner = MainActivity.user.user;
        config.target = MainActivity.user.user;
        messageThread = new Thread(new MessageThreadPolling(this, config));
        messageThread.start();
    }

    public void getUserToUserMessages(String targetUser) {
        setAction("get-user-to-user-messages");
        UserMessageConfig config = new UserMessageConfig();
        config.creator = MainActivity.user.user;
        config.target = targetUser;
        config.page = String.valueOf(page);
        config.pageSize = String.valueOf(pageSize);
        config.resultsSize = String.valueOf(resultSize);
        HttpUserMessageAction action = new HttpUserMessageAction(this, config);
        action.execute();
    }

    public void sendMessage() {
        String message = userMessageText.getText().toString().trim();
        if(message.equals("")) {
            return;
        }
        UserMessageConfig config = new UserMessageConfig();
        UserMessageConfig mostRecentMessage = getMostRecentMessage();
        if(mostRecentMessage != null) {
            setAction("");
            config.subject = mostRecentMessage.subject;
            config.target = getTargetUserId();
            config.message = message;
            HttpUserMessageAction action = new HttpUserMessageAction(this, config);
            action.execute();
        }
    }

    public void sendMessage(View view) {
        sendMessage();
    }

    public void nextConversationMessagePage(View view) {
        page--;
        getUserToUserMessages(getTargetUserId());
    }

    public void previousConversationMessagePage(View view) {
        page++;
        getUserToUserMessages(getTargetUserId());
    }

    public void displayPaging() {
        if(page == 0 && resultSize > pageSize) {
            previousConversationButton.setVisibility(View.VISIBLE);
            nextConversationButton.setVisibility(View.GONE);
        } else if(page > 0 && resultSize > pageSize) {
            previousConversationButton.setVisibility(View.VISIBLE);
            nextConversationButton.setVisibility(View.VISIBLE);
        } else if(page > 0 && resultSize <= pageSize) {
            previousConversationButton.setVisibility(View.GONE);
            nextConversationButton.setVisibility(View.VISIBLE);
        }
    }

    public void deleteConversation() {
        setAction("delete-user-conversation");
        UserMessageConfig config = new UserMessageConfig();
        config.token = MainActivity.user.token;
        config.owner = MainActivity.user.user;
        config.target = getTargetUserId();
        HttpUserMessageAction action = new HttpUserMessageAction(this, config, "delete-user-conversation");
        action.execute();
    }

    public void deleteConversationDialog(String title, String message, final Activity activity) {
        AlertDialog dialog = new AlertDialog.Builder(activity).create();
        dialog.setMessage(message);
        dialog.setTitle(title);
        dialog.setCancelable(false);
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getApplicationContext().getString(R.string.dialogCancelButton), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getApplicationContext().getString(R.string.dialogDeleteButton), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                deleteConversation();
            }
        });
        dialog.show();
    }

    public void hideSoftKeyboard(EditText text) {
        InputMethodManager inputMethodManager = (InputMethodManager) MessagesActivity.this.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager .isActive()) {
            inputMethodManager .hideSoftInputFromWindow(text.getWindowToken(), 0);
        }
    }

    public void menu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_messages, popup.getMenu());
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
            case R.id.menuDeleteMessages:
                deleteConversationDialog("Delete Conversation Dialog","Delete conversation?", this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        messagePool = true;
    }
    @Override
    public void onStart(){
        super.onStart();
        messagePool = true;
    }
    @Override
    public void onStop(){
        super.onStop();
        messagePool = false;
    }

    @Override
    public  void onDestroy() {
        super.onDestroy();
        messagePool = false;
    }

    private static class MessageThreadPolling implements Runnable {
        Activity activity;
        private UserMessageConfig config;

        public MessageThreadPolling(Activity activity, UserMessageConfig config){
            this.activity = activity;
            this.config = config;
        }

        @Override
        public void run() {
            int timer = 1000;
            try {
                while(messagePool) {
                    sleep(timer);
                    sendMessagePoolRequest();
                    timer+= 1000;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        private void sendMessagePoolRequest() {
            setAction("check-user-new-messages");
            this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    HttpUserMessageAction action = new HttpUserMessageAction(activity, config);
                    action.execute();
                }
            });
        }
    }
}
