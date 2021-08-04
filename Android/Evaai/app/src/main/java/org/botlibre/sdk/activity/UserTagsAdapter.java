/******************************************************************************
 *
 *  Copyright 2014-2019 Paphus Solutions Inc.
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

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import io.evaai.R;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class UserTagsAdapter extends BaseAdapter {

    private Activity context;
    private UserTagsAdapter.ViewHolder holder;
    private List<String> list;
    private LinkedHashSet<String> tagSet;

    public void addTagToSet(String tag) {
        tagSet.add(tag);
    }

    public void setTagList() {
        list = new ArrayList<>(tagSet);
    }


    public UserTagsAdapter(Activity context, LinkedHashSet<String> tagSet) {
        this.context = context;
        this.tagSet = tagSet;
        list = new ArrayList<>(tagSet);
    }

    @Override
    public int getCount() {
        if(list != null) {
            return list.size();
        }
        return 0;
    }

    @Override
    public String getItem(int position) {
        String tag = list.get(position);
        return tag;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final UserTagsAdapter.ViewHolder holder;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if(convertView == null) {
            convertView = layoutInflater.inflate(R.layout.user_tag_list_row, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (UserTagsAdapter.ViewHolder) convertView.getTag();
        }
        String tag = getItem(position);
        holder.tag.setText(tag);
        if(((UserTagsActivity)this.context).getIsAllUsersSelected()) {
            holder.checkBox.setChecked(true);
            ((UserTagsActivity) context).deleteTagSet.add(tag);
        } else if(!((UserTagsActivity)this.context).getIsAllUsersSelected()) {
            holder.checkBox.setChecked(false);
            ((UserTagsActivity) context).deleteTagSet.remove(tag);
        }


        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = getItem(position);
                if(holder.checkBox.isChecked()) {
                    ((UserTagsActivity) context).deleteTagSet.add(tag);
                } else {
                    ((UserTagsActivity) context).deleteTagSet.remove(tag);
                }
            }
        });

        return convertView;
    }

    public UserTagsAdapter.ViewHolder createViewHolder(View v) {
        holder = new UserTagsAdapter.ViewHolder();
        holder.checkBox = v.findViewById(R.id.tagCheckBox);
        holder.tag = v.findViewById(R.id.userTag);
        return holder;
    }

    public class ViewHolder {
        public CheckBox checkBox;
        public TextView tag;
    }
}
