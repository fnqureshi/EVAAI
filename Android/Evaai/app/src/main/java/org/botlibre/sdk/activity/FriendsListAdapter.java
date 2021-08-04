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
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import io.evaai.R;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.config.UserConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FriendsListAdapter extends BaseAdapter {

    private View prevConvertView;
    private ViewHolder holder;
    private HashMap<String, UserConfig> map;
    private Activity context;
    private OnItemClickListener listener;
    private OnItemTouchListener touchListener;

    public interface OnItemClickListener {
        void onItemClick(String userId);
    }

    public interface OnItemTouchListener {
        void onItemDoubleTap(String userId);
    }

    public FriendsListAdapter(Activity context, HashMap<String, UserConfig> map, OnItemClickListener listener, OnItemTouchListener touchListener) {
        this.context = context;
        this.map = map;
        this.listener = listener;
        this.touchListener = touchListener;
    }

    @Override
    public int getCount() {
        if(this.map != null) {
            return this.map.size();
        }
        return 0;
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
        final ViewHolder holder;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            if(this.context instanceof ViewUserFriendsActivity || this.context instanceof ListFriendsViewActivity) {
                convertView = layoutInflater.inflate(R.layout.user_friends_list_row, null);
            } else {
                convertView = layoutInflater.inflate(R.layout.friends_list, null);
            }
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        String friendId = getItem(position);
        UserConfig userConfig = map.get(friendId);
        HttpGetImageAction.fetchImage(this.context, userConfig.avatar, holder.friendImageView);
        if(this.context instanceof UserFriendsActivity) {
            if(((UserFriendsActivity)this.context).getIsAllUsersSelected()) {
                holder.checkBox.setChecked(true);
                ((UserFriendsActivity) context).friendshipSet.add(friendId);
            } else if(!((UserFriendsActivity)this.context).getIsAllUsersSelected()) {
                holder.checkBox.setChecked(false);
                ((UserFriendsActivity) context).friendshipSet.remove(friendId);
            }
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,5,20,0);
        holder.username.setLayoutParams(params);
        holder.username.setText(userConfig.user);
        if(!userConfig.name.isEmpty()) {
            params.setMargins(0,0,20,5);
            holder.name.setLayoutParams(params);
            holder.name.setText(userConfig.name);
        } else {
            holder.name.setVisibility(View.GONE);
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null) {
                    String friendId = getItem(position);
                    listener.onItemClick(friendId);
                }
            }
        });

        final View finalConvertView = convertView;
        convertView.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    String friendId = getItem(position);
                    if(touchListener != null) {
                        touchListener.onItemDoubleTap(friendId);
                    }
                    return super.onDoubleTap(e);
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(prevConvertView != null) {
                    prevConvertView.setBackgroundResource(0);
                }
                prevConvertView = finalConvertView;
                finalConvertView.setBackgroundResource(R.color.DarkGray);
                return gestureDetector.onTouchEvent(event);
            }
        });

        if(holder.checkBox != null) {
            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String friend = getItem(position);
                    if(holder.checkBox.isChecked()) {
                        ((UserFriendsActivity) context).friendshipSet.add(friend);
                    } else {
                        ((UserFriendsActivity) context).friendshipSet.remove(friend);
                    }
                }
            });
        }
        return convertView;
    }

    public ViewHolder createViewHolder(View v) {
        holder = new ViewHolder();
        if(this.context instanceof UserFriendsActivity) {
            holder.checkBox = v.findViewById(R.id.checkBox);
        }
        holder.friendImageView = v.findViewById(R.id.friendAvatarImageView);
        holder.username = v.findViewById(R.id.friendId);
        holder.name = v.findViewById(R.id.friendName);
        return holder;
    }

    public class ViewHolder {
        public ImageView friendImageView;
        public CheckBox checkBox;
        public TextView username;
        public TextView name;
    }
}
