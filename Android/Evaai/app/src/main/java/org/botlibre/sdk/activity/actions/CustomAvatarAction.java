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

package org.botlibre.sdk.activity.actions;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import org.botlibre.sdk.activity.MainActivity;
import org.botlibre.sdk.activity.WebMediumAdminActivity;
import org.botlibre.sdk.config.WebMediumConfig;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Random;

public class CustomAvatarAction extends HttpUIAction {

	WebMediumConfig config;
	String file;

	public CustomAvatarAction(Activity activity, String file, WebMediumConfig config) {
		super(activity);
		this.config = config;
		this.file = file;
	}

	@Override
	protected String doInBackground(Void... params) {
		try {
			Bitmap bitmap = MainActivity.connection.loadImage(file, 600, 600);
			if (bitmap == null) {
				throw new Exception("Could not load image, try a different source");
			}
			String fileName = String.valueOf(new Random().nextLong()) + ".jpg";
			File file = HttpGetImageAction.getFile(fileName, this.activity);
			file.createNewFile();
			FileOutputStream stream = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
			stream.flush();
			stream.close();
			SharedPreferences.Editor cookie = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
			cookie.putString("customAvatar", fileName);
			cookie.commit();
		} catch (Exception exception) {
			this.exception = exception;
		}
		return "";
	}

	@Override
	public void onPostExecute(String xml) {
		super.onPostExecute(xml);
		if (this.exception != null) {
			return;
		}
		SharedPreferences avatarIdCookie = MainActivity.current.getPreferences(Context.MODE_PRIVATE);
		avatarIdCookie.edit().remove("avatarID").commit();
		this.activity.finish();
	}
}