package rs.raf.cloud.raf_cloud_backend.model;

public enum Permission {

    USER_CREATE("user-create"),
    USER_READ("user-read"),
    USER_UPDATE("user-update"),
    USER_DELETE("user-delete"),

    MACHINE_SEARCH("machine-search"),
    MACHINE_CREATE("machine-create"),
    MACHINE_DESTROY("machine-destroy"),

    MACHINE_START("machine-power_on"),
    MACHINE_STOP("machine-power_off"),
    MACHINE_RESTART("machine-restart");

    private final String value;

    Permission(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
