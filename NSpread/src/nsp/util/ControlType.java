package nsp.util;

/**
 * Note, the ordering is important as it used by the Occupant class to determine
 * the maximum control applied (which is in turn used by Patch)
 * 
 * @author Johnathan Kool
 * 
 */

public enum ControlType {
	NONE("None"), GROUND_CONTROL("Ground Control"), CONTAINMENT("Containment"), CONTAINMENT_CORE(
			"Containment Core");

	private String displayName;

	ControlType(String displayName) {
		this.displayName = displayName;
	}

	public String displayName() {
		return displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
