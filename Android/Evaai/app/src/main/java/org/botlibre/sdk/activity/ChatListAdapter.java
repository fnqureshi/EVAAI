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

import android.app.Activity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import io.evaai.R;
import org.botlibre.sdk.SDKConnection;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.activity.livechat.LiveChatActivity;
import org.botlibre.sdk.config.ChatConfig;
import org.botlibre.sdk.config.ChatResponse;
import org.botlibre.sdk.util.Utils;

public class ChatListAdapter extends ArrayAdapter<Object> {
	 
	Activity activity;
 
    public ChatListAdapter(Activity activity, int resourceId, List<Object> items) {
        super(activity, resourceId, items);
        this.activity = activity;
    }
 
    class ChatListViewHolder {
        ScrollView responseScroll;
        ImageView botAvatarView;
        ImageView userAvatarView;
        TextView userMessage;
        TextView botMessage;
        WebView botMessageWebView;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
    	ChatListViewHolder holder = null;
        Object message = getItem(position);
        holder = new ChatListViewHolder();
        LayoutInflater mInflater = (LayoutInflater)this.activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (message instanceof ChatConfig) {
            convertView = mInflater.inflate(R.layout.user_chat_list_row, null);
        	ChatConfig config = (ChatConfig)message;
            holder.userAvatarView = (ImageView)convertView.findViewById(R.id.userAvatarView);
            holder.userMessage = (TextView) convertView.findViewById(R.id.userMessage);
            holder.userMessage.setText(Html.fromHtml(config.message));
            if (MainActivity.user != null && MainActivity.user.avatar != null) {
            	HttpGetImageAction.fetchImage(this.activity, MainActivity.user.avatar, holder.userAvatarView);
            } else {
            	HttpGetImageAction.fetchImage(this.activity, SDKConnection.defaultUserImage(), holder.userAvatarView);
            }
            convertView.setTag(holder);
        } else if (message instanceof ChatResponse) {
            convertView = mInflater.inflate(R.layout.bot_chat_list_row, null);
        	ChatResponse config = (ChatResponse)message;
            holder.botAvatarView = (ImageView)convertView.findViewById(R.id.botAvatarView);
            holder.botMessage = (TextView) convertView.findViewById(R.id.botMessage);
            holder.responseScroll = (ScrollView) convertView.findViewById(R.id.responseScroll);
            holder.botMessageWebView = (WebView) convertView.findViewById(R.id.botMessageWebView);
            String html = Utils.linkHTML(config.message);
            if (html.contains("<") && html.contains(">")) {
                config.message = ((ChatActivity) this.activity).linkPostbacks(html);
                holder.botMessage.setVisibility(convertView.GONE);
                holder.responseScroll.setVisibility(convertView.VISIBLE);
                holder.botMessageWebView.getSettings().setJavaScriptEnabled(true);
                holder.botMessageWebView.getSettings().setDomStorageEnabled(true);
                holder.botMessageWebView.addJavascriptInterface(((ChatActivity) this.activity).webAppInterface, "Android");
                holder.botMessageWebView.setBackgroundColor(0);
                holder.botMessageWebView.loadDataWithBaseURL(null, config.message, "text/html", "utf-8", null);
            } else {
                holder.botMessage.setVisibility(convertView.VISIBLE);
                holder.responseScroll.setVisibility(convertView.GONE);
                holder.botMessage.setText(Utils.stripTags(config.message));
            }
            String avatar = ((ChatActivity)this.activity).getAvatarIcon(config);
            HttpGetImageAction.fetchImage(this.activity, avatar, holder.botAvatarView);
            convertView.setTag(holder);
        }
 
        return convertView;
    }
}