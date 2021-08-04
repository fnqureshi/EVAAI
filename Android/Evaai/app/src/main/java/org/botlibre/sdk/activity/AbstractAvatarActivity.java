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


import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import io.evaai.R;
import org.botlibre.sdk.activity.actions.HttpGetInstancesAction;
import org.botlibre.sdk.config.AvatarConfig;
import org.botlibre.sdk.config.BrowseConfig;

public abstract class AbstractAvatarActivity extends LibreActivity {

    public abstract void resetAvatar(AvatarConfig config);

    public void menu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_bot_avatar, popup.getMenu());
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
        menuInflater.inflate(R.menu.menu_bot_avatar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menuBrowse:
                browse(null);
                return true;
            case R.id.menuCreate:
                create(null);
                return true;
            case R.id.menuTest:
                test();
                return true;
            case R.id.menuEdit:
                edit();
                return true;
            case R.id.menuClear:
                clear();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void browse(View view) {
        MainActivity.browsing = true;
        BrowseConfig config = new BrowseConfig();
        config.type = "Avatar";
        config.typeFilter = "Featured";
        config.contentRating = MainActivity.contentRating;
        HttpGetInstancesAction action = new HttpGetInstancesAction(this, config);
        action.execute();
    }

    public abstract void edit();

    public abstract void create(View view);

    public abstract void test();

    public abstract void clear();
}
