package nsp.util;

public enum ControlType { GROUND_CONTROL("Ground Control"), CONTAINMENT("Containment"), CONTAINMENT_CORE("Containment Core");

private String displayName;

ControlType(String displayName) {
    this.displayName = displayName;
}

public String displayName() { return displayName; }

@Override public String toString() { return displayName; }
}
