package rs.raf.cloud.raf_cloud_backend.model;

import lombok.Getter;

import java.util.Arrays;

@Getter

public enum Permission {
    MACHINE_CREATE("machine-create"),
    MACHINE_DESTROY("machine-destroy"),
    MACHINE_RESTART("machine-restart"),
    MACHINE_SEARCH("machine-search"),

    MACHINE_START("machine-power_on"),
    MACHINE_STOP("machine-power_off"),
    USER_CREATE("user-create"),

    USER_DELETE("user-delete"),
    USER_READ("user-read"),
    USER_UPDATE("user-update");

    private final String value;

    Permission(String value) {
        this.value = value;
    }

    public static Permission fromValue(String value) {
        return Arrays.stream(values())
                .filter(p -> p.value.equals(value))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Unknown permission: " + value)
                );
    }
}
