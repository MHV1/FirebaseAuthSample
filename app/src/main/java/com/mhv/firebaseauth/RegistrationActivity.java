/*
 * Copyright (C) 2018 Milan Herrera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mhv.firebaseauth;

import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * In charge of the Sign up process. Since it's not an AuthenticatorActivity decendent,
 * it returns the result back to the calling activity, which is an AuthenticatorActivity,
 * and it return the result back to the Authenticator
 */
public class RegistrationActivity extends AppCompatActivity {

    private String TAG = "RegistrationActivity";

    private EditText mUserNameInputView;
    private EditText mUserEmailInputView;
    private EditText mUserPasswordView;

    private BroadcastReceiver mAuthReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mUserNameInputView = findViewById(R.id.username_input_view);
        mUserEmailInputView = findViewById(R.id.user_email_input_view);
        mUserPasswordView = findViewById(R.id.user_password_input_view);

        final String accountName = getIntent()
                .getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

        // If we already have a user name we can show it.
        if (!TextUtils.isEmpty(accountName)) {
            mUserEmailInputView.setText(accountName);
        }

        Button registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        TextView existingAccountView = findViewById(R.id.existing_account_view);
        existingAccountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        mAuthReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "authReceiver - onReceive: " + intent);
                finishRegistration(intent);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AuthService.ACTION_REGISTER);

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mAuthReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mAuthReceiver);
    }

    private void registerUser() {
        String userName = mUserNameInputView.getText()
                .toString();

        String userEmail = mUserEmailInputView.getText()
                .toString().toLowerCase().trim();

        String userPassword = mUserPasswordView.getText()
                .toString().trim();

        if ((!TextUtils.isEmpty(userEmail) && !TextUtils.isEmpty(userPassword))
                && userPassword.length() > 6) {

            Intent intent = new Intent(this, AuthService.class);
            intent.setAction(AuthService.ACTION_REGISTER);

            intent.putExtra(AuthService.EXTRA_USER_NAME, userName);
            intent.putExtra(AuthService.EXTRA_USER_EMAIL, userEmail);
            intent.putExtra(AuthService.EXTRA_USER_PASSWORD, userPassword);

            startService(intent);
        } else {
            Toast.makeText(this, "Please enter a valid username/password. " +
                    "Password must be at least 6 characters.", Toast.LENGTH_LONG).show();
        }
    }

    private void finishRegistration(Intent intent) {
        if (intent.hasExtra(AccountManager.KEY_ERROR_MESSAGE)) {
            Toast.makeText(getBaseContext(),
                    intent.getStringExtra(AccountManager.KEY_ERROR_MESSAGE),
                    Toast.LENGTH_SHORT).show();
        } else {
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }
}
