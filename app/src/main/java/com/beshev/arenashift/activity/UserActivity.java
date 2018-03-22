package com.beshev.arenashift.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.beshev.arenashift.R;
import com.beshev.arenashift.user.UserActivator;

public class UserActivity extends Activity {
	
	private TextView tvUserText;
	private TextView tvActivatingUserStatus;
	private EditText etActivationCode;
	private Button buttonActivate;

	private final String USER_GREATING = "Здравей, ти пусна програмата за първи път. Браво !" +
			" Сега обаче ти трябва Стели за да ти даде личния ти код за активиране. Пиши му......";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user);
		
		tvUserText = (TextView)findViewById(R.id.textViewUserText);
		tvUserText.setText(USER_GREATING);
		tvActivatingUserStatus = (TextView)findViewById(R.id.tvActivatetingUserStatus);
		tvActivatingUserStatus.setText("");
		
		etActivationCode = (EditText)findViewById(R.id.etActivationCod);
		buttonActivate = (Button)findViewById(R.id.buttonSaveUser);
		buttonActivate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

            	String activationCode = etActivationCode.getText().toString();

            	if(activationCode.length() != 6) {

					tvActivatingUserStatus.setText("Кодът трябва да е шест числа !");

				} else {

					buttonActivate.setVisibility(View.GONE);
					etActivationCode.setVisibility(View.GONE);

					tvActivatingUserStatus.setText("Активирам юзър...");

					new ActivateUser().execute(etActivationCode.getText().toString());

				}
            }
        });
	}

	private class ActivateUser extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... activationCode) {

			return new UserActivator().activateUser(getApplicationContext(), activationCode[0]);
		}

		protected void onPostExecute(String serverMSG) {

			tvUserText.setVisibility(View.GONE);
			tvActivatingUserStatus.setText(serverMSG);


			if(serverMSG.equals(UserActivator.MSG_WRONG_ACTIVATION_CODE)) {

				etActivationCode.setText("");

				tvUserText.setVisibility(View.VISIBLE);
				buttonActivate.setVisibility(View.VISIBLE);
				etActivationCode.setVisibility(View.VISIBLE);
			}
		}
	}
}
