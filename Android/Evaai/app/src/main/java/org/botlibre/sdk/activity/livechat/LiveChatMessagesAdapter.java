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

package org.botlibre.sdk.activity.livechat;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import io.evaai.R;
import org.botlibre.sdk.SDKConnection;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.config.ChatConfig;
import org.botlibre.sdk.config.UserConfig;
import org.botlibre.sdk.util.Utils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class LiveChatMessagesAdapter extends BaseAdapter {

    private Activity activity;
    private List<ChatConfig> list;

    public LiveChatMessagesAdapter(Activity activity, List<ChatConfig> list) {
        this.activity = activity;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatListViewHolder holder;
        ChatConfig config = (ChatConfig) getItem(position);
        holder = new ChatListViewHolder();
        LayoutInflater mInflater = (LayoutInflater)this.activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if(MainActivity.user != null && (MainActivity.user.user.equals(config.user) || "Nick".equals(config.user))) {
            convertView = mInflater.inflate(R.layout.live_chat_user_list_row, null);
            holder.liveChatReceiverAvatar = (ImageView)convertView.findViewById(R.id.liveChatReceiverAvatar);
            holder.liveChatReceiverDate = (TextView) convertView.findViewById(R.id.liveChatReceiverDate);
            holder.liveChatReceiverMessage = (TextView) convertView.findViewById(R.id.liveChatReceiverMessage);
            holder.liveChatReceiverDate.setText(Utils.displayTime(new Date()));
            holder.liveChatReceiverMessage.setText(Html.fromHtml(config.message));
            if (MainActivity.user != null && MainActivity.user.avatar != null) {
                HttpGetImageAction.fetchImage(this.activity, MainActivity.user.avatar, holder.liveChatReceiverAvatar);
            } else {
                HttpGetImageAction.fetchImage(this.activity, SDKConnection.defaultUserImage(), holder.liveChatReceiverAvatar);
            }
            convertView.setTag(holder);
        } else {
            if(MainActivity.user == null && ((LiveChatActivity)this.activity).nick.equals(config.user)) {
                convertView = mInflater.inflate(R.layout.live_chat_user_list_row, null);
                holder.liveChatReceiverAvatar = (ImageView)convertView.findViewById(R.id.liveChatReceiverAvatar);
                holder.liveChatReceiverDate = (TextView) convertView.findViewById(R.id.liveChatReceiverDate);
                holder.liveChatReceiverMessage = (TextView) convertView.findViewById(R.id.liveChatReceiverMessage);
                holder.liveChatReceiverDate.setText(Utils.displayTime(new Date()));
                holder.liveChatReceiverMessage.setText(Html.fromHtml(config.message));
                if (MainActivity.user != null && MainActivity.user.avatar != null) {
                    HttpGetImageAction.fetchImage(this.activity, MainActivity.user.avatar, holder.liveChatReceiverAvatar);
                } else {
                    HttpGetImageAction.fetchImage(this.activity, SDKConnection.defaultUserImage(), holder.liveChatReceiverAvatar);
                }
            } else {
                convertView = mInflater.inflate(R.layout.live_chat_bot_list_row, null);
                holder.liveChatSenderAvatar = (ImageView)convertView.findViewById(R.id.liveChatSenderAvatar);
                holder.liveChatSenderId = (TextView) convertView.findViewById(R.id.liveChatSenderId);
                holder.liveChatSenderDate = (TextView) convertView.findViewById(R.id.liveChatSenderDate);
                holder.liveChatSenderMessage = (TextView) convertView.findViewById(R.id.liveChatSenderMessage);
                holder.liveChatScrollView = (ScrollView) convertView.findViewById(R.id.liveChatScrollView);
                holder.liveChatWebView = (WebView) convertView.findViewById(R.id.liveChatWebView);
                if(config.message.contains("<") && config.message.contains(">")) {
                    if ("Info".equals(config.user) || "Error".equals(config.user)) {
                        config.message = "<span style='font-size:18px;color:white'>" + config.message + "</span>";
                    } else {
                        config.message = "<span style='font-size:18px;color:black'>" + config.message + "</span>";
                    }
                    config.message = ((LiveChatActivity) this.activity).linkPostbacks(config.message);
                    holder.liveChatSenderId.setText(config.user);
                    holder.liveChatSenderDate.setText(Utils.displayTime(new Date()));
                    holder.liveChatSenderMessage.setVisibility(convertView.GONE);
                    holder.liveChatScrollView.setVisibility(convertView.VISIBLE);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.liveChatSenderDate.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_RIGHT, R.id.liveChatScrollView);
                    holder.liveChatWebView.getSettings().setJavaScriptEnabled(true);
                    holder.liveChatWebView.getSettings().setDomStorageEnabled(true);
                    holder.liveChatWebView.addJavascriptInterface(((LiveChatActivity) this.activity).webAppInterface, "Android");
                    holder.liveChatWebView.setBackgroundColor(0);
                    holder.liveChatWebView.loadDataWithBaseURL(null, config.message, "text/html", "utf-8", null);
                } else {
                    holder.liveChatSenderMessage.setVisibility(convertView.VISIBLE);
                    holder.liveChatScrollView.setVisibility(convertView.GONE);
                    holder.liveChatSenderId.setText(config.user);
                    holder.liveChatSenderDate.setText(Utils.displayTime(new Date()));
                    holder.liveChatSenderMessage.setText(Utils.stripTags(config.message));
                }
                if("Info".equals(config.user)) {
                    holder.liveChatSenderAvatar.setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.info_2));
                    holder.liveChatSenderMessage.setTextColor(ContextCompat.getColor(activity, R.color.white));
                    holder.liveChatSenderMessage.setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.info_chat_bubble));
                    holder.liveChatWebView.setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.info_chat_bubble));
                } else if("Error".equals(config.user)) {
                    holder.liveChatSenderAvatar.setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.issue_2));
                    holder.liveChatSenderMessage.setTextColor(ContextCompat.getColor(activity, R.color.white));
                    holder.liveChatSenderMessage.setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.info_chat_bubble));
                } else {
                    if(config.avatar == null) {
                        Set<String> set =  ((LiveChatActivity) activity).getUsersMap().keySet();
                        ArrayList<String> aList = new ArrayList<>(set);
                        UserConfig userConfig = ((LiveChatActivity) activity).getUsersMap().get(config.user);
                        if(userConfig != null) {
                            HttpGetImageAction.fetchImage(activity, userConfig.avatar, holder.liveChatSenderAvatar);
                        } else {
                            HttpGetImageAction.fetchImage(this.activity, SDKConnection.defaultUserImage(), holder.liveChatSenderAvatar);
                        }
                    } else {
                        HttpGetImageAction.fetchImage(activity, config.avatar, holder.liveChatSenderAvatar);
                    }
                }
                convertView.setTag(holder);
            }
        }
        return convertView;
    }

    private class ChatListViewHolder {
        ImageView liveChatSenderAvatar;
        ImageView liveChatReceiverAvatar;
        TextView liveChatSenderId;
        TextView liveChatSenderDate;
        TextView liveChatReceiverDate;
        TextView liveChatSenderMessage;
        TextView liveChatReceiverMessage;
        ScrollView liveChatScrollView;
        WebView liveChatWebView;
    }

}
