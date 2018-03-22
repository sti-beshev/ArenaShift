package com.beshev.arenashift.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.beshev.arenashift.R;

import java.io.UnsupportedEncodingException;

import static android.util.Base64.encode;

public class UserManager {

    public static final String USERNAME = "username";
    public static final String CLIENT_USERNAME = "clientUsername";
    public static final String CLIENT_PASSWORD = "clientPassword";

    public static String getUserName(Context context) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        return sharedPref.getString(context.getString(R.string.username), USERNAME);
    }

    public static String getClientUserName(Context context) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        return sharedPref.getString(CLIENT_USERNAME, CLIENT_USERNAME);
    }

    public static String getClientPassword(Context context) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        return sharedPref.getString(CLIENT_PASSWORD, CLIENT_PASSWORD);
    }

    public static Boolean checkForUsername(Context context) {

        return !(getUserName(context).equals(USERNAME));
    }

    public static Boolean checkForClientUsername(Context context) {

        return !(getClientUserName(context).equals(CLIENT_USERNAME));
    }

    public static String createAuthHttpHeaderString(Context context) {

        String userCredentials = String.format("%s:%s", getClientUserName(context), getClientPassword(context));

        String basicAuth = null;

        try {

            basicAuth = String.format("Basic %s",
                    new String(encode(userCredentials.getBytes("UTF-8"), Base64.DEFAULT)));

        } catch (UnsupportedEncodingException e) {

            Log.e("ARENA_SHIFT", e.toString());
        }

        return basicAuth;
    }
}
