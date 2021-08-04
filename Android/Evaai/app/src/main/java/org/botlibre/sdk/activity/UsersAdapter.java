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

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import io.evaai.R;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.config.UserConfig;
import org.botlibre.sdk.util.Utils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

public class UsersAdapter extends BaseAdapter {

    public UsersAdapter.ViewHolder holder;
    private LinkedHashMap<String, UserConfig> map;
    private Activity context;
    public UsersAdapter(Activity context, LinkedHashMap<String, UserConfig> map) {
        this.context = context;
        this.map = map;
    }

    @Override
    public int getCount() {
        if(this.map != null) {
            return this.map.size();
        } else {
            return 0;
        }
    }

    @Override
    public String getItem(int position) {
        String username = "";
        if(this.map.keySet() != null) {
            List<String> keys = new ArrayList<>(this.map.keySet());
            if (keys != null) {
                username = keys.get(position);
            }
        }
        return username;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            if(!MainActivity.userFilter) {
                convertView = layoutInflater.inflate(R.layout.bot_list_row, null);
            } else {
                convertView = layoutInflater.inflate(R.layout.user_list_row, null);
            }
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String userId = getItem(position);
        UserConfig userConfig = map.get(userId);
        if(userConfig != null) {
            if (userConfig.avatar != null && MainActivity.showImages) {
                holder.userAvatar.setVisibility(View.VISIBLE);
                HttpGetImageAction.fetchImage(this.context, userConfig.avatar, holder.userAvatar);
            } else {
                holder.userAvatar.setVisibility(View.GONE);
            }
            if(!MainActivity.userFilter) {
                holder.name.setText(userConfig.name);
                holder.bio.setText(userConfig.bio);
                String[] connectsStr = userConfig.connects.split("/");
                holder.connects.setText("connects " + connectsStr[0] + ",  daily " + connectsStr[1] + ",  weekly " + connectsStr[2] + ",  monthly " + connectsStr[3]);
            } else {
                holder.userId.setText(userId);
                holder.name.setText(userConfig.name);
                holder.bio.setText(userConfig.bio);
                try {
                    if ((userConfig.lastConnect != null) && (userConfig.lastConnect.length() > 0) && (!"null".equals(userConfig.lastConnect))) {
                        SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
                        Date date = formatter.parse(userConfig.lastConnect);
                        String dateStr = Utils.displayDate(date);
                        String connects = holder.connects.getText().toString();
                        connects += "connects " + userConfig.connects + ", last connected " + dateStr;
                        holder.connects.setText(connects);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return convertView;
    }

    public ViewHolder createViewHolder(View v) {
        holder = new UsersAdapter.ViewHolder();
        holder.userAvatar = v.findViewById(R.id.userAvatar);
        holder.userId = v.findViewById(R.id.userId);
        holder.name = v.findViewById(R.id.userName);
        holder.bio = v.findViewById(R.id.userBio);
        holder.connects = v.findViewById(R.id.userConnects);
        return holder;
    }

    public class ViewHolder {
        public ImageView userAvatar;
        public TextView userId;
        public TextView name;
        public TextView bio;
        public TextView connects;
    }
}
