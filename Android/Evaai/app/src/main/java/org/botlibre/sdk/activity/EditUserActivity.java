/******************************************************************************
 *
 *  Copyright 2014 Paphus Solutions Inc.
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

import io.evaai.R;

import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpUpdateUserAction;
import org.botlibre.sdk.config.UserConfig;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Activity for editing a user's details.
 */
public class EditUserActivity extends CreateUserActivity {

    private EditText userTagsText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MainActivity.current == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_edit_user);

        UserConfig user = MainActivity.user;
        
        setTitle("Edit: " + user.user);

        userTagsText = findViewById(R.id.userTagsText);
        userTagsText.setText(user.tags);
        TextView text = (EditText) findViewById(R.id.hintText);
        text.setText(user.hint);
        text = (EditText) findViewById(R.id.nameText);
        text.setText(user.name);
        text = (EditText) findViewById(R.id.emailText);
        text.setText(user.email);
        text = (EditText) findViewById(R.id.websiteText);
        text.setText(user.website);
        text = (EditText) findViewById(R.id.bioText);
        text.setText(user.bio);
		CheckBox checkbox = findViewById(R.id.showNameCheckBox);
		checkbox.setChecked(user.showName);
        text = findViewById(R.id.title);
        text.setText(user.user);
        Spinner accessModeSpin = findViewById(R.id.userAccessModeSpin);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, MainActivity.userAccessModes);
        accessModeSpin.setAdapter(adapter);
        
        //this is important to fetch the image to the icon.
        MainActivity.viewUser = MainActivity.user;
        
        HttpGetImageAction.fetchImage(this, MainActivity.viewUser.avatar, (ImageView)findViewById(R.id.icon));
	}

    public void browseTags(View view) {
        Intent intent = new Intent(EditUserActivity.this, ListTagsView.class);
        intent.putExtra("type", "User");
        startActivityForResult(intent,1);
    }

    /**
     * Create the user.
     */
    public void save(View view) {
    	UserConfig config = new UserConfig();
    	config.user = MainActivity.connection.getUser().user;
    	
    	EditText text = (EditText) findViewById(R.id.passwordText);
    	config.password = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.newPasswordText);
        config.newPassword = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.hintText);
        config.hint = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.nameText);
        config.name = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.emailText);
        config.email = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.websiteText);
        config.website = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.bioText);
        config.bio = text.getText().toString().trim();
		CheckBox checkbox = (CheckBox)findViewById(R.id.showNameCheckBox);
		config.showName = checkbox.isChecked();
        Spinner userAccess = (Spinner)findViewById(R.id.userAccessModeSpin);
        config.userAccess = userAccess.getSelectedItem().toString();
		config.tags = userTagsText.getText().toString().trim();
        
    	HttpUpdateUserAction action = new HttpUpdateUserAction(this, config);
    	action.execute();
    }

    /**
     * Cancel
     */
    public void cancel(View view) {        
    	finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case 1:
                if(resultCode == RESULT_OK) {
                    if(userTagsText.getText().toString().trim().isEmpty()) {
                        userTagsText.setText(data.getExtras().getString("tag"));
                    } else {
                        userTagsText.setText(userTagsText.getText().toString().trim() + ", " + data.getExtras().getString("tag"));
                    }
                }
                break;
        }
    }
}
