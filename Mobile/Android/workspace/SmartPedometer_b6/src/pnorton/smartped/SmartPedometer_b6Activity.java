package pnorton.smartped;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * Class SmartPedometer_b6Activity
 * 
 * Responsibilities: Main Activity for Build 6 of the Smart Pedometer responsible
 * for displaying the Pedometer user interface and holding all other components including 
 * the PedometerManager class. This also provides the required handling methods
 * for the application controls and passes appropriate commands onto other sections.
 * 
 * Dependencies: This is dependent on the other modules of the application as well
 * as the Android API (Level 9 and above) for the AccelerometerHardwareInterface
 * class. 
 * 
 * Android Dependencies: Android API Level 9 (Android 2.3.1 and above)
 * @author Peter B Norton
 * @version 0.30
 * 
 *          Revision History
 * 
 *          0.14 Fundamentally different from SmartPedometer_b1 with controls to
 *          manipulate the filtering and displaying the step data to the end
 *          user but does not store the data in a file
 * 
 *          0.15 Merge of Builds 1 and 2 into a single application with some
 *          extra features such as Run Time
 * 
 *          0.16 Addition of BeepHandler section to beep on a step
 * 
 *          0.17 New version for Build 4. Force close issue for the stop button
 *          has been fixed
 * 
 *          0.18 Modification to Sound Beeper to hopefully not crash on Mark's
 *          HTC.
 * 
 *          0.19 Add Volume control for the beeper
 * 
 *          0.20 Routine to update the IIRCascadeLowPassFilter with the current
 *          sample rate to improve the frequency response ( < 4 Hz )
 * 
 *          0.20B Shorter higher pitched beep sound added and screen rotations
 *          prevented from occurring
 * 
 *          0.21 Removal of the Gain Selector as gain no longer applied to newer
 *          cascade FIR filter, addition of new Threshold slider to determine a
 *          lowest level at which the gradient of a zero crossing event triggers
 *          a step.
 * 
 *          0.22 Addition of calibration function to reset scalar value to zero
 *          when the Reset button is pressed this then adds to all subsequent
 *          scalar values improving the zero crossing characteristic
 * 
 *          0.23 Addition of SharedPreferences to save previous state across
 *          invocations of the application, so stores the state of all controls
 *          and run time as well as number of steps that have been recorded.
 * 
 *          0.25 Addition of Wake Locking system to prevent the device from
 *          sleeping while the application is running. Addition of version
 *          system to preferences so new version can be installed over older
 *          versions. (PREF_VERSION 25)
 * 
 *          0.26 Changed to Layout (main.xml) to allow orientation change from
 *          Portrait to Landscape modes. This also specifies an extra
 *          configuration for large screens (Tablet size) for both Portrait and
 *          Landscape modes. (PREF_VERSION 25)
 * 
 *          0.26B Removal of Landscape modes as they are not useful in
 *          displaying all the controls on the Activity. (PREF_VERSION 25)
 * 
 *          0.26C Change of Application name to 'Smart Pedometer Development
 *          Build 4'. (PREF_VERSION 25)
 * 
 *          0.27 Initial Version for SmartPedometer_b5, the main changes include
 *          the removal of data processing code to a seperate class called
 *          PedometerManager, the main activity is still however responsible for
 *          managing the Accelerometer.It therefore passes the values obtained
 *          onto the PedometerManager for data processing and logging as well as
 *          sound output.This removes a huge amount of code from the Activity.
 *          Preferences changed to all the number of moving average weights.
 *          (PREF_VERSION 27)
 * 
 *          0.28 Removal of Moving Average Filter from main trunk along with
 *          changes to UI to reflect new percentage threshold system. This is to
 *          be evaluated for further testing and adjustment. (PREF_VERSION 28)
 * 
 *          0.29 Conversion to SmartPedometer_b6 (PREF_VERSION 28)
 *          
 *          0.30 Decoupling of the BeepHandler to this class from Pedometer
 *          Manager to avoid any specialised Android code in that class 
 *          (PREF_VERSION 28)
 */
public class SmartPedometer_b6Activity extends Activity implements
		IAccelerometerListener {
	private PedometerManager pManager; /* Pedometer Data Manager */
	private AccelerometerHardwareInterface accelerometer; /*
														 * Accelerometer
														 * Hardware Interface
														 */
	/* List of Controls */
	private TextView textViewX;
	private TextView textViewY;
	private TextView textViewZ;
	private TextView textViewScalar;
	private TextView textViewSampleRate;
	private TextView textViewRunTime;
	private TextView textViewPeak;
	private TextView textViewSteps;
	private TextView textViewStepRate;
	private ToggleButton toggleButtonLowPass;
	private SeekBar seekBarTriggerThreshold;
	private SeekBar seekBarVolume;
	private TextView textViewTriggerThreshold;
	private TextView textViewVolume;
	private TextView textViewDataFile;
	private Button buttonReset;
	private Button buttonRecord;
	private Button buttonStop;
	/* Constants */
	private static final int MAX_TRIG_BAR = 100;
	private static final int MAX_VOL_BAR = 10;
	
	private BeepHandler beeper;
	private int volume;
	private int steps;

	private SharedPreferences pref;
	private static final int PREFERENCES_VERSION = 28;
	private PowerManager.WakeLock wakeLock;

	/*
	 * Preferences to be saved
	 * 
	 * long - total_time int - volume float - CALIBRATOR float - threshold int -
	 * steps_scalar boolean - moving_av_enable boolean - low_pass_enable
	 */
	/* Defines for saved preferences */
	private static final String PREF_VERSION = "version_number";
	private static final String PREF_TOTAL_TIME = "total_time";
	private static final String PREF_VOLUME = "volume";
	private static final String PREF_CALIBRATOR = "CALIBRATOR";
	private static final String PREF_THRESHOLD = "threshold";
	private static final String PREF_STEPS_SCALAR = "steps_scalar";
	private static final String PREF_LOW_PASS_ENABLE = "low_pass_enable";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE))
				.newWakeLock(PowerManager.FULL_WAKE_LOCK,
						"SmartPedometer_b5Activity");
		pManager = new PedometerManager();
		pref = this.getPreferences(MODE_PRIVATE);
		accelerometer = new AccelerometerHardwareInterface(this);
		if (pref.getInt(PREF_VERSION, 0) == PREFERENCES_VERSION) {
			// Load the Preferences from the file and send
			// to the PedometerManager
			pManager = new PedometerManager(pref.getLong(PREF_TOTAL_TIME, 0),
					 pref.getFloat(
							PREF_CALIBRATOR, 0.0f), pref.getInt(PREF_THRESHOLD,
							70), pref.getInt(PREF_STEPS_SCALAR, 0),
					pref.getBoolean(PREF_LOW_PASS_ENABLE, false));
			volume = pref.getInt(PREF_VOLUME, 10);
			steps = pref.getInt(PREF_STEPS_SCALAR, 0);
		} else {
			// Else allow the default values
			pManager = new PedometerManager();
			volume = 10;
			steps = 0;
		}
		setupControls();
	}

	public void onResume() {
		super.onResume();
		beeper = new BeepHandler(this);
		accelerometer.start(this);
		wakeLock.acquire();
	}

	public void onPause() {
		super.onPause();
		accelerometer.stop();
		beeper.close();
		beeper = null;
		wakeLock.release();
		SharedPreferences.Editor ed = pref.edit();
		ed.putInt(PREF_VERSION, PREFERENCES_VERSION);
		ed.putLong(PREF_TOTAL_TIME, pManager.getRunTime());
		ed.putInt(PREF_VOLUME, volume);
		ed.putFloat(PREF_CALIBRATOR, pManager.getOffset());
		ed.putInt(PREF_THRESHOLD, pManager.getThreshold());
		ed.putInt(PREF_STEPS_SCALAR, pManager.getSteps());
		ed.putBoolean(PREF_LOW_PASS_ENABLE, pManager.getLowPassEnable());
		ed.commit();
	}

	public void setupControls() {
		// Unwrap Android Controls to Class Variables
		textViewX = (TextView) findViewById(R.id.textViewX);
		textViewY = (TextView) findViewById(R.id.textViewY);
		textViewZ = (TextView) findViewById(R.id.textViewZ);
		textViewScalar = (TextView) findViewById(R.id.textViewScalar);
		textViewSampleRate = (TextView) findViewById(R.id.textViewSampleRate);
		textViewRunTime = (TextView) findViewById(R.id.textViewRunTime);
		textViewPeak = (TextView) findViewById(R.id.textViewPeaks);
		textViewSteps = (TextView) findViewById(R.id.textViewSteps);
		textViewStepRate = (TextView) findViewById(R.id.textViewStepRate);
		toggleButtonLowPass = (ToggleButton) findViewById(R.id.toggleButtonLowPass);
		seekBarTriggerThreshold = (SeekBar) findViewById(R.id.seekBarTriggerThreshold);
		seekBarVolume = (SeekBar) findViewById(R.id.seekBarVolume);
		textViewTriggerThreshold = (TextView) findViewById(R.id.textViewTriggerThreshold);
		textViewVolume = (TextView) findViewById(R.id.textViewVolume);
		textViewDataFile = (TextView) findViewById(R.id.textViewDataFile);
		buttonReset = (Button) findViewById(R.id.buttonReset);
		buttonRecord = (Button) findViewById(R.id.buttonRecord);
		buttonStop = (Button) findViewById(R.id.buttonStop);
		// Setup any event handlers (Buttons)
		buttonReset.setOnClickListener(resetPress);
		buttonRecord.setOnClickListener(recordPress);
		buttonStop.setOnClickListener(stopPress);
		// Setup ToggleButton handlers
		toggleButtonLowPass.setOnCheckedChangeListener(lowPassCheckChange);
		// Setup SeekBar handlers
		seekBarTriggerThreshold.setOnSeekBarChangeListener(thresholdSeekChange);
		seekBarVolume.setOnSeekBarChangeListener(volumeSeekChange);
		// Set Correct Limits for SeekBar Controls
		seekBarTriggerThreshold.setMax(MAX_TRIG_BAR);
		seekBarTriggerThreshold.setProgress(pManager.getThreshold());
		seekBarVolume.setMax(MAX_VOL_BAR);
		seekBarVolume.setProgress(10);
		textViewRunTime.setText("Run Time = "
				+ (int) (pManager.getRunTime() / 1e9) + " s");
		textViewVolume.setText("Volume = " + volume);
		textViewTriggerThreshold.setText("Trigger Threshold = "
				+ pManager.getThreshold() + "%");
		textViewSteps.setText("Steps = " + pManager.getSteps());
		toggleButtonLowPass.setChecked(pManager.getLowPassEnable());
	}

	/**
	 * Handler for the changes in the Volume SeekBar
	 */
	private OnSeekBarChangeListener volumeSeekChange = new OnSeekBarChangeListener() {
		public void onProgressChanged(SeekBar s, int i, boolean b) {
			// TODO Auto-generated method stub
			textViewVolume.setText("Volume = " + i);
			volume = i;
			if(beeper != null)
			{
				beeper.setVolume(((float)i / 10.0f));
			}
		}

		public void onStartTrackingTouch(SeekBar s) {
			// TODO Auto-generated method stub

		}

		public void onStopTrackingTouch(SeekBar s) {
			// TODO Auto-generated method stub

		}
	};

	/**
	 * Handler for the changes in the Threshold Control SeekBar
	 */
	private OnSeekBarChangeListener thresholdSeekChange = new OnSeekBarChangeListener() {
		public void onProgressChanged(SeekBar s, int i, boolean b) {
			// TODO Auto-generated method stub
			pManager.setThreshold(i);
			textViewTriggerThreshold.setText("Trigger Threshold = "
					+ pManager.getThreshold() + "%");
		}

		public void onStartTrackingTouch(SeekBar s) {
			// TODO Auto-generated method stub

		}

		public void onStopTrackingTouch(SeekBar s) {
			// TODO Auto-generated method stub

		}
	};

	/**
	 * Handler for the Low Pass Toggle Button
	 */
	private OnCheckedChangeListener lowPassCheckChange = new OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton c, boolean b) {
			pManager.setLowPassEnable(b);
		}
	};

	/**
	 * Handler for the Reset Button being pressed
	 */
	private OnClickListener resetPress = new OnClickListener() {
		public void onClick(View v) {
			pManager.reset();
		}
	};

	/**
	 * Handler for the Record Button being pressed
	 */
	private OnClickListener recordPress = new OnClickListener() {
		public void onClick(View v) {
			textViewDataFile.setText(pManager.openLog() + " file opened");
		}
	};

	/**
	 * Handler for the Stop Button being pressed
	 */
	private OnClickListener stopPress = new OnClickListener() {
		public void onClick(View v) {
			pManager.closeLog();
			textViewDataFile.setText("Data File not open");
		}
	};

	@Override
	public void updateAcceleration(float x, float y, float z, long timestamp) {
		// TODO Auto-generated method stub
		// Poll the PedometerManager for a UIupdate and pass the acceleration
		// values
		if (pManager.update(x, y, z, timestamp)) {
			// Update the UI from the PedometerManager
			NumberFormat fmat = new DecimalFormat("0.00");
			NumberFormat fmat2 = new DecimalFormat("0.0");
			textViewX.setText("X = " + fmat.format(x));
			textViewY.setText("Y = " + fmat.format(y));
			textViewZ.setText("Z = " + fmat.format(z));
			textViewScalar.setText("S = "
					+ fmat.format(pManager.getScalarAcceleration()));
			textViewSampleRate.setText("Sample Rate = "
					+ fmat.format(pManager.getSampleRate()) + " Hz");
			if(steps != pManager.getSteps())
			{
				Thread t = new Thread(beeper);
				t.start();
			}
			textViewSteps.setText("Steps = " + (steps = pManager.getSteps()));
			textViewRunTime.setText("RunTime = "
					+ (int) (pManager.getRunTime() / 1e9) + " s");
			textViewPeak.setText("P = " + fmat.format(pManager.getLastPeak()));
			textViewStepRate.setText("Step Rate = "
					+ fmat2.format(pManager.getStepRate()) + " s/min");

		}
	}
}
