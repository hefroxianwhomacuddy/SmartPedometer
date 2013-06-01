package pnorton.smartped;

/**
 * Interface ISignalFilter
 * 
 * Responsibilities: Interface to specify a number of common methods for a 
 * signal processing filter. This originally in early versions of the 
 * Pedometer allowed the chaining of filters in array for processing. This
 * is now largely redundant but has been retained to allow the export of 
 * some of the signal filters and to retain a common interface by how
 * they are managed.
 * 
 * Dependencies: Not dependent on any specific Java or Android features 
 * 
 * Android Dependencies: No Android Dependencies
 * 
 * @author Peter B Norton
 * @version 0.30
 * 
 *          Revision History
 * 
 *          0.11 Initial version to be imported for signal processing
 * 
 *          0.15 Port to SmartPedometer_b3
 * 
 *          0.21 New Version with additional timing parameter added to the
 *          processSample method
 * 
 */
public interface ISignalFilter {

	/**
	 * Reset the filter must be defined by classes implementing the interface
	 * however the method need not perform any tasks
	 */
	public void reset();

	/**
	 * Process an individual sample through the filter and return the modified
	 * value
	 * 
	 * @param n
	 *            Value to process
	 * @return Modified Value
	 */
	public float processSample(float n, long t);

}
