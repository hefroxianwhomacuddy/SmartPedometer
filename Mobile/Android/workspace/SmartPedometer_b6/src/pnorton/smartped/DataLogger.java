package pnorton.smartped;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.os.Environment;
import android.util.Log;

/**
 * Class DataLogger
 * 
 * Responsibilities: To provide a simple interface for the PedometerManager to write its
 * data blocks to the SD Card on the device for later analysis and processing. This is done
 * via a number of simple methods to open and close a log file, then 2 data entry method one
 * that writes a new float decimal value to the log followed by a tab to provide column spacing
 * and a second method to insert a newline to denote a new data row.
 * 
 * Dependencies: This depends on the Android OS to obtain the location to write data log files
 * along with many methods and operations from the Java API to write data to files and obtain
 * date and time information. This class does not require direct access to an activity however
 * it must be run on the Android OS and from an activity that has the correct permission for
 * writing to SD card.(android.permission.WRITE_EXTERNAL_STORAGE)
 * 
 * Android Dependencies: Android API Level 1 (Any Android Version)
 *
 * @author Peter B Norton
 * @version 0.30
 * 
 *          Revision History
 * 
 *          0.15 Initial version to encapsulate the data logging facility
 * 
 *          0.24 Refactor for Build 5 imported in advance to Build 4 to improve
 *          file names for stored data
 * 
 *          0.28 Modification for dynamic threshold system this stores an extra
 *          parameter for each sample point, this is the current threshold value
 *          from the dynamic system.
 * 
 * 
 */
public class DataLogger {

	private File file;
	private String filename;
	private FileWriter writer;
	private static final String errorTAG = "DataLogger";

	/**
	 * Create a Log File ready for writing creates the filename with the current
	 * data and time as its filename and appends with _ should the filename
	 * already exist for this specific minute in time
	 */
	public DataLogger() {
		// Generate a Calendar object with the current time and date
		GregorianCalendar gc = new GregorianCalendar();
		// Establish Filename
		filename = "data_" + gc.get(Calendar.YEAR) + "_"
				+ gc.get(Calendar.MONTH) + "_" + gc.get(Calendar.DAY_OF_MONTH)
				+ "_" + gc.get(Calendar.HOUR_OF_DAY) + "_"
				+ gc.get(Calendar.MINUTE);
		try {
			if (Environment.MEDIA_MOUNTED.equals(Environment
					.getExternalStorageState())) {
				// Establish file object
				file = new File(Environment.getExternalStorageDirectory(),
						filename);
				// While loop to prevent overwriting of files
				while (file.exists()) {
					// If it does keep adding _ character till it doesn't
					filename += "_";
					file = new File(Environment.getExternalStorageDirectory(),
							filename);
				}
				// Establish FileWriter
				writer = new FileWriter(file);
			} else {
				throw new IOException("No External Media Available!");
			}
		} catch (IOException e) {
			Log.e(errorTAG, e.getMessage());
		}
	}

	/**
	 * Write a single data value to a line in the log is appended by a tab at
	 * the end
	 * 
	 * @param value
	 *            32 bit floating point value to write
	 */
	public void writeDataValue(float value) {
		try {
			if (writer != null) {
				writer.write(value + "\t");
			} else {
				throw new NullPointerException(
						"Attempt to write a null writer!");
			}
		} catch (IOException e) {
			Log.e(errorTAG, e.getMessage());
		} catch (NullPointerException e) {
			Log.e(errorTAG, e.getMessage());
		}
	}

	/**
	 * Write a new line in the log to indicate the end of this series of data
	 */
	public void writeNewLine() {
		try {
			if (writer != null) {
				writer.write("\r\n");
			} else {
				throw new NullPointerException(
						"Attempt to write a null writer!");
			}
		} catch (IOException e) {
			Log.e(errorTAG, e.getMessage());
		} catch (NullPointerException e) {
			Log.e(errorTAG, e.getMessage());
		}
	}

	/**
	 * Get the current filename for this log
	 * 
	 * @return Current Filename
	 */
	public String getFileName() {
		return filename;
	}

	/**
	 * Close the Log and conclude the file writing
	 */
	public void close() {
		try {
			if (writer != null) {
				writer.close();
			} else {
				throw new NullPointerException("Attempt to close a null writer");
			}
		} catch (IOException e) {
			Log.e(errorTAG, e.getMessage());
		} catch (NullPointerException e) {
			Log.e(errorTAG, e.getMessage());
		}
	}
}
