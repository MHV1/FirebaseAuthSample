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
package com.mhv.firebaseauth.util;

import android.util.Base64;
import android.util.Log;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

// TODO: The logic in this class is only for testing/mocking purposes.
// This logic is what you would have in the auth component in your backend.
// More info: https://firebase.google.com/docs/auth/android/manage-users
public class AuthUtils {

    private static final String TAG = "AuthUtils";

    // For more info about what's going on here, see:
    // https://firebase.google.com/docs/auth/admin/verify-id-tokens
    public static String generateFirebaseAuthToken(String userName) {
        Long nowSeconds = System.currentTimeMillis() / 1000;

        // TODO: Under ANY circumstance hardcode and/or commit private keys!!!
        // This is just a dirty way of testing authentication using Firebase!
        // Don't use it in your app! Remember: Every time you commit keys to a repo a puppy dies.
        final String privateKey = "test_your_secret_key";

        PKCS8EncodedKeySpec spec =
                new PKCS8EncodedKeySpec(Base64.decode(privateKey, Base64.DEFAULT));

        KeyFactory kf;
        try {
            kf = KeyFactory.getInstance("RSA");

            String compactJws = Jwts.builder().setHeaderParam("typ", "JWT")
                    .setPayload(
                        "{" +
                            "\"iss\":\"firebase-adminsdk-wswac@fir-auth-dfed0.iam.gserviceaccount.com\","                            + "\n" +
                            "\"sub\":\"firebase-adminsdk-wswac@fir-auth-dfed0.iam.gserviceaccount.com\","                            + "\n" +
                            "\"aud\":\"https://identitytoolkit.googleapis.com/google.identity.identitytoolkit.v1.IdentityToolkit\"," + "\n" +
                            "\"uid\":\"" + userName + "\","                                                                          + "\n" +
                            "\"iat\":\"" + Long.toString(nowSeconds) + "\","                                                         + "\n" +
                            "\"exp\":\"" + Long.toString(nowSeconds + (60 * 60)) + "\"" /* Maximum expiration time is one hour */    + "\n" +
                        "}"
                    )
                    .signWith(SignatureAlgorithm.RS256, kf.generatePrivate(spec)).compact();

            Log.d(TAG, compactJws);
            return compactJws;

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }
}
