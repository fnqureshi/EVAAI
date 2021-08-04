/******************************************************************************
 *
 *  Copyright 2014-2021 Paphus Solutions Inc.
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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import io.evaai.R;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpUpdateUserAction;
import org.botlibre.sdk.config.UserConfig;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

public class UserTagsActivity extends LibreActivity {

    private boolean isSelected;
    private ListView userTagsListView;
    public LinkedHashSet<String> userTagSet;
    public HashSet<String> deleteTagSet;
    private EditText userTagEditTextView;
    public BaseAdapter userTagsAdapter;
    public void setIsAllSelected(boolean selected) { this.isSelected = selected; }
    public boolean getIsAllUsersSelected() { return isSelected; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_tags);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        HttpGetImageAction.fetchImage(this, MainActivity.viewUser.avatar, findViewById(R.id.icon));

        setIsAllSelected(false);
        userTagEditTextView = findViewById(R.id.userTagsText);
        userTagSet = new LinkedHashSet<>();
        deleteTagSet = new HashSet<>();
        userTagsListView = findViewById(R.id.userTagsListView);
        if(MainActivity.user != null) {
            getUserTags(MainActivity.user.tags.trim());
        }
        userTagsListView = findViewById(R.id.userTagsListView);
        userTagsAdapter = new UserTagsAdapter(UserTagsActivity.this, userTagSet);
        userTagsListView.setAdapter(userTagsAdapter);

        userTagEditTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    editUserTags(userTagEditTextView.getText().toString().trim());
                }
                return false;
            }
        });
    }

    public void clearEditTextView() {
        userTagEditTextView.setText("");
    }

    public void getUserTags(String tags) {
        String[] tagArray = tags.split(",");
        for(String tag : tagArray) {
            tag = tag.trim();
            if(!tag.isEmpty()) {
                userTagSet.add(tag);
            }
        }
    }

    public void browseTags(View view) {
        Intent intent = new Intent(UserTagsActivity.this, ListTagsView.class);
        intent.putExtra("type", "User");
        startActivityForResult(intent,1);
    }

    public void selectUserTags(View view) {
        setIsAllSelected(!isSelected);
        userTagsAdapter.notifyDataSetChanged();
    }

    public void deleteUserTags(View view) {
        Iterator<String> it = deleteTagSet.iterator();
        while(it.hasNext()) {
            userTagSet.remove(it.next());
        }
        deleteTagSet = new HashSet<>();
        Iterator<String> userTagIterator = userTagSet.iterator();
        StringBuilder stringBuilder = new StringBuilder();
        while(userTagIterator.hasNext()) {
            stringBuilder.append(userTagIterator.next() + ",");
        }
        deleteUserTags(stringBuilder.toString().trim());
    }

    public void deleteUserTags(String tags) {
        UserConfig config = new UserConfig();
        config.user = MainActivity.connection.getUser().user;
        config.tags =  tags;
        config.token = MainActivity.user.token;
        HttpUpdateUserAction action = new HttpUpdateUserAction(this, config);
        action.execute();
    }

    public void editUserTags(String tags) {
        UserConfig config = new UserConfig();
        config.user = MainActivity.connection.getUser().user;
        config.tags =  MainActivity.connection.getUser().tags + "," + tags;
        config.token = MainActivity.user.token;
        HttpUpdateUserAction action = new HttpUpdateUserAction(this, config);
        action.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case 1:
                if(resultCode == RESULT_OK) {
                    editUserTags(data.getExtras().getString("tag").trim());
                }
                break;
        }
    }
}
