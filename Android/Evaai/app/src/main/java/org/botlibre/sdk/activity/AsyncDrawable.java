
/******************************************************************************
 *
 *  Copyright 2021 Paphus Solutions Inc.
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

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import org.botlibre.sdk.activity.actions.HttpGetImageAction;
import java.lang.ref.WeakReference;

public class AsyncDrawable extends BitmapDrawable {
    private final WeakReference<HttpGetImageAction> action;

    public AsyncDrawable(Resources res, Bitmap bitmap, HttpGetImageAction bitmapWorkerTask) {
        super(res, bitmap);
        action = new WeakReference<HttpGetImageAction>(bitmapWorkerTask);

    }

    public HttpGetImageAction getAction() {
        return action.get();
    }
}
