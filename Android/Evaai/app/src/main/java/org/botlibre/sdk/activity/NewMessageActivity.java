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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import io.evaai.R;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpUserMessageAction;
import org.botlibre.sdk.config.UserMessageConfig;

public class NewMessageActivity extends LibreActivity {

    private ImageButton userFriendsBtn;
    private EditText userIdEditText, topicEditText;
    public EditText messageEditText;
    private String action;
    public void setAction(String action) { this.action = action; }
    public String getAction() { return this.action; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);
        if(MainActivity.viewUser != null) {
            HttpGetImageAction.fetchImage(this, MainActivity.viewUser.avatar, findViewById(R.id.icon));
        } else if(MainActivity.user != null) {
            HttpGetImageAction.fetchImage(this, MainActivity.user.avatar, findViewById(R.id.icon));
        }
        userFriendsBtn = findViewById(R.id.userFriends);
        userIdEditText = findViewById(R.id.userId);
        topicEditText = findViewById(R.id.messageTopic);
        messageEditText = findViewById(R.id.userMessage);

        Intent intent = getIntent();
        if(intent != null && intent.getExtras() != null) {
            String userId = getIntent().getExtras().getString("view-user-id");
            if (userId != null) {
                userIdEditText.setText(userId);
            }
        }

        userFriendsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ListFriendsViewActivity.class);
                startActivityForResult(intent,1);
            }
        });

        messageEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    sendMessage();
                }
                return false;
            }
        });
    }

    public void sendMessage() {
        String userId = userIdEditText.getText().toString().trim();
        String message = messageEditText.getText().toString().trim();
        if(userId.equals("") || message.equals("")) return;
        setAction("new-user-message");
        UserMessageConfig config = new UserMessageConfig();
        if(MainActivity.user != null && MainActivity.viewUser != null && !MainActivity.user.equals(MainActivity.viewUser)) {
            config.creator = MainActivity.viewUser.user;
            config.owner = MainActivity.viewUser.user;
        } else {
            config.creator = MainActivity.user.user;
            config.owner = MainActivity.user.user;
        }
        config.target = userId;
        config.subject = topicEditText.getText().toString().trim();
        config.message = message;
        HttpUserMessageAction action = new HttpUserMessageAction(this, config);
        action.execute();
    }

    public void sendMessage(View view) {
        sendMessage();
    }

    public void hideSoftKeyboard(EditText text) {
        InputMethodManager inputMethodManager = (InputMethodManager) NewMessageActivity.this.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager .isActive()) {
            inputMethodManager .hideSoftInputFromWindow(text.getWindowToken(), 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case 1:
                if(resultCode == RESULT_OK) {
                    userIdEditText.setText(data.getExtras().getString("friend-id"));
                }
                break;
        }
    }
}
