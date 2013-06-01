package pnorton.smartped;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Class AccelerometerHardwareInterface
 * 
 * Responsibilities: To manage the low level action of getting data from the accelerometer
 * and passing that onto a class implementing the IAccelerometerListener interface, this
 * is done without any modification to the underlying data and passes on all 3 axes values
 * along with a timestamp to indicated when the reading took place.
 * 
 * Dependencies: This depends upon the Android Activity class to start the accelerometer
 * and must also have the IAccelerometerInterface object to send values to. This class
 * also implements the SensorEventListener interface to read the sensor.
 * 
 * Android Dependencies: Android API Level 9 (Android 2.3.1 and above)
 * 
 * @author Peter B Norton
 * @version 0.30
 * 
 *          Revision History
 * 
 *          0.1 Initial version brought over from acc_logger project
 * 
 *          0.11 Addition of time stamp value to the logging
 * 
 *          0.15 Port over for SmartPedometer_b3 project with modification to
 *          class to make it less dependent upon a specific activity and now
 *          only looks for the first accelerometer sensor
 * 
 *          0.29 Changes made for SmartPedometer_b6 to change the sensor over to
 *          a LINEAR ACCELERATION type, note this means that this version is
 *          only compatible with Gingerbread or above
 * 
 */
public class AccelerometerHardwareInterface implements SensorEventListener {

	private SensorManager acc_manager;
	private IAccelerometerListener listener;
	private Activity host;
	private boolean active;

	/**
	 * Default Constructor for this Hardware Manager
	 * 
	 * @param host_activity
	 *            Activity that hosts this Manager
	 */
	public AccelerometerHardwareInterface(Activity host_activity) {
		acc_manager = null;
		host = host_activity;
		active = false;
		listener = null;
	}

	/**
	 * Start the Accelerometer Listening Capture must have the required
	 * Accelerometer Listener passed in
	 * @param acc_listener
	 *            Listener for Accelerometer Events
	 */
	public void start(IAccelerometerListener acc_listener) {
		try {
			if (active) {
				throw new Exception("Accelerometer Listener is already active");
			}
			if (host != null) {
				// Obtain the Sensors
				acc_manager = (SensorManager) host
						.getSystemService(Context.SENSOR_SERVICE);
				// Register the Listener
				active = acc_manager.registerListener(this, acc_manager
						.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
						SensorManager.SENSOR_DELAY_FASTEST);
				// Store the Listener for the Manager to send data to
				listener = acc_listener;
			} else {
				throw new Exception(
						"Host Activity is null or accelerometer not supported");
			}
		} catch (Exception e) {
			// TODO Display a message to the user
		}
	}

	/**
	 * Stop the Accelerometer Listening Capture
	 */
	public void stop() {
		if (active) {
			try {
				if (acc_manager != null) {
					acc_manager.unregisterListener(this, acc_manager
							.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION));
				}
				listener = null;
				active = false;
			} catch (Exception e) {
				// TODO Display a message to the user
			}
		} else {
			// TODO Display a message to the user
		}

	}

	/**
	 * Get the current active state of the accelerometer
	 * 
	 * @return Active state flag
	 */
	public boolean getActive() {
		return active;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		// Ignore this Handler
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		// Obtain the values from the accelerometer and
		// send them to the listener

		if (listener != null) {
			listener.updateAcceleration(event.values[0], event.values[1],
					event.values[2], event.timestamp);
		}

	}

}
