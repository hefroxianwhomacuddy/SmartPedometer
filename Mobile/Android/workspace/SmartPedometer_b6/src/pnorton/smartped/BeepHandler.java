package pnorton.smartped;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

/**
 * Class BeepHandler
 * 
 * Responsibilities: The basic class for handling the beep noise, this is a thin wrapper
 * for the Android MediaPlayer class which does the work in playing the beep noise which
 * is contained in the resource R.raw.sp_beep. It also has the facility to provide very
 * basic volume management.
 * 
 * Dependencies: This depends on the Android MediaPlayer class along with a host activity
 * to provide access to this. This class also implements the Runnable and OnCompletionListener 
 * interfaces. The first allows the beep to be executed in a separate thread, with the latter
 * being part of the Android MediaPlayer framework to indicated when the current beep playback
 * has finished this is useful in preventing one beep from interrupting another.
 * 
 * Android Dependencies: Android API Level 1 (Any Android Version)
 *
 * @author Peter B Norton
 * @version 0.30
 * 
 *          Revision History
 * 
 *          0.16 Added to SmartPedometer_b3Activity
 * 
 *          0.18 Change to Single MediaPlayer to prevent crashes and other bad
 *          things happening.
 * 
 *          0.19 Addition of Volume adjustment function
 * 
 *          0.20B Shorter and higher pitched beep sound used
 * 
 *          0.21 Correction to Beep Sound
 * 
 *          0.27 Addition of getter function for the Error Message
 */
public class BeepHandler implements Runnable, OnCompletionListener {

	private Activity host;
	private MediaPlayer player;
	private String errorMessage;
	private boolean playFlag;

	/**
	 * Default Constructor
	 * 
	 * @param host_number
	 *            Activity Hosting Beep
	 */
	public BeepHandler(Activity host_number) {
		host = host_number;
		errorMessage = "";
		playFlag = false;
		player = MediaPlayer.create(host, R.raw.sp_beep);
		player.setOnCompletionListener(this);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if (host != null && player != null) {
			try {
				if (!playFlag) {
					player.start();
					playFlag = true;
				}
			} catch (IllegalStateException e) {
				errorMessage += e.getMessage();
			}
		}
	}

	/**
	 * Close the Beep MediaPlayer and sets to null this instance should now be
	 * discarded
	 */
	public void close() {
		player.release();
		player = null;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		try {
			// Reset the player back to start of beep
			player.seekTo(0);
			playFlag = false;
		} catch (IllegalStateException e) {
			errorMessage += e.getMessage();
		}
	}

	/**
	 * Set the Volume of this Beep
	 * 
	 * @param volume
	 */
	public void setVolume(float volume) {
		player.setVolume(volume, volume);
	}

	/**
	 * Get the Error Message from this class
	 * 
	 * @return The Error message string
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

}

