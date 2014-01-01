package fr.skyost.sms.tasks;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.TimerTask;

import fr.skyost.sms.utils.EventListenerList;
import android.app.PendingIntent;
import android.telephony.SmsManager;

/**
 * SMSSender class.
 * @author Skyost (http://www.skyost.eu).
 */

public class SMSSender extends TimerTask {
	
	private String phoneNumber;
	private String message;
	
	private ArrayList<String> smsPart;
	private static final ArrayList<PendingIntent> nullIntent = new ArrayList<PendingIntent>();
	private static final SmsManager smsManager = SmsManager.getDefault();
	
	private final EventListenerList listeners = new EventListenerList();
	
	/**
	 * SMSSender constructor.
	 * @param phoneNumber The receiver's number.
	 * @param message The message you want to send.
	 */
	
	public SMSSender(final String phoneNumber, final String message) {
		this.phoneNumber = phoneNumber;
		this.message = message;
	}
	
	public void setPhoneNumber(final String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	
	public void setMessage(final String message) {
		this.message = message;
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	public String getMessage() {
		return message;
	}
	
	public interface SMSListener extends EventListener {
		void onSMSSent(final String phoneNumber, final String message);
	}
	
	public void addSMSListener(final SMSListener listener) {
		listeners.add(SMSListener.class, listener);
	}
	
	public void removeSMSListener(final SMSListener listener) {
		listeners.remove(SMSListener.class, listener);
	}
	
    public SMSListener[] getSMSListeners() {
    	return listeners.getListeners(SMSListener.class);
    }
    
    private void fireSMSSent(final String phoneNumber, final String message) {
    	for(SMSListener listener : getSMSListeners()) {
    		listener.onSMSSent(phoneNumber, message);
    	}
    }

	@Override
	public void run() {
		if(message.length() > 160) {
			if(smsPart == null) {
				smsPart = smsManager.divideMessage(message);
			}
			smsManager.sendMultipartTextMessage(phoneNumber, null, smsManager.divideMessage(message), nullIntent, nullIntent);
		}
		else {
			smsManager.sendTextMessage(phoneNumber, null, message, null, null);
		}
		fireSMSSent(phoneNumber, message);
	}

}
