package rs.raf.cloud.raf_cloud_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class UpdateUserDto {
    private String firstName;
    private String lastName;
    private Set<String> permissions;
    private Boolean active;
}
