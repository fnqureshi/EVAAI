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

import java.util.List;
import org.botlibre.sdk.activity.MainActivity.LaunchType;
import org.botlibre.sdk.activity.actions.HttpAction;
import org.botlibre.sdk.activity.actions.HttpBrowseCategoriesAction;
import org.botlibre.sdk.activity.actions.HttpFetchAction;
import org.botlibre.sdk.activity.actions.HttpGetInstancesAction;
import org.botlibre.sdk.activity.actions.HttpPageInstancesAction;
import org.botlibre.sdk.activity.avatar.AvatarSearchActivity;
import org.botlibre.sdk.activity.forum.ForumSearchActivity;
import org.botlibre.sdk.activity.graphic.GraphicSearchActivity;
import org.botlibre.sdk.activity.livechat.ChannelSearchActivity;
import org.botlibre.sdk.activity.script.ScriptSearchActivity;
import org.botlibre.sdk.config.BrowseConfig;
import org.botlibre.sdk.config.InstanceConfig;
import org.botlibre.sdk.config.WebMediumConfig;
import io.evaai.R;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity for choosing an instance from the search results.
 */
public class BrowseActivity extends LibreActivity {
	private ListView list;
	public BrowseConfig browse;
	public List<WebMediumConfig> instances;
	public WebMediumConfig instance;
	protected int page = 0;
	public void superOnCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (MainActivity.current == null) {
			finish();
			return;
		}
		setContentView(R.layout.activity_browse);
		
		this.instances = MainActivity.instances;
		this.browse = MainActivity.browse;
		TextView title = (TextView) findViewById(R.id.title);
		title.setText("Browse " + getDisplayType() + "s");
		ListView list = (ListView) findViewById(R.id.instancesList);
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				ListView list = (ListView) findViewById(R.id.instancesList);
				selectInstance(list);
			}
		});
		
		// Remove search button if a single bot app.
		if (MainActivity.launchType == LaunchType.Bot) {
			if (getType().equals("Bot")) {
				findViewById(R.id.searchButton).setVisibility(View.GONE);
				findViewById(R.id.menuButton).setVisibility(View.GONE);
			}
		}
		
		resetView();
	}
	
	public void resetView() {
		list = (ListView) findViewById(R.id.instancesList);
		list.setAdapter(new ImageListAdapter(this, R.layout.image_list, this.instances));
		View next = (View) findViewById(R.id.nextButton);
		if (this.instances.size() >= 56 || this.page > 0) {
			if (this.instances.size() >= 56) {
				next.setVisibility(View.VISIBLE);
			} else {
				next.setVisibility(View.GONE);
			}
		} else {
			next.setVisibility(View.GONE);
		}
		View previous = (View) findViewById(R.id.previousButton);
		if (this.page > 0) {
			previous.setVisibility(View.VISIBLE);
		} else {
			previous.setVisibility(View.GONE);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@SuppressLint("Override")
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case 1: {
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					Intent upload = new Intent(Intent.ACTION_PICK);
					upload.setType("image/*");
					startActivityForResult(upload, 1);
				} else {
					Toast.makeText(BrowseActivity.this, getApplicationContext().getString(R.string.storagePermissionAccess), Toast.LENGTH_SHORT).show();
				}
				return;
			}
		}
	}
	
	public void previousPage(View view) {
		this.page--;
		this.browse.page = String.valueOf(this.page);
		HttpAction action = new HttpPageInstancesAction(this, this.browse);
    	action.execute();
	}
	
	public void nextPage(View view) {
		this.page++;
		this.browse.page = String.valueOf(this.page);
		HttpAction action = new HttpPageInstancesAction(this, this.browse);
    	action.execute();
	}
	
	public void superOnResume() {
		super.onResume();
	}
	
	@Override
	public void onResume() {
		if (MainActivity.current == null) {
			finish();
			return;
		}
		if (!this.instances.isEmpty() && !MainActivity.instances.isEmpty()) {
			if (this.instances.get(0).getClass() == MainActivity.instances.get(0).getClass()) {
				this.instances = MainActivity.instances;
			}
		} else {
			this.instances = MainActivity.instances;
		}

		resetView();
		
		super.onResume();
	}

	public void menu(View view) {
		PopupMenu popup = new PopupMenu(this, view);
	    MenuInflater inflater = popup.getMenuInflater();
	    inflater.inflate(R.menu.menu_browse, popup.getMenu());
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
		if (getType().equals("Bot") && MainActivity.launchType == LaunchType.Bot) {
			return false;
		}
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_browse, menu);
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
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void browseMyBots() {
		BrowseConfig config = new BrowseConfig();
		config.type = getType();
		config.typeFilter = "Personal";
		config.contentRating = "Mature";
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config, true);
		action.execute();
	}

	public void browseFeatured() {
		BrowseConfig config = new BrowseConfig();
		config.type = getType();
		config.typeFilter = "Featured";
		config.contentRating = MainActivity.contentRating;
		HttpGetInstancesAction action = new HttpGetInstancesAction(this, config, true);
		action.execute();
	}

	public void browseCategories() {
		BrowseConfig config = new BrowseConfig();
		config.contentRating = MainActivity.contentRating;
		HttpAction action = new HttpBrowseCategoriesAction(this, getType(), true);
		action.execute();
	}

	public String getDisplayType() {
		return getType();
	}
	
	public String getType() {
		return "Bot";
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem item = menu.findItem(R.id.menuMyBots);
        if (MainActivity.user == null) {
        	item.setVisible(false);
        }
        item.setTitle("My " + getType() + "s");
	    return true;
	}

	public void selectInstance(View view) {
        ListView list = (ListView) findViewById(R.id.instancesList);
        int index = list.getCheckedItemPosition();
        if (index < 0) {
        	MainActivity.showMessage("Select a bot", this);
        	return;
        }
        this.instance = instances.get(index);
		if (MainActivity.browsing) {
			MainActivity.instance = this.instance;
			finish();
			return;
		}
        InstanceConfig config = new InstanceConfig();
        config.id = this.instance.id;
        config.name = this.instance.name;
		
        HttpAction action = new HttpFetchAction(this, config);
    	action.execute();
	}

	public void chat(View view) {
        ListView list = (ListView) findViewById(R.id.instancesList);
        int index = list.getCheckedItemPosition();
        if (index < 0) {
        	MainActivity.showMessage("Select a bot", this);
        	return;
        }
        this.instance = instances.get(index);
        InstanceConfig config = new InstanceConfig();
        config.id = this.instance.id;
        config.name = this.instance.name;
		
        HttpAction action = new HttpFetchAction(this, config, true);
    	action.execute();
	}

	public void search(View view) {
		finish();
		if (!MainActivity.searching) {
			Intent intent = null;
			if (getType().equals("Domain")) {
		        intent = new Intent(this, DomainSearchActivity.class);
			} else if (getType().equals("Forum")) {
		        intent = new Intent(this, ForumSearchActivity.class);
			} else if (getType().equals("Channel")) {
		        intent = new Intent(this, ChannelSearchActivity.class);
			} else if (getType().equals("Avatar")) {
		        intent = new Intent(this, AvatarSearchActivity.class);
			} else if (getType().equals("Script")) {
				intent = new Intent(this, ScriptSearchActivity.class);
			}else if (getType().equals("Graphic")) {
				intent = new Intent(this, GraphicSearchActivity.class);
			} else {
				intent = new Intent(this, BotSearchActivity.class);
			}
	        startActivity(intent);
		}
	}
}
