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

import org.botlibre.sdk.activity.actions.HttpCreateUserAction;
import org.botlibre.sdk.config.UserConfig;
import io.evaai.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Activity for creating a new user.
 */
public class CreateUserActivity extends LibreActivity {

    private CheckBox terms;
    private EditText dateEdit;
    private TextView termsLink;
    private ImageButton datePicker;
    private boolean invalidDateOfBirth;
    public static long YEAR = 365L * 24L * 60l * 60L * 1000L;
    private DatePickerDialog.OnDateSetListener datePickerDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MainActivity.current == null) {
            finish();
            return;
        }
        setContentView(R.layout.activity_create_user);

        termsLink = findViewById(R.id.terms);
        dateEdit = findViewById(R.id.birthday);
        terms = findViewById(R.id.acceptTerms);
        datePicker = findViewById(R.id.datePicker);
        /*termsLink.setText(Html.fromHtml("Accept <a href=\"https://www.botlibre.com/terms.jsp\">terms</a>"
                + " and <a href=\"https://www.botlibre.com/privacy.jsp\">privacy</a>"));*/
        termsLink.setText(Html.fromHtml(getApplicationContext().getString(R.string.termsLink)));
        termsLink.setMovementMethod(LinkMovementMethod.getInstance());
        Spinner accessModeSpin = findViewById(R.id.userAccessModeSpin);
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, MainActivity.userAccessModes);
        accessModeSpin.setAdapter(adapter);

        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePicker = new DatePickerDialog(
                        CreateUserActivity.this,
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
    
    /**
     * Create the user.
     */
    public void create(View view) {
    	UserConfig config = new UserConfig();
    	
        EditText text = (EditText) findViewById(R.id.userText);
        config.user = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.passwordText);
        config.password = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.hintText);
        config.hint = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.nameText);
        config.name = text.getText().toString().trim();
        text = (EditText) findViewById(R.id.emailText);
        config.email = text.getText().toString().trim();
		CheckBox checkbox = (CheckBox)findViewById(R.id.showNameCheckBox);
		config.showName = checkbox.isChecked();
        CheckBox termsCheckbox = (CheckBox)findViewById(R.id.acceptTerms);
        config.acceptTerms = termsCheckbox.isChecked();
        EditText birthday  = (EditText)findViewById(R.id.birthday);
        config.dateOfBirth = "2000-01-01"; //birthday.getText().toString().trim();
        Spinner userAccess = (Spinner)findViewById(R.id.userAccessModeSpin);
        config.userAccess = userAccess.getSelectedItem().toString();
        String dateOfBirth = dateEdit.getText().toString().trim();

        boolean notMinor = true; //ageValidation(dateOfBirth);
        boolean termsAccepted = true; //terms.isChecked();
        if (!notMinor) {
            return;
        } else if(!termsAccepted) {
            MainActivity.showMessage(getApplicationContext().getString(R.string.termsVerification), getApplicationContext().getString(R.string.acceptTermsError),this);
            return;
        }
        SharedPreferences.Editor cookies = MainActivity.current.getPreferences(Context.MODE_PRIVATE).edit();
        cookies.putBoolean("terms", termsAccepted);
        cookies.commit();

    	HttpCreateUserAction action = new HttpCreateUserAction(this, config);
    	action.execute();
    }
    
    /**
     * Cancel
     */
    public void cancel(View view) {        
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
        } catch (ParseException e) {
            MainActivity.showMessage(getApplicationContext().getString(R.string.ageVerification), getApplicationContext().getString(R.string.ageFormatError),this);
            e.printStackTrace();
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
}
