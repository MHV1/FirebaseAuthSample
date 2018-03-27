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
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import static com.mhv.firebaseauth.Constants.ACCOUNT_TYPE;
import static com.mhv.firebaseauth.Constants.AUTH_TOKEN_TYPE;

/**
 * This Activity is the entry point of this application.
 *
 * Here we check if there is a user currently authenticated or
 * if we need to launch the Activity used for authenticating
 * the user if needed (LoginActivity).
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_AUTHENTICATE = 1000;

    private TextView mInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mInfoTextView = findViewById(R.id.info_text_view);

        // We start by checking if we have a auth token stored.
        // We only support an single account of a certain type, otherwise we would
        // have to choose from existing accounts. The same goes for the auth token type.
        // The GetAuthTokenCallback helps us know when we have a result.
        final AccountManager accountManager = AccountManager.get(this);
        accountManager.getAuthTokenByFeatures(ACCOUNT_TYPE, AUTH_TOKEN_TYPE,
                null, this, null, null, new GetAuthTokenCallback(), null);
    }

    private class GetAuthTokenCallback implements AccountManagerCallback<Bundle> {

        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            Bundle bundle;
            try {
                // Get the result of our request.
                bundle = result.getResult();

                // This intent comes from the Authenticator and can be used to launch
                // the Activity used for authenticating the user if needed (AuthActivity).
                final Intent intent = (Intent) bundle.get(AccountManager.KEY_INTENT);

                // The Intent being not null means user authentication is required.
                if (intent != null) {
                    startActivityForResult(intent, REQUEST_CODE_AUTHENTICATE);
                } else {
                    // We have already logged in successfully!
                    String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                    final String accountName = bundle.getString(AccountManager.KEY_ACCOUNT_NAME);

                    // TODO: In a real app you would use the auth token for something...
                    // Here we just display it as it is.
                    mInfoTextView.setText(getString(R.string.main_info_message,
                            accountName, authToken));
                }
            } catch (OperationCanceledException e) {
                Toast.makeText(MainActivity.this, "Authentication cancelled",
                        Toast.LENGTH_SHORT).show();
            } catch (AuthenticatorException | IOException e) {
                Log.e(TAG, "A error occurred while authenticating user", e);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
