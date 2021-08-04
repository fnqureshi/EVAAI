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
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import io.evaai.R;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.config.UserMessageConfig;
import org.botlibre.sdk.util.Utils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MessageListAdapter extends BaseAdapter {

    public ViewHolder holder;
    private HashMap<String, UserMessageConfig> map;
    private Activity context;
    public MessageListAdapter(Activity context, HashMap<String, UserMessageConfig> map) {
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
        String friendName = "";
        if(this.map.keySet() != null) {
            List<String> keys = new ArrayList<>(this.map.keySet());
            if (keys != null) {
                friendName = keys.get(position);
            }
        }
        return friendName;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = createViewHolder(convertView);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        String userId = getItem(position);
        UserMessageConfig config = map.get(userId);
        if((MainActivity.user.user).equals(config.creator)) {
            convertView = layoutInflater.inflate(R.layout.receiver_message_row, null);
            holder.messageView = convertView.findViewById(R.id.receiverMessage);
            holder.dateView = convertView.findViewById(R.id.receiverMessageDate);
            if(config != null) {
                String date = "";
                holder.messageView.setText(Html.fromHtml(config.message));
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                try {
                    Date creationDate = formatter.parse(config.creationDate);
                    date = Utils.displayTimestamp(creationDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                holder.dateView.setText(date);
            }
            convertView.setTag(holder);
        } else {
            convertView = layoutInflater.inflate(R.layout.sender_message_row, null);
            holder.userAvatarImageView = convertView.findViewById(R.id.senderAvatar);
            holder.usernameView = convertView.findViewById(R.id.creatorId);
            holder.messageView = convertView.findViewById(R.id.senderMessage);
            holder.dateView = convertView.findViewById(R.id.senderMessageDate);
            if(config != null) {
                HttpGetImageAction.fetchImage(this.context, config.avatar, holder.userAvatarImageView);
                holder.usernameView.setText(config.creator);
                holder.messageView.setText(Html.fromHtml(config.message));
                String date = "";
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                try {
                    Date creationDate = formatter.parse(config.creationDate);
                    date = Utils.displayTimestamp(creationDate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                holder.dateView.setText(date);
            }
            convertView.setTag(holder);
        }
        return convertView;
    }

    public ViewHolder createViewHolder(View v) {
        holder = new ViewHolder();
        return holder;
    }

    public class ViewHolder {
        public ImageView userAvatarImageView;
        public TextView usernameView;
        public TextView messageView;
        public TextView dateView;
    }
}
