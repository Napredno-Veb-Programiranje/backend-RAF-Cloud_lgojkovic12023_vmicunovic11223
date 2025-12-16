package rs.raf.cloud.raf_cloud_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class CreateUserDto {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Set<String> permissions;
}
