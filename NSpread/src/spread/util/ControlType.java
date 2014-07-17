package spread.util;

/**
 * An enumeration of different management control types
 * 
 * Note, the ordering is important as it used by the Occupant class to determine
 * the maximum control applied (which is in turn used by Patch)
 */

public enum ControlType {
	NONE("None"), GROUND_CONTROL("Ground Control"), CONTAINMENT("Containment"), CONTAINMENT_CORE(
			"Containment Core"), CONTAINMENT_CORE_CONTROL("Core Control");

	private String displayName;

	ControlType(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return the display name of the control type
	 */
	
	public String displayName() {
		return displayName;
	}
	
	/**
	 * Returns the control type as a String.
	 */

	@Override
	public String toString() {
		return displayName;
	}
}
