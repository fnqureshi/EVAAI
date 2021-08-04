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

package org.botlibre.sdk.activity.avatar;

import org.botlibre.sdk.activity.EditWebMediumActivity;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpUpdateAction;
import org.botlibre.sdk.config.AvatarConfig;

import io.evaai.R;

import android.os.Bundle;
import android.view.View;

/**
 * Activity for editing a avatar's details.
 */
public class EditAvatarActivity extends EditWebMediumActivity {
	
	@Override
	public String getType() {
		return "Avatar";
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		if (MainActivity.current == null) {
			finish();
			return;
		}
        setContentView(R.layout.activity_edit_avatar);
		
		resetView();
	}

    public void save(View view) {
    	AvatarConfig instance = new AvatarConfig();
    	saveProperties(instance);
        
    	HttpAction action = new HttpUpdateAction(this, instance);
        action.execute();
    }
}
