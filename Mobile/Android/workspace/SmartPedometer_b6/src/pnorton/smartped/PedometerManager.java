package pnorton.smartped;

/**
 * Class PedometerManager
 * 
 * Responsibilities: Central Management system for the Smart Pedometer filters
 * and calculations contains a method for updating the acceleration values along
 * with timestamps and axes values. This also has a number of methods for 
 * accessing calculated parameters and operates in a similar manner as defined
 * by the ISignal Interface although does not implement it itself. 
 * 
 * Dependencies: This is dependent on the Android OS for the DataLogger class
 * which is defined in this module so data output can be written to the devices
 * sd card. It is also dependent on some elements of the Java API.
 * 
 * Android Dependencies: Android OS (Level 1 API via DataLogger)
 * 
 * @author Peter B Norton
 * @version 0.30
 * 
 *          Revision History
 * 
 *          0.27 Initial Build for SmartPedometer_b5 to manage all filter and
 *          data processing for the Smart Pedometer
 * 
 *          0.28 Addition of dynamic threshold system to adjust the step trigger
 *          threshold based on previous values whether they trigger a step or
 *          not. This system has been made to use a weighted average filter with
 *          an exponential type response.
 * 
 *          0.29 Change to SmartPedometer_b6 port (Gingerbread and above) with
 *          Linear Acceleration sensor and negative bias system
 *          
 *          0.30 Removal of BeepHandler from this class there is now no Android
 *          specific code in this class or the DataLogger system
 * 
 */
public class PedometerManager {

	private float[] value_buffer; /*
								 * Buffer to store chain of values for
								 * processing
								 */
	private long time_buffer; /* Buffer to store timestamp value */
	private long block_length; /* Length in time of current block */
	private static final int SAMPLE_COUNT = 8; /*
												 * Number of samples to count
												 * before UI Update
												 */
	private static final int DATA_LENGTH = 8; /*
											 * Number of Data elements in chain
											 * buffer
											 */
	private static final int DATA_X = 0; /* Index for X axis data */
	private static final int DATA_Y = 1; /* Index for Y axis data */
	private static final int DATA_Z = 2; /* Index for Z axis data */
	private static final int DATA_SCALAR = 3; /* Index for Scalar (-G) data */
	private static final int DATA_FILTER = 4; /* Index for filtered values */
	private static final int DATA_PEAK = 5; /* Index for peak values */
	private static final int DATA_THRESHOLD = 6;
	private static final float BIAS = 9.8f; /*
											 * Bias Value for negative G
											 * readings
											 */
	private static final float BIAS_MARGIN = 0.2f; /*
													 * Bias Margin value to
													 * introduce small error to
													 * prevent excess step
													 * events
													 */
	private boolean low_pass; /* Low Pass Enable flag */
	private int sample_count; /* Number of Samples Run */
	private int steps; /* Steps recorded */
	private float last_peak; /* Last Peak value recorded */
	private long run_time; /* Total Run Time of the Application in ns */
	private int threshold; /* Threshold value to trigger a step */
	private float step_rate; /* Rate per minute for steps */
	private float offset; /* Offset for the reset system */
	private float instant_threshold; /* Current dynamic threshold value */

	/* Variables for the Step per Minute Value */
	private int block_steps;
	private long block_start;
	private long block_end;
	private int block_count;
	private static final int BLOCK_NUMBER = 16;

	private WeightedAverageFilter moving_average_filter; /*
														 * Moving Average Filter
														 * Object
														 */
	private IIRCascadeLowPassFilter low_pass_filter; /* Low Pass Filter Object */
	private ZeroCrossingFilter zero_crossing_filter; /*
													 * Zero Crossing Filter
													 * Object
													 */
	private DataLogger logger;

	/**
	 * Default Constructor called from Activity
	 */
	public PedometerManager() {
		// Create a buffer for the values
		value_buffer = new float[DATA_LENGTH];
		// Create a buffer to store the time value
		time_buffer = 0L;
		// Position Flag for the buffer
		sample_count = 0;
		last_peak = 0.0f;
		step_rate = 0.0f;
		offset = 0.0f;
		run_time = 0L;
		steps = 0;
		low_pass = false;
		logger = null;
		block_steps = 0;
		block_start = 0;
		block_end = 0;
		block_count = 0;
		threshold = 70;
		instant_threshold = 0.0f;
		this.low_pass_filter = new IIRCascadeLowPassFilter(0.0625f, 4);
		this.moving_average_filter = new WeightedAverageFilter();
		this.zero_crossing_filter = new ZeroCrossingFilter();
		flush_buffers();
	}

	public PedometerManager(long time, float calibrator, int thres,
			int st, boolean lp) {
		// Create a buffer for the values
		value_buffer = new float[DATA_LENGTH];
		// Create a buffer to store the time value
		time_buffer = 0L;
		// Position Flag for the buffer
		sample_count = 0;
		last_peak = 0.0f;
		step_rate = 0.0f;
		offset = calibrator;
		run_time = time;
		steps = st;
		low_pass = lp;
		logger = null;
		block_steps = 0;
		block_start = 0;
		block_end = 0;
		block_count = 0;
		threshold = thres;
		instant_threshold = 0.0f;
		this.low_pass_filter = new IIRCascadeLowPassFilter(0.0625f, 4);
		this.moving_average_filter = new WeightedAverageFilter();
		this.zero_crossing_filter = new ZeroCrossingFilter();
		flush_buffers();
	}

	/**
	 * Clear all the buffers
	 */
	private void flush_buffers() {
		// Erase all the buffer values
		for (int j = 0; j < DATA_LENGTH; j++) {
			value_buffer[j] = 0.0f;
		}
	}

	/**
	 * Update the Pedometer System with new accelerometer values
	 * 
	 * @param x
	 *            X axis value
	 * @param y
	 *            Y axis value
	 * @param z
	 *            Z axis value
	 * @param timestamp
	 *            Time of this update
	 * @return false - data processed but no UI update true - data processed and
	 *         UI Update to be made
	 */
	public boolean update(float x, float y, float z, long timestamp) {
		// Apply the Bias to a single axis along with Bias Margin
		z += (BIAS + BIAS_MARGIN);
		value_buffer[DATA_X] = x;
		value_buffer[DATA_Y] = y;
		value_buffer[DATA_Z] = z;
		// Do the scalar combination and deduct the bias from the result without
		// the Margin
		value_buffer[DATA_SCALAR] = (((float) (Math.pow(
				Math.pow(x, 2.0) + Math.pow(y, 2.0) + Math.pow(z, 2.0), 0.5))) - BIAS);
		if (this.low_pass) {
			// Low Pass enabled process the value in filter buffer and store
			// back to this location
			value_buffer[DATA_FILTER] = this.low_pass_filter.processSample(
					value_buffer[DATA_SCALAR], timestamp);
		} else {
			// Low Pass not enabled process the value do not modify any buffers
			this.low_pass_filter.processSample(value_buffer[DATA_SCALAR],
					timestamp);
			value_buffer[DATA_FILTER] = value_buffer[DATA_SCALAR];
		}
		// Process the filter buffer through the zero crossing filter to check
		// for a peak crossing
		value_buffer[DATA_PEAK] = this.zero_crossing_filter.processSample(
				value_buffer[DATA_FILTER], timestamp);
		if (value_buffer[DATA_PEAK] != 0) {
			value_buffer[DATA_THRESHOLD] = instant_threshold = this.moving_average_filter
					.processSample(value_buffer[DATA_PEAK], timestamp);
		} else {
			value_buffer[DATA_THRESHOLD] = instant_threshold;
		}
		// Check if the filter returns a peak
		if (value_buffer[DATA_PEAK] > (((float) threshold / 100.0f) * instant_threshold)) {
			// Make a beep
			// If it does store that as the last peak value
			last_peak = value_buffer[DATA_PEAK];
			// and advance the step count by 1
			steps++;
			// Block Step count also up by 1
			this.block_steps++;
		}
		// Write data to the logger should the logger be active
		this.writeLogData(timestamp);
		// Check if the Sample Count has been reached for a UI update
		if (sample_count == SAMPLE_COUNT) {
			// reset the sample count
			sample_count = 0;
			// Check if the time buffer is holding a non zero value
			if (time_buffer != 0) {
				// If it is calculate the ns length of this block
				block_length = timestamp - time_buffer;
				// Store the current timestamp in the time buffer
				time_buffer = timestamp;
			} else {
				// If not store only this timestamp and leave the
				// block size set to zero
				time_buffer = timestamp;
			}
			// Add the block length to the run time
			this.run_time += block_length;
			// Check the Blocks for Steps per minute
			if (this.block_count == BLOCK_NUMBER) {
				// Correct Number of Blocks has been passed
				// Set End of Block Count
				block_end = timestamp;
				if (block_start != 0) {
					step_rate = ((float) block_steps / ((float) (block_end - block_start) / (float) (1e9 * 60)));
				} else {
					step_rate = 0.0f;
				}
				block_start = timestamp;
				block_steps = 0;
				block_count = 0;
			} else {
				// Increment the block count
				block_count++;
			}
			return true;
		} else {
			// No UI Update required
			// Advance the sample counter
			sample_count++;
			return false;
		}
	}

	/**
	 * Reset the Pedometer System with 0 steps and new offset value
	 */
	public void reset() {
		steps = 0;
		run_time = 0;
		offset += -(value_buffer[DATA_SCALAR]);
		this.moving_average_filter.reset();
		this.instant_threshold = 0.0f;
	}

	/**
	 * Write the current data set to the log
	 * 
	 * @param timestamp
	 *            Current Timestamp from update()
	 */
	private void writeLogData(long timestamp) {
		if (logger != null) {
			logger.writeDataValue(value_buffer[DATA_SCALAR]);
			logger.writeDataValue(value_buffer[DATA_FILTER]);
			logger.writeDataValue(value_buffer[DATA_PEAK]);
			logger.writeDataValue(value_buffer[DATA_THRESHOLD]);
			logger.writeDataValue(timestamp);
			logger.writeNewLine();
		}
	}

	/**
	 * Open a new log but close an existing one if open
	 * 
	 * @return the filename of the new log file
	 */
	public String openLog() {
		if (logger != null) {
			logger.close();
		}
		logger = new DataLogger();
		return logger.getFileName();
	}

	/**
	 * Close a log should it be open
	 */
	public void closeLog() {
		if (logger != null) {
			logger.close();
		}
	}

	/**
	 * Get the Last Peak value
	 * 
	 * @return Last Peak value
	 */
	public float getScalarAcceleration() {
		return value_buffer[DATA_SCALAR];
	}

	/**
	 * Get the Current Sample Rate
	 * 
	 * @return Current Sample Rate
	 */
	public float getLastPeak() {
		return last_peak;
	}

	/**
	 * Get the current run time
	 * 
	 * @return current run time in seconds
	 */
	public float getSampleRate() {
		// Determine the time elapsed since the last block
		if (block_length != 0) {
			// Calculate the sample rate
			float sr = (float) (SAMPLE_COUNT * 1e9) / (float) (block_length);
			// Update the Low Pass Filter
			this.low_pass_filter.setCentreFrequency(4.0f / sr);
			// return the value to the Activity
			return sr;
		} else {
			return 0.0f;
		}
	}

	/**
	 * Get the current run time
	 * 
	 * @return current run time in seconds
	 */
	public long getRunTime() {
		return run_time;
	}

	/**
	 * Set the Run Time
	 * 
	 * @param rt
	 *            Run Time to set
	 */
	public void setRunTime(long rt) {
		run_time = rt;
	}

	/**
	 * Get the Offset value
	 * 
	 * @return Offset value
	 */
	public float getOffset() {
		return this.offset;
	}

	/**
	 * Set the Offset Value
	 * 
	 * @param o
	 *            Offset Value
	 */
	public void setOffset(float o) {
		offset = o;
	}

	/**
	 * Get the Threshold value
	 * 
	 * @return Threshold value
	 */
	public int getThreshold() {
		return threshold;
	}

	/**
	 * Set the Threshold for step trigger
	 * 
	 * @param f
	 *            Threshold to set
	 */
	public void setThreshold(int t) {
		threshold = t;
	}

	/**
	 * Get the number of steps that have been recorded
	 * 
	 * @return steps that have been recorded
	 */
	public int getSteps() {
		return steps;
	}

	/**
	 * Set the number of steps
	 * 
	 * @param s
	 *            Number of Steps
	 */
	public void setSteps(int s) {
		steps = s;
	}

	/**
	 * Get the Low Pass Enable flag
	 * 
	 * @return Low Pass Enable flag
	 */
	public boolean getLowPassEnable() {
		return this.low_pass;
	}

	/**
	 * Set the Low Pass Filter Enable flag
	 * 
	 * @param b
	 *            Boolean Value to set
	 */
	public void setLowPassEnable(boolean b) {
		this.low_pass = b;
	}

	/**
	 * Get the Average Step Rate
	 * 
	 * @return Average Step Rate
	 */
	public float getStepRate() {
		return this.step_rate;
	}
}
