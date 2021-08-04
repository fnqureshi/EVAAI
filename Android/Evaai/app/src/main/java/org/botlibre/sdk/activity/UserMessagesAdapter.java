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
import android.graphics.Typeface;
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
import java.util.LinkedHashMap;
import java.util.List;

public class UserMessagesAdapter extends BaseAdapter {

    public ViewHolder holder;
    private LinkedHashMap<String, UserMessageConfig> map;
    private Activity context;

    public UserMessagesAdapter(Activity context, LinkedHashMap<String, UserMessageConfig> map) {
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
            convertView = layoutInflater.inflate(R.layout.message_list, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String userId = getItem(position);
        UserMessageConfig messageConfig = UserMessagesActivity.userMessageMap.get(userId);
        if(messageConfig != null) {
            holder.userId.setText(userId);
            if (messageConfig.subject.startsWith("RE: ")) {
                messageConfig.subject = messageConfig.subject.substring(4, messageConfig.subject.length());
            }
            holder.topic.setText(Html.fromHtml(messageConfig.subject));
            HttpGetImageAction.fetchImage(this.context, messageConfig.avatar, holder.userFriendAvatar);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            try {
                Date date = formatter.parse(messageConfig.creationDate);
                holder.date.setText(Utils.displayDate(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String messageSubHeader =((UserMessagesActivity) context).getSubMessageHeader(messageConfig.message);
            holder.message.setText(Html.fromHtml(messageSubHeader));
        }
        return convertView;
    }

    public ViewHolder createViewHolder(View v) {
        holder = new UserMessagesAdapter.ViewHolder();
        holder.userFriendAvatar = v.findViewById(R.id.userAvatar);
        holder.userId = v.findViewById(R.id.userId);
        holder.topic = v.findViewById(R.id.messageTopic);
        holder.message = v.findViewById(R.id.userMessage);
        holder.date = v.findViewById(R.id.messageDate);
        holder.topic.setTypeface(null, Typeface.BOLD);
        return holder;
    }

    public class ViewHolder {
        public ImageView userFriendAvatar;
        public TextView userId;
        public TextView topic;
        public TextView message;
        public TextView date;
    }
}
