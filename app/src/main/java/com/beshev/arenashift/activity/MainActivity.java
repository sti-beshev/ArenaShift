package com.beshev.arenashift.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.beshev.arenashift.R;
import com.beshev.arenashift.beans.Shift;
import com.beshev.arenashift.database.DataBaseManager;
import com.beshev.arenashift.database.EventsManager;
import com.beshev.arenashift.database.ShiftsManager;
import com.beshev.arenashift.services.LogSyncService;
import com.beshev.arenashift.services.UpdateService;
import com.beshev.arenashift.user.UserManager;
import com.beshev.arenashift.util.DataConverter;

import java.util.Date;
import java.util.HashMap;

public class MainActivity extends Activity {

	private ViewFlipper mainViewFlipper;
	private RelativeLayout shiftLayout, updateLayout, calendarLayout;
	private CalendarView calendarView;
	private Button buttonShowShift, buttonCalendar, buttonOK;
	private TextView tvPanMehanik, tvPanKasaOne, tvPanKasaTwo, tvPanKasaThree,
	tvRazporeditelOne, tvRazporeditelTwo;
	private TextView tvUpdateStatus;
	
	private BroadcastReceiver connectionBroadcastReceiver;
	private DataBaseManager dataBaseManager;
	private ShiftsManager shiftsManager;
	private EventsManager eventsManager;
	private Boolean cheakedForShiftUpdates;

	private String userName;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		cheakedForShiftUpdates = false;
		
		initViews();

		// Listening for messages from 'Update Service'.
		LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
			      new IntentFilter("updateMessage"));
		
		/* За да следи дали има промяна във връзката с интернет. */
		IntentFilter connectionFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
		connectionBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
            	
            	if(!cheakedForShiftUpdates && !(UpdateService.isServiceRunning())) {
            		checkForUpdates();
            	}        
            }
        };
        registerReceiver(connectionBroadcastReceiver, connectionFilter);
		
        if(UserManager.checkForUsername(this)) {
        	checkIfUpdateServiceIsRunning();
			syncEvents();
        }
		

        dataBaseManager = new DataBaseManager(this);
		shiftsManager = new ShiftsManager(this);
		eventsManager = new EventsManager(this);
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		if(!(UserManager.checkForClientUsername(this))) {
			
			Intent intent = new Intent(this, UserActivity.class);
			startActivity(intent);
		} else {
			userName = UserManager.getUserName(this);
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		shiftsManager.closeDataBase();
		eventsManager.closeDataBase();
	}
	
	@Override
	protected void onDestroy() {
	  
	  LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
	  unregisterReceiver(connectionBroadcastReceiver);
	  super.onDestroy();
	}
	
	private void initViews() {

		mainViewFlipper = (ViewFlipper)findViewById(R.id.mainViewFlipper);
		calendarLayout = (RelativeLayout)findViewById(R.id.calendarLayout);
		shiftLayout = (RelativeLayout)findViewById(R.id.shiftLayout);
		updateLayout = (RelativeLayout)findViewById(R.id.updateLayout);
		
		buttonShowShift = (Button)findViewById(R.id.buttonShowShift);
		buttonShowShift.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                HashMap<String, Integer> dateMap = DataConverter.getDateAsMap(calendarView.getDate());

            	// Adding 1 because months number in 'Calendar' starts from 0.
				changeShift(dateMap.get("year"), dateMap.get("month")+1,
                        dateMap.get("day"));
            }
        });
		tvPanMehanik = (TextView)findViewById(R.id.textViewPanMehanik);
		tvPanKasaOne = (TextView)findViewById(R.id.textViewPanKasaOne);
		tvPanKasaTwo = (TextView)findViewById(R.id.textViewPanKasaTwo);
		tvPanKasaThree = (TextView)findViewById(R.id.textViewPanKasaThree);
		tvRazporeditelOne = (TextView)findViewById(R.id.textViewRazOne);
		tvRazporeditelTwo = (TextView)findViewById(R.id.textViewRazTwo);
		tvUpdateStatus = (TextView)findViewById(R.id.textViewUpdate);	
		calendarView = (CalendarView)findViewById(R.id.calendar);

		// This is a hack allowing me to change the color of the month's name.
		ViewGroup vg = (ViewGroup) calendarView.getChildAt(0);
		View child = vg.getChildAt(0);

		if(child instanceof TextView) {
			((TextView)child).setTextColor(ContextCompat.getColor(this, R.color.myWhite));
		}
		
		buttonCalendar = (Button)findViewById(R.id.buttonCalendar);
		buttonCalendar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

				mainViewFlipper.setInAnimation(getApplicationContext(),R.anim.slide_in_left);
				mainViewFlipper.setOutAnimation(getApplicationContext(),R.anim.slide_out_right);
				mainViewFlipper.setDisplayedChild(0);
            }
        });
		
		buttonOK = (Button)findViewById(R.id.buttonOK);
		buttonOK.setVisibility(View.GONE);
		buttonOK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

				mainViewFlipper.setOutAnimation(getApplicationContext(),R.anim.fade_out);
				mainViewFlipper.setInAnimation(getApplicationContext(),R.anim.fade_in);
				mainViewFlipper.setDisplayedChild(0);
            }
        });
	}
	
	private void changeShift(int year, int month, int day) {

		String dayToCheck = Integer.toString(year) + "-" + Integer.toString(month) + "-"
				+ Integer.toString(day);

		eventsManager.logDayEvent(userName, dayToCheck, new Date());
		
		Shift shift = shiftsManager.getShift(year, month, day);
		
		if(shift != null) {

			mainViewFlipper.setOutAnimation(this,R.anim.slide_out_left);
			mainViewFlipper.setInAnimation(this,R.anim.slide_in_right);
			mainViewFlipper.setDisplayedChild(1);
			
			tvPanMehanik.setText(shift.getPanMehanik());
			tvPanKasaOne.setText(shift.getPanKasaOne());
			tvPanKasaTwo.setText(shift.getPanKasaTwo());
			tvPanKasaThree.setText(shift.getPanKasaThree());
			tvRazporeditelOne.setText(shift.getRazporeditelOne());
			tvRazporeditelTwo.setText(shift.getRazporeditelTwo());
			
		}else{
			Toast.makeText(getApplicationContext(), "Няма смяна", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void checkIfUpdateServiceIsRunning() {
		
		if(UpdateService.isServiceRunning()) {

			mainViewFlipper.setOutAnimation(this,R.anim.fade_out);
			mainViewFlipper.setInAnimation(this,R.anim.fade_in);
			mainViewFlipper.setDisplayedChild(2);
			
			tvUpdateStatus.setText(UpdateService.getUpdateStatus());
			
			if(!(UpdateService.getUpdateStatus().equals("Проверявам за обновления на графика..."))) {
				
				buttonOK.setVisibility(View.VISIBLE);
			}
			
		}else{
			checkForUpdates();
		}
	}
	
	private void checkForUpdates() {
		
		if(isConnected()) {
			
			cheakedForShiftUpdates = true;

			mainViewFlipper.setOutAnimation(this,R.anim.fade_out);
			mainViewFlipper.setInAnimation(this,R.anim.fade_in);
			mainViewFlipper.setDisplayedChild(2);
			
			tvUpdateStatus.setText("Проверявам за обновления на графика...");
			
			Intent intent = new Intent(this, UpdateService.class);
			startService(intent);
			
		}
	}

	private void syncEvents() {

		if(!(LogSyncService.isServiceRunning()) && isConnected()) {

			Intent intent = new Intent(this, LogSyncService.class);
			startService(intent);
		}
	}

	private Boolean isConnected() {

		ConnectivityManager cm =
				(ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

		return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
	}
	
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		  @Override
		  public void onReceive(Context context, Intent intent) {
		    
		    tvUpdateStatus.setText(intent.getStringExtra("message"));
		    buttonOK.setVisibility(View.VISIBLE);
		   
		  }
	};

}

