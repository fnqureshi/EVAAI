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
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Spinner;
import io.evaai.R;
import org.botlibre.sdk.activity.actions.HttpGetUsersAction;
import org.botlibre.sdk.config.BrowseUserConfig;

public class UserSearchActivity extends LibreActivity implements AdapterView.OnItemSelectedListener {

    final static int TAGTEXT = 1;

    private RadioButton userRadioBtn, botUserRadioBtn;
    private Spinner sortSpinner;
    private EditText userNameEditText;
    private ImageButton userTagBtn;
    private AutoCompleteTextView userTagTextView;
    private CheckBox userImagesCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        userRadioBtn = findViewById(R.id.publicUserRadio);
        botUserRadioBtn = findViewById(R.id.botUserRadio);
        userNameEditText = findViewById(R.id.filterUsername);
        userTagTextView = findViewById(R.id.userTag);
        userTagBtn = findViewById(R.id.userTagImageBtn);
        userImagesCheckbox = findViewById(R.id.userImagesCheckBox);
        userImagesCheckbox.setChecked(MainActivity.showImages);
        sortSpinner = findViewById(R.id.userSortSpin);
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_dropdown_item, new String[]{
                "connects",
                "last connect",
                "name",
                "date",
        });
        MainActivity.searching = !MainActivity.browsing;
        sortSpinner.setAdapter(adapter);
        sortSpinner.setOnItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(ListUsersViewActivity.isUserSelected) {
            finish();
        }
    }

    public void getUserTags(View view) {
        Intent intent = new Intent(UserSearchActivity.this, ListTagsView.class);
        intent.putExtra("type", "User");
        if(botUserRadioBtn.isChecked()) {
            intent.putExtra("type", "Bot");
        }
        startActivityForResult(intent, TAGTEXT);
    }

    public void browse(View view) {
        BrowseUserConfig config = new BrowseUserConfig();
        config.type = "User";
        config.sort = (String) sortSpinner.getSelectedItem();
        config.nameFilter = userNameEditText.getText().toString().trim();
        config.tag = userTagTextView.getText().toString().trim();
        MainActivity.showImages = userImagesCheckbox.isChecked();
        if(botUserRadioBtn.isChecked()) {
            config.userFilter = "Bot";
            config.type = "Bot";
            MainActivity.userFilter = false;
        } else {
            config.userFilter = "Public";
            MainActivity.userFilter = true;
        }
        HttpGetUsersAction action = new HttpGetUsersAction(this, config, MainActivity.browsing);
        action.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case TAGTEXT:
                if(resultCode == RESULT_OK){
                    userTagTextView.setText(userTagTextView.getText() + data.getExtras().getString("tag") + ", ");
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MainActivity.searching = false;
    }
}

