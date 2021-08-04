/******************************************************************************
 *
 *  Copyright 2017 Paphus Solutions Inc.
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
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import io.evaai.R;

import org.botlibre.sdk.config.AvatarConfig;
import org.botlibre.sdk.config.InstanceConfig;

import java.util.List;

public class ListAvatarView extends LibreActivity {
	ArrayAdapter adapter;
	ListView listView;
	Intent data = new Intent();
	TextView textView;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (MainActivity.current == null) {
			finish();
			return;
		}
		setContentView(R.layout.activity_list_view);
		this.listView = (ListView) findViewById(R.id.theListView);
		this.textView = (TextView) findViewById(R.id.theTitle);
		this.textView.setText("Select Avatar");

		//this.adapter = new ImageListAdapter(this, R.layout.image_list, (List) MainActivity.getAllAvatars(this));
		this.listView.setAdapter(adapter);
		this.listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				AvatarConfig avatars = (AvatarConfig) (listView.getItemAtPosition(arg2));
				Toast.makeText(ListAvatarView.this, avatars.alias + " selected", Toast.LENGTH_SHORT).show();
				data.putExtra("avatar", avatars.alias);
				setResult(RESULT_OK, data);
				finish();
			}
		});
	}

}
