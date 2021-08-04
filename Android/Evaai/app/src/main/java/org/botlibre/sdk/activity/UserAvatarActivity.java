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
import android.widget.TextView;
import io.evaai.R;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpCreateAction;
import org.botlibre.sdk.activity.actions.HttpFetchBotAvatarAction;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.actions.HttpSaveBotAvatarAction;
import org.botlibre.sdk.activity.avatar.AvatarActivity;
import org.botlibre.sdk.activity.avatar.AvatarTestActivity;
import org.botlibre.sdk.config.AvatarConfig;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.util.Utils;

public class UserAvatarActivity extends AbstractAvatarActivity {

    public static boolean create;
    InstanceConfig instance;
    AvatarConfig avatar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MainActivity.user == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_user_avatar);
        Log.i("USER AVATAR ACTIVITY","AVATAR ACTIVITY ON CREATE METHOD THIS INSTANCE => " + this.instance);
        Log.i("USER AVATAR ACTIVITY","AVATAR ACTIVITY ON CREATE METHOD USER AVATAR => " + MainActivity.user.avatar);
        if (MainActivity.user.instanceAvatarId != 0L) {
            Log.i("USER AVATAR ACTIVITY","AVATAR ACTIVITY ON CREATE HERE 0");
            AvatarConfig config = new AvatarConfig();
            config.id = Long.toString(MainActivity.user.instanceAvatarId);
            config.name = MainActivity.user.user;
            HttpFetchBotAvatarAction action = new HttpFetchBotAvatarAction(this, config);
            action.execute();
        } else {
            Log.i("USER AVATAR ACTIVITY","AVATAR ACTIVITY ON CREATE HERE 1");
            HttpGetImageAction.fetchImage(this, MainActivity.user.avatar, findViewById(R.id.imageView));
        }
        create = false;
        MainActivity.browsing = false;
    }

    @Override
    public void onResume() {
        try {
            if (MainActivity.current == null) {
                finish();
                return;
            }
            AvatarConfig oldAvatar = this.avatar;
            if (create && (MainActivity.instance instanceof AvatarConfig)) {
                this.avatar = (AvatarConfig)MainActivity.instance;
                InstanceConfig config = new InstanceConfig();
                config.name = MainActivity.user.user;
                config.instanceAvatar = this.avatar.id;
                Log.i("USER AVATAR ACTIVITY","AVATAR ACTIVITY ON RESUME METHOD HERE 1");
                HttpAction action = new HttpSaveBotAvatarAction(this, config);
                action.execute();
            }
            create = false;
            if (MainActivity.browsing && (MainActivity.instance instanceof AvatarConfig)) {
                this.avatar = (AvatarConfig)MainActivity.instance;

                InstanceConfig config = new InstanceConfig();
                config.name = MainActivity.user.user;
                config.instanceAvatar = this.avatar.id;

                Log.i("USER AVATAR ACTIVITY","AVATAR ACTIVITY ON RESUME MAIN ACTIVITY INSTANCE => " + MainActivity.instance);
                Log.i("USER AVATAR ACTIVITY","AVATAR ACTIVITY ON RESUME MAIN THIS INSTANCE => " + this.instance);
                Log.i("USER AVATAR ACTIVITY","AVATAR ACTIVITY ON RESUME THIS AVATAR => " + this.avatar);
                Log.i("USER AVATAR ACTIVITY","AVATAR ACTIVITY ON RESUME THIS AVATAR ID => " + this.avatar.id);
                Log.i("USER AVATAR ACTIVITY","AVATAR ACTIVITY ON RESUME THIS AVATAR => " + this.avatar.avatar);
                Log.i("USER AVATAR ACTIVITY","AVATAR ACTIVITY ON RESUME THIS AVATAR INSTANCE => " + this.avatar.instance);

                MainActivity.user.instanceAvatarId = Long.parseLong(this.avatar.id);
                HttpAction action = new HttpSaveBotAvatarAction(this, config);
                action.execute();

                AvatarConfig avatarConfig = (AvatarConfig)this.avatar.credentials();
                action = new HttpFetchBotAvatarAction(this, avatarConfig);
                action.execute();
            }
            MainActivity.browsing = false;
            MainActivity.searching = false;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        super.onResume();
    }

    @Override
    public void resetAvatar(AvatarConfig config) {
        this.avatar = config;
        if (this.avatar == null) {
            Log.i("USER AVATAR ACTIVITY","RESET AVATAR USER INSTANCE AVATAR HERE 0");
            ((TextView)findViewById(R.id.nameText)).setText("");
            HttpGetImageAction.fetchImage(this, MainActivity.user.avatar, findViewById(R.id.icon));
            HttpGetImageAction.fetchImage(this, MainActivity.user.avatar, findViewById(R.id.imageView));
        } else {
            Log.i("USER AVATAR ACTIVITY","RESET AVATAR USER INSTANCE AVATAR HERE 1");
            ((TextView)findViewById(R.id.nameText)).setText(Utils.stripTags(this.avatar.name));
            HttpGetImageAction.fetchImage(this, this.avatar.avatar, findViewById(R.id.icon));
            HttpGetImageAction.fetchImage(this, this.avatar.avatar, findViewById(R.id.imageView));
        }
    }

    public void browse(View view) {
        super.browse(view);
    }

    public void create(View view) {
        AvatarConfig config = new AvatarConfig();
        config.name = MainActivity.user.user;
        config.type = "user-avatar";
        Log.i("USER AVATAR ACTIVITY","CREATE NEW AVATAR HERE 0");
        create = true;
        HttpAction action = new HttpCreateAction(this, config, false);
        action.execute();
    }

    public void edit() {
        if (this.avatar == null) {
            MainActivity.showMessage("This user does not have an avatar.", this);
            return;
        }
        MainActivity.instance = this.avatar;
        Intent intent = new Intent(this, AvatarActivity.class);
        startActivity(intent);
    }

    public void test() {
        Log.i("USER AVATAR ACTIVITY","USER AVATAR TEST METHOD CALL");
        if (this.avatar == null) {
            MainActivity.showMessage("This user does not have an avatar.", this);
            return;
        }
        MainActivity.instance = this.avatar;
        Intent intent = new Intent(this, AvatarTestActivity.class);
        startActivity(intent);
    }

    public void clear() {
        Log.i("USER AVATAR ACTIVITY","USER AVATAR CLEAR AVATAR METHOD CALL");
        InstanceConfig config = new InstanceConfig();
        config.instanceAvatar = null;
        MainActivity.user.instanceAvatarId = 0;
        HttpAction action = new HttpSaveBotAvatarAction(this, config);
        action.execute();
        resetAvatar(null);
    }
}
