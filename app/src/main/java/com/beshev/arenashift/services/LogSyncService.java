package com.beshev.arenashift.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.beshev.arenashift.R;
import com.beshev.arenashift.beans.ArenaShiftUserEvent;
import com.beshev.arenashift.database.EventsManager;
import com.beshev.arenashift.user.UserManager;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class LogSyncService extends IntentService {

    private EventsManager eventsManager;
    private static Boolean isServiceRunning = false;

    public LogSyncService() {
        super("LogSyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        isServiceRunning = true;

        Context context = getApplicationContext();
        eventsManager = new EventsManager(context);

        HashMap<Integer, ArenaShiftUserEvent> eventsHashMap = eventsManager.getEventsAsHashMapWithTimeAsString();

        if (eventsHashMap != null) {

            ArrayList<ArenaShiftUserEvent> newEventsList = new ArrayList<>(eventsHashMap.values());

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            String username = sharedPref.getString(getString(R.string.username), UserManager.USERNAME);

            final String productionServerURL = "http://arenashiftserver.appspot.com/rest/user/sync/events/" + username;
            final String devServerRestURL = "http://dev-server/rest/user/sync/events/" + username;
            URL url;
            HttpURLConnection urlConnection;
            BufferedWriter bufferedWriter = null;
            BufferedReader bufferedReader;


            try {

                url = new URL(productionServerURL);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("PUT");
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                urlConnection.setRequestProperty("User-agent", System.getProperty("http.agent"));
                urlConnection.setRequestProperty("Authorization", UserManager.createAuthHttpHeaderString(context));
                urlConnection.setConnectTimeout(10000);

                bufferedWriter = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));

                bufferedWriter.write(new Gson().toJson(newEventsList));
                bufferedWriter.flush();
                bufferedWriter.close();

               // Waits for response from the server even if is empty or it will not work.

                bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                String textLine = "";
                String endMSG = "";

                while ((textLine = bufferedReader.readLine()) != null) {
                    endMSG += textLine;
                }

                bufferedReader.close();
                urlConnection.disconnect();

            } catch (MalformedURLException e) {

                Log.e("AS-Mal", e.toString());
                eventsManager.logException(username, e.toString(), new Date());
            } catch (SocketTimeoutException e) {
                Log.e("AS-Socket", e.toString());
                eventsManager.logException(username, e.toString(), new Date());
            } catch (IOException e) {
                Log.e("AS-IO", e.toString());
                eventsManager.logException(username, e.toString(), new Date());

            } finally {

                if (bufferedWriter != null) {
                    try {
                        bufferedWriter.close();
                    } catch (IOException e) {
                        eventsManager.logException(username, e.toString(), new Date());
                    }
                }
            }

            eventsManager.deleteUserEvents(eventsHashMap);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        isServiceRunning = false;
        eventsManager.closeDataBase();
    }

    public static Boolean isServiceRunning() {
        return isServiceRunning;
    }
}