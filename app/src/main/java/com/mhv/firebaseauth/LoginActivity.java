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

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AccountAuthenticatorActivity
        implements View.OnClickListener {

    @SuppressWarnings("unused")
    private static final String TAG = "AuthenticationActivity";

    private static final int REQUEST_CODE_REGISTER = 123;

    public static final String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    private EditText mUserEmailInputView;
    private EditText mUserPasswordInputView;

    private AccountManager mAccountManager;

    private BroadcastReceiver mAuthReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(AccountManager.KEY_ERROR_MESSAGE)) {
                Toast.makeText(LoginActivity.this,
                        intent.getStringExtra(AccountManager.KEY_ERROR_MESSAGE),
                        Toast.LENGTH_SHORT).show();
            } else {
                completeLogin(intent);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAccountManager = AccountManager.get(this);

        mUserEmailInputView = findViewById(R.id.user_email_input_view);
        mUserPasswordInputView = findViewById(R.id.user_password_input_view);

        final Button loginButton = findViewById(R.id.login_button);
        loginButton.setOnClickListener(this);

        final TextView registerRequestView = findViewById(R.id.register_request_view);
        registerRequestView.setOnClickListener(this);

        // We already have a name but the login (authToken) is not valid anymore.
        // User must authenticate again to get a fresh token.
        final String accountName = getIntent()
                .getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

        // If we already have a user name we can show it.
        if (!TextUtils.isEmpty(accountName)) {
            mUserEmailInputView.setText(accountName);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AuthService.ACTION_LOGIN);

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mAuthReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mAuthReceiver);
    }

    public void login() {
        final String userEmail = mUserEmailInputView.getText()
                .toString().toLowerCase().trim();

        final String userPassword = mUserPasswordInputView.getText()
                .toString().trim();

        if ((!TextUtils.isEmpty(userEmail) && !TextUtils.isEmpty(userPassword))
                && userPassword.length() > 6) {

            Intent intent = new Intent(this, AuthService.class);
            intent.setAction(AuthService.ACTION_LOGIN);

            intent.putExtra(AuthService.EXTRA_USER_EMAIL, userEmail);
            intent.putExtra(AuthService.EXTRA_USER_PASSWORD, userPassword);
            intent.putExtra(AuthService.EXTRA_AUTH_TOKEN, getIntent()
                    .getStringExtra(AccountManager.KEY_AUTHTOKEN));

            startService(intent);
        } else {
            Toast.makeText(this, "Please enter a valid username/password. " +
                    "Password must be at least 6 characters.", Toast.LENGTH_LONG).show();
        }
    }

    private void completeLogin(Intent intent) {
        String accountName = intent
                .getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

        String accountPassword = intent
                .getStringExtra(AccountManager.KEY_PASSWORD);

        final Account account = new Account(accountName,
                intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            String authToken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);

            mAccountManager.addAccountExplicitly(account, accountPassword, null);
            mAccountManager.setAuthToken(account, Constants.AUTH_TOKEN_TYPE, authToken);
        } else {
            mAccountManager.setPassword(account, accountPassword);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);

        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_REGISTER && resultCode == RESULT_OK) {
            completeLogin(data);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_button:
                login();
                break;
            case R.id.register_request_view:
                // Here we launch the Registration activity where
                // the user can create an account and subsequently log in.
                // Once that is done a result will be returned to this activity.
                Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);

                if (getIntent().getExtras() != null) {
                    intent.putExtras(getIntent().getExtras());
                }

                startActivityForResult(intent, REQUEST_CODE_REGISTER);
                break;
        }
    }
}
