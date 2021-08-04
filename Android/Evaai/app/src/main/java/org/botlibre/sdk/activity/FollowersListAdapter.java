/******************************************************************************
 *
 *  Copyright 2018-2020 Paphus Solutions Inc.
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

public class FollowersListAdapter extends BaseAdapter {

    private View prevConvertView;
    private ViewHolder holder;
    private List<UserConfig> followersList;
    private Activity context;
    private OnItemClickListener listener;
    private OnItemTouchListener touchListener;

    public interface OnItemClickListener {
        void onItemClick(String userId);
    }

    public interface OnItemTouchListener {
        void onItemDoubleTap(String userId);
    }

    public FollowersListAdapter(Activity activity, int resourceId, List<UserConfig> followersList, OnItemClickListener listener, OnItemTouchListener touchListener) {
        this.context = activity;
        this.followersList = followersList;
        this.listener = listener;
        this.touchListener = touchListener;
    }

    @Override
    public int getCount() {
        if(this.followersList != null) {
            return this.followersList.size();
        } else {
            return 0;
        }
    }

    @Override
    public UserConfig getItem(int position) {
        return followersList.get(position);
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
            convertView = layoutInflater.inflate(R.layout.followers_list, null);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        UserConfig userConfig = getItem(position);
        HttpGetImageAction.fetchImage(this.context, userConfig.avatar, holder.followerImageView);
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
                    UserConfig userConfig = getItem(position);
                    listener.onItemClick(userConfig.user);
                }
            }
        });

        final View finalConvertView = convertView;
        convertView.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    UserConfig userConfig = getItem(position);
                    if(touchListener != null) {
                        touchListener.onItemDoubleTap(userConfig.user);
                    }
                    return super.onDoubleTap(e);
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i("FOLLOWERS ADAPTER","GESTURE LISTENER ON TOUCH EVENT");
                if(prevConvertView != null) {
                    prevConvertView.setBackgroundResource(0);
                }
                prevConvertView = finalConvertView;
                finalConvertView.setBackgroundResource(R.color.DarkGray);
                return gestureDetector.onTouchEvent(event);
            }
        });

        return convertView;
    }

    public ViewHolder createViewHolder(View v) {
        holder = new ViewHolder();
        holder.followerImageView = v.findViewById(R.id.followerAvatarImageView);
        holder.username = v.findViewById(R.id.followerId);
        holder.name = v.findViewById(R.id.followerName);
        return holder;
    }

    public class ViewHolder {
        public ImageView followerImageView;
        public TextView username;
        public TextView name;
    }
}
