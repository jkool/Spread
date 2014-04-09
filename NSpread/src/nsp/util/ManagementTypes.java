package nsp.util;

public enum ManagementTypes { GROUND_CONTROL("Ground Control"), CONTAINMENT("Containment"), CONTAINMENT_CORE("Containment Core");

private String displayName;

ManagementTypes(String displayName) {
    this.displayName = displayName;
}

public String displayName() { return displayName; }

@Override public String toString() { return displayName; }
}
