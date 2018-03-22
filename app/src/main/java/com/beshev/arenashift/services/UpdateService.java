package com.beshev.arenashift.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.beshev.arenashift.R;
import com.beshev.arenashift.beans.Shift;
import com.beshev.arenashift.beans.UpdateResponse;
import com.beshev.arenashift.database.EventsManager;
import com.beshev.arenashift.database.ShiftsManager;
import com.beshev.arenashift.user.UserManager;
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

public class UpdateService extends IntentService {
	
	private ShiftsManager shiftsManager;
	private EventsManager eventsManager;
	private long dbVersion;
	private String username;
	private Boolean someNetworkExceptionHappened = false;
	private static Boolean isServiceRunning = false;
	private static String updateStatus = "";

	public UpdateService() {
		super("UpdateService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
		isServiceRunning = true;
		updateStatus = "Проверявам за обновления на графика...";
		
		Context context = getApplicationContext();
		shiftsManager = new ShiftsManager(context);
		eventsManager = new EventsManager(context);
				
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		String dbVersionString = getString(R.string.database_version);

		// default version is o
		dbVersion = sharedPref.getLong(dbVersionString, 0);
		username = sharedPref.getString(getString(R.string.username), UserManager.USERNAME);
		
		UpdateResponse updateResponse = downloadUpdate(context);
		
		if(updateResponse != null) {
			
			if(updateResponse.getChangesList().isEmpty()) {
				
				sendMessage("Няма нови промени в графика.");
				
			}else{
				
				updateDataBase(updateResponse);
				
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putLong(dbVersionString, updateResponse.getDbVersion());
				editor.commit();
			
				sendMessage("Графика е актуализиран");
			}
			
		}else{
			
			if(someNetworkExceptionHappened) {
				sendMessage("Проблем със сървъра");
			}
		}		
	}
	
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        isServiceRunning = false;
        updateStatus = "";
        shiftsManager.closeDataBase();
		eventsManager.closeDataBase();
    }
	
	private UpdateResponse downloadUpdate(Context context) {

		final String productionServerURL = "http://arenashiftserver.appspot.com/rest/user/sync/shifts/" +
				username + "/" + String.valueOf(dbVersion);

		final String devServerRestURL = String.format("http://dev-server/rest/user/sync/shifts/%s/%s",
				username, String.valueOf(dbVersion));

		URL url;
		HttpURLConnection urlConnection;
		BufferedWriter bufferedWriter = null;
		BufferedReader bufferedReader = null;
		UpdateResponse updateResponse = null;
		
		try {

			url = new URL(productionServerURL);
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
		    urlConnection.setRequestProperty("User-agent", System.getProperty("http.agent"));
			urlConnection.setRequestProperty("Authorization", UserManager.createAuthHttpHeaderString(context));
		    urlConnection.setConnectTimeout(10000);
		    
		    bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
		    
			String textLine = "";
			String endMSG = "";
			
			while((textLine = bufferedReader.readLine()) != null) {
				endMSG += textLine;
			}
			
			updateResponse = new Gson().fromJson(endMSG, UpdateResponse.class);
			bufferedReader.close();

			urlConnection.disconnect();
			
		} catch (MalformedURLException  e) {
			Log.e("AS", e.toString());
			eventsManager.logException(username, e.toString(), new Date());
			someNetworkExceptionHappened = true;
		} catch (SocketTimeoutException e) {
			Log.e("AS", e.toString());
			eventsManager.logException(username, e.toString(), new Date());
			someNetworkExceptionHappened = true;
		} catch (IOException e) {
			Log.e("AS", e.toString());
			eventsManager.logException(username, e.toString(), new Date());
			someNetworkExceptionHappened = true;
			
		}finally {
			
			if(bufferedWriter != null) {	
				try {
					bufferedWriter.close();
				} catch (IOException e) {
					eventsManager.logException(username, e.toString(), new Date());
				}
			}
			
			if(bufferedReader != null) {	
				try {
					bufferedReader.close();
				} catch (IOException e) {
					eventsManager.logException(username, e.toString(), new Date());
				}
			}
		}
		
		return updateResponse;
	}

	private void updateDataBase(UpdateResponse updateResponse) {
		
		for(Shift shift : updateResponse.getChangesList()) {
			
			shiftsManager.addShift(shift);
		}
	}
	
	private void sendMessage(String msg) {
		
		updateStatus = msg;
		
		Intent updateIntent = new Intent("updateMessage");
		updateIntent.putExtra("message", msg);
	    LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);
	}
	
	public static Boolean isServiceRunning() {
		return isServiceRunning;
	}
	
	public static String getUpdateStatus() {
		return updateStatus;
	}

}
