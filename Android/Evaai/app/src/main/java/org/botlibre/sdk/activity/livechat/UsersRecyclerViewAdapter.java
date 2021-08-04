/******************************************************************************
 *
 *  Copyright 2018 Paphus Solutions Inc.
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
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import io.evaai.R;
import org.botlibre.sdk.SDKConnection;
import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import org.botlibre.sdk.config.UserConfig;
import java.util.List;

public class UsersRecyclerViewAdapter extends RecyclerView.Adapter<UsersRecyclerViewAdapter.UsersViewHolder> {

    private Activity activity;
    private List<UserConfig> list;
    private OnItemClickListener listener;
    private OnItemTouchListener touchListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemTouchListener {
        void onItemDoubleTap(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemTouchListener(OnItemTouchListener touchListener) {
        this.touchListener = touchListener;
    }

    public UsersRecyclerViewAdapter(Activity activity, List<UserConfig> list) {
        this.activity = activity;
        this.list = list;
    }

    @Override
    public UsersRecyclerViewAdapter.UsersViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_list, viewGroup, false);
        return new UsersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UsersRecyclerViewAdapter.UsersViewHolder holder, int position) {
        UserConfig user = list.get(position);
        String userAvatar = user.avatar;
        if(userAvatar != null) {
            HttpGetImageAction.fetchImage(this.activity, userAvatar, holder.userAvatarImageView);
        } else {
            HttpGetImageAction.fetchImage(this.activity, SDKConnection.defaultUserImage(), holder.userAvatarImageView);
        }
        holder.userIdTextView.setText(user.user);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public UserConfig getItem(int position) {
        return list.get(position);
    }

    class UsersViewHolder extends RecyclerView.ViewHolder {

        public ImageView userAvatarImageView;
        public TextView userIdTextView;
        public CardView userCardView;

        public UsersViewHolder(View itemView) {
            super(itemView);
            userAvatarImageView = itemView.findViewById(R.id.imageView);
            userIdTextView = itemView.findViewById(R.id.nameView);
            userCardView = itemView.findViewById(R.id.userCardView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null) {
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
            GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTapEvent(MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if(touchListener != null) {
                            int position = getAdapterPosition();
                            if(position != RecyclerView.NO_POSITION) {
                                touchListener.onItemDoubleTap(position);
                            }
                        }
                        return true;
                    }
                    return false;
                }
            };
            final GestureDetector detector = new GestureDetector(activity, gestureListener);
            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return detector.onTouchEvent(event);
                }
            });
        }
    }
}


