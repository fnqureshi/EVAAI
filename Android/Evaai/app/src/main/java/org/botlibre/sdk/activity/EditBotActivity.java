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


import android.os.Bundle;
import android.view.View;
import io.evaai.R;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpUpdateAction;
import org.botlibre.sdk.config.InstanceConfig;

/**
 * Activity for editing a bot's details.
 */
public class EditBotActivity extends EditWebMediumActivity {
	@Override
	public String getType() {
		return "Bot";
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		if (MainActivity.current == null) {
			finish();
			return;
		}
        setContentView(R.layout.activity_edit_bot);
        
		resetView();
		
	}

		
	
    public void save(View view) {
    	InstanceConfig instance = new InstanceConfig();   	
    	saveProperties(instance);
    	
        
        HttpAction action = new HttpUpdateAction(this, instance);
        action.execute();
    }
}
