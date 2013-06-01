package pnorton.smartped;

/**
 * Interface IAccelerometerListener
 * 
 * Responsibilities: Provides a simple accelerometer updating interface to a class so that
 * other components are not explicitly dependent on specific components such as a specific
 * Android Activity class this allows the PedometerManager to be suitable decoupled from
 * the host activity.
 * 
 * Dependencies: Not dependent on any specific Java or Android features
 * 
 * Android Dependencies: No Android Dependencies
 * 
 *          Revision History
 * 
 *          0.1 Initial Version (3 axes passed)
 * 
 *          0.11 Addition of long time value to log and record a time stamp
 *          value when the actual value is recorded used for time accuracy when
 *          providing plots
 * 
 *          0.15 Port to SmartPedometer_b3
 * 
 */
public interface IAccelerometerListener {

	/**
	 * Called when the Accelerometer polls an update
	 * 
	 * @param x
	 *            X-axis acceleration value
	 * @param y
	 *            Y-axis acceleration value
	 * @param z
	 *            Z-axis acceleration value
	 * @param timestamp
	 *            Time Stamp value for data logging
	 */
	public void updateAcceleration(float x, float y, float z, long timestamp);
}
