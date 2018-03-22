package com.beshev.arenashift.user;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.beshev.arenashift.beans.UpdateResponse;
import com.beshev.arenashift.database.EventsManager;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;

public class UserActivator {

    public static final String MSG_USER_ACTIVATE = "Юзърат активиран, браво";
    public static final String MSG_WRONG_ACTIVATION_CODE = "Грешен код, нещо бъркаш...";
    public static final String MSG_ERROR = "Опа стана някакъв проблем. Пиши на Стели...";

    public String activateUser(Context context,String activationCode) {

        UserInfoDownloader userInfoDownloader = new UserInfoDownloader();

        HashMap<String, String> userInfoMap = userInfoDownloader.getUserInfoFromServer(context,activationCode);

        if(userInfoMap == null) {

            if(userInfoDownloader.hadNetworkError()) {

                return MSG_ERROR;
            }

            return MSG_WRONG_ACTIVATION_CODE;

        } else {

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
			SharedPreferences.Editor editor = sharedPref.edit();
			editor.putString(UserManager.USERNAME, userInfoMap.get(UserManager.USERNAME));
            editor.putString(UserManager.CLIENT_USERNAME, userInfoMap.get(UserManager.CLIENT_USERNAME));
            editor.putString(UserManager.CLIENT_PASSWORD, userInfoMap.get(UserManager.CLIENT_PASSWORD));
			editor.commit();

			return MSG_USER_ACTIVATE;
        }
    }


    private class UserInfoDownloader {

        private boolean hadNetworkError = false;

        HashMap<String, String> getUserInfoFromServer(Context context, String activationCode) {

            HashMap<String, String> userInfoMap = null;

            EventsManager eventsManager = new EventsManager(context);

            final String productionServerRestURL = String.format("http://arenashiftserver.appspot.com/rest/user/activate/%s",
                    activationCode);

            final String devServerRestURL = String.format("http://dev-server-ip/rest/user/activate/%s",
                    activationCode);


            URL url;
            HttpURLConnection urlConnection;
            BufferedWriter bufferedWriter = null;
            BufferedReader bufferedReader = null;
            UpdateResponse updateResponse = null;

            try {

                url = new URL(productionServerRestURL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
                urlConnection.setRequestProperty("User-agent", System.getProperty("http.agent"));
                urlConnection.setConnectTimeout(10000);

                bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                String textLine = "";
                String endMSG = "";

                while((textLine = bufferedReader.readLine()) != null) {
                    endMSG += textLine;
                }

                userInfoMap = new Gson().fromJson(endMSG, HashMap.class);
                bufferedReader.close();

                urlConnection.disconnect();

            } catch (MalformedURLException e) {
                hadNetworkError = true;
                Log.e("AS", e.toString());
                eventsManager.logException("activation", e.toString(), new Date());
            } catch (SocketTimeoutException e) {
                hadNetworkError = true;
                Log.e("AS", e.toString());
                eventsManager.logException("activation", e.toString(), new Date());

            } catch (IOException e) {
                hadNetworkError = true;
                Log.e("AS", e.toString());
                eventsManager.logException("activation", e.toString(), new Date());


            }finally {

                if(bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        eventsManager.logException("activation", e.toString(), new Date());
                    }
                }
            }

            return userInfoMap;
        }

        boolean hadNetworkError() {
            return hadNetworkError;
        }
    }
}
