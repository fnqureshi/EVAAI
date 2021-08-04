/******************************************************************************
 *
 *  Copyright 2018 Paphus Solutions Inc.
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

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupMenu;

import io.evaai.R;

import org.botlibre.sdk.activity.actions.CustomAvatarAction;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpFetchAction;
import org.botlibre.sdk.activity.avatar.BrowseAvatarActivity;
import org.botlibre.sdk.config.AvatarConfig;

public class ChangeAvatarActivity extends BrowseAvatarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button uploadAvatarBtn = (Button) findViewById(R.id.uploadAvatarButton);
        uploadAvatarBtn.setVisibility(View.VISIBLE);
    }

    public void uploadAvatar(View view) {
        ActivityCompat.requestPermissions(ChangeAvatarActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
    }

    public void uploadCustomAvatar() {
        ActivityCompat.requestPermissions(ChangeAvatarActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        try {
            String imageUrl = MainActivity.getFilePathFromURI(this, data.getData());
            CustomAvatarAction customAvatar = new CustomAvatarAction(this, imageUrl, null);
            customAvatar.execute();
        } catch (Exception exception) {
            MainActivity.error(exception.getMessage(), exception, this);
            return;
        }
    }

    public void menu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_change_avatar_browse, popup.getMenu());
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
        if (getType().equals("Bot") && MainActivity.launchType == MainActivity.LaunchType.Bot) {
            return false;
        }
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_change_avatar_browse, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menuMyBots:
                browseMyBots();
                return true;
            case R.id.menuSearch:
                search(null);
                return true;
            case R.id.menuFeatured:
                browseFeatured();
                return true;
            case R.id.menuCategories:
                browseCategories();
                return true;
            case R.id.uploadCustomAvatar:
                uploadCustomAvatar();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void selectInstance(View view) {
        ListView list = (ListView) findViewById(R.id.instancesList);
        int index = list.getCheckedItemPosition();
        this.instance = this.instances.get(index);
        if (MainActivity.browsing) {
            MainActivity.instance = this.instance;
            SharedPreferences avatarIdCookie = MainActivity.current.getPreferences(Context.MODE_PRIVATE);
            avatarIdCookie.edit().remove("customAvatar").commit();
            SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
            cookies.putString("avatarID", this.instance.id);
            cookies.commit();
            this.finish();
            return;
        }
        AvatarConfig config = new AvatarConfig();
        config.id = this.instance.id;
        HttpAction action = new HttpFetchAction(this, config);
        action.execute();
    }
}
