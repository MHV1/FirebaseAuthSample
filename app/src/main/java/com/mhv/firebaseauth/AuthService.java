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
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.mhv.firebaseauth.util.AuthUtils;

public class AuthService extends IntentService {

    private static final String TAG = "AuthService";

    public static final String ACTION_LOGIN = "auth_action_login";
    public static final String ACTION_REGISTER = "auth_action_register";

    public static final String EXTRA_USER_NAME = "extra_user_name";
    public static final String EXTRA_USER_EMAIL = "extra_user_email";
    public static final String EXTRA_USER_PASSWORD = "extra_user_password";

    public static final String EXTRA_AUTH_TOKEN = "extra_auth_token";

    private FirebaseAuth mAuth;

    public AuthService() {
        super("AuthService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        String action = intent.getAction();
        if (action == null) {
            return;
        }

        mAuth = FirebaseAuth.getInstance();

        switch (action) {
            case ACTION_REGISTER:
                register(intent);
                break;
            case ACTION_LOGIN:
                login(intent);
                break;
        }
    }

    private void register(Intent registrationIntent) {
        final String userName = registrationIntent.getStringExtra(EXTRA_USER_NAME);
        final String userEmail = registrationIntent.getStringExtra(EXTRA_USER_EMAIL);
        final String userPassword = registrationIntent.getStringExtra(EXTRA_USER_PASSWORD);

        Log.d(TAG, "Registering - user: " + userName + " email: " + userEmail);

        final Bundle registerData = new Bundle();

        mAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String authToken = AuthUtils.generateFirebaseAuthToken(userName);

                            registerData.putString(AccountManager.KEY_ACCOUNT_NAME, userEmail);
                            registerData.putString(AccountManager.KEY_ACCOUNT_TYPE,
                                    Constants.ACCOUNT_TYPE);
                            registerData.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                            registerData.putString(AccountManager.KEY_PASSWORD, userPassword);

                            Log.d(TAG, "Registration successful - user: " + userName
                                    + " email: " + userEmail + " token: " + authToken);

                            // TODO: Also broadcast errors!
                            final Intent result = new Intent(ACTION_REGISTER);
                            result.putExtras(registerData);
                            LocalBroadcastManager
                                    .getInstance(AuthService.this).sendBroadcast(result);

                        } else {
                            Log.e(TAG, "Registration failed", task.getException());
                            registerData.putString(AccountManager.KEY_ERROR_MESSAGE,
                                    task.getException().getMessage());
                        }
                    }
                });
    }

    private void login(Intent loginIntent) {
        final String userEmail = loginIntent.getStringExtra(EXTRA_USER_EMAIL);
        final String userPassword = loginIntent.getStringExtra(EXTRA_USER_PASSWORD);
        final String authToken = loginIntent.getStringExtra(EXTRA_AUTH_TOKEN);

        Log.d(TAG, "Login - user: " + userEmail + " authToken: " + authToken);

        final Bundle loginData = new Bundle();

        if (!TextUtils.isEmpty(authToken)) {
            mAuth.signInWithCustomToken(authToken)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                loginData.putString(AccountManager.KEY_ACCOUNT_NAME, userEmail);
                                loginData.putString(AccountManager.KEY_ACCOUNT_TYPE,
                                        Constants.ACCOUNT_TYPE);
                                loginData.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                                loginData.putString(AccountManager.KEY_PASSWORD, userPassword);

                                Log.d(TAG, "Registration successful - user: " + userEmail
                                        + " token: " + authToken);

                            } else {
                                Log.e(TAG, "Login failed", task.getException());
                                loginData.putString(AccountManager.KEY_ERROR_MESSAGE,
                                        task.getException().getMessage());
                            }
                        }
                    });

            final Intent result = new Intent(ACTION_LOGIN);
            result.putExtras(loginData);
            LocalBroadcastManager.getInstance(this).sendBroadcast(result);
        }
    }
}
