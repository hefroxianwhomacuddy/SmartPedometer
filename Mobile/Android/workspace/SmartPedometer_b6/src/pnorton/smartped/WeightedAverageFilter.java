package pnorton.smartped;

/**
 * Class WeightedAverageFilter
 * 
 * Responsibilities: This is a signal filter which perfoms a simple exponential
 * moving average over 4 values, this is used by the dynamic threshold system
 * to adjust the trigger point for steps and has been designed to provide a 
 * reasonable response to changes in step intensity.
 * 
 * Dependencies: Not dependent on any specific Java or Android features however
 * does depend upon the ISignalFilter implementation so must provide the methods
 * specified in that interface.
 * 
 * Android Dependencies: No Android Dependencies
 * 
 * @author Peter B Norton
 * @version 0.30
 * 
 *          Revision History
 * 
 *          0.28 Initial version for dynamic threshold system
 * 
 */
public class WeightedAverageFilter implements ISignalFilter {

	private float values[];
	private static final float weights[] = { 0.533f, 0.267f, 0.133f, 0.067f };

	/**
	 * Default Constructor simply calls the reset
	 */
	public WeightedAverageFilter() {
		reset();
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		values = new float[weights.length];
		for (int iterator = 0; iterator < weights.length; iterator++) {
			values[iterator] = 0.0f;
		}
	}

	@Override
	public float processSample(float n, long t) {
		// TODO Auto-generated method stub
		// Shunt Values in Mov Average Stack up by 1
		for (int iterator = 0; iterator < (weights.length - 1); iterator++) {
			values[iterator + 1] = values[iterator];
		}
		// Store this value in the first slot
		values[0] = n;
		float sum = 0.0f;
		for (int iterator = 0; iterator < weights.length; iterator++) {
			sum += values[iterator] * weights[iterator];
		}
		return sum;
	}

}
