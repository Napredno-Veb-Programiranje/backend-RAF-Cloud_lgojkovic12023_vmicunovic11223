package rs.raf.cloud.raf_cloud_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class UserDto {

    private Long userID;
    private String firstName;
    private String lastName;
    private String email;
    private List<String> permissions;
    private boolean isAdmin;
}
