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

package org.botlibre.sdk.activity.actions;

import android.app.Activity;
import android.widget.EditText;
import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.MessagesActivity;
import org.botlibre.sdk.activity.NewMessageActivity;
import org.botlibre.sdk.activity.UserMessagesActivity;
import org.botlibre.sdk.config.UserMessageConfig;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class HttpUserMessageAction extends HttpUIAction {

    private String action = "";
	private UserMessageConfig config;
	private ArrayList<UserMessageConfig> messagesList;
	private Activity activity;

	public HttpUserMessageAction(Activity activity, UserMessageConfig config) {
		super(activity);
		this.config = config;
		this.activity = activity;
		if(activity instanceof MessagesActivity) {
            action = ((MessagesActivity) activity).getAction();
        } else if(activity instanceof NewMessageActivity){
			action = ((NewMessageActivity) activity).getAction();
		} else if(activity instanceof UserMessagesActivity){
			action = ((UserMessagesActivity) activity).getAction();
		}
	}

	public HttpUserMessageAction(Activity activity, UserMessageConfig config, String action) {
		super(activity);
		this.config = config;
		this.activity = activity;
		this.action = action;
	}

	@Override
	protected void onPreExecute() {
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
			messagesList = null;
			if(action.equals("get-user-to-user-messages")) {
				messagesList = MainActivity.connection.getUserToUserMessages(this.config);
			} else if(action.equals("check-user-new-messages")) {
				messagesList = MainActivity.connection.checkUserNewMessages(this.config);
			} else if(action.equals("get-user-greeting")) {
				this.config = MainActivity.connection.getUserGreeting(this.config);
			} else if(action.equals("get-user-conversations")) {
				messagesList = MainActivity.connection.getUserConversations(this.config);
			} else if(action.equals("delete-user-conversation")) {
				this.config = MainActivity.connection.deleteUserConversation(this.config);
			} else {
				this.config = MainActivity.connection.createUserMessage(this.config);
			}
		} catch (Exception exception) {
			this.exception = exception;
		}
		return "";
	}

	@Override
	public void onPostExecute(String xml) {
		super.onPostExecute(xml);
		if(this.exception != null) {
			return;
		}
		if(this.action.equals("get-user-to-user-messages")) {
			if(messagesList != null) {
				((MessagesActivity) this.activity).messagesMap.clear();
				for (UserMessageConfig config : messagesList) {
					((MessagesActivity) this.activity).messagesMap.put(config.id, config);
				}
				if(messagesList.size() > 0) {
					UserMessageConfig config = messagesList.get(0);
					((MessagesActivity) this.activity).setResultSize(Integer.parseInt(config.resultsSize));
				} else {
					((MessagesActivity) this.activity).setResultSize(0);
				}
				if(MessagesActivity.getMostRecentMessage() == null) {
					MessagesActivity.setMostRecentMessage(messagesList.get(messagesList.size() - 1));
				}
				((MessagesActivity)this.activity).messagesListView.post(new Runnable() {
					@Override
					public void run() {
						((MessagesActivity) activity).messagesAdapter.notifyDataSetChanged();
						((MessagesActivity) activity).messagesListView.invalidateViews();
					}
				});
				((MessagesActivity)this.activity).displayPaging();
				if(((MessagesActivity)this.activity).getPoolMessages()) {
					((MessagesActivity) this.activity).setPoolMessages(false);
					((MessagesActivity)this.activity).startMessagePool();
				}
			}
		} else if(this.action.equals("new-user-message")) {
			EditText sendMessageEditText = ((NewMessageActivity) this.activity).messageEditText;
			((NewMessageActivity) this.activity).hideSoftKeyboard(sendMessageEditText);
			MessagesActivity.setMostRecentMessage(this.config);
			this.activity.finish();
		} else if(this.action.equals("check-user-new-messages")) {
			if(messagesList != null) {
				UserMessageConfig messageConfigOne = MessagesActivity.getMostRecentMessage();
				UserMessageConfig messageConfigTwo = null;
				if(messagesList.size() >= 1) {
					messageConfigTwo = messagesList.get(messagesList.size() - 1);
				}
				if(messageConfigOne != null && messageConfigTwo != null) {
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
					Date dateOne, dateTwo;
					try {
						dateOne = formatter.parse(messageConfigOne.creationDate);
						dateTwo = formatter.parse(messageConfigTwo.creationDate);
						if(dateOne != null && dateTwo != null) {
							if(dateTwo.after(dateOne)) {
								MessagesActivity.setMostRecentMessage(messageConfigTwo);
								UserMessagesActivity.newPollMessage = true;
								((MessagesActivity) this.activity).messagesMap.put(messageConfigTwo.id, messageConfigTwo);
								((MessagesActivity) this.activity).messagesListView.post(new Runnable() {
									@Override
									public void run() {
										((MessagesActivity) activity).messagesAdapter.notifyDataSetChanged();
										((MessagesActivity) activity).messagesListView.invalidateViews();
										if (((MessagesActivity) activity).messagesListView.getCount() > 2) {
											((MessagesActivity) activity).messagesListView.setSelection(((MessagesActivity) activity).messagesListView.getCount() - 2);
										}
									}
								});
							}
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
		} else if(action.equals("get-user-conversations")) {
			try {
				if(messagesList != null) {
					UserMessagesActivity.userMessageMap.clear();
					for (UserMessageConfig config : messagesList) {
						UserMessagesActivity.userMessageMap.put(config.user, config);
					}
					if(messagesList.size() > 0) {
						UserMessageConfig config = messagesList.get(0);
						((UserMessagesActivity) this.activity).setResultSize(Integer.parseInt(config.resultsSize));
					} else {
						((UserMessagesActivity) this.activity).setResultSize(0);
					}
					((UserMessagesActivity) this.activity).displayPaging();
					((UserMessagesActivity) this.activity).messagesListView.post(new Runnable() {
						@Override
						public void run() {
							((UserMessagesActivity) activity).messagesAdapter.notifyDataSetChanged();
							((UserMessagesActivity) activity).messagesListView.invalidateViews();
						}
					});
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (this.action.equals("delete-user-conversation")) {
			this.activity.finish();
		} else {
			if (this.config.id != null && !this.config.id.equals("")) {
				if(this.config.creator.startsWith("@")) {
					return;
				}
				EditText sendMessageEditText = ((MessagesActivity) this.activity).userMessageText;
				((MessagesActivity) this.activity).hideSoftKeyboard(sendMessageEditText);
				String message = ((MessagesActivity) this.activity).userMessageText.getText().toString().trim();
				if(this.config.creator.startsWith("@")) {
					UserMessageConfig config = new UserMessageConfig();
					config.id = this.config.parent;
					config.subject = this.config.subject;
					config.message = message;
					config.creator = MainActivity.user.user;
					config.owner = MainActivity.user.user;
					config.target = this.config.creator;
					config.creationDate = this.config.creationDate;
					((MessagesActivity) this.activity).messagesMap.put(config.id, config);
				}
				((MessagesActivity) this.activity).userMessageText.setText("");
				((MessagesActivity) this.activity).messagesMap.put(this.config.id, this.config);
				UserMessagesActivity.newMessage = true;
				((MessagesActivity)this.activity).messagesListView.post(new Runnable() {
					@Override
					public void run() {
						((MessagesActivity) activity).messagesAdapter.notifyDataSetChanged();
						((MessagesActivity) activity).messagesListView.invalidateViews();
						if (((MessagesActivity) activity).messagesListView.getCount() > 2) {
							((MessagesActivity) activity).messagesListView.setSelection(((MessagesActivity) activity).messagesListView.getCount() - 2);
						}
					}
				});
				((MessagesActivity) this.activity).setAction("check-user-new-messages");
				MessagesActivity.setMostRecentMessage(this.config);
			}
		}
    }
}
