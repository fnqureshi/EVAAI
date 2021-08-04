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
package org.botlibre.sdk.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.app.Activity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.evaai.R;

import org.botlibre.sdk.activity.actions.HttpGetInstancesAction;
import org.botlibre.sdk.activity.actions.HttpVerifyEmailAction;
import org.botlibre.sdk.activity.avatar.AvatarSearchActivity;
import org.botlibre.sdk.activity.forum.ForumSearchActivity;
import org.botlibre.sdk.activity.graphic.GraphicSearchActivity;
import org.botlibre.sdk.activity.livechat.ChannelSearchActivity;
import org.botlibre.sdk.activity.script.ScriptSearchActivity;
import org.botlibre.sdk.config.BrowseConfig;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TermsActivity extends LibreActivity {

    public static String searchType = "";
    private boolean invalidDateOfBirth;
    private TextView termsLink;
    private EditText dateEdit;
    private ImageButton datePicker;
    private CheckBox terms;
    private DatePickerDialog.OnDateSetListener datePickerDialog;
    public static long YEAR = 365L * 24L * 60l * 60L * 1000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MainActivity.current == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_terms);
        termsLink = (TextView) findViewById(R.id.terms);
        dateEdit = (EditText) findViewById(R.id.birthday);
        terms = (CheckBox) findViewById(R.id.acceptTerms);
        datePicker = (ImageButton) findViewById(R.id.datePicker);
        termsLink.setText(Html.fromHtml(getApplicationContext().getString(R.string.termsPrivacyMessage)));
        termsLink.setMovementMethod(LinkMovementMethod.getInstance());

        datePicker.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePicker = new DatePickerDialog(
                        TermsActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        datePickerDialog, year, month, day);

                datePicker.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                datePicker.show();
            }
        });

        datePickerDialog = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                String birthDay = year + "-" + month + "-" + dayOfMonth;
                dateEdit.setText(birthDay);
            }
        };
    }

    public void login(View view) {
        login();
    }

    public void login() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    public void signUp(View view) {
        Intent intent = new Intent(this, CreateUserActivity.class);
        startActivity(intent);
        finish();
    }

    public boolean ageValidation(String date) {
        if (date == null || date.equals("")) {
            MainActivity.showMessage(getApplicationContext().getString(R.string.ageVerification), getApplicationContext().getString(R.string.emptyAgeField),this);
            return false;
        }
        Date birthDate = null;
        try {
            birthDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException exception) {
            MainActivity.showMessage(getApplicationContext().getString(R.string.ageVerification), getApplicationContext().getString(R.string.ageFormatError),this);
            Log.wtf("terms", exception);
        }
        if (birthDate != null) {
            if (System.currentTimeMillis() - birthDate.getTime() < (YEAR * 1)) {
                MainActivity.showMessage(getApplicationContext().getString(R.string.ageVerification), getApplicationContext().getString(R.string.validAge),this);
                return false;
            }
            if (System.currentTimeMillis() - birthDate.getTime() < (YEAR * 13)) {
                invalidDateOfBirth = true;
                showBirthdayDialog(getApplicationContext().getString(R.string.ageVerification), getApplicationContext().getString(R.string.illegalAge),this);
                return false;
            }
            return true;
        }
        return false;
    }

    public void showBirthdayDialog(String title, String message, final Activity activity) {
        AlertDialog dialog = new AlertDialog.Builder(activity).create();
        dialog.setMessage(message);
        dialog.setTitle(title);
        dialog.setCancelable(false);
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, getApplicationContext().getString(R.string.dialogOkButton), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (invalidDateOfBirth) {
                    MainActivity.BIRTHDATE_FAILED = true;
                    finish();
                }
            }
        });
        dialog.show();
    }

    public void accept(View view) {
        String dateOfBirth = dateEdit.getText().toString().trim();
        boolean notMinor = ageValidation(dateOfBirth);
        boolean termsAccepted = terms.isChecked();
        if (!notMinor) {
            return;
        } else if(!termsAccepted) {
            MainActivity.showMessage(getApplicationContext().getString(R.string.termsVerification), getApplicationContext().getString(R.string.acceptTermsError),this);
            return;
        }
        SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
        cookies.putBoolean("terms", termsAccepted);
        cookies.commit();
        if (searchType.equals("Search")) {
            if (MainActivity.type.equals("Bots")) {
                Intent intent = new Intent(this, BotSearchActivity.class);
                startActivity(intent);
            } else if (MainActivity.type.equals("Forums")) {
                Intent intent = new Intent(this, ForumSearchActivity.class);
                startActivity(intent);
            } else if (MainActivity.type.equals("Live Chat")) {
                Intent intent = new Intent(this, ChannelSearchActivity.class);
                startActivity(intent);
            } else if (MainActivity.type.equals("Workspaces")) {
                Intent intent = new Intent(this, DomainSearchActivity.class);
                startActivity(intent);
            } else if (MainActivity.type.equals("Avatars")) {
                Intent intent = new Intent(this, AvatarSearchActivity.class);
                startActivity(intent);
            } else if (MainActivity.type.equals("Scripts")) {
                Intent intent = new Intent(this, ScriptSearchActivity.class);
                startActivity(intent);
            } else if (MainActivity.type.equals("Graphics")) {
                Intent intent = new Intent(this, GraphicSearchActivity.class);
                startActivity(intent);
            }
        } else if (searchType.equals("Browse")) {
            BrowseConfig config = new BrowseConfig();
            if (MainActivity.type.equals("Bots")) {
                config.type = "Bot";
            } else if (MainActivity.type.equals("Forums")) {
                config.type = "Forum";
            } else if (MainActivity.type.equals("Live Chat")) {
                config.type = "Channel";
            } else if (MainActivity.type.equals("Workspaces")) {
                config.type = "Domain";
            } else if (MainActivity.type.equals("Avatars")) {
                config.type = "Avatar";
            } else if (MainActivity.type.equals("Scripts")) {
                MainActivity.importingBotScript = false;
                config.type = "Script";
            } else if (MainActivity.type.equals("Graphics")) {
                config.type = "Graphic";
            }
            config.contentRating = MainActivity.contentRating;
            HttpGetInstancesAction action = new HttpGetInstancesAction(this, config, true);
            action.execute();
        }
    }
}
