package fr.skyost.sms;

import java.util.Timer;

import fr.skyost.sms.tasks.SMSSender;
import fr.skyost.sms.tasks.SMSSender.SMSListener;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;

public class MainActivity extends Activity implements SMSListener {
	
	private static final Timer timer = new Timer();
	private int count = 0;
	private int repetitions;
	private Button btnSend;
	private SMSSent action;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		btnSend = (Button)findViewById(R.id.btnSend);
		btnSend.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View view) {
				final String txtrepetitions = ((TextView)findViewById(R.id.txtRepetitions)).getText().toString();
				final String txtPhoneNumber = ((TextView)findViewById(R.id.txtPhoneNumber)).getText().toString();
				final String txtMessage = ((TextView)findViewById(R.id.txtMessage)).getText().toString();
				if(txtPhoneNumber.length() < 4 || (txtrepetitions.length() == 0 && !txtrepetitions.equals("0")) || txtMessage.length() == 0) {
					Toast.makeText(getApplicationContext(), R.string.toast_fill_frrom, Toast.LENGTH_LONG).show();
					return;
				}
				repetitions = Integer.valueOf(txtrepetitions);
				final MainActivity mainActivity = getInstance();
				Builder dlgAlert  = new Builder(mainActivity);
				dlgAlert.setTitle(R.string.app_name);
				dlgAlert.setMessage(R.string.dlg_system_capability);
				dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(final DialogInterface dialog, final int which) {
						final SMSSender smsSender = new SMSSender(txtPhoneNumber, txtMessage);
						smsSender.addSMSListener(mainActivity);
						timer.scheduleAtFixedRate(smsSender, 0, 3000);
						btnSend.setClickable(false);
						action = new SMSSent();
					}
					
				});
				dlgAlert.setCancelable(false);
				dlgAlert.create().show();
			}
			
		});
		((Button)findViewById(R.id.btnContactPicker)).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(final View view) {
				final Intent intent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
				intent.setType(Phone.CONTENT_TYPE);
				startActivityForResult(intent, 1);
			}
			
		});
	}
	
	public MainActivity getInstance() {
		return this;
	}

	@Override
	public void onSMSSent(final String phoneNumber, final String message) {
		runOnUiThread(action);
	}
	
	@Override
	public void onActivityResult(final int reqCode, final int resultCode, final Intent data) {
		super.onActivityResult(reqCode, resultCode, data);
		switch(reqCode) {
		case 1:
			if(resultCode == Activity.RESULT_OK) {
				Cursor cursor =  getContentResolver().query(data.getData(), null, null, null, null);
				if(cursor.moveToFirst()) {
					((TextView)findViewById(R.id.txtPhoneNumber)).setText(cursor.getString(cursor.getColumnIndex(Phone.NUMBER)));
				}
			}
			break;
		}
	}
	
	private class SMSSent implements Runnable {

		@Override
		public void run() {
			count++;
			btnSend.setText(count + " / " + repetitions);
			if(count == repetitions) {
				timer.cancel();
				Toast.makeText(getApplicationContext(), R.string.toast_done, Toast.LENGTH_SHORT).show();
				btnSend.setText(R.string.btn_send);
				btnSend.setClickable(true);
			}
		}
		
	}

}
