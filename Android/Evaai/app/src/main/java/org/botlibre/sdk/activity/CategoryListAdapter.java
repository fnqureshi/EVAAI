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

import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.config.ContentConfig;
import org.botlibre.sdk.util.Utils;

import io.evaai.R;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CategoryListAdapter extends ArrayAdapter<ContentConfig> {
	 
	Activity activity;
 
    public CategoryListAdapter(Activity activity, int resourceId, List<ContentConfig> items) {
        super(activity, resourceId, items);
        this.activity = activity;
    }
 
    class ImageListViewHolder {
        ImageView imageView;
        TextView nameView;
        TextView descriptionView;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
    	ImageListViewHolder holder = null;
    	ContentConfig config = getItem(position);
 
        LayoutInflater mInflater = (LayoutInflater)this.activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.image_list, null);
            holder = new ImageListViewHolder();
            holder.imageView = (ImageView)convertView.findViewById(R.id.imageView);
            holder.nameView = (TextView) convertView.findViewById(R.id.nameView);
            holder.descriptionView = (TextView)convertView.findViewById(R.id.descriptionView);
            convertView.setTag(holder);
        } else {
            holder = (ImageListViewHolder) convertView.getTag();
        }
        
        holder.nameView.setText(config.name);
        holder.descriptionView.setText(Utils.stripTags(config.description));
        if (MainActivity.showImages) {
        	HttpGetImageAction.fetchImage(this.activity, config.icon, holder.imageView);
        } else {
        	holder.imageView.setVisibility(View.GONE);
        }
 
        return convertView;
    }
}