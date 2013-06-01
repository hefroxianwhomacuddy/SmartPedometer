package pnorton.smartped;

/**
 * Class ZeroCrossingFilter
 * 
 * Responsibilities: This is the detection filter for the steps and checks for 
 * a change from negative value to positive or zero and performs a differential
 * at that point to indicate the rate of change upon step inception. It is this
 * value that is returned via the processSample method.
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
 *          0.12 Initial version detecting positive reversal in differential
 *          signal (positive peaks)
 * 
 *          0.21 New version to provide differential output instead of binary
 *          value (so gradient at zero crossing can be detected) this also now
 *          triggers a record on a positive crossing rather than a negative
 *          crossing therefore produces a positive output.
 * 
 */
public class ZeroCrossingFilter implements ISignalFilter {

	private float previous_value;
	private long previous_time_stamp;

	/**
	 * Enumeration for SIGN of number
	 * 
	 * @author Peter B Norton
	 * @version 0.28
	 */
	private enum SIGN {
		/** Negative Sign */
		NEGATIVE,
		/** Positive Sign */
		POSITIVE,
		/** Zero Sign */
		ZERO
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		previous_value = 0.0f;
	}

	@Override
	public float processSample(float n, long t) {
		// TODO Auto-generated method stub
		float returnValue = 0.0f;
		SIGN n_signum = signum(n);
		SIGN previous_signum = signum(previous_value);
		if ((previous_signum == SIGN.NEGATIVE)
				&& (n_signum == SIGN.ZERO || n_signum == SIGN.POSITIVE)) {
			// Determine the gradient
			returnValue = Math.abs((n - previous_value)
					/ ((t - previous_time_stamp) / (float) 1e9));
		}
		previous_value = n;
		previous_time_stamp = t;
		return returnValue;
	}

	/**
	 * Get the Signum value for this number
	 * 
	 * @param f
	 *            Number to check
	 * @return SIGN enumeration for this number
	 */
	private SIGN signum(float f) {
		if (f == 0.0f) {
			return SIGN.ZERO;
		} else if (f > 0.0f) {
			return SIGN.POSITIVE;
		} else {
			return SIGN.NEGATIVE;
		}
	}

}
